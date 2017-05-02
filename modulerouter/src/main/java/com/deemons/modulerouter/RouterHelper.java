package com.deemons.modulerouter;


import com.deemons.modulerouter.router.LocalRouterConnectService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * author： deemons
 * date:    2017/4/30
 * desc:
 */

public interface RouterHelper {

    void addLocalRouterService(HashMap<String, Class<LocalRouterConnectService>> mServiceMap);

    void injectLogic(MaApplication maApplication);

    void addProvider(HashMap<String, ArrayList<MaProvider>> mProviderMap, HashMap<String, ArrayList<MaAction>> mActionMap);
}
