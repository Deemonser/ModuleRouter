package com.deemons.modulerouter.tools;

import android.content.Context;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * author： deemons
 * date:    2017/6/4
 * desc:
 */

public class PackageUtils {


    public static List<Class> getClasses(Context mContext, String packageName) {
        ArrayList<Class> classes = new ArrayList<>();
        try {
            String packageCodePath = mContext.getPackageCodePath();
            DexFile df = new DexFile(packageCodePath);
            String regExp = "^" + packageName + ".\\w+$";
            for (Enumeration<String> iter = df.entries(); iter.hasMoreElements(); ) {
                String className = iter.nextElement();
                if (className.matches(regExp)) {
                    classes.add(Class.forName(className));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }



}
