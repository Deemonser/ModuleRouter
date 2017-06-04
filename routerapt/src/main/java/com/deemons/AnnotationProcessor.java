package com.deemons;

import com.deemons.processor.RouterProcessor;
import com.google.auto.service.AutoService;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static com.deemons.helpe.RouterHelp.KEY_MODULE_NAME;

@AutoService(Processor.class)//自动生成 javax.annotation.processing.IProcessor 文件
@SupportedSourceVersion(SourceVersion.RELEASE_8)//java版本支持
@SupportedAnnotationTypes({//标注注解处理器支持的注解类型
        "com.deemons.modulerouter.RouterService",
        "com.deemons.modulerouter.RouterLogic",
        "com.deemons.modulerouter.RouterAction"
})
public class AnnotationProcessor extends AbstractProcessor{
    public Filer mFiler; //文件相关的辅助类
    public Elements mElements; //元素相关的辅助类
    public Messager mMessager; //日志相关的辅助类
    public String moduleName;



    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();

        initModuleName();

        new RouterProcessor().process(roundEnv, this,moduleName);
        return true;
    }

    private void initModuleName() {
        Map<String, String> options = processingEnv.getOptions();
        if (options != null && !options.isEmpty()) {
            moduleName = options.get(KEY_MODULE_NAME);
        }

        if (moduleName!=null && !"".equals(moduleName)) {
            debug("The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            debug("These no module name, at 'build.gradle', like :\n" +
                    "defaultConfig {\n"+
                    "   javaCompileOptions {\n" +
                    "       annotationProcessorOptions {\n" +
                    "           arguments = [moduleName : project.getName()]\n" +
                    "       }\n" +
                    "   }\n"+
                    "}\n");
            throw new RuntimeException("ModuleRouter::Compiler >>> No module name, for more information, look at gradle log.");
        }
    }

    public void debug(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
