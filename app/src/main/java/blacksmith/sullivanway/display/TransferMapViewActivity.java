package blacksmith.sullivanway.display;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import blacksmith.sullivanway.MainActivity;
import blacksmith.sullivanway.R;
import blacksmith.sullivanway.database.MyDBOpenHelper;
import blacksmith.sullivanway.database.TransMap;

public class TransferMapViewActivity extends AppCompatActivity {
    final private int selectedDrawable = Color.parseColor("#64a2ff");
    final private int defaultDrawable = Color.parseColor("#bfd9ff");
    private int selectedBtnId = 0;

    private ViewPager vp;
    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<TransMapViewFragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_map_view);

        TextView stnNmTextView = (TextView) findViewById(R.id.stnNm);
        TextView directionTextView = (TextView) findViewById(R.id.direction);
        TextView walkTimeTextView = (TextView) findViewById(R.id.walkTime);
        LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.ButtonLayout);
        vp = (ViewPager) findViewById(R.id.vp);

        MyDBOpenHelper dbOpenHelper = new MyDBOpenHelper(this);

        Intent intent = getIntent();
        String stnNm=intent.getStringExtra("stnNm");
        String startLineNm = intent.getStringExtra("startLineNm");
        String startNextStnNm = intent.getStringExtra("startNextStnNm");
        String endLineNm = intent.getStringExtra("endLineNm");
        String endNextStnNm = intent.getStringExtra("endNextStnNm");

        // 역명, 환승호선, 방향으로 DB에서 환승지도를 찾는다
        String sql = String.format("SELECT res, floor, time FROM %s WHERE stnNm='%s' AND startLineNm='%s' AND startNextStnNm='%s'" +
                        " AND endLineNm='%s' AND endNextStnNm='%s'",
                TransMap.TB_NAME, stnNm, startLineNm, startNextStnNm, endLineNm, endNextStnNm);
        Cursor cursor = dbOpenHelper.getReadableDatabase().rawQuery(sql, null);

        int time = -1; //소요시간
        int numOfTransMaps = cursor.getCount(); //현재 프래그먼트에 포함될 환승지도 개수

        // 뷰페이저, 어댑터
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,(float)1.0/numOfTransMaps);

        while (cursor.moveToNext()) {
            int res = cursor.getInt(0);
            String floor = cursor.getString(1);
            final int position = cursor.getPosition();
            if (time == -1)
                time = cursor.getInt(2);

            // 환승지도를 보여줄 Fragment 동적생성
            TransMapViewFragment fragment = new TransMapViewFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("map",res); //맵의 int형 리소스ID를 프래그먼트로 보낸다
            fragment.setArguments(bundle);
            pagerAdapter.add(fragment);

            // '층' 버튼 동적생성
            Button button = new Button(this);
            button.setText(floor);
            button.setLayoutParams(btnParams);
            button.setBackgroundColor(defaultDrawable);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vp.setCurrentItem(position);
                }
            });
            buttons.add(button);
            buttonLayout.addView(button);
        }
        cursor.close();
        vp.setAdapter(pagerAdapter);
        vp.setCurrentItem(0);
        buttons.get(0).setBackgroundColor(selectedDrawable);

        // 어떤 환승지도인지, 소요시간
        stnNmTextView.setText(stnNm);
        String direction = String.format("%s %s방향 → %s %s방향", startLineNm, startNextStnNm, endLineNm, endNextStnNm);
        directionTextView.setText(direction);
        String walkTime = String.format(MainActivity.DEFAULT_LOCALE, "예상소요시간: %d분", time);
        walkTimeTextView.setText(walkTime);

        //ViewPager에서 페이지 변경시 호출하는 리스너 등록
        vp.addOnPageChangeListener(new OnViewPagerPageChange());

    }


    private class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void add(TransMapViewFragment fragment) {
            fragments.add(fragment);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    private class OnViewPagerPageChange implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            buttons.get(selectedBtnId).setBackgroundColor(defaultDrawable);
            buttons.get(position).setBackgroundColor(selectedDrawable);
            selectedBtnId = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}
