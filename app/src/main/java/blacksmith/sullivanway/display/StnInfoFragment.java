package blacksmith.sullivanway.display;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;

import blacksmith.sullivanway.R;
import blacksmith.sullivanway.data.LineInfo;
import blacksmith.sullivanway.database.CongestionInfo;
import blacksmith.sullivanway.database.EvInfo;
import blacksmith.sullivanway.database.StnInfo;

public class StnInfoFragment extends Fragment {
    private StnInfo stn;
    private CongestionInfo congestionInfo;
    private ArrayList<EvInfo> evInfos;
    private int bgResId;

    /* View */
    private StnInfoMapFragment mapFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        stn = bundle.getParcelable("StnInfo"); //역 정보
        congestionInfo = bundle.getParcelable("CongestionInfo"); //혼잡도
        evInfos = bundle.getParcelableArrayList("EvInfos"); //엘리베이터
        bgResId = LineInfo.getBgResId(stn.getLineNm());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stn_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* View */
        View view = getView();
        if (view != null) {
            // 테두리 설정
            TableLayout basicContent = (TableLayout) view.findViewById(R.id.basicContent);
            basicContent.setBackground(getResources().getDrawable(bgResId, null));
            LinearLayout mapContent = (LinearLayout) view.findViewById(R.id.mapContent);
            mapContent.setBackground(getResources().getDrawable(bgResId, null));
            RelativeLayout etcContent = (RelativeLayout) view.findViewById(R.id.etcContent);
            etcContent.setBackground(getResources().getDrawable(bgResId, null));

            // 기본정보
            TextView toilet = (TextView) view.findViewById(R.id.toilet);
            TextView door = (TextView) view.findViewById(R.id.door);
            TextView contact = (TextView) view.findViewById(R.id.contact);
            TextView elevator = (TextView) view.findViewById(R.id.elevator);
            TextView escalator = (TextView) view.findViewById(R.id.escalator);
            TextView wheelChairLift = (TextView) view.findViewById(R.id.wheelChairLift);

            toilet.setText(String.format("%s", stn.getToilet()));
            door.setText(String.format("%s", stn.getDoor()));
            contact.setText(String.format("%s", stn.getContact()));
            elevator.setText(String.format("%s", stn.getElevator()));
            escalator.setText(String.format("%s", stn.getEscalator()));
            wheelChairLift.setText(String.format("%s", stn.getWheelLift()));

            // 주변지도
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            mapFragment = new StnInfoMapFragment();
            Bundle mapBundle = new Bundle();
            mapBundle.putDouble("latitude", stn.getWgsy());
            mapBundle.putDouble("longitude", stn.getWgsx());
            mapBundle.putParcelableArrayList("EvInfos", evInfos);
            mapFragment.setArguments(mapBundle);
            ft.replace(R.id.mapContainer, mapFragment);
            ft.commit();

            FrameLayout mapContainer = (FrameLayout) view.findViewById(R.id.mapContainer);
            mapContainer.setClickable(true);
            mapContainer.setOnTouchListener(new OnMapTouch());
            mapContainer.setLongClickable(true);

            // 혼잡도
            TextView congstnBtn = (TextView) view.findViewById(R.id.cngstnBtn);
            congstnBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CongestionActivity.class);
                    intent.putExtra("lineNm", stn.getLineNm());
                    intent.putExtra("stnNm", stn.getStnNm());
                    intent.putExtra("bgResId", bgResId);
                    intent.putExtra("CongestionInfo", congestionInfo);
                    getActivity().startActivity(intent);
                }
            });

            // 시간표
            TextView timeTableBtn = (TextView) view.findViewById(R.id.timetableBtn);
            timeTableBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), TimeTableActivity.class);
                    intent.putExtra("lineNm", stn.getLineNm());
                    intent.putExtra("stnNm", stn.getStnNm());
                    intent.putExtra("bgResId", bgResId);
                    getActivity().startActivity(intent);
                }
            });
        }

    }

    public StnInfoMapFragment getMapFragment() {
        return mapFragment;
    }

    private class OnMapTouch implements ViewGroup.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_UP:
                    Intent intent = new Intent(getActivity(), StnInfoMapActivity.class);
                    intent.putExtra("lineNm", stn.getLineNm());
                    intent.putExtra("stnNm", stn.getStnNm());
                    intent.putExtra("latitude", stn.getWgsy());
                    intent.putExtra("longitude", stn.getWgsx());
                    intent.putParcelableArrayListExtra("EvInfos", evInfos);
                    intent.putExtra("bgResId", bgResId);
                    startActivity(intent);
                    return true;
                default:
            }
            return false;
        }
    }

}
