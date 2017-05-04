package com.deemons;

import com.deemons.processor.RouterProcessor;
import com.google.auto.service.AutoService;

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

@AutoService(Processor.class)//自动生成 javax.annotation.processing.IProcessor 文件
@SupportedSourceVersion(SourceVersion.RELEASE_7)//java版本支持
@SupportedAnnotationTypes({//标注注解处理器支持的注解类型
        "com.deemons.activityrouter.Extra",
        "com.deemons.activityrouter.Router",
        "com.deemons.activityrouter.SceneTransition",
        "com.deemons.modulerouter.RouterService",
        "com.deemons.modulerouter.RouterLogic",
        "com.deemons.modulerouter.RouterAction"
})
public class AnnotationProcessor extends AbstractProcessor{
    public Filer mFiler; //文件相关的辅助类
    public Elements mElements; //元素相关的辅助类
    public Messager mMessager; //日志相关的辅助类



    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        //new RouterActivityProcessor().process(roundEnv,this);
        new RouterProcessor().process(roundEnv, this);
        return true;
    }

    public void debug(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
