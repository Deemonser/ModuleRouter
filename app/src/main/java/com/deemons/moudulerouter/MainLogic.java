package com.deemons.moudulerouter;

import com.deemons.modulerouter.RouterLogic;
import com.deemons.modulerouter.multiprocess.BaseApplicationLogic;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/5/3 12:36
 * 包名       com.deemons.moudulerouter
 * 描述
 */
@RouterLogic(processName = "com.deemons.moudulerouter",Priority = 999,moduleName = "main")
public class MainLogic extends BaseApplicationLogic {
}
