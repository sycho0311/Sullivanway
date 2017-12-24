package blacksmith.sullivanway.display;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import blacksmith.sullivanway.MainActivity;
import blacksmith.sullivanway.R;
import blacksmith.sullivanway.data.LineInfo;
import blacksmith.sullivanway.database.EvInfo;
import blacksmith.sullivanway.database.MyDBOpenHelper;
import blacksmith.sullivanway.database.StnInfo;
import blacksmith.sullivanway.database.TransMap;
import blacksmith.sullivanway.path.PathInfo;
import blacksmith.sullivanway.path.PathResultItem;

public class PathInfoFragment extends Fragment {
    private SQLiteDatabase db;
    private int time;
    private int transCnt;
    private int cost;
    private ArrayList<PathResultItem> items;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        items = bundle.getParcelableArrayList("items");
        PathInfo pathInfo = bundle.getParcelable("pathInfo");
        if (pathInfo != null) {
            time = pathInfo.getTime();
            transCnt = pathInfo.getTransCnt();
            cost = pathInfo.getCost();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_path_info, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        db = new MyDBOpenHelper(getContext()).getReadableDatabase();

        /* View 설정 */
        TextView totalTimeView;
        TextView transCntView;
        TextView costView;
        ListView pathList;
        if (getView() != null) {
            totalTimeView = (TextView) getView().findViewById(R.id.total_time);
            transCntView = (TextView) getView().findViewById(R.id.trans_cnt);
            costView = (TextView) getView().findViewById(R.id.cost);
            pathList = (ListView) getView().findViewById(R.id.path_list);

            // 소요시간, 환승시간 표시
            if (time < 60)
                totalTimeView.setText(String.format(MainActivity.DEFAULT_LOCALE, "%d분 소요", time));
            else
                totalTimeView.setText(String.format(MainActivity.DEFAULT_LOCALE, "%d시간 %d분 소요", time / 60, time % 60));
            transCntView.setText(String.format(MainActivity.DEFAULT_LOCALE, "%d회 환승", transCnt));
            //costView.setText(String.format(MainActivity.DEFAULT_LOCALE, "%d 원", cost));

            // 경로 리스트뷰 표시
            PathInfoAdapter adapter = new PathInfoAdapter(getActivity(), items);
            pathList.setAdapter(adapter);
        }
    }

    private class PathInfoAdapter extends BaseAdapter {
        private static final int NUM_OF_ITEMS = 2;
        private static final int TYPE1 = 0;
        //private static final int TYPE2 = 1;

        private Holder holder;

        private Context context;
        private int[] id = { R.layout.item_path_line, R.layout.item_walk_line };
        private ArrayList<PathResultItem> items;

        private PathInfoAdapter(Context context, ArrayList<PathResultItem> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).type;
        }

        @Override
        public int getViewTypeCount() {
            return NUM_OF_ITEMS;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            int itemType = getItemViewType(position);

            // Holder 생성 or ConvertView로부터 받아오기
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                holder = new PathInfoAdapter.Holder();
                if (itemType == TYPE1) {
                    convertView = inflater.inflate(id[0], parent, false);
                    holder.startCircle = (ImageView) convertView.findViewById(R.id.startCircle);
                    holder.viaLine = (ImageView) convertView.findViewById(R.id.viaLine);
                    holder.endCircle = (ImageView) convertView.findViewById(R.id.endCircle);

                    holder.startLine = (TextView) convertView.findViewById(R.id.startLine);
                    holder.startStnNm = (TextView) convertView.findViewById(R.id.startStnNm);
                    holder.direction = (TextView) convertView.findViewById(R.id.direction);
                    holder.remain = (TextView) convertView.findViewById(R.id.remain);
                    holder.endLine = (TextView) convertView.findViewById(R.id.endLine);
                    holder.endStnNm = (TextView) convertView.findViewById(R.id.endStnNm);
                    holder.door = (TextView) convertView.findViewById(R.id.door);
                } else {
                    convertView = inflater.inflate(id[1], parent, false);
                    // 엘리베이터 정보 리스트뷰
                    holder.startNearEvLocationView = (TextView) convertView.findViewById(R.id.startNearEvLocationView);
                    holder.endNearEvLocationView = (TextView) convertView.findViewById(R.id.endNearEvLocationView);
                    holder.walkImage = (ImageView) convertView.findViewById(R.id.walkImage);
                    holder.remainWalkTime = (TextView) convertView.findViewById(R.id.remainWalkTime);
                    holder.transMap = (ImageView) convertView.findViewById(R.id.transMap);
                }
                convertView.setTag(holder);
            } else {
                holder = (PathInfoAdapter.Holder) convertView.getTag();
            }

