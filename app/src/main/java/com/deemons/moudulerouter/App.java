package com.deemons.moudulerouter;

import com.deemons.modulerouter.MaApplication;
import com.deemons.modulerouter.RouterHelper;
import com.deemons.modulerouter.apt.RouterHelperMain;

import java.util.ArrayList;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/5/3 12:35
 * 包名       com.deemons.moudulerouter
 * 描述
 */
public class App extends MaApplication {
    @Override
    protected void initRouter(ArrayList<RouterHelper> mRouterHelpers) {
        RouterHelperMain.newInstance(mRouterHelpers);
    }
}
