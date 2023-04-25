package com.example.seniordesign2;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
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
    private DeviceDialogViewModel deviceDialogViewModel;
    private DatabaseAlarmUpdater databaseAlarmUpdater;
    static AlarmManager alarmManager;
    public AlarmsHandler(Context context, DeviceDialogViewModel deviceDialogViewModel, DatabaseAlarmUpdater databaseAlarmUpdater) {
        this.context = context;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.deviceDialogViewModel = deviceDialogViewModel;
        this.databaseAlarmUpdater = databaseAlarmUpdater;
    }

    public void setCheckAlarm(int hour, int minute) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM_CHECK);
        intent.putExtra(AlarmReceiver.EXTRA_DATA, "Alarm Check at " + System.currentTimeMillis());

        int pendingIntentRequestCode = 0;
        int flag = PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, pendingIntentRequestCode, intent, flag);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.e("APPDEBUG", "Can schedule: " + alarmManager.canScheduleExactAlarms());
        }
        deviceDialogViewModel.getAlarmStatus().setValue(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        databaseAlarmUpdater.UpdateAlarms(hour, minute);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    static class AlarmReceiver extends BroadcastReceiver {
        public final static String ACTION_ALARM_NOTIFY = "SENIOR_DESIGN.ACTION_ALARM_BROADCAST";
        public final static String ACTION_ALARM_CHECK = "SENIOR_DESIGN.ACTION_ALARM_CHECK";
        public final static String EXTRA_DATA = "EXTRA_DATA";
        @Override
        public void onReceive(Context context, Intent intent) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(4000);
            if (intent.getAction().equals(ACTION_ALARM_NOTIFY)) {
                String intentExtra = intent.getStringExtra(EXTRA_DATA);
                Toast.makeText(context, intentExtra, Toast.LENGTH_LONG).show();
            } else if(intent.getAction().equals(ACTION_ALARM_CHECK)) {
                Log.e("APPDEBUG", "ALARM CHECK");
                Toast.makeText(context, "Medicine Alarm!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public TimePickerFragment getTimePickerFragment(DeviceDialogFragment deviceDialogFragment) {
        return new TimePickerFragment(deviceDialogFragment, this);
    }

    public interface DatabaseAlarmUpdater {
        void UpdateAlarms(int hour, int minute);
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        DeviceDialogFragment deviceDialogFragment;
        AlarmsHandler alarmsHandler;

        public TimePickerFragment(DeviceDialogFragment deviceDialogFragment, AlarmsHandler alarmsHandler) {
            this.deviceDialogFragment = deviceDialogFragment;
            this.alarmsHandler = alarmsHandler;
        }

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
            alarmsHandler.setCheckAlarm(hourOfDay, minute);
        }
    }

}
