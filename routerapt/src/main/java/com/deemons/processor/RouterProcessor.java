package com.deemons.processor;


import com.deemons.AnnotationProcessor;
import com.deemons.helpe.RouterHelp;
import com.deemons.inter.IProcessor;
import com.deemons.modulerouter.RouterAction;
import com.deemons.modulerouter.RouterLogic;
import com.deemons.modulerouter.RouterService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/4/28 11:27
 * 包名       com.deemons.processor
 * 描述
 */
public class RouterProcessor implements IProcessor {


    @Override
    public void process(RoundEnvironment roundEnv, AnnotationProcessor processor) {
        processor.debug("RouterProcessor =======================");


        RouterHelp routerHelp = gatherInfo(roundEnv, processor);

        buildProviderClass(roundEnv, processor, routerHelp);
        buildModuleHelper(roundEnv, processor, routerHelp);

        //buildSRouter(roundEnv, processor,routerHelp);

    }

    //收集信息
    private RouterHelp gatherInfo(RoundEnvironment roundEnv, AnnotationProcessor processor) {
        RouterHelp help = new RouterHelp();

        //@RouterService
        Set<TypeElement> serviceTypeElements = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(RouterService.class));
        if (serviceTypeElements.size() > 1) {
            throw new RuntimeException("@RouterService 在一个 module 中最多只能有一个");
        }

        for (TypeElement typeElement : serviceTypeElements) {
            help.serviceElement = typeElement;
            help.processName = typeElement.getAnnotation(RouterService.class).value();
        }

        //@RouterLogic
        Set<TypeElement> logicTypeElements = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(RouterLogic.class));
        if (logicTypeElements.size() > 1) {
            throw new RuntimeException("@RouterLogic 在一个 module 中最多只能有一个");
        }

        for (TypeElement typeElement : logicTypeElements) {
            RouterLogic annotation = typeElement.getAnnotation(RouterLogic.class);

            if (help.processName != null && !annotation.processName().equals(help.processName)) {
                throw new RuntimeException("同一 module 中，@RouterService 与 @RouterLogic 所在进程不同");
            }


            help.logicElement = typeElement;
            help.moduleName = annotation.moduleName();
            help.priority = annotation.Priority();
            help.processName = annotation.processName();
            help.providerName = captureName(help.moduleName) + "Provider";
            help.routerHelperName = "RouterHelper" + captureName(help.moduleName);
        }


        //@RouterAction
        Set<TypeElement> actionElements = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(RouterAction.class));
        help.actionElementList = new ArrayList<>();
        for (TypeElement actionElement : actionElements) {
            help.actionElementList.add(actionElement);
        }


        if (RouterHelp.routerHelpers == null) {
            RouterHelp.routerHelpers = new HashMap<>();
        }

        //@RouterModule
