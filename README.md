# ModuleRouter
An easy to modular development tool library


使用

1. 在 gradle 中添加依赖

       compile 'com.deemons.modulerouter:modulerouter:xxx'
       annotationProcessor 'com.deemons.modulerouterapt:xxx'

2. 在AndroidManifest中使用MaApplication

       android:name="com.deemons.modulerouter.MaApplication"

3. 每个 Module 中，新建 MainLogic ，它取代了 Application 的功能

       @RouterLogic(processName = "com.deemons.sample",Priority = 999)
       public class MainLogic extends BaseApplicationLogic {
           @Override
           public void onCreate() {
               super.onCreate();
       
       
           }
       }

4. 如果需要 多线程，新建 LcalService

       @RouterService("com.deemons.sample")
       public class LocalService extends LocalRouterConnectService {
       }

5. 提供本模块的 Action

       @RouterAction
       public class PlayAction implements MaAction<String> {
           @Override
           public boolean isAsync(Context context, RouterRequest<String> routerRequest) {
               return false;
           }
       
           @Override
           public MaActionResult invoke(Context context, RouterRequest<String> routerRequest) {
               String requestObject = routerRequest.getRequestObject();
       
               return  new MaActionResult.Builder<String>()
                       .code(MaActionResult.CODE_SUCCESS)
                       .result("is ok!")
                       .build();
           }
       }

6. 调用

       LocalRouter.getInstance()
            .rxRoute(this, new RouterRequest()
                   .domain("com.deemons.sample")
                   .provider("app")
                   .action("PlayAction")
                   .requestObject("hello!"))
             .subscribe(new Consumer<MaActionResult>() {
                @Override
                public void accept(@NonNull MaActionResult maActionResult) throws Exception {
                }
              });
   
