package com.gawain.androidsms;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class MainActivity extends AppCompatActivity {


    private EventHandler handler;

    private EditText editTextSMSNum,phoneNumber;

    private Button btnGetIdentifyingCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initHandler();
    }

    private void init(){
        editTextSMSNum = findViewById(R.id.identifyingCode);
        phoneNumber = findViewById(R.id.phoneNumber);

        btnGetIdentifyingCode = findViewById(R.id.btnGetIdentifyingCode);
    }

    private void initHandler() {

        handler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //提交验证码成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                                startActivity(intent);
                            }
                        });

                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        //获取验证码成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                                timer.start();
                            }
                        });
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                    }
                } else {
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;
                    try {
                        JSONObject obj = new JSONObject(throwable.getMessage());
                        final String des = obj.optString("detail");
                        if (!TextUtils.isEmpty(des)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "提交错误信息", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        SMSSDK.registerEventHandler(handler);
    }

    public void play(View view) {
        //获取验证码
        SMSSDK.getVerificationCode("86", getPhoneNumber());
    }

    public void submitIdentifyingCode(View view) {
        String number = editTextSMSNum.getText().toString();
        SMSSDK.submitVerificationCode("86", getPhoneNumber(), number);
    }

    private String getPhoneNumber(){
        if(isInteger(this.phoneNumber.getText().toString())){
            return this.phoneNumber.getText().toString().trim();
        }else {
            Toast.makeText(MainActivity.this, "请输入正确手机号码", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    //countdown and check resent identifyingCode
    final CountDownTimer timer=new CountDownTimer(30000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            long l = millisUntilFinished / 1000;
            btnGetIdentifyingCode.setText(""+l);
            btnGetIdentifyingCode.setClickable(false);
        }

        @Override
        public void onFinish() {
            btnGetIdentifyingCode.setText("重发");
            btnGetIdentifyingCode.setClickable(true);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }
}
