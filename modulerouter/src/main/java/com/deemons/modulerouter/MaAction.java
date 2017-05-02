package com.deemons.modulerouter;

import android.content.Context;

import com.deemons.modulerouter.router.MaActionResult;
import com.deemons.modulerouter.router.RouterRequest;

/**
 * Created by wanglei on 2016/11/29.
 */

public interface MaAction<T> {
    boolean isAsync(Context context, RouterRequest<T> routerRequest);

    MaActionResult invoke(Context context, RouterRequest<T> routerRequest);

}
