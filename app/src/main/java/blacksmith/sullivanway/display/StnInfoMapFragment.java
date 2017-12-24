package blacksmith.sullivanway.display;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;

import java.util.ArrayList;

import blacksmith.sullivanway.R;
import blacksmith.sullivanway.database.EvInfo;

/**
 * Fragment LifeCycle
 * onAttach -> onCreate -> onCreateView -> onActivityCreated -> onStart -> onResume
 * running
 * onPause -> onStop -> onDestroyView -> onDestroy -> onDetach
 *
 * onCreateView <- onDestroyView
 * onStart <- onStop
 */
public class StnInfoMapFragment extends Fragment {
    private static final String CLIENT_ID = "st0CXRNX5tTLT0YepCPH";// 애플리케이션 클라이언트 아이디 값

    private double latitude, longitude;
    private ArrayList<EvInfo> evInfos;

    /* View */
    private NMapContext mMapContext;
    private NMapView mapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 지도 실행 컨텍스트
        mMapContext = new NMapContext(getActivity());
        mMapContext.onCreate();

        Bundle bundle = getArguments();

        // 위도, 경도
        latitude = bundle.getDouble("latitude");
        longitude = bundle.getDouble("longitude");

        // 엘리베이터
        evInfos = bundle.getParcelableArrayList("EvInfos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stn_info_map, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        if (view != null) {
            mapView = (NMapView) view.findViewById(R.id.mapView);
            mapView.setClientId(CLIENT_ID);
            mMapContext.setupMapView(mapView);
            mapView.setScalingFactor(1.5f, true);
            setMapCenter();

            // 엘리베이터 정보 리스트뷰
            if (evInfos != null && evInfos.size() > 0) {
                ListView evListView = (ListView) view.findViewById(R.id.evListView);
                evListView.setAdapter(new EvAdapter(getActivity(), R.layout.item_stn_info_ev, evInfos));
                setListViewHeight(evListView);
            } else {
                TextView tmp = (TextView) view.findViewById(R.id.tmp);
                tmp.setText(R.string.no_ev_item);
                tmp.setTextSize(18);
                tmp.setPadding(0, 15, 0, 0);
                tmp.setGravity(Gravity.CENTER);
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        mMapContext.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapContext.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapContext.onPause();
    }

    @Override
    public void onStop() {
        mMapContext.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mMapContext.onDestroy();
        super.onDestroy();
    }

    public void setMapCenter() {
        if (mapView != null) {
            // 확대, 축소
            NMapController mMapController = mapView.getMapController();
            mMapController.setZoomEnabled(true);
            mMapController.setMapViewPanoramaMode(false);
            mMapController.setMapCenter(new NGeoPoint(latitude, longitude), 11);

            /*
            NMapProjection nMapProjection = mapView.getMapProjection();
            nMapProjection.fromPixels(37.588458, 127.006221);
            */
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
