package com.deemons.sample;

import android.content.Context;

import com.deemons.modulerouter.MaAction;
import com.deemons.modulerouter.RouterAction;
import com.deemons.modulerouter.router.MaActionResult;
import com.deemons.modulerouter.router.RouterRequest;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/6/6 10:29
 * 包名       com.deemons.sample
 * 描述
 */
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
