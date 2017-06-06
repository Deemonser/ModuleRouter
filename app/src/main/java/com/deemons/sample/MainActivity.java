package com.deemons.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.deemons.modulerouter.router.LocalRouter;
import com.deemons.modulerouter.router.MaActionResult;
import com.deemons.modulerouter.router.RouterRequest;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jump(View view) {
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
    }
}
