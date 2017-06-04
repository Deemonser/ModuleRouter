package com.deemons.helpe;

import java.util.ArrayList;
import java.util.HashMap;

import javax.lang.model.element.TypeElement;

/**
 * author： deemons
 * date:    2017/4/30
 * desc:
 */

public class RouterHelp {

    public static final String KEY_MODULE_NAME = "moduleName";

    public String processName;

    public String moduleName;

    public int priority;

    public TypeElement serviceElement;

    public TypeElement logicElement;

    public String providerName;

    public String routerHelperName;

    public ArrayList<TypeElement> actionElementList;


    public boolean isRootModule;


    public static HashMap<String,TypeElement> routerHelpers;

}
