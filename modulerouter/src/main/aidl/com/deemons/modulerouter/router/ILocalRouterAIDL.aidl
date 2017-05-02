package com.deemons.modulerouter.router;
import com.deemons.modulerouter.router.MaActionResult;
import com.deemons.modulerouter.router.RouterRequest;

interface ILocalRouterAIDL {
    boolean checkResponseAsync(in RouterRequest routerRequset);
    MaActionResult route(in RouterRequest routerRequest);
    boolean stopWideRouter();
    void connectWideRouter();
}
