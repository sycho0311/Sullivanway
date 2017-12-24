package blacksmith.sullivanway.display;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

import blacksmith.sullivanway.R;
import blacksmith.sullivanway.database.CongestionInfo;

public class CongestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congestion);

        final String BUSY = "#cc0000";
        final String NORMAL = "#faf2dd1d";
        final String GOOD = "#FA009B39";

        String lineNm = getIntent().getStringExtra("lineNm");
        String stnNm = getIntent().getStringExtra("stnNm");
        int bgResId = getIntent().getIntExtra("bgResId", -1);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(String.format("%s %s", lineNm, stnNm));
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        FrameLayout chartContainer = (FrameLayout) findViewById(R.id.chartContainer);
        if (bgResId != -1)
            chartContainer.setBackground(getResources().getDrawable(bgResId, null));

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        CongestionInfo congestionInfo = getIntent().getParcelableExtra("CongestionInfo");
        if (congestionInfo != null) {
            ArrayList<Integer> numOfPeople = congestionInfo.getNumOfPeople();
            for (int i = 0; i < numOfPeople.size(); i++) {
                int num = numOfPeople.get(i);
                ArrayList<BarEntry> valueSet = new ArrayList<>();
                valueSet.add(new BarEntry(num, numOfPeople.size() - i - 1));
                BarDataSet dataSet = new BarDataSet(valueSet, null);
                if (num > 130000)
                    dataSet.setColor(Color.parseColor(BUSY));
                else if (num > 80000)
                    dataSet.setColor(Color.parseColor(NORMAL));
                else
                    dataSet.setColor(Color.parseColor(GOOD));
                dataSets.add(dataSet);
            }

            HorizontalBarChart chart = new HorizontalBarChart(this);
            BarData data = new BarData(CongestionInfo.fields_reverse, dataSets);
            data.setValueFormatter(new IntegerFormatter());
            chart.setData(data);
            chart.invalidate();
            chart.setClickable(false);
            chart.setDescription("(인원)");
            chart.getLegend().setEnabled(false);
            chart.setDoubleTapToZoomEnabled(false);
            chart.setPinchZoom(false);

            chartContainer.addView(chart);
        } else {
            TextView tmp = new TextView(this);
            tmp.setText(R.string.no_cg_item);
            tmp.setTextSize(18);
            tmp.setPadding(0, 15, 0, 0);
            tmp.setGravity(Gravity.CENTER);

            chartContainer.addView(tmp);
        }
    }

    private class IntegerFormatter implements ValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return String.valueOf(Math.round(value));
        }
    }

}
