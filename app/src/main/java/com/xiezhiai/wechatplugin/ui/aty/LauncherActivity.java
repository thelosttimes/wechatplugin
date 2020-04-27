package com.xiezhiai.wechatplugin.ui.aty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiezhiai.wechatplugin.R;
import com.xiezhiai.wechatplugin.func.nohttp.URLManager;
import com.xiezhiai.wechatplugin.func.nohttp.base.SimpleRequest;
import com.xiezhiai.wechatplugin.func.nohttp.base.SimpleResult;
import com.xiezhiai.wechatplugin.func.transfer.PluginHandler;
import com.xiezhiai.wechatplugin.ui.base.BaseActivity;
import com.xiezhiai.wechatplugin.utils.PluginLoginManager;
import com.xiezhiai.wechatplugin.utils.others.StatusBarCompat;
import com.yanzhenjie.nohttp.rest.Response;

import java.util.HashMap;

/**
 * Created by shijiwei on 2018/11/1.
 *
 * @Desc:
 */
public class LauncherActivity extends BaseActivity {

    private static Handler uiHanlder = new Handler();

    private String user;
    private String psw;
    private boolean isLogin;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_launch;
    }

    @Override
    public void initialData(Bundle savedInstanceState) {
        PluginHandler.cookie.updatePluginLogin(false);
        PluginHandler.cookie.setInitialize(false);
        PluginHandler.bindWXUserList.clear();
    }

    @Override
    public void initialView() {

    }

    @Override
    public void initialEvn() {
        StatusBarCompat.setStatusBar(this, false, false);
    }

    @Override
    public void onHttpSucceed(int what, SimpleResult ret, Response response) {
        super.onHttpSucceed(what, ret, response);
        String KEY_USER_ID = "user_id";
        String KEY_SIGN = "sign";
        if (ret != null) {
            if (ret.getCode() == SimpleResult.SUCCESS) {
                JSONObject data = JSON.parseObject(ret.getData());
                if (data != null) {
                    if (data.containsKey(KEY_USER_ID)) {
                        PluginHandler.cookie.setUserID(data.getString(KEY_USER_ID));
                    }

                    if (data.containsKey(KEY_SIGN)) {
                        PluginHandler.cookie.setSign(data.getString(KEY_SIGN));
                    }
                    PluginHandler.cookie.updatePluginLogin(true);
                    goPluginMainUI();
                } else {
                    goPluginMainUI();
                }
            } else {
                goPluginMainUI();
            }
        }
    }

    @Override
    public void onHttpFailed(int what, SimpleResult ret, Response response) {
        super.onHttpFailed(what, ret, response);
        goPluginMainUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHanlder.postDelayed(loginRunnale, 2000);
    }

    @Override
    public void onBackKeyPressed() {
        uiHanlder.removeCallbacks(loginRunnale);
        goPluginMainUI();
    }

    @Override
    public void onNetworkStateChanged(int type, boolean isAvailable) {

    }

    /**
     * 登录
     *
     * @param phone
     * @param psw
     */
    private void login(String phone, String psw) {
        SimpleRequest<SimpleResult> request = new SimpleRequest<SimpleResult>(URLManager.Login.getURL(), URLManager.Login.method, SimpleResult.class);
        HashMap<String, String> p = new HashMap<>();
        p.put("mobile", phone);
        p.put("password", psw);
        request.setRequestBodyAsJson(p);
        addTask2Queue(
                URLManager.Login.action,
                request);
    }


    Runnable loginRunnale = new Runnable() {
        @Override
        public void run() {

            if (!PluginHandler.cookie.isPluginLogin()) {
                Object[] lastLoginAccount = PluginLoginManager.getLastLoginAccount(LauncherActivity.this);
                user = (String) lastLoginAccount[0];
                psw = (String) lastLoginAccount[1];
                isLogin = (boolean) lastLoginAccount[2];

                if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(psw) && isLogin) {
                    login(user, psw);
                } else {
                    goPluginMainUI();
                }

            } else {
                goPluginMainUI();
            }
        }
    };

    /**
     * 跳转到 MainActivity
     */
    private void goPluginMainUI() {
        startActivity(new Intent(LauncherActivity.this, MainActivity.class));
        finish();
    }
}