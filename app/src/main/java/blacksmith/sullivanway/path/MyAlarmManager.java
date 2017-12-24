package blacksmith.sullivanway.path;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class MyAlarmManager {
    private final int REQUEST_CODE = 0;

    private Context context;
    private static AlarmManager manager;

    public MyAlarmManager(Context context) {
        this.context = context;
    }

    public void alarmOn(int[] times, String[] endStnNms, int index) {
        if (manager == null)
            manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        int time = times[index] - 1;
        if (index > 0)
            time += times[index - 1];

        Intent receive = new Intent(context, AlarmReceiver.class);
        receive.putExtra("times", times);
        receive.putExtra("stnNms", endStnNms);
        receive.putExtra("index", index);
        PendingIntent pIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE, receive, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + time * 60000, pIntent);
    }

    public void alarmOff() {
        Intent receive = new Intent(context, AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE, receive, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.cancel(pIntent);
        pIntent.cancel();
        manager = null;
    }

}
