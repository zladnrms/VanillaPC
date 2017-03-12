package zladnrms.defytech.vanillapc.notice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import zladnrms.defytech.vanillapc.R;

public class App_notice extends AppCompatActivity {

    private String subject, content;
    TextView tv_subject, tv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_notice);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 배너광고
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2412221717253803~7827153179");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent intent = getIntent();
        subject = intent.getStringExtra("subject");
        content = intent.getStringExtra("content");

        tv_subject = (TextView) findViewById(R.id.tv_subject);
        tv_content = (TextView) findViewById(R.id.tv_content);
        tv_subject.setText(subject);
        tv_content.setText(content);
    }
}
