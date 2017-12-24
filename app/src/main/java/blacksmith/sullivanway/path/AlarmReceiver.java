package blacksmith.sullivanway.path;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import blacksmith.sullivanway.R;
import blacksmith.sullivanway.display.AlarmDialogActivity;
import blacksmith.sullivanway.display.PathInfoActivity;

public class AlarmReceiver extends BroadcastReceiver {
    private final int REQUEST_CODE = 1;
    private final int NOTI_ID = 0;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        int[] times = intent.getIntArrayExtra("times");
        String[] stnNms = intent.getStringArrayExtra("stnNms");
        int index = intent.getIntExtra("index", 0);

        String alarmMsg = String.format("잠시 후 %s역에 도착합니다!", stnNms[index]);
        Toast.makeText(context, alarmMsg, Toast.LENGTH_SHORT).show();
        notificationAlarm(alarmMsg);

        new NextAlarmThread(times, stnNms, index).start();
    }

    private void notificationAlarm(String msg) {
        Intent intent = new Intent(context, AlarmDialogActivity.class);
        intent.putExtra("message",msg);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentTitle("지하철 알람서비스")
                .setSmallIcon(R.mipmap.sullivanway_launcher_round)
                .setContentText(msg)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTI_ID, builder.build());
    }

    private class NextAlarmThread extends Thread {
        private int[] times;
        private String[] stnNms;
        private int index;

        private NextAlarmThread(int[] times, String[] stnNms, int index) {
            this.times = times;
            this.stnNms = stnNms;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (times.length > index+1)
                new MyAlarmManager(context).alarmOn(times, stnNms, index+1);
            else
                PathInfoActivity.isAlarmSet = false; //false 로 변경시 PathInfoActivity의 alarmView의 이미지가 변경된다
        }

    }

}
