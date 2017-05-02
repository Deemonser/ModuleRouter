package com.deemons.modulerouter;

import android.content.Context;

import com.deemons.modulerouter.router.MaActionResult;
import com.deemons.modulerouter.router.RouterRequest;


public class ErrorAction implements MaAction {

    private static final String DEFAULT_MESSAGE = "Something was really wrong. Ha ha!";
    private int mCode;
    private String mMessage;
    private boolean mAsync;
    public ErrorAction() {
        mCode = MaActionResult.CODE_ERROR;
        mMessage = DEFAULT_MESSAGE;
        mAsync = false;
    }

    public ErrorAction(boolean isAsync,int code, String message) {
        this.mCode = code;
        this.mMessage = message;
        this.mAsync = isAsync;
    }

    @Override
    public boolean isAsync(Context context, RouterRequest requestData) {
        return mAsync;
    }

    @Override
    public MaActionResult invoke(Context context, RouterRequest requestData) {
        return new MaActionResult.Builder()
                .code(mCode)
                .msg(mMessage)
                .data(null)
                .result(null)
                .build();
    }


}
