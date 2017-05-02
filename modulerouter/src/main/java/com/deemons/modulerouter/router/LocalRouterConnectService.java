package com.deemons.modulerouter.router;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;


public class LocalRouterConnectService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("MRCS","onBind");
        return stub;
    }

    ILocalRouterAIDL.Stub stub = new ILocalRouterAIDL.Stub() {

        @Override
        public boolean checkResponseAsync(RouterRequest routerRequest) throws RemoteException {
            return LocalRouter.getInstance().
                    answerWiderAsync(routerRequest);
        }

        @Override
        public MaActionResult route(RouterRequest routerRequest) {
            try {
                LocalRouter localRouter = LocalRouter.getInstance();
                RouterRequest routerRequest1 = routerRequest;
                return localRouter.route(LocalRouterConnectService.this,routerRequest1);
            } catch (Exception e) {
                e.printStackTrace();
                return new MaActionResult.Builder().msg(e.getMessage()).build();
            }
        }

        @Override
        public boolean stopWideRouter() throws RemoteException {
            LocalRouter
                    .getInstance()
                    .disconnectWideRouter();
            return true;
        }

        @Override
        public void connectWideRouter() throws RemoteException {
            LocalRouter
                    .getInstance()
                    .connectWideRouter();
        }
    };
}
