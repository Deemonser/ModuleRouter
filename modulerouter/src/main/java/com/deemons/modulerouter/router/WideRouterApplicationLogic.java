package com.deemons.modulerouter.router;


import com.deemons.modulerouter.multiprocess.BaseApplicationLogic;

/**
 * Created by wanglei on 2016/11/25.
 */

public final class WideRouterApplicationLogic extends BaseApplicationLogic {
    @Override
    public void onCreate() {
        super.onCreate();
        initRouter();
    }

    protected void initRouter() {
        WideRouter.getInstance(mApplication);
        mApplication.initializeAllProcessRouter();
    }
}
