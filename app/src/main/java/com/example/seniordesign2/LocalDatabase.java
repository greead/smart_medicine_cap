package com.example.seniordesign2;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import java.util.List;

@Database(entities = {LocalDatabase.DeviceLog.class, LocalDatabase.ContactLog.class, LocalDatabase.AlarmLog.class}, version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {

    public abstract DeviceDao deviceDao();
    public abstract ContactDao contactDao();
    public abstract AlarmDao alarmDao();

    @Entity(tableName = "device_log")
    public static class DeviceLog {
        @PrimaryKey(autoGenerate = true)
        public int id;

        @ColumnInfo(name = "device_address")
        public String deviceAddress;

        @ColumnInfo(name = "date")
        public String date;

        @ColumnInfo(name = "time")
        public String time;

        @ColumnInfo(name = "comment")
        public String comment;
    }

    @Entity(tableName = "alarm_log")
    public static class AlarmLog {
        @PrimaryKey
        @ColumnInfo(name = "device_address")
        @NonNull
        public String deviceAddress;

        @ColumnInfo(name = "phone_number")
        public String phoneNumber;
    }

    @Entity(tableName = "contact_log")
    public static class ContactLog {
        @PrimaryKey
        @ColumnInfo(name = "device_address")
        @NonNull
        public String deviceAddress;

        @ColumnInfo(name = "hour")
        public int hour;

        @ColumnInfo(name = "minute")
        public int minute;
    }

    @Dao
    public interface DeviceDao {

        @Query("SELECT * FROM device_log WHERE device_address LIKE :deviceAddress")
        List<DeviceLog> loadByAddress(String deviceAddress);

        @Insert
        void insertAll(DeviceLog... deviceLogs);

        @Delete
        void deleteAll(DeviceLog... deviceLogs);

    }

    @Dao
    public interface AlarmDao {

        @Query("SELECT * FROM alarm_log WHERE device_address LIKE :deviceAddress")
        List<AlarmLog> loadByAddress(String deviceAddress);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertAll(AlarmLog... alarmLogs);

        @Delete
        void deleteAll(AlarmLog... alarmLogs);

    }

    @Dao
    public interface ContactDao {

        @Query("SELECT * FROM contact_log WHERE device_address LIKE :deviceAddress")
        List<ContactLog> loadByAddress(String deviceAddress);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertAll(ContactLog... contactLogs);

        @Delete
        void deleteAll(ContactLog... contactLogs);
    }

}
