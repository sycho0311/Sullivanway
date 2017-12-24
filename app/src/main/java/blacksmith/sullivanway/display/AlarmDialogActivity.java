package blacksmith.sullivanway.display;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import blacksmith.sullivanway.R;

public class AlarmDialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_alarm_dialog);
        getWindow().getAttributes().width = (int)(getResources().getDisplayMetrics().widthPixels * 0.7);
        getWindow().getAttributes().height = (int)(getResources().getDisplayMetrics().heightPixels * 0.2);

        String msg = getIntent().getStringExtra("message");

        // TextView
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(msg);//도착 지하철정보 설정

        // Button
        TextView button = (TextView) findViewById(R.id.confirmButton);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
