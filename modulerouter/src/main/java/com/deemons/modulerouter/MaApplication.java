package com.deemons.modulerouter;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.deemons.modulerouter.multiprocess.BaseApplicationLogic;
import com.deemons.modulerouter.multiprocess.PriorityLogicWrapper;
import com.deemons.modulerouter.router.LocalRouter;
import com.deemons.modulerouter.router.LocalRouterConnectService;
import com.deemons.modulerouter.router.WideRouter;
import com.deemons.modulerouter.router.WideRouterApplicationLogic;
import com.deemons.modulerouter.router.WideRouterConnectService;
import com.deemons.modulerouter.tools.Logger;
import com.deemons.modulerouter.tools.ProcessUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public abstract class MaApplication extends Application {
    private static final String TAG = "MaApplication";
    private static MaApplication sInstance;
    private ArrayList<RouterHelper> mRouterHelpers;
    private ArrayList<PriorityLogicWrapper> mLogicList;
    private HashMap<String, Class<LocalRouterConnectService>> mServiceMap;
    private HashMap<String, ArrayList<PriorityLogicWrapper>> mLogicClassMap;
    private HashMap<String, ArrayList<MaProvider>> mProviderMap;
    private HashMap<String, ArrayList<MaAction>> mActionMap;

    @CallSuper
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        init();
        startWideRouter();
        initializeLogic();
        dispatchLogic();
        instantiateLogic();

        // Traverse the application logic.
        if (null != mLogicList && mLogicList.size() > 0) {
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper && null != priorityLogicWrapper.instance) {
                    priorityLogicWrapper.instance.onCreate();
                    String precessName = ProcessUtil.getProcessName(this, ProcessUtil.getMyProcessId());
                    Logger.d(TAG, precessName);
                    if (mProviderMap.get(precessName) != null) {
                        for (MaProvider maProvider : mProviderMap.get(precessName)) {
                            LocalRouter.getInstance().registerProvider(maProvider.getName(), maProvider);
                            for (MaAction maAction : mActionMap.get(precessName + "_" + maProvider.getName())) {
                                maProvider.registerAction(maAction.getClass().getSimpleName(), maAction);
                            }
                        }
                    }
                }
            }
        }

    }

    private void init() {

        mRouterHelpers = new ArrayList<>();
        initRouter(mRouterHelpers);


        mLogicClassMap = new HashMap<>();

        mServiceMap = new HashMap<>();
        mProviderMap = new HashMap<>();
        mActionMap = new HashMap<>();

        for (RouterHelper helper : mRouterHelpers) {
            helper.addLocalRouterService(mServiceMap);
            helper.addProvider(mProviderMap, mActionMap);
        }
    }

    protected abstract void initRouter(ArrayList<RouterHelper> mRouterHelpers);


    protected void startWideRouter() {
        if (needMultipleProcess()) {
            registerApplicationLogic(WideRouter.PROCESS_NAME, 1000, WideRouterApplicationLogic.class);
            Intent intent = new Intent(this, WideRouterConnectService.class);
            startService(intent);
        }
    }

    public void initializeAllProcessRouter() {
        if (mServiceMap != null) {
            for (Map.Entry<String, Class<LocalRouterConnectService>> entry : mServiceMap.entrySet()) {
                WideRouter.registerLocalRouter(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void initializeLogic() {
        if (mRouterHelpers != null) {
            for (RouterHelper helper : mRouterHelpers) {
                helper.injectLogic(this);
            }
        }
    }


    public void registerApplicationLogic(String processName, int priority, @NonNull Class<? extends BaseApplicationLogic> logicClass) {
        if (null != mLogicClassMap) {
            ArrayList<PriorityLogicWrapper> tempList = mLogicClassMap.get(processName);
            if (null == tempList) {
                tempList = new ArrayList<>();
                mLogicClassMap.put(processName, tempList);
            }
            if (tempList.size() > 0) {
                for (PriorityLogicWrapper priorityLogicWrapper : tempList) {
                    if (logicClass.getName().equals(priorityLogicWrapper.logicClass.getName())) {
                        throw new RuntimeException(logicClass.getName() + " has registered.");
                    }
                }
            }
            PriorityLogicWrapper priorityLogicWrapper = new PriorityLogicWrapper(priority, logicClass);
            tempList.add(priorityLogicWrapper);
        }
    }

    private void dispatchLogic() {
        if (null != mLogicClassMap) {
            mLogicList = mLogicClassMap.get(ProcessUtil.getProcessName(this, ProcessUtil.getMyProcessId()));
        }
    }

    private void instantiateLogic() {
        if (null != mLogicList && mLogicList.size() > 0) {
            Collections.sort(mLogicList);
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper) {
                    try {
                        priorityLogicWrapper.instance = priorityLogicWrapper.logicClass.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (null != priorityLogicWrapper.instance) {
                        priorityLogicWrapper.instance.setApplication(this);
                    }
                }
            }
        }

    }


    public boolean needMultipleProcess() {
        return mServiceMap != null && mServiceMap.size() > 1;
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        if (null != mLogicList && mLogicList.size() > 0) {
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper && null != priorityLogicWrapper.instance) {
                    priorityLogicWrapper.instance.onTerminate();
                }
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != mLogicList && mLogicList.size() > 0) {
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper && null != priorityLogicWrapper.instance) {
                    priorityLogicWrapper.instance.onLowMemory();
                }
            }
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (null != mLogicList && mLogicList.size() > 0) {
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper && null != priorityLogicWrapper.instance) {
                    priorityLogicWrapper.instance.onTrimMemory(level);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null != mLogicList && mLogicList.size() > 0) {
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper && null != priorityLogicWrapper.instance) {
                    priorityLogicWrapper.instance.onConfigurationChanged(newConfig);
                }
            }
        }
    }

    public static MaApplication getMaApplication() {
        return sInstance;
    }
}
