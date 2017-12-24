package blacksmith.sullivanway.display;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import blacksmith.sullivanway.R;

public class FcmDialogActivtiy extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fcm_dialog_activtiy);
        getWindow().getAttributes().width = (int)(getResources().getDisplayMetrics().widthPixels * 0.8);
        getWindow().getAttributes().height = (int)(getResources().getDisplayMetrics().heightPixels * 0.4);

        String msg = getIntent().getStringExtra("message");

        // TextView
        TextView fcmMsgTextView = (TextView)findViewById(R.id.textView);
        fcmMsgTextView.setMovementMethod(new ScrollingMovementMethod());
        fcmMsgTextView.setText(msg);//도착 지하철정보 설정

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
