package zladnrms.defytech.vanillapc.member;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zladnrms.defytech.vanillapc.R;
import zladnrms.defytech.vanillapc.SQLite.LoginSQLHelper;
import zladnrms.defytech.vanillapc.home.App_main;

public class App_login extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    private JSONArray jarray = null; // PHP에서 받아온 JSON Array에 대한 처리

    private OkHttpClient client = new OkHttpClient();

    // 로그인 버튼
    private BootstrapButton btn_login;
    private Button btn_join;

    // 아이디 패스워드
    private EditText et_login_id, et_login_pw;
    private String login_id, login_pw;

    // 로그인 성공 시 아이디, 실명 저장
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private LoginSQLHelper loginSqlHelper;
    private Boolean autoLogin = false;

    private RotateLoading rotateLoading;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_login);

        loginSqlHelper = new LoginSQLHelper(getApplicationContext(), "LoginData.db", null, 1);

        pref = getSharedPreferences("logininfo", 0);
        editor = pref.edit();

        btn_login = (BootstrapButton) findViewById(R.id.btn_login);
        btn_join = (Button) findViewById(R.id.btn_join);
        et_login_id = (EditText) findViewById(R.id.et_login_id);
        et_login_pw = (EditText) findViewById(R.id.et_login_pw);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2412221717253803~7783453974");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_login_id.getText().toString().equals("") && !et_login_pw.getText().toString().equals("")) {
                    login_id = et_login_id.getText().toString().trim();
                    login_pw = et_login_pw.getText().toString().trim();
                    try{
                    loginRequest(URLlink + "/vanilla/member/login.php");
                    }catch (IOException e){
                        e.getMessage();
                    }
                } else {
                    showCustomToast("아이디와 비밀번호를 모두 입력해주세요", Toast.LENGTH_SHORT);
                }
            }
        });

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(App_login.this, App_join.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // 자동 로그인 ( 한번 로그인 시 다음부터는 자동 ), 인트로 0.6초간 보여준 뒤 실행
        if (loginSqlHelper.chkIdForAuto() != null) { // 저장된 회원 정보가 있을 경우
            login_id = loginSqlHelper.chkIdForAuto();
            login_pw = loginSqlHelper.getPwForAuto();
            autoLogin = true;
            try {
                loginRequest(URLlink + "/vanilla/member/login_auto.php");
            }catch (IOException e){
                e.getMessage();
            }
        }
    }

    // 아이디 중복 체크
    void loginRequest(String url) throws IOException {

        btn_login.setVisibility(View.GONE);
        rotateLoading.start();

        RequestBody body = new FormBody.Builder()
                .add("id", login_id)
                .add("pw", login_pw)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handlerToast("네트워크 오류입니다.");
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
                                String js_error, js_result, js_id, js_name, js_md5pw;

                                if (!c.isNull("error")) { // 우선 에러를 검출함
                                    js_error = c.getString("error");

                                    switch (js_error) {
                                        case "01":
                                            handlerToast("DB 연결에 실패하였습니다");
                                            break;
                                        case "02":
                                            handlerToast("서버 오류입니다 (date_fail)");
                                            break;
                                    }

                                } else { // 에러가 없으면 진행
                                    if (!c.isNull("result")) {
                                        js_result = c.getString("result");

                                        switch (js_result) {
                                            case "miss_id":
                                                handlerToast("가입되어 있지 않은 아이디입니다");
                                                break;
                                            case "miss_pw":
                                                handlerToast("비밀번호가 틀렸습니다");
                                                break;
                                            case "success":
                                                if (!c.isNull("name")&&!c.isNull("id")) {
                                                    js_id = c.getString("id");
                                                    js_name = c.getString("name");
                                                    handlerToast("로그인 성공");
                                                    // php에서 받아온 pw의 md5 암호화값
                                                    if (!c.isNull("md5pw") && !autoLogin) { // 자동 로그인이 아닐 경우
                                                        js_md5pw = c.getString("md5pw");
                                                        loginSqlHelper.insert(login_id, js_md5pw);
                                                    }
                                                    //아이디, 실명 저장
                                                    editor.putString("id", js_id);
                                                    editor.putString("name", js_name);
                                                    editor.commit();
                                                    //아이디, 실명 저장 //
                                                    Intent intent = new Intent(App_login.this, App_main.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else { // 받아온 name이 null이라 일어나는 문제
                                                    handlerToast("로그인 실패 (네트워크 문제)");
                                                }
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
            btn_login.setVisibility(View.VISIBLE);
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

    private void showCustomToast(String msg, int duration){
        //Retrieve the layout inflator
        LayoutInflater inflater = getLayoutInflater();
        //Assign the custom layout to view
        //Parameter 1 - Custom layout XML
        //Parameter 2 - Custom layout ID present in linearlayout tag of XML
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.llayout_custom_toast));
        TextView msgView = (TextView)layout.findViewById(R.id.tv_toast);
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
