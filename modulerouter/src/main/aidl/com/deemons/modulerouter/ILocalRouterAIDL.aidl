package com.deemons.modulerouter;


interface ILocalRouterAIDL {
    boolean checkResponseAsync(String routerRequset);
    String route(String routerRequest);
    boolean stopWideRouter();
    void connectWideRouter();
}
