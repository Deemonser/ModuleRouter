package com.deemons.modulerouter.router;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.deemons.modulerouter.ErrorAction;
import com.deemons.modulerouter.MaAction;
import com.deemons.modulerouter.MaApplication;
import com.deemons.modulerouter.MaProvider;
import com.deemons.modulerouter.tools.Logger;
import com.deemons.modulerouter.tools.ProcessUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * The Local Router
 */

public class LocalRouter {
    private static final String TAG = "LocalRouter";
    private String mProcessName = ProcessUtil.UNKNOWN_PROCESS_NAME;
    private static LocalRouter sInstance = null;
    private HashMap<String, MaProvider> mProviders = null;
    private MaApplication mApplication;
    private IWideRouterAIDL mWideRouterAIDL;

    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

    private static ExecutorService threadPool = null;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(TAG, "wideRouter service  is  connection ");
            mWideRouterAIDL = IWideRouterAIDL.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "wideRouter service  is  disconnected ");
            mWideRouterAIDL = null;
        }
    };

    private LocalRouter(MaApplication context) {
        mApplication = context;
        mProcessName = ProcessUtil.getProcessName(context, ProcessUtil.getMyProcessId());
        mProviders = new HashMap<>();
        if (mApplication.needMultipleProcess() && !WideRouter.PROCESS_NAME.equals(mProcessName)) {
            connectWideRouter();
        }

    }

    public static synchronized LocalRouter getInstance() {
        if (sInstance == null) {
            sInstance = new LocalRouter(MaApplication.getMaApplication());
        }
        return sInstance;
    }

    private static ExecutorService getThreadPool() {
        if (null == threadPool) {
            synchronized (LocalRouter.class) {
                threadPool = Executors.newCachedThreadPool();
            }
        }
        return threadPool;
    }

    public void connectWideRouter() {
        Intent binderIntent = new Intent(mApplication, WideRouterConnectService.class);
        binderIntent.putExtra("domain", mProcessName);
        mApplication.bindService(binderIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public void disconnectWideRouter() {
        if (null == mServiceConnection) {
            return;
        }
        mApplication.unbindService(mServiceConnection);
        mWideRouterAIDL = null;
    }

    public void registerProvider(String providerName, MaProvider provider) {
        mProviders.put(providerName, provider);
    }


    public boolean checkWideRouterConnection() {
        return mWideRouterAIDL != null;
    }

    boolean answerWiderAsync(@NonNull RouterRequest routerRequest) {
        if (mProcessName.equals(routerRequest.getDomain()) && checkWideRouterConnection()) {
            return findRequestAction(routerRequest).isAsync(mApplication, routerRequest);
        } else {
            return true;
        }
    }

    private static class RouteResultWrap {
        MaActionResult maActionResult;
        Observable<MaActionResult> maActionResultObservable;
    }


    public MaActionResult route(Context context, @NonNull RouterRequest routerRequest) throws Exception {
        RouteResultWrap routeResultWrap = new RouteResultWrap();
        rxRoute(context, routerRequest, routeResultWrap, RouteResultType.MA_ACTION_RESULT);
        return routeResultWrap.maActionResult;
    }

    public Observable<MaActionResult> rxRoute(Context context, @NonNull RouterRequest routerRequest) {
        RouteResultWrap routeResultWrap = new RouteResultWrap();

        try {
            rxRoute(context, routerRequest, routeResultWrap, RouteResultType.OBSERVABLE);
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
        return routeResultWrap.maActionResultObservable;
    }

    private void rxRoute(Context context, @NonNull RouterRequest routerRequest, RouteResultWrap routeResultWrap, RouteResultType type) throws Exception {
        Logger.d(TAG, "Process:" + mProcessName + "\nLocal rxRoute start: " + sdf.format(new Date()));
        // Local request
        if (mProcessName.equals(routerRequest.getDomain())) {
            Logger.d(TAG, "Process:" + mProcessName + "\nLocal find action start: " + sdf.format(new Date()));
            MaAction targetAction = findRequestAction(routerRequest);
            routerRequest.isIdle.set(true);
            Logger.d(TAG, "Process:" + mProcessName + "\nLocal find action end: " + sdf.format(new Date()));
            // Sync result, return the result immediately.
            if (!targetAction.isAsync(context, routerRequest)) {
                routeResultWrap.maActionResult = targetAction.invoke(context, routerRequest);
                Logger.d(TAG, "Process:" + mProcessName + "\nLocal sync end: " + sdf.format(new Date()));
                if (type == RouteResultType.OBSERVABLE) {
                    routeResultWrap.maActionResultObservable = Observable.just(routeResultWrap.maActionResult);
                    return;
                } else {
                    return;
                }
            }
            // Async result, use the thread pool to execute the task.
            else {
                LocalTask task = new LocalTask(routerRequest, context, targetAction);
                if (type == RouteResultType.OBSERVABLE) {
                    routeResultWrap.maActionResultObservable = Observable.fromFuture(getThreadPool().submit(task));
                } else {
                    routeResultWrap.maActionResult = getThreadPool().submit(task).get();
                }
                return;
            }
        } else if (!mApplication.needMultipleProcess()) {
            throw new RuntimeException("Please make sure the returned value of needMultipleProcess in MaApplication is true, so that you can invoke other process action.");
        }
        // IPC request
        else {
            String domain = routerRequest.getDomain();
            routerRequest.isIdle.set(true);
            boolean mIsAsync = false;
            if (checkWideRouterConnection()) {
                Logger.d(TAG, "Process:" + mProcessName + "\n wideRouter is connection");
                Logger.d(TAG, "Process:" + mProcessName + "\nWide async check start: " + sdf.format(new Date()));
                //If you don'requestObject need wide async check, use "maActionResult.mIsAsync = false;" replace the next line to improve performance.
                mIsAsync = mWideRouterAIDL.checkResponseAsync(domain, routerRequest);
                Logger.d(TAG, "Process:" + mProcessName + "\nWide async check end: " + sdf.format(new Date()));
                if (!mIsAsync) {
                    Logger.d(TAG, "Process:" + mProcessName + "\nWide sync end: " + sdf.format(new Date()));
                    if (type == RouteResultType.OBSERVABLE) {
                        routeResultWrap.maActionResultObservable = Observable.just(mWideRouterAIDL.route(domain, routerRequest));
                    } else {
                        routeResultWrap.maActionResult = mWideRouterAIDL.route(domain, routerRequest);
                    }
                    return;
                }
                // Async result, use the thread pool to execute the task.
                else {
                    WideTask task = new WideTask(domain, routerRequest);
                    if (type == RouteResultType.OBSERVABLE) {
                        routeResultWrap.maActionResultObservable = Observable.fromFuture(getThreadPool().submit(task))
                                .subscribeOn(Schedulers.io());
                    } else {
                        routeResultWrap.maActionResult = getThreadPool().submit(task).get();
                    }
                    return;
                }
            }
            // Has not connected with the wide router.
            else {
                Logger.d(TAG, "Process:" + mProcessName + "\n wideRouter is'n connection");
                ConnectWideTask task = new ConnectWideTask(domain, routerRequest);
                if (type == RouteResultType.OBSERVABLE) {
                    routeResultWrap.maActionResultObservable = Observable.fromFuture(getThreadPool().submit(task))
                            .subscribeOn(Schedulers.io());
                } else {
                    routeResultWrap.maActionResult = getThreadPool().submit(task).get();
                }
                return;
            }
        }
    }

    public boolean stopSelf(Class<? extends LocalRouterConnectService> clazz) {
        if (checkWideRouterConnection()) {
            try {
                return mWideRouterAIDL.stopRouter(mProcessName);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            mApplication.stopService(new Intent(mApplication, clazz));
            return true;
        }
    }

    public void stopWideRouter() {
        if (checkWideRouterConnection()) {
            try {
                mWideRouterAIDL.stopRouter(WideRouter.PROCESS_NAME);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Logger.e(TAG, "This local router has'n requestObject connected the wide router.");
        }
    }

    private MaAction findRequestAction(RouterRequest routerRequest) {
        MaProvider targetProvider = mProviders.get(routerRequest.getProvider());
        ErrorAction defaultNotFoundAction = new ErrorAction(false, MaActionResult.CODE_NOT_FOUND, "Not found the action.");
        if (null == targetProvider) {
            return defaultNotFoundAction;
        } else {
            MaAction targetAction = targetProvider.findAction(routerRequest.getAction());
            if (null == targetAction) {
                return defaultNotFoundAction;
            } else {
                return targetAction;
            }
        }
    }

    private class LocalTask implements Callable<MaActionResult> {
        private RouterRequest mRequestData;
        private Context mContext;
        private MaAction mAction;

        public LocalTask(RouterRequest requestData, Context context, MaAction maAction) {
            this.mContext = context;
            this.mRequestData = requestData;
            this.mAction = maAction;
        }

        @Override
        public MaActionResult call() throws Exception {
            MaActionResult result = mAction.invoke(mContext, mRequestData);
            Logger.d(TAG, "Process:" + mProcessName + "\nLocal async end: " + sdf.format(new Date()));
            return result;
        }
    }

    private class WideTask implements Callable<MaActionResult> {

        private String mDomain;
        private RouterRequest routerRequest;

        public WideTask(String domain, RouterRequest routerRequest) {
            this.mDomain = domain;
            this.routerRequest = routerRequest;
        }

        @Override
        public MaActionResult call() throws Exception {
            Logger.d(TAG, "Process:" + mProcessName + "\nWide async start: " + sdf.format(new Date()));
            MaActionResult result = mWideRouterAIDL.route(mDomain, routerRequest);
            Logger.d(TAG, "Process:" + mProcessName + "\nWide async end: " + sdf.format(new Date()));
            return result;
        }
    }

    private class ConnectWideTask implements Callable<MaActionResult> {
        private String mDomain;
        private RouterRequest routerRequest;

        public ConnectWideTask(String domain, RouterRequest routerRequest) {
            this.mDomain = domain;
            this.routerRequest = routerRequest;
        }

        @Override
        public MaActionResult call() throws Exception {
            Logger.d(TAG, "Process:" + mProcessName + "\nBind wide router start: " + sdf.format(new Date()));
            connectWideRouter();
            int time = 0;
            while (true) {
                if (null == mWideRouterAIDL) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    time++;
                } else {
                    break;
                }
                if (time >= 200) {
                    ErrorAction defaultNotFoundAction = new ErrorAction(true, MaActionResult.CODE_CANNOT_BIND_WIDE, "Bind wide router time out. Can not bind wide router.");
                    return defaultNotFoundAction.invoke(mApplication, new RouterRequest());
                }
            }
            Logger.d(TAG, "Process:" + mProcessName + "\nBind wide router end: " + sdf.format(new Date()));
            MaActionResult result = mWideRouterAIDL.route(mDomain, routerRequest);
            Logger.d(TAG, "Process:" + mProcessName + "\nWide async end: " + sdf.format(new Date()));
            return result;
        }
    }

    private enum RouteResultType {
        OBSERVABLE,
        MA_ACTION_RESULT
    }
}
