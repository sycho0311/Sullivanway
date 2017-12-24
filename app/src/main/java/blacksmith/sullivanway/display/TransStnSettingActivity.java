package blacksmith.sullivanway.display;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import blacksmith.sullivanway.MainActivity;
import blacksmith.sullivanway.R;
import blacksmith.sullivanway.data.LineInfo;
import blacksmith.sullivanway.database.MyDBOpenHelper;
import blacksmith.sullivanway.database.TransInfo;

/*
* 리스트의 역을 선택하여 선택/해제 가능
* 모든역 선택 해제 버튼
*
* 설정이 저장되기 전에 다익스트라를 돌려보고 무인도 역이 생기는지 확인한다 (에러발생시 무인도역)
* */

public class TransStnSettingActivity extends AppCompatActivity {
    public static final String SETTING = "setting"; //MainActivity에서 쓰고 TransStnSettingActivity에서 읽는다
    public static final String TEMP_SETTING = "temp_setting"; //TransStnSettingActivity에서 쓰고 MainActivity에서 읽는다
    public static final String TRANS_STN = "transStnPref";

    private MyDBOpenHelper myDBOpenHelper = new MyDBOpenHelper(this);

    private ListView transStnListView;
    private TextView cntView;

    private int cnt = 0;
    private int totalCnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans_stn_setting);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(false);

        transStnListView = (ListView) findViewById(R.id.transStnListView);
        cntView = (TextView) findViewById(R.id.cntView);
        TextView applyBtn = (TextView) findViewById(R.id.applyBtn);
        applyBtn.setOnClickListener(new OnApplyBtnClick());

        SharedPreferences transStnPref = getSharedPreferences(SETTING, MODE_PRIVATE);
        ArrayList<ExceptedTransStn> items = new ArrayList<>();
        String sql = String.format("SELECT stnNm, startLineNm, endLineNm FROM %s ORDER BY stnNm", TransInfo.TB_NAME);
        Cursor cursor = myDBOpenHelper.getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext())
            items.add(new ExceptedTransStn(cursor.getString(0), cursor.getString(1), cursor.getString(2), false));
        cursor.close();

        Set<String> values = transStnPref.getStringSet(TRANS_STN, null);
        if (values != null) {
            for (String value : values) {
                StringTokenizer cutter = new StringTokenizer(value, "|", false);
                String stnNm = cutter.nextToken();
                String startLineNm = cutter.nextToken();
                String endLineNm = cutter.nextToken();
                for (ExceptedTransStn item : items)
                    if (item.stnNm.equals(stnNm) && item.startLineNm.equals(startLineNm) && item.endLineNm.equals(endLineNm))
                        item.isChecked = true;
            }

            cnt = values.size();
        }

        ExceptedTransStnAdapter adapter = new ExceptedTransStnAdapter(this, R.layout.item_trans_stn_setting, items);
        transStnListView.setAdapter(adapter);

        totalCnt = items.size();
        refreshCntView();
    }

    void refreshCntView() {
        String numOfStns = String.format(MainActivity.DEFAULT_LOCALE, "선택된 역 개수: %d / %d", cnt, totalCnt);
        cntView.setText(numOfStns);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_trans_stn_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.uncheck_all:
                ExceptedTransStnAdapter adapter = (ExceptedTransStnAdapter) transStnListView.getAdapter();
                for (int i = 0, n = adapter.getCount(); i < n; i++)
                    adapter.getItem(i).isChecked = false;
                transStnListView.setAdapter(adapter);

                // 선택된 역 개수 업데이트
                cnt = 0;
                refreshCntView();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }


    private class ExceptedTransStn {
        String stnNm;
        String startLineNm, endLineNm;
        boolean isChecked;

        ExceptedTransStn(String stnNm, String startLineNm, String endLineNm, boolean isChecked) {
            this.stnNm = stnNm;
            this.startLineNm = startLineNm;
            this.endLineNm = endLineNm;
            this.isChecked = isChecked;
        }
    }

    private class ExceptedTransStnAdapter extends BaseAdapter {
        private Context context;
        private int itemRes;
        private ArrayList<ExceptedTransStn> items;
        private Holder holder;

        ExceptedTransStnAdapter(Context context, int itemRes, ArrayList<ExceptedTransStn> items) {
            this.context = context;
            this.itemRes = itemRes;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ExceptedTransStn getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemRes, parent, false);
                holder = new Holder();
                holder.stnNm = (TextView) convertView.findViewById(R.id.stnNm);
                holder.startLineSym = (ImageView) convertView.findViewById(R.id.startLineSym);
                holder.endLineSym = (ImageView) convertView.findViewById(R.id.endLineSym);
                holder.transStnCheckBox = (CheckBox) convertView.findViewById(R.id.transStnCheckBox);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.stnNm.setText(items.get(position).stnNm);
            holder.startLineSym.setImageResource(LineInfo.getResId(items.get(position).startLineNm));
            holder.endLineSym.setImageResource(LineInfo.getResId(items.get(position).endLineNm));
            holder.transStnCheckBox.setChecked(items.get(position).isChecked);
            holder.transStnCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    items.get(position).isChecked = !items.get(position).isChecked;

                    // 선택된 역 개수 업데이트
                    if (items.get(position).isChecked)    cnt++;
                    else                                    cnt--;
                    refreshCntView();
                }
            });

            return convertView;
        }

        private class Holder {
            TextView stnNm;
            ImageView startLineSym, endLineSym;
            CheckBox transStnCheckBox;
        }
    }

    private class OnApplyBtnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SharedPreferences temp_transStnPref = getSharedPreferences(TEMP_SETTING, MODE_PRIVATE);
            SharedPreferences.Editor editor = temp_transStnPref.edit();
            ExceptedTransStnAdapter adapter = (ExceptedTransStnAdapter) transStnListView.getAdapter();
            Set<String> values = new HashSet<>();
            for (int position = 0; position < adapter.getCount(); position++) {
                ExceptedTransStn transStn = adapter.getItem(position);
                if (transStn.isChecked) {
                    String value = String.format(MainActivity.DEFAULT_LOCALE, "%s|%s|%s",
                            transStn.stnNm, transStn.startLineNm, transStn.endLineNm);
                    values.add(value);
                }
            }
            editor.putStringSet(TRANS_STN, values);
            editor.apply();

            setResult(RESULT_OK);
            finish();
        }

    }

}
