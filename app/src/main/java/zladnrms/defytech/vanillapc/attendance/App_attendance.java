package zladnrms.defytech.vanillapc.attendance;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zladnrms.defytech.vanillapc.R;

public class App_attendance extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    private JSONArray jarray = null; // PHP에서 받아온 JSON Array에 대한 처리

    private OkHttpClient client = new OkHttpClient();

    private BootstrapButton btn_attendance, btn_account;
    private ImageView iv_at_1, iv_at_2, iv_at_3;
    private TextView tv_attendance_date, tv_account_date, tv_attendance_daybefore, tv_attendance_text, tv_attendance_alert;
    private Handler handler = new Handler();
    private RotateLoading rotateLoading;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String userName = null;
    private String userId = null;
    private int attendanceValue = 0;
    private int timeKinds;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_attendance);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        pref = getSharedPreferences("logininfo", 0);
        editor = pref.edit();

        userName = pref.getString("name", "손님");
        userId = pref.getString("id", "손님");

        btn_attendance = (BootstrapButton) findViewById(R.id.btn_attendance);
        btn_account = (BootstrapButton) findViewById(R.id.btn_account);
        iv_at_1 = (ImageView) findViewById(R.id.iv_at_1);
        iv_at_2 = (ImageView) findViewById(R.id.iv_at_2);
        iv_at_3 = (ImageView) findViewById(R.id.iv_at_3);
        tv_attendance_date = (TextView) findViewById(R.id.tv_attendance_date);
        tv_attendance_daybefore = (TextView) findViewById(R.id.tv_attendance_daybefore);
        tv_attendance_text = (TextView) findViewById(R.id.tv_attendance_text);
        tv_attendance_alert = (TextView) findViewById(R.id.tv_attendance_alert);

        tv_account_date = (TextView) findViewById(R.id.tv_account_date);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);

        // 배너광고
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2412221717253803~9443487172");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // 전면광고 설정
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-2412221717253803/2339022772");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();
        // 전면광고 설정 //

        btn_attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    try {
                        attendanceRequest(URLlink + "/vanilla/handle/attendance.php");
                    } catch (IOException e) {

                    }
                }
            }
        });

        btn_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (attendanceValue == 3) {
                    final Dialog_attendance customdialog = new Dialog_attendance(App_attendance.this);

                    customdialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            customdialog.setName(userName);
                        }
                    });
                    customdialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            boolean ok = customdialog.getOk();
                            if (ok) {
                                try {
                                    accountRequest(URLlink + "/vanilla/handle/account.php");
                                } catch (IOException e) {

                                }
                            }

                        }
                    });
                    customdialog.show();
                } else {
                    showCustomToast("3일 출석을 채우지 않으셨습니다", Toast.LENGTH_SHORT);
                }
            }
        });

        try {
            userInfoRequest(URLlink + "/vanilla/handle/get_user_info.php");
        } catch (IOException e) {

        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        mInterstitialAd.loadAd(adRequest);
    }

    // 사용자 출석, 정산 정보 받아오기
    void userInfoRequest(String url) throws IOException {

        rotateLoading.start();

        RequestBody body = new FormBody.Builder()
                .add("id", userId)
                .add("name", userName)
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

                        try {
                            JSONObject jsonObj = new JSONObject(res);
                            jarray = jsonObj.getJSONArray("result");

                            for (int i = 0; i < jarray.length(); i++) {
                                final JSONObject c = jarray.getJSONObject(i);
                                final String js_error, js_result, js_at_date, js_ac_date;

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
                                                if (!c.isNull("l_at_d")) {
                                                    js_at_date = c.getString("l_at_d");
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            tv_attendance_date.setText(js_at_date);
                                                            tv_attendance_daybefore.setText(compareTime(nowTime(), js_at_date));

                                                            switch (timeKinds) {
                                                                case 0:
                                                                    tv_attendance_alert.setText("아직 출석 하실 수 없습니다");
                                                                    tv_attendance_alert.setTextColor(Color.parseColor("#DF4D4D"));
                                                                    break;
                                                                case 1:
                                                                    tv_attendance_alert.setText("어서 출석해주세요!!");
                                                                    tv_attendance_alert.setTextColor(Color.parseColor("#368AFF"));
                                                                    break;
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            tv_attendance_text.setText("");
                                                        }
                                                    });
                                                }
                                                if (!c.isNull("l_ac_d")) {
                                                    js_ac_date = c.getString("l_ac_d");
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            tv_account_date.setText(js_ac_date);
                                                        }
                                                    });
                                                }
                                                if (!c.isNull("attendance")) {
                                                    attendanceValue = Integer.valueOf(c.getString("attendance"));
                                                    switch (attendanceValue){
                                                        case 1:
                                                            handler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    iv_at_1.setImageResource(R.drawable.attendance_yes);
                                                                }
                                                            });
                                                            break;
                                                        case 2:
                                                            handler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    iv_at_1.setImageResource(R.drawable.attendance_yes);
                                                                    iv_at_2.setImageResource(R.drawable.attendance_yes);
                                                                }
                                                            });
                                                            break;
                                                        case 3:
                                                            handler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    iv_at_1.setImageResource(R.drawable.attendance_yes);
                                                                    iv_at_2.setImageResource(R.drawable.attendance_yes);
                                                                    iv_at_3.setImageResource(R.drawable.attendance_yes);
                                                                }
                                                            });
                                                            break;
                                                        default:
                                                            break;
                                                    }
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
            rotateLoading.stop();
        }
    }

    // 출석 도장찍기
    void attendanceRequest(String url) throws IOException {

        rotateLoading.start();

        RequestBody body = new FormBody.Builder()
                .add("id", userId)
                .add("name", userName)
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
                                String js_error, js_result;
                                final String js_date;

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
                                                if (!c.isNull("date")) {
                                                    js_date = c.getString("date");
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            tv_attendance_date.setText(js_date);
                                                            tv_attendance_daybefore.setText("방금 전");
                                                            tv_attendance_text.setText("에 출석을 하셨습니다. ");
                                                            tv_attendance_alert.setText("아직 출석 하실 수 없습니다");
                                                            tv_attendance_alert.setTextColor(Color.parseColor("#DF4D4D"));
                                                        }
                                                    });
                                                }
                                                attendanceValue += 1;
                                                handlerToast(attendanceValue + "일차 출석하였습니다!");
                                                switch (attendanceValue){
                                                    case 1:
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                iv_at_1.setImageResource(R.drawable.attendance_yes);
                                                            }
                                                        });
                                                        break;
                                                    case 2:
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                iv_at_1.setImageResource(R.drawable.attendance_yes);
                                                                iv_at_2.setImageResource(R.drawable.attendance_yes);
                                                            }
                                                        });
                                                        break;
                                                    case 3:
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                iv_at_1.setImageResource(R.drawable.attendance_yes);
                                                                iv_at_2.setImageResource(R.drawable.attendance_yes);
                                                                iv_at_3.setImageResource(R.drawable.attendance_yes);
                                                            }
                                                        });
                                                        break;
                                                    default:
                                                        break;
                                                }
                                                if (attendanceValue == 3) {
                                                    handlerToast("3일 출석을 채우셨습니다. 카운터에서 정산 후 계속해서 출석 가능합니다.");
                                                }
                                                break;
                                            case "failure_0":
                                                handlerToast("출석한 지 24시간이 지나지 않았습니다");
                                                break;
                                            case "failure_1":
                                                handlerToast("이미 3일 출석을 채우셨습니다. 카운터에서 정산하신 후 출석해주세요");
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

    // 정산하기
    void accountRequest(String url) throws IOException {

        rotateLoading.start();

        RequestBody body = new FormBody.Builder()
                .add("id", userId)
                .add("name", userName)
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
                                String js_error, js_result;
                                final String js_date;

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
                                                if (!c.isNull("date")) {
                                                    js_date = c.getString("date");
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            tv_account_date.setText(js_date);
                                                        }
                                                    });
                                                }
                                                handlerToast("정산하셨습니다");
                                                attendanceValue = 0;
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        iv_at_1.setImageResource(R.drawable.attendance_no);
                                                        iv_at_2.setImageResource(R.drawable.attendance_no);
                                                        iv_at_3.setImageResource(R.drawable.attendance_no);
                                                    }
                                                });
                                                break;
                                            case "failure_0":
                                                handlerToast("3일 출석을 채우지 않으셨습니다");
                                                break;
                                            case "failure_1":
                                                handlerToast("정산한지 3일이 지나지 않았습니다");
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
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private String nowTime() {
        // 현재시간을 msec 으로 구한다.
        long now = System.currentTimeMillis();
        // 현재시간을 date 변수에 저장한다.
        Date date = new Date(now);
        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // nowDate 변수에 값을 저장한다.
        String formatDate = sdfNow.format(date);
        return formatDate;
    }

    private String compareTime(String t1, String t2) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date day1 = null, day2 = null;
        long diff, diffDays = 0;
        String strbefore = "";
        try {
            day1 = format.parse(t1);
            day2 = format.parse(t2);
            System.out.println("day1:" + day1 + ",day2 :" + day2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int compare = day1.compareTo(day2);
        if (compare > 0) {
            diff = day1.getTime() - day2.getTime();
            if (diff > 24 * 60 * 60 * 1000) { // 하루 이상 차이나면
                diffDays = diff / (24 * 60 * 60 * 1000);
                strbefore = diffDays + "일 전";
                timeKinds = 1;
            } else if (diff < 24 * 60 * 60 * 1000 && diff > 60 * 60 * 1000) { // 하루 이상 안나고 몇 시간 차이나면
                diffDays = diff / (60 * 60 * 1000);
                strbefore = diffDays + "시간 전";
                timeKinds = 0;
            } else if (diff < 60 * 60 * 1000 && diff > 60 * 1000) { // 시간 차이 안나고 몇 분 차이나면
                diffDays = diff / (60 * 1000);
                strbefore = diffDays + "분 전";
                timeKinds = 0;
            } else if (diff < 60 * 1000) { // 방금 전
                strbefore = "방금 전";
                timeKinds = 0;
            }
        } else {
            strbefore = "지금";
            timeKinds = 0;
        }
        return strbefore;
    }

    void handlerToast(final String msg) {
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