            // Holder의 View에 값 설정
            if (itemType == TYPE1) {
                int color = Color.parseColor(LineInfo.getLineColor(items.get(position).lineSymbol));
                String lineSymbol = items.get(position).lineSymbol;
                String lineNm = LineInfo.getLineNm(lineSymbol);
                String startStnNm = items.get(position).startStnNm;
                String endStnNm = items.get(position).endStnNm;
                int time = items.get(position).time;

                holder.startCircle.setColorFilter(color);
                holder.viaLine.setColorFilter(color);
                holder.endCircle.setColorFilter(color);

                holder.startLine.setText(lineSymbol);
                holder.startStnNm.setText(startStnNm);
                holder.direction.setText(String.format("다음역: %s역", items.get(position).nextStnNm));
                if (time < 60)
                    holder.remain.setText(String.format(MainActivity.DEFAULT_LOCALE, "%d개 역 (%d분)", items.get(position).numOfStn, time));
                else
                    holder.remain.setText(String.format(MainActivity.DEFAULT_LOCALE, "%d개 역 (%d시간 %d분)", items.get(position).numOfStn, time / 60, time % 60));
                holder.endLine.setText(lineSymbol);
                holder.endStnNm.setText(endStnNm);
                holder.door.setText(String.format("내리는 문: %s", items.get(position).door));

                holder.startStnNm.setOnTouchListener(new OnStnNmTouch(lineNm, startStnNm));
                holder.endStnNm.setOnTouchListener(new OnStnNmTouch(lineNm, endStnNm));
            } else {
                String transStnNm = items.get(position).transStnNm;
                String transStartLineNm = items.get(position).transStartLineNm;
                String transStartNextStnNm = items.get(position).transStartNextStnNm;
                String transEndLineNm = items.get(position).transEndLineNm;
                String transEndNextStnNm = items.get(position).transEndNextStnNm;

                // 엘리베이터 정보
                String startNearEvLocations = EvInfo.getNearEvLocations(transStartLineNm, transStnNm, transStartNextStnNm);
                if (startNearEvLocations == null)
                    startNearEvLocations = getString(R.string.path_info_no_ev_info);
                String endNearEvLocations = EvInfo.getNearEvLocations(transEndLineNm, transStnNm, transEndNextStnNm);
                if (endNearEvLocations == null)
                    endNearEvLocations = getString(R.string.path_info_no_ev_info);
                holder.startNearEvLocationView.setText(startNearEvLocations);
                holder.endNearEvLocationView.setText(endNearEvLocations);

                // 환승 소요시간
                holder.walkImage.setImageResource(R.drawable.ic_man);
                holder.remainWalkTime.setText(String.format(MainActivity.DEFAULT_LOCALE, "%d 분", items.get(position).walkTime));

                // 역명, 환승호선, 방향으로 DB에서 환승지도를 찾는다 (버튼 활성화/비활성화를 위해 데이터가 있는지만 확인)
                String sql = String.format("SELECT stnNm FROM %s WHERE stnNm='%s' AND startLineNm='%s' AND startNextStnNm='%s'" +
                                " AND endLineNm='%s' AND endNextStnNm='%s'",
                        TransMap.TB_NAME, transStnNm, transStartLineNm, transStartNextStnNm, transEndLineNm, transEndNextStnNm);
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor.moveToNext()) {
                    holder.transMap.setOnClickListener(new OnTransMapBtnClick(transStnNm, transStartLineNm, transStartNextStnNm, transEndLineNm, transEndNextStnNm));
                    holder.transMap.setImageResource(R.drawable.ic_map_black_48dp);
                }
                cursor.close();
            }
            return convertView;
        }

        private class Holder {
            // item_path_line
            ImageView startCircle, viaLine, endCircle;
            TextView startLine, startStnNm, direction,
                    remain,
                    endLine, endStnNm, door;

            // item_walk_line
            TextView startNearEvLocationView;
            TextView endNearEvLocationView;
            ImageView walkImage, transMap;
            TextView remainWalkTime;
        }

    }

    /* Listener */
    private class OnStnNmTouch implements View.OnTouchListener {
        private String lineNm;
        private String stnNm;

        private OnStnNmTouch(String lineNm, String stnNm) {
            this.lineNm = lineNm;
            this.stnNm = stnNm;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_UP:
                    Intent intent = new Intent(PathInfoFragment.this.getActivity(), StnInfoActivity.class);
                    intent.putExtra("lineNm", lineNm);
                    intent.putExtra("stnNm", stnNm);
                    ArrayList<String> lines = StnInfo.getLines(lineNm, stnNm);
                    intent.putExtra("lines", lines);
                    startActivity(intent);
                    return true;
                default:
            }
            return false;
        }

    }

    private class OnTransMapBtnClick implements View.OnClickListener {
        private String transStnNm;
        private String transStartLineNm;
        private String transStartNextStnNm;
        private String transEndLineNm;
        private String transEndNextStnNm;

        private OnTransMapBtnClick(String transStnNm, String transStartLineNm, String transStartNextStnNm, String transEndLineNm, String transEndNextStnNm) {
            this.transStnNm = transStnNm;
            this.transStartLineNm = transStartLineNm;
            this.transStartNextStnNm = transStartNextStnNm;
            this.transEndLineNm = transEndLineNm;
            this.transEndNextStnNm = transEndNextStnNm;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(PathInfoFragment.this.getActivity(), TransferMapViewActivity.class);
            intent.putExtra("stnNm", transStnNm);
            intent.putExtra("startLineNm", transStartLineNm);
            intent.putExtra("startNextStnNm", transStartNextStnNm);
            intent.putExtra("endLineNm", transEndLineNm);
            intent.putExtra("endNextStnNm", transEndNextStnNm);
            startActivity(intent);
        }

    }

    private class OnReStartBtnClick implements View.OnClickListener { //현재역부터 경로를 다시 설정하는 버튼의 이벤트
        private String startLineNm;
        private String endLineNm;
        private String startStnNm;
        private String endStnNm;

        public OnReStartBtnClick(String startLineNm, String endLineNm, String startStnNm, String endStnNm) {
            this.startLineNm = startLineNm;
            this.endLineNm = endLineNm;
            this.startStnNm = startStnNm;
            this.endStnNm = endStnNm;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.putExtra("startLineNm", endLineNm);
            intent.putExtra("endLineNm", startLineNm);
            intent.putExtra("startStnNm", endStnNm);
            intent.putExtra("endStnNm", startStnNm);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }

    }

    private class EvAdapter extends BaseAdapter {
        private Context context;
        private int resId = R.layout.item_stn_info_ev;
        private ArrayList<EvInfo> evInfos;

        EvAdapter(Context context, ArrayList<EvInfo> evInfos) {
            this.context = context;
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
