package blacksmith.sullivanway.display;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;

import blacksmith.sullivanway.R;
import blacksmith.sullivanway.database.InoutInfo;

public class TimeTableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        String lineNm = getIntent().getStringExtra("lineNm");
        String stnNm = getIntent().getStringExtra("stnNm");
        int bgResId = getIntent().getIntExtra("bgResId", -1);

        // 상단 TextView 텍스트 출력
        TextView stnNmView = (TextView) findViewById(R.id.stnNm);
        stnNmView.setText(String.format("%s %s", lineNm, stnNm));

        TextView upStnNmView = (TextView) findViewById(R.id.upwardStnNm);
        String upStnNms = InoutInfo.getNextStnNms(lineNm, stnNm, 1);
        upStnNmView.setText(upStnNms);

        TextView downStnNmView = (TextView) findViewById(R.id.downwardStnNm);
        String downStnNms = InoutInfo.getNextStnNms(lineNm, stnNm, 2);
        downStnNmView.setText(downStnNms);

        // Fragment 생성, Bundle 전달
        Bundle[] bundle = new Bundle[3];
        for (int i = 0; i < bundle.length; i++) {
            bundle[i] = new Bundle();
            bundle[i].putString("lineNm", lineNm);
            bundle[i].putString("stnNm", stnNm);
            bundle[i].putInt("weekTag", i + 1);
        }
        TimeTableFragment workDayFragment = new TimeTableFragment();
        TimeTableFragment saturDayFragment = new TimeTableFragment();
        TimeTableFragment sunDayFragment = new TimeTableFragment();
        workDayFragment.setArguments(bundle[0]);
        saturDayFragment.setArguments(bundle[1]);
        sunDayFragment.setArguments(bundle[2]);

        // ViewPager
        TTPagerAdapter vpAdapter = new TTPagerAdapter(getSupportFragmentManager());
        vpAdapter.addFragment(workDayFragment, getString(R.string.workDay));
        vpAdapter.addFragment(saturDayFragment, getString(R.string.saturDay));
        vpAdapter.addFragment(sunDayFragment, getString(R.string.sunDay));
        viewPager.setAdapter(vpAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);

        // 테두리
        if (bgResId != -1) {
            tabLayout.setBackground(getResources().getDrawable(bgResId, null));
            viewPager.setBackground(getResources().getDrawable(bgResId, null));
        }
    }

    private class TTPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments = new ArrayList<>();
        private ArrayList<String> titles = new ArrayList<>();

        TTPagerAdapter(FragmentManager fm) {
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

}
