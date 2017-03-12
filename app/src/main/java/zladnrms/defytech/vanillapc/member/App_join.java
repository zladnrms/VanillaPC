package zladnrms.defytech.vanillapc.member;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zladnrms.defytech.vanillapc.R;

public class App_join extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    private JSONArray jarray = null; // PHP에서 받아온 JSON Array에 대한 처리

    private OkHttpClient client = new OkHttpClient();

    // 중복체크 / 가입 버튼, 회원정보 입력칸
    private BootstrapButton btn_chk_id, btn_join;
    private EditText et_join_id, et_join_pw, et_join_name;

    // 중복체크 필요 변수, 회원정보 변수
    private int checkNum = 0;
    private Boolean checkId = false;
    private String join_id, join_pw, join_name;

    private RotateLoading rotateLoading;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_join);

        btn_chk_id = (BootstrapButton) findViewById(R.id.btn_chk_id);
        btn_join = (BootstrapButton) findViewById(R.id.btn_join);
        et_join_id = (EditText) findViewById(R.id.et_join_id);
        et_join_pw = (EditText) findViewById(R.id.et_join_pw);
        et_join_name = (EditText) findViewById(R.id.et_join_name);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2412221717253803~9722688776");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        btn_chk_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_join_id.getText().toString().equals("")) {
                    checkNum = 0;
                    join_id = et_join_id.getText().toString().trim().toLowerCase();

                    try {
                        checkRequest(URLlink + "/vanilla/member/join_check.php");
                    } catch (IOException e) {

                    }

                } else {
                    showCustomToast("아이디를 입력해주세요", Toast.LENGTH_SHORT);
                    et_join_id.requestFocus();
                }
            }
        });

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                join_pw = et_join_pw.getText().toString().trim();
                join_name = et_join_name.getText().toString().trim();

                if (Pattern.matches("^[A-Za-z0-9]*.{8,30}$", join_pw)) {
                    if (checkId) {

                        try {
                            joinRequest(URLlink + "/vanilla/member/join.php");
                        } catch (IOException e) {

                        }

                    } else {
                        showCustomToast("아이디 중복체크를 해주세요", Toast.LENGTH_SHORT);
                    }
                } else {
                    showCustomToast("비밀번호는 숫자, 영문을 포함한 8자 이상이여야 합니다.", Toast.LENGTH_SHORT);
                    et_join_pw.requestFocus();
                }
            }
        });

        et_join_id.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_chk_id.setClickable(true);
                checkId = false;
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });
    }

    // 아이디 중복 체크
    void checkRequest(String url) throws IOException {

        rotateLoading.start();

        RequestBody body = new FormBody.Builder()
                .add("id", join_id)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.println(e.getMessage());

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();
                        System.out.println(res);

                        try {
                            JSONObject jsonObj = new JSONObject(res);
                            jarray = jsonObj.getJSONArray("result");

                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject c = jarray.getJSONObject(i);
                                String js_error = null, js_result = null;

                                if (!c.isNull("error")) { // 우선 에러를 검출함
                                    js_error = c.getString("error");

                                    switch (js_error) {
                                        case "01":
                                            handlerToast("DB 연결에 실패하였습니다");
                                            break;
                                        case "02":
                                            handlerToast("가입에 실패하였습니다. 잠시 후 다시 시도해보세요.");
                                            break;
                                    }

                                } else { // 에러가 없으면 진행
                                    if (!c.isNull("result")) {
                                        js_result = c.getString("result");

                                        switch (js_result) {
                                            case "success":
                                                if (checkNum == 0) {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            showCustomToast("사용가능한 아이디 입니다.", Toast.LENGTH_SHORT);
                                                            et_join_id.setClickable(false);
                                                            et_join_id.setEnabled(false);
                                                            et_join_id.setFocusable(false);
                                                            et_join_id.setFocusableInTouchMode(false);
                                                            btn_chk_id.setClickable(false);
                                                        }
                                                    });

                                                    checkId = true;

                                                }

                                                break;
                                            case "failure":
                                                handlerToast("중복입니다. 다시 입력해주세요");
                                                break;
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            System.out.println("JSONException : " + e);
                        }

                    }
                });

        if (rotateLoading.isStart()) {
            rotateLoading.stop();
        }
    }

    // 가입
    void joinRequest(String url) throws IOException {

        rotateLoading.start();

        RequestBody body = new FormBody.Builder()
                .add("id", join_id)
                .add("pw", join_pw)
                .add("name", join_name)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.println(e.getMessage());

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();
                        System.out.println(res);

                        try {
                            JSONObject jsonObj = new JSONObject(res);
                            jarray = jsonObj.getJSONArray("result");

                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject c = jarray.getJSONObject(i);
                                String js_error = null, js_result = null;

                                if (!c.isNull("error")) { // 우선 에러를 검출함
                                    js_error = c.getString("error");

                                    switch (js_error) {
                                        case "01":
                                            handlerToast("DB 연결에 실패하였습니다");
                                            break;
                                    }

                                } else { // 에러가 없으면 진행
                                    if (!c.isNull("result")) {
                                        js_result = c.getString("result");

                                        switch (js_result) {
                                            case "success":
                                                handlerToast("가입성공");
                                                // 가입 성공 시 로그인 화면으로
                                                finish();
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                break;
                                            case "already_id":
                                                handlerToast("이미 있는 아이디입니다");
                                                break;
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            System.out.println("JSONException : " + e);
                        }

                    }
                });

        if (rotateLoading.isStart()) {
            rotateLoading.stop();
        }
    }

    void handlerToast(final String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                showCustomToast(msg, Toast.LENGTH_SHORT);
            }
        });
    }

    private void showCustomToast(String msg, int duration) {
        //Retrieve the layout inflator
        LayoutInflater inflater = getLayoutInflater();
        //Assign the custom layout to view
        //Parameter 1 - Custom layout XML
        //Parameter 2 - Custom layout ID present in linearlayout tag of XML
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.llayout_custom_toast));
        TextView msgView = (TextView) layout.findViewById(R.id.tv_toast);
        msgView.setText(msg);
        //Return the application context
        Toast toast = new Toast(getApplicationContext());
        ////Set toast gravity to bottom
        //toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        //Set toast duration
        toast.setDuration(duration);
        //Set the custom layout to Toast
        toast.setView(layout);
        //Display toast
        toast.show();
    }
}
