package blacksmith.sullivanway.display;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;

import java.util.ArrayList;

import blacksmith.sullivanway.R;
import blacksmith.sullivanway.database.EvInfo;

public class StnInfoMapActivity extends NMapActivity {
    private static final String CLIENT_ID = "st0CXRNX5tTLT0YepCPH";// 애플리케이션 클라이언트 아이디 값

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stn_info_map);

        // Get Intent
        Intent intent = getIntent();
        String lineNm = intent.getStringExtra("lineNm");
        String stnNm = intent.getStringExtra("stnNm");
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);
        ArrayList<EvInfo> evInfos = intent.getParcelableArrayListExtra("EvInfos");
        int bgResId = intent.getIntExtra("bgResId", -1);

        // AppBarLayout Title 설정
        TextView appBarTitleView = (TextView) findViewById(R.id.appBarTitle);
        appBarTitleView.setText(String.format("%s %s", lineNm, stnNm));

        // NMapView 생성
        NMapView mapView = (NMapView) findViewById(R.id.mapView);
        mapView.setClientId(CLIENT_ID); // 클라이언트 아이디 값 설정
        setupMapView(mapView);

        mapView.setClickable(true);
        mapView.setEnabled(true);
        mapView.setFocusable(true);
        mapView.setFocusableInTouchMode(true);
        mapView.requestFocus();

        // 확대, 축소
        mapView.setScalingFactor(1.5f, true);
        NMapController nMapController = mapView.getMapController();
        nMapController.setZoomEnabled(true);
        nMapController.setMapViewPanoramaMode(false);
        nMapController.setMapCenter(new NGeoPoint(latitude, longitude), 11);

        /*
        NMapProjection nMapProjection = mapView.getMapProjection();
        nMapProjection.fromPixels(37.588458, 127.006221);
        */

        // 지도 높이 설정
        ViewGroup.LayoutParams mapContainerParam = mapView.getLayoutParams();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int height = metrics.heightPixels;
        mapContainerParam.height = (int) (height * 0.7);
        mapView.setLayoutParams(mapContainerParam);

        // 테두리
        if (bgResId != -1) {
            LinearLayout mapContent = (LinearLayout) findViewById(R.id.mapContent);
            mapContent.setBackground(getResources().getDrawable(bgResId, null));
            LinearLayout evContent = (LinearLayout) findViewById(R.id.evContent);
            evContent.setBackground(getResources().getDrawable(bgResId, null));
        }

        // 엘리베이터 정보 리스트뷰
        if (evInfos != null && evInfos.size() > 0) {
            ListView evListView = (ListView) findViewById(R.id.evListView);
            evListView.setAdapter(new EvAdapter(this, R.layout.item_stn_info_ev, evInfos));
            setListViewHeight(evListView);
        } else {
            TextView tmp = (TextView) findViewById(R.id.tmp);
            tmp.setText(R.string.no_ev_item);
            tmp.setTextSize(18);
            tmp.setPadding(0, 15, 0, 0);
            tmp.setGravity(Gravity.CENTER);
        }
    }


    /* EvListView */
    void setListViewHeight(ListView listView) {
        ListAdapter adapter = listView.getAdapter();

        int height = 0;
        for (int position = 0; position < adapter.getCount(); position++) {
            View itemView = adapter.getView(position, null, listView);
            itemView.measure(0, 0);
            height += itemView.getMeasuredHeight();
        }

        int dividersHeight = listView.getDividerHeight() * adapter.getCount();
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = height + dividersHeight;
        listView.setLayoutParams(params);
    }

    private class EvAdapter extends BaseAdapter {
        private Context context;
        private int resId;
        private ArrayList<EvInfo> evInfos;

        EvAdapter(Context context, int resId, ArrayList<EvInfo> evInfos) {
            this.context = context;
            this.resId = resId;
            this.evInfos = evInfos;
        }

        @Override
        public int getCount() {
            return evInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return evInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resId, parent, false);
                holder = new Holder();
                holder.num = (TextView) convertView.findViewById(R.id.num);
                holder.floor = (TextView) convertView.findViewById(R.id.floor);
                holder.location = (TextView) convertView.findViewById(R.id.location);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.num.setText(evInfos.get(position).getNum());
            holder.floor.setText(evInfos.get(position).getFloor());
            holder.location.setText(evInfos.get(position).getLocation());

            return convertView;
        }

        private class Holder {
            TextView num, floor, location;
        }

    }

}
