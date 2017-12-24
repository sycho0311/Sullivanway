package blacksmith.sullivanway.display;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import blacksmith.sullivanway.R;
import blacksmith.sullivanway.database.FavoritePath;
import blacksmith.sullivanway.path.MyAlarmManager;
import blacksmith.sullivanway.path.PathInfo;
import blacksmith.sullivanway.path.PathResultItem;

import static java.lang.Thread.sleep;

public class PathInfoActivity extends AppCompatActivity {
    private ArrayList<PathResultItem> lessTransItems;
    private PathInfo lessTransInfo;
    private ArrayList<PathResultItem> minDistItems;
    private PathInfo minDistInfo;
    private String startLineNm, endLineNm;
    private String startStnNm, endStnNm;

    /* View */
    private ImageView alarmView;
    private ImageView favoriteView;

    /* alarmView 이미지변경을 위한 임시방편... */
    private MyAlarmManager alarmManager;
    public static boolean isAlarmSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_info);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());

        alarmManager = new MyAlarmManager(this);

        Intent intent = getIntent();
        lessTransItems = intent.getParcelableArrayListExtra("lessTransItems");
        lessTransInfo = intent.getParcelableExtra("lessTransInfo");
        minDistItems = intent.getParcelableArrayListExtra("minDistItems");
        minDistInfo = intent.getParcelableExtra("minDistInfo");

        startLineNm = intent.getStringExtra("startLineNm");
        endLineNm = intent.getStringExtra("endLineNm");
        startStnNm = intent.getStringExtra("startStnNm");
        endStnNm = intent.getStringExtra("endStnNm");

        // 최소환승 Fragment
        PathInfoFragment lessTransFragment = new PathInfoFragment();
        Bundle lessTransBundle = new Bundle();
        lessTransBundle.putParcelableArrayList("items", lessTransItems);
        lessTransBundle.putParcelable("pathInfo", lessTransInfo);
        lessTransFragment.setArguments(lessTransBundle);

        // 최단거리 Fragment
        PathInfoFragment minDistFragment = new PathInfoFragment();
        Bundle minDistBundle = new Bundle(getIntent().getExtras());
        minDistBundle.putParcelableArrayList("items", minDistItems);
        minDistBundle.putParcelable("pathInfo", minDistInfo);
        minDistFragment.setArguments(minDistBundle);

        adapter.addFragment(lessTransFragment, getString(R.string.less_trans));
        adapter.addFragment(minDistFragment, getString(R.string.min_dist));
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new OnPathInfoPageChange());
        tabLayout.setupWithViewPager(viewPager);

        alarmView = (ImageView) findViewById(R.id.alarm);
        if (isAlarmSet) //알람이 켜져있으면
            alarmView.setImageResource(R.drawable.ic_alarm_on_black_36dp);
        else
            alarmView.setImageResource(R.drawable.ic_alarm_off_black_36dp);
        alarmView.setOnClickListener(new OnAlarmClick(0, alarmManager));

        // 반대로 버튼
        ImageView reverseView = (ImageView) findViewById(R.id.reverse);
        reverseView.setOnClickListener(new OnReverseClick());

        // 즐겨찾기 이미지 설정
        favoriteView = (ImageView) findViewById(R.id.favorite);
        if (intent.getBooleanExtra("favorite", false)) //경로가 즐겨찾기되어 있으면....
            favoriteView.setImageResource(R.drawable.ic_star_black_48dp);
        else
            favoriteView.setImageResource(R.drawable.ic_star_border_black_48dp);
        favoriteView.setOnClickListener(new OnFavoriteClick());
    }

    // ViewPager
    private class PagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments = new ArrayList<>();
        private ArrayList<String> titles = new ArrayList<>();

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

    }

    private class OnPathInfoPageChange implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            View.OnClickListener listener;
            if (position == 0) //최소환승 경로의 시간을 잰다
                listener = new OnAlarmClick(0, alarmManager);
            else //최단시간 경로의 시간을 잰다
                listener = new OnAlarmClick(1, alarmManager);
            alarmView.setOnClickListener(listener);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

    }

    /* OnClickListener */
    private class OnAlarmClick implements View.OnClickListener {
        private int type;
        private MyAlarmManager alarmManager;

        OnAlarmClick(int type, MyAlarmManager alarmManager) {
            this.type = type;
            this.alarmManager = alarmManager;
        }

        @Override
        public void onClick(View v) {
            int alaramCnt;
            int[] alarmTimes;
            String[] alarmStnNms;
            ArrayList<PathResultItem> items;
            if (type == 0) {
                items = lessTransItems;
                alaramCnt = lessTransInfo.getTransCnt() + 1;
                alarmTimes = new int[alaramCnt];
                alarmStnNms = new String[alaramCnt];
            } else {
                items = minDistItems;
                alaramCnt = minDistInfo.getTransCnt() + 1;
                alarmTimes = new int[alaramCnt];
                alarmStnNms = new String[alaramCnt];
            }

            for (int i = 0; i < alaramCnt; i++) {
                alarmTimes[i] = items.get(i * 2).time;
                if (i * 2 - 1 >= 0)
                    alarmTimes[i] += items.get(i * 2 - 1).time;
                alarmStnNms[i] = items.get(i * 2).endStnNm;
            }

            if (isAlarmSet) { //알람이 켜져있다면 끈다
                isAlarmSet = false;
                alarmManager.alarmOff();
                alarmView.setImageResource(R.drawable.ic_alarm_off_black_36dp);
                Toast.makeText(PathInfoActivity.this, "알람정지!", Toast.LENGTH_SHORT).show();
            } else { //꺼져있다면 켠다
                isAlarmSet = true;
                alarmManager.alarmOn(alarmTimes, alarmStnNms, 0);
                alarmView.setImageResource(R.drawable.ic_alarm_on_black_36dp);
                Toast.makeText(PathInfoActivity.this, "알람동작!", Toast.LENGTH_SHORT).show();
                new AlarmStopCheker().execute(); //현재 Activity가 종료되기 전까지 알람종료를 체크한다
            }
        }

    }

    private class OnReverseClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra("startLineNm", endLineNm);
            intent.putExtra("endLineNm", startLineNm);
            intent.putExtra("startStnNm", endStnNm);
            intent.putExtra("endStnNm", startStnNm);
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    private class OnFavoriteClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (FavoritePath.insert(true, startLineNm, endLineNm, startStnNm, endStnNm)) //history를 favorite로
                favoriteView.setImageResource(R.drawable.ic_star_black_48dp);
            else //favorite를 history로
                favoriteView.setImageResource(R.drawable.ic_star_border_black_48dp);
        }

    }

    /* AsyncTast */
    private class AlarmStopCheker extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (isAlarmSet) {
                try {
                    sleep(333);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { //시간이 다 돼어 자동으로 알람이 꺼질 경우
            alarmView.setImageResource(R.drawable.ic_alarm_off_black_36dp);
            super.onPostExecute(aVoid);
        }
    }

}
