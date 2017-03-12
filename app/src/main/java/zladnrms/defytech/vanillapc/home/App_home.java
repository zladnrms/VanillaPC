package zladnrms.defytech.vanillapc.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.api.attributes.BootstrapBrand;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zladnrms.defytech.vanillapc.R;
import zladnrms.defytech.vanillapc.attendance.App_attendance;
import zladnrms.defytech.vanillapc.attendance.Dialog_attendance;
import zladnrms.defytech.vanillapc.notice.App_notice;

public class App_home extends Fragment {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    private JSONArray jarray = null; // PHP에서 받아온 JSON Array에 대한 처리

    private OkHttpClient client = new OkHttpClient();

    private View view; // Fragment View 처리

    private int version = 0; // 현재 버전

    Handler handler = new Handler(Looper.getMainLooper());

    private RecyclerView rv_noticelist;
    private ArrayList<noticeInfo> noticelist;
    private NoticelistAdapter rv_adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.app_home, null);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MobileAds.initialize(getActivity(), "ca-app-pub-2412221717253803~3257352775");
        AdView mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        try {
            versioncheckRequest(URLlink + "/vanilla/version/check.php");
            noticeRequest(URLlink + "/vanilla/notice/get_notice_list.php");
        } catch (IOException e) {

        }

        rv_noticelist = (RecyclerView) view.findViewById(R.id.rv_noticeList);
        noticelist = new ArrayList<noticeInfo>();
        rv_adapter = new NoticelistAdapter(noticelist);
        LinearLayoutManager verticalLayoutmanager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv_noticelist.setLayoutManager(verticalLayoutmanager);
        rv_noticelist.setAdapter(rv_adapter);
    }

    // 버젼 체크
    void versioncheckRequest(String url) throws IOException {

        RequestBody body = new FormBody.Builder()
                .add("version", String.valueOf(version))
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
                                String js_result;

                                if (!c.isNull("result")) {
                                    js_result = c.getString("result");

                                    switch (js_result) {
                                        case "yes":
                                            break;
                                        case "no":

                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    final Dialog_versionupdate customdialog = new Dialog_versionupdate(getActivity());

                                                    customdialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                                        @Override
                                                        public void onShow(DialogInterface dialog) {
                                                        }
                                                    });
                                                    customdialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                        @Override
                                                        public void onDismiss(DialogInterface dialog) {
                                                            boolean ok = customdialog.getOk();
                                                            if (ok) {
                                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                                intent.setData(Uri.parse("market://details?id=" + "zladnrms.defytech.vanillapc"));
                                                                startActivity(intent);

                                                            }
                                                        }
                                                    });
                                                    customdialog.show();
                                                }
                                            });

                                            break;
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            System.out.println("JSONException : " + e);
                        }
                    }
                });
    }

    // 공지사항 받아오기
    void noticeRequest(String url) throws IOException {

        RequestBody body = new FormBody.Builder()
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
                                int js_id = 0;
                                String js_result = null, js_subject = null, js_content = null;

                                if (!c.isNull("result")) {
                                    js_result = c.getString("result");

                                    switch (js_result) {
                                        case "success":

                                            if (!c.isNull("_id")) {
                                                js_id = Integer.valueOf(c.getString("_id"));
                                            }

                                            if (!c.isNull("subject")) {
                                                js_subject = c.getString("subject");
                                            }

                                            if (!c.isNull("content")) {
                                                js_content = c.getString("content");
                                            }

                                            noticeInfo noticeinfo = new noticeInfo(js_id, js_subject, js_content);
                                            noticelist.add(noticeinfo);
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    rv_adapter.notifyDataSetChanged();
                                                }
                                            });
                                            break;
                                    }
                                }

                            }

                        } catch (JSONException e) {
                            System.out.println("JSONException : " + e);
                        }

                    }
                });
    }

    public class NoticelistAdapter extends RecyclerView.Adapter<NoticelistAdapter.ViewHolder> {

        private List<noticeInfo> verticalList;

        public class ViewHolder extends RecyclerView.ViewHolder {

            BootstrapButton btn_type;
            TextView tv_noticelist_subject;

            public ViewHolder(View view) {
                super(view);

                btn_type = (BootstrapButton) view.findViewById(R.id.btn_type);
                tv_noticelist_subject = (TextView) view.findViewById(R.id.tv_notice_subject);
            }
        }

        public NoticelistAdapter(List<noticeInfo> verticalList) {
            this.verticalList = verticalList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_noticelist, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final int noticeId = noticelist.get(position).getId();
            final String subject = noticelist.get(position).getSubject();
            final String content = noticelist.get(position).getContent();

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), App_notice.class);
                    intent.putExtra("subject", subject);
                    intent.putExtra("content", content);
                    startActivity(intent);
                }
            });

            if (holder.tv_noticelist_subject != null) {
                holder.tv_noticelist_subject.setText(subject);
            }
        }

        @Override
        public int getItemCount() {
            return verticalList.size();
        }
    }

    class noticeInfo { // 게임방 정보 클래스

        private int noticeId;
        private String subject;
        private String content;

        public noticeInfo(int _noticeId, String _subject, String _content) {
            this.noticeId = _noticeId;
            this.subject = _subject;
            this.content = _content;
        }

        public int getId() {
            return noticeId;
        }

        public String getSubject() {
            return subject;
        }

        public String getContent() {
            return content;
        }
    }

}
