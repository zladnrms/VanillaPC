package zladnrms.defytech.vanillapc.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONArray;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import zladnrms.defytech.vanillapc.R;
import zladnrms.defytech.vanillapc.SQLite.LoginSQLHelper;
import zladnrms.defytech.vanillapc.attendance.App_attendance;
import zladnrms.defytech.vanillapc.member.App_login;

public class App_main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener{

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    private JSONArray jarray = null; // PHP에서 받아온 JSON Array에 대한 처리

    private OkHttpClient client = new OkHttpClient();

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Pager adapter;

    // SQLite
    private LoginSQLHelper loginSqlHelper = null;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String userName = null;
    private String userId = null;

    private RotateLoading rotateLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pref = getSharedPreferences("logininfo", 0);
        editor = pref.edit();

        userName = pref.getString("name", "손님");
        userId = pref.getString("id", "손님");

        //Tablayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        // SQLite
        loginSqlHelper = new LoginSQLHelper(getApplicationContext(), "LoginData.db", null, 1);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // ViewPager
        viewPager = (ViewPager) findViewById(R.id.main_viewpager);

        adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        viewPager.setAdapter(adapter);

        // TabSelectedListener 달기
        tabLayout.setOnTabSelectedListener(this);

        // DrawerLayout 처리
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Navigation DrawerHeader 부분 처리
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0); // Hedaer 객체 생성
        rotateLoading = (RotateLoading) hView.findViewById(R.id.rotateloading);
        TextView nav_user = (TextView) hView.findViewById(R.id.tv_drawerheader_nickname);
        nav_user.setText(userName); // 사용자 닉네임 Set
        navigationView.setNavigationItemSelectedListener(this);
    }

    // BackPressed 시 DrawerLayout 처리
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // DrawerLayout 버튼 처리
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_attendance) {
            Intent intent = new Intent(App_main.this, App_attendance.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            loginSqlHelper.delete(); // 자동 로그인 해제
            Intent intent = new Intent(App_main.this, App_login.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // ViewPager 처리
    public class Pager extends FragmentStatePagerAdapter {

        int tabCount;

        public Pager(android.support.v4.app.FragmentManager fm, int tabCount) {
            super(fm);
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    App_home homeTab = new App_home();
                    return homeTab;
                case 1:
                    App_map boardTab = new App_map();
                    return boardTab;
                default:
                    return null;
            }
        }

        public void destroyFragment(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}


