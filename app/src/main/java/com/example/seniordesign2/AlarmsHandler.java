package com.example.seniordesign2;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * Class for handling alarms
 */
public class AlarmsHandler {

    private Context context;
    static AlarmManager alarmManager;
    public AlarmsHandler(Context context) {
        this.context = context;
        alarmManager = context.getSystemService(AlarmManager.class);
    }

    public void setNotifyAlarm(int minutesAhead) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM_NOTIFY);
        intent.putExtra(AlarmReceiver.EXTRA_DATA, "Alarm Notify at " + System.currentTimeMillis());

        int pendingIntentRequestCode = 0;
        int flag = 0;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, pendingIntentRequestCode, intent, flag);

        long alarmDelay = minutesAhead * 60_000L;
        long alarmTime = System.currentTimeMillis() + alarmDelay;

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

    public void setCheckAlarm(int minutesAhead, int minutesInterval) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM_CHECK);
        intent.putExtra(AlarmReceiver.EXTRA_DATA, "Alarm Check at " + System.currentTimeMillis());

        int pendingIntentRequestCode = 0;
        int flag = 0;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, pendingIntentRequestCode, intent, flag);

        long alarmDelay = (minutesAhead) * 60_000L;
        long alarmTime = System.currentTimeMillis() + alarmDelay;

        long intervalDelay = minutesInterval * 60_000L;
        long intervalTime = System.currentTimeMillis() + intervalDelay;

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, intervalTime, pendingIntent);
    }

    static class AlarmReceiver extends BroadcastReceiver {
        public final static String ACTION_ALARM_NOTIFY = "SENIOR_DESIGN.ACTION_ALARM_BROADCAST";
        public final static String ACTION_ALARM_CHECK = "SENIOR_DESIGN.ACTION_ALARM_CHECK";
        public final static String EXTRA_DATA = "EXTRA_DATA";
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_ALARM_NOTIFY)) {
                String intentExtra = intent.getStringExtra(EXTRA_DATA);
                Toast.makeText(context, intentExtra, Toast.LENGTH_LONG).show();
            } else if(intent.getAction().equals(ACTION_ALARM_CHECK)) {
                new AlarmsHandler(context).setNotifyAlarm(5);
                Toast.makeText(context, "Alarm will sound in 5 minutes", Toast.LENGTH_LONG).show();
            }
        }
    }

    public TimePickerFragment getTimePickerFragment() {
        return new TimePickerFragment();
    }

    public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            final Calendar calendar = Calendar.getInstance();
            int currentHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            int hourOfDayDifference = hourOfDay - currentHourOfDay;
            int minuteDifference = minute - currentMinute;

            if(hourOfDayDifference < 0) {
                hourOfDayDifference = 24 + hourOfDayDifference;
            }
            setCheckAlarm((hourOfDayDifference * 60 + minuteDifference), (24 * 60));
        }
    }

}
