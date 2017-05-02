// IRouterAIDL.aidl
package com.deemons.modulerouter.router;
import com.deemons.modulerouter.router.MaActionResult;
import com.deemons.modulerouter.router.RouterRequest;
// Declare any non-default types here with import statements

interface IWideRouterAIDL {
    boolean checkResponseAsync(String domain,in RouterRequest routerRequset);
    MaActionResult route(String domain,in RouterRequest routerRequest);
    boolean stopRouter(String domain);
}