//        Set<TypeElement> routerModuleElements = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(RouterModule.class));
//        for (TypeElement typeElement : routerModuleElements) {
//            RouterHelp.routerHelpers.put(ClassName.get(typeElement).simpleName(), typeElement);
//        }
//
//
//        for (TypeElement typeElement : ElementFilter.typesIn(roundEnv.getRootElements())) {
//            TypeMirror mirror = typeElement.getSuperclass();
//
//            if ("com.deemons.srouter.MaApplication".equals(mirror.toString())) {
//                help.isRootModule = true;
//            }
//        }

        return help;
    }

    //构建 Provider
    private void buildProviderClass(RoundEnvironment roundEnv, AnnotationProcessor processor, RouterHelp routerHelp) {
        String moduleName = routerHelp.moduleName;
        if (moduleName == null || "".equals(moduleName)) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("getName")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $S", moduleName)
                .returns(String.class);

        TypeSpec providerTypeSpec = TypeSpec.classBuilder(routerHelp.providerName)
                .superclass(ClassName.get("com.deemons.modulerouter", "MaProvider"))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(builder.build())
                .build();
        try {
            JavaFile.builder("com.deemons.modulerouter.apt", providerTypeSpec)
                    .build()
                    .writeTo(processor.mFiler);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    //构建 Helper
    private void buildModuleHelper(RoundEnvironment roundEnv, AnnotationProcessor processor, RouterHelp routerHelp) {
        String moduleName = routerHelp.moduleName;
        if (moduleName == null || "".equals(moduleName)) {
            return;
        }


        TypeSpec providerTypeSpec = TypeSpec.classBuilder(routerHelp.routerHelperName)
              //  .addAnnotation(RouterModule.class)
                .addSuperinterface(ClassName.get("com.deemons.modulerouter", "RouterHelper"))
                .addModifiers(Modifier.PUBLIC)
                .addField(ClassName.get("com.deemons.modulerouter", "RouterHelper"), "mRouterHelper", Modifier.STATIC)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                .addMethod(getNewInstanceMethod(roundEnv, processor, routerHelp))
                .addMethod(getInjectServiceMethod(roundEnv, processor, routerHelp))
                .addMethod(getInjectLogicMethod(roundEnv, processor, routerHelp))
                .addMethod(getProviderMethod(roundEnv, processor, routerHelp))
                .build();
        try {
            JavaFile.builder("com.deemons.modulerouter.apt", providerTypeSpec)
                    .build()
                    .writeTo(processor.mFiler);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private MethodSpec getNewInstanceMethod(RoundEnvironment roundEnv, AnnotationProcessor processor, RouterHelp routerHelp) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("newInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(ParameterizedTypeName.get(
                        ClassName.get(ArrayList.class),
                        ClassName.get("com.deemons.modulerouter", "RouterHelper"))
                        , "mRouterHelpers");

        builder.beginControlFlow("if (mRouterHelper == null)")
                .addStatement("mRouterHelper = new $T()", ClassName.get("com.deemons.modulerouter.apt", routerHelp.routerHelperName))
                .addStatement("mRouterHelpers.add(mRouterHelper)")
                .endControlFlow();

        return builder.build();
    }

    private MethodSpec getInjectServiceMethod(RoundEnvironment roundEnv, AnnotationProcessor processor, RouterHelp routerHelp) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("addLocalRouterService")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(HashMap.class, "mServiceMap")
                .returns(TypeName.VOID);

        if (routerHelp.serviceElement != null) {
            builder.addStatement("mServiceMap.put($S, $L.class);", routerHelp.processName, routerHelp.serviceElement);
        }

        return builder.build();
    }


    private MethodSpec getInjectLogicMethod(RoundEnvironment roundEnv, AnnotationProcessor processor, RouterHelp routerHelp) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("injectLogic")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(ClassName.get("com.deemons.modulerouter", "MaApplication"), "maApplication")
                .addStatement("maApplication.registerApplicationLogic($S, $L,$L.class)", routerHelp.processName, routerHelp.priority, routerHelp.logicElement);

        return builder.build();
    }


    private MethodSpec getProviderMethod(RoundEnvironment roundEnv, AnnotationProcessor processor, RouterHelp routerHelp) {


        MethodSpec.Builder builder = MethodSpec.methodBuilder("addProvider")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(ParameterizedTypeName.get(ClassName.get(HashMap.class),
                        ClassName.get(String.class),
                        ParameterizedTypeName.get(ClassName.get(ArrayList.class), ClassName.get("com.deemons.modulerouter", "MaProvider"))
                ), "providerMap")
                .addParameter(ParameterizedTypeName.get(ClassName.get(HashMap.class),
                        ClassName.get(String.class),
                        ParameterizedTypeName.get(ClassName.get(ArrayList.class), ClassName.get("com.deemons.modulerouter", "MaAction"))
                ), "actionMap");

        ClassName providerClass = ClassName.get("com.deemons.modulerouter.apt", captureName(routerHelp.moduleName) + "Provider");


        builder.addStatement("$T provider = new $T()", providerClass, providerClass)
                .beginControlFlow("if (providerMap.get($S) == null)", routerHelp.processName)
                .addStatement("providerMap.put($S , new $T())", routerHelp.processName, ArrayList.class)
                .endControlFlow();
        builder.addStatement("providerMap.get($S).add(provider)", routerHelp.processName);


        String actionKey = routerHelp.processName + "_" + routerHelp.moduleName;
        for (TypeElement element : routerHelp.actionElementList) {
            builder.addStatement("$T action = new $T()", ClassName.get(element), ClassName.get(element))
                    .beginControlFlow("if (actionMap.get($S) == null)", actionKey)
                    .addStatement("actionMap.put($S , new $T());", actionKey, ArrayList.class)
                    .endControlFlow();
            builder.addStatement("actionMap.get($S).add(action)",  actionKey);
        }


        return builder.build();
    }



    private void buildSRouter(RoundEnvironment roundEnv, AnnotationProcessor processor, RouterHelp routerHelp) {
        processor.debug("RouterHelp.routerHelpers.size() ===" + RouterHelp.routerHelpers.size());
        if (!routerHelp.isRootModule || RouterHelp.routerHelpers.size() == 0) {
            return;
        }


        MethodSpec.Builder builder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(ArrayList.class, "mRouterHelpers");


        for (Map.Entry<String, TypeElement> entry : RouterHelp.routerHelpers.entrySet()) {
            processor.debug("routerHelpers: " + entry.getKey());
            builder.addStatement("mRouterHelpers.add(new $T())", ClassName.get(entry.getValue()));
        }


        TypeSpec providerTypeSpec = TypeSpec.classBuilder("SRouter")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(builder.build())
                .build();
        try {
            JavaFile.builder("com.deemons.modulerouter.apt", providerTypeSpec)
                    .build()
                    .writeTo(processor.mFiler);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    //首字母大写
    public String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);

    }


}
