package com.xiezhiai.wechatplugin.ui.aty;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiezhiai.wechatplugin.R;
import com.xiezhiai.wechatplugin.func.nohttp.URLManager;
import com.xiezhiai.wechatplugin.func.nohttp.base.SimpleRequest;
import com.xiezhiai.wechatplugin.func.nohttp.base.SimpleResult;
import com.xiezhiai.wechatplugin.func.transfer.PluginHandler;
import com.xiezhiai.wechatplugin.ui.base.BaseActivity;
import com.xiezhiai.wechatplugin.utils.LogUtil;
import com.xiezhiai.wechatplugin.utils.PluginLoginManager;
import com.xiezhiai.wechatplugin.utils.VerifyUtil;
import com.xiezhiai.wechatplugin.widget.CommonTopBar;
import com.yanzhenjie.nohttp.rest.Response;

import java.net.URL;
import java.util.HashMap;

/**
 * Created by shijiwei on 2018/10/29.
 *
 * @Desc:
 */
public class RegisterActivity extends BaseActivity implements View.OnFocusChangeListener, View.OnClickListener, CommonTopBar.CommonTopBarListener {

    private CommonTopBar commonTopBar;
    private EditText etPhone;
    private EditText etPsw;
    private EditText etYzm;
    private ImageView ivPhone;
    private ImageView ivPsw;
    private ImageView ivYzm;
    private TextView dividerPhone;
    private TextView dividerPsw;
    private TextView dividerYzm;
    private View btnLogin;
    private View btnRegister;
    private TextView btnGetCaptcha;

    private static Handler uiHandler = new Handler();
    private int countdown = 60;

