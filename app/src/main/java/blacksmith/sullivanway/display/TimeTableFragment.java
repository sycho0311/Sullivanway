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
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import blacksmith.sullivanway.MainActivity;
import blacksmith.sullivanway.R;
import blacksmith.sullivanway.data.TimeTableInfo;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TimeTableFragment extends Fragment {
    private ArrayList<TimeTableInfo> upInfos = new ArrayList<>();
    private ArrayList<TimeTableInfo> downInfos = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_table, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        String lineNm = bundle.getString("lineNm");
        String stnNm = bundle.getString("stnNm");
        int weekTag = bundle.getInt("weekTag");

        if (getView() != null) {
            ListView listView = (ListView) getView().findViewById(R.id.listView);

            // 열차 시간표
            try {
                ArrayList<TimeTableInfo> ttInfos = TimeTableInfo.createArrayListInstance(getActivity(), lineNm, stnNm, weekTag);
                for (TimeTableInfo ttInfo : ttInfos) {
                    if (ttInfo.getInoutTag().equals("1"))
                        upInfos.add(ttInfo);
                    else
                        downInfos.add(ttInfo);
                }

                ArrayList<TTItem> ttItems = new ArrayList<>();
                boolean up_insert_flag = false;
                boolean down_insert_flag = false;
                int ui = 0, di = 0;
                for (int i = 0; i < 1200; i++) {
                    // 현재 행의 시간 분
                    int hour = i / 60 + 5;
                    int min = (i + 1) % 60;

                    TTItem item = new TTItem();

                    // 현재 행의 시간 분보다 작으면 데이터를 넣는다
                    if (ui < upInfos.size() && upInfos.get(ui).getLeftHour() == hour && upInfos.get(ui).getLeftMin() <= min) {
                        TimeTableInfo info = upInfos.get(ui);
                        item.setUpward(info.getEndStnNm(), info.getLeftHour(), info.getLeftMin(), info.getIsExpress());
                        ui++;
                        up_insert_flag = true;
                    }
                    if (di < downInfos.size() && downInfos.get(di).getLeftHour() == hour && downInfos.get(di).getLeftMin() <= min) {
                        TimeTableInfo info = downInfos.get(di);
                        item.setDownward(info.getEndStnNm(), info.getLeftHour(), info.getLeftMin(), info.getIsExpress());
                        di++;
                        down_insert_flag = true;
                    }

                    /* 상행데이터, 하행데이터 중 하나라도 있으면 ttItems에 삽입한다
                       그러면 위에서 조건이 안 맞아 데이터가 세팅안된 item은 빈 상태로 출력된다 */
                    if (up_insert_flag || down_insert_flag) {
                        ttItems.add(item);
                        up_insert_flag = false;
                        down_insert_flag = false;
                    }
                }

                MyAdapter adapter = new MyAdapter(getActivity(), R.layout.item_timetable, ttItems);
                listView.setAdapter(adapter);
            } catch (IOException e) {
                e.printStackTrace();
                TextView tmp = (TextView) getView().findViewById(R.id.tmp);
                tmp.setText(R.string.no_tt_item);
                tmp.setTextSize(18);
                tmp.setPadding(0, 15, 0, 0);
                tmp.setGravity(Gravity.CENTER);
            }
        }
    }

    private class TTItem {
        String upTime = "";
        String upStnNm = "";
        boolean upIsExpress = false;

        String downTime = "";
        String downStnNm = "";
        boolean downIsExpress = false;

        void setUpward(String stnNm, int hour, int min, boolean isExpress) {
            upStnNm = String.format("%s행", stnNm);
            upTime = String.format(MainActivity.DEFAULT_LOCALE, "%02d:%02d", hour, min);
            upIsExpress = isExpress;
        }
        void setDownward(String stnNm, int hour, int min, boolean isExpress) {
            downStnNm = String.format("%s행", stnNm);
            downTime = String.format(MainActivity.DEFAULT_LOCALE, "%02d:%02d", hour, min);
            downIsExpress = isExpress;
        }
    }

    private class MyAdapter extends BaseAdapter {
        private Context context;
        private int itemResId;
        private ArrayList<TTItem> ttItems;

        MyAdapter(Context context, int itemResId, ArrayList<TTItem> ttItems) {
            this.context = context;
            this.itemResId = itemResId;
            this.ttItems = ttItems;
        }

        @Override
        public int getCount() {
            return ttItems.size();
        }

        @Override
        public Object getItem(int position) {
            return ttItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemResId, parent, false);
                holder = new Holder();
                holder.upTimeView = (TextView) convertView.findViewById(R.id.upTimeView);
                holder.upStnNmView = (TextView) convertView.findViewById(R.id.upStnNmView);
                holder.downTimeView = (TextView) convertView.findViewById(R.id.downTimeView);
                holder.downStnNmView = (TextView) convertView.findViewById(R.id.downStnNmView);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.upTimeView.setText(ttItems.get(position).upTime);
            holder.upStnNmView.setText(ttItems.get(position).upStnNm);
            holder.downTimeView.setText(ttItems.get(position).downTime);
            holder.downStnNmView.setText(ttItems.get(position).downStnNm);

            return convertView;
        }

        private class Holder {
            TextView upTimeView;
            TextView upStnNmView;
            TextView downTimeView;
            TextView downStnNmView;
        }
    }
}