    private Runnable captchaRunnable = new Runnable() {
        @Override
        public void run() {
            if (countdown == 0) {
                countdown = 60;
                btnGetCaptcha.setEnabled(true);
                btnGetCaptcha.setText("获取验证码");
            } else {
                btnGetCaptcha.setEnabled(false);
                btnGetCaptcha.setText(countdown + "");
                countdown--;
                uiHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public int getLayoutResId() {
        return R.layout.activity_register;
    }

    @Override
    public void initialData(Bundle savedInstanceState) {

    }

    @Override
    public void initialView() {
        commonTopBar = findViewById(R.id.common_topbar);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnGetCaptcha = findViewById(R.id.btn_get_captcha);
        etPhone = findViewById(R.id.et_phone);
        etPsw = findViewById(R.id.et_psw);
        etYzm = findViewById(R.id.et_yzm);
        ivPhone = findViewById(R.id.iv_phone_icon);
        ivPsw = findViewById(R.id.iv_psw_icon);
        ivYzm = findViewById(R.id.iv_yzm_icon);
        dividerPhone = findViewById(R.id.divider_phone);
        dividerPsw = findViewById(R.id.divider_psw);
        dividerYzm = findViewById(R.id.divider_yzm);
        ivPhone.setEnabled(false);
        ivPsw.setEnabled(false);
        ivYzm.setEnabled(false);
    }

    @Override
    public void initialEvn() {
        commonTopBar.setCommonTopBarListener(this);
        etPhone.setOnFocusChangeListener(this);
        etYzm.setOnFocusChangeListener(this);
        etPsw.setOnFocusChangeListener(this);
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnGetCaptcha.setOnClickListener(this);
    }

    @Override
    public void onBackKeyPressed() {
        finish();
    }

    @Override
    public void onNetworkStateChanged(int type, boolean isAvailable) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_phone:
                ivPhone.setEnabled(hasFocus);
                dividerPhone.setBackgroundColor(hasFocus ? Color.parseColor("#007AFF") : Color.parseColor("#B2B2B2"));
                break;
            case R.id.et_psw:
                ivPsw.setEnabled(hasFocus);
                dividerPsw.setBackgroundColor(hasFocus ? Color.parseColor("#007AFF") : Color.parseColor("#B2B2B2"));
                break;
            case R.id.et_yzm:
                ivYzm.setEnabled(hasFocus);
                dividerYzm.setBackgroundColor(hasFocus ? Color.parseColor("#007AFF") : Color.parseColor("#B2B2B2"));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case R.id.btn_register:
                VerifyUtil.Result phone = VerifyUtil.isEmptyInput(etPhone);
                VerifyUtil.Result yzm = VerifyUtil.isEmptyInput(etYzm);
                VerifyUtil.Result psw = VerifyUtil.isEmptyInput(etPsw);
                if (phone.isEmpty) {
                    showMsg("手机号格式错误!");
                    return;
                }
                if (yzm.isEmpty) {
                    showMsg("验证码格式错误!");
                    return;
                }
                if (psw.isEmpty) {
                    showMsg("密码格式错误!");
                    return;
                }
                regisger(phone.value, yzm.value, psw.value);

                break;
            case R.id.btn_get_captcha:
                VerifyUtil.Result result = VerifyUtil.isEmptyInput(etPhone);
                if (!result.isEmpty) {
                    getCaptcha(result.value);
                } else {
                    showMsg("手机号格式错误!");
                }
                break;

        }
    }

    @Override
    public void onTopLeftButtonClick(View v) {
        finish();
    }

    @Override
    public void onTopRightButtonClick(View v) {

    }

    @Override
    public void onHttpSucceed(int what, SimpleResult ret, Response response) {
        super.onHttpSucceed(what, ret, response);
        if (ret != null) {
            if (what == URLManager.Get_Captcha.action) {
                if (ret.getCode() == SimpleResult.SUCCESS) {
                    uiHandler.postDelayed(captchaRunnable, 0);
                } else {
                    showMsg(ret.getMessage());
                }
            } else if (what == URLManager.Register.action) {
                if (ret.getCode() == SimpleResult.SUCCESS) {
                    showMsg("注册成功!");
                    PluginHandler.cookie.updatePluginLogin(false);
                    VerifyUtil.Result phone = VerifyUtil.isEmptyInput(etPhone);
                    VerifyUtil.Result psw = VerifyUtil.isEmptyInput(etPsw);
                    PluginLoginManager.saveLoginAccount(RegisterActivity.this, phone.value, psw.value, true);
                    startActivity(new Intent(this, LauncherActivity.class));
                    finish();
                } else {
                    showMsg(ret.getMessage());
                }
            }
        }
    }

    @Override
    public void onHttpFailed(int what, SimpleResult ret, Response response) {
        super.onHttpFailed(what, ret, response);
        if (ret != null) {
            showMsg(ret.getMessage());
        }
    }

    @Override
    public void onHttpFinish(int what) {
        super.onHttpFinish(what);
        if (what == URLManager.Get_Captcha.action) {

        } else if (what == URLManager.Register.action) {

        }
    }

    /**
     * 获取验证码
     *
     * @param phone
     */
    public void getCaptcha(String phone) {
        SimpleRequest<SimpleResult> request = new SimpleRequest<SimpleResult>(URLManager.Get_Captcha.getURL() + "?mobile=" + phone, URLManager.Get_Captcha.method, SimpleResult.class);
        addTask2Queue(
                URLManager.Get_Captcha.action,
                request
        );
    }

    /**
     * 注册
     *
     * @param mobile
     * @param captcha
     * @param psw
     */
    public void regisger(String mobile, String captcha, String psw) {
        SimpleRequest<SimpleResult> request = new SimpleRequest<SimpleResult>(URLManager.Register.getURL(), URLManager.Register.method, SimpleResult.class);
        HashMap<String, String> p = new HashMap<>();
        p.put("mobile", mobile);
        p.put("vcode", captcha);
        p.put("password", psw);
        request.setRequestBodyAsJson(p);
        addTask2Queue(
                URLManager.Register.action,
                request
        );
    }

}