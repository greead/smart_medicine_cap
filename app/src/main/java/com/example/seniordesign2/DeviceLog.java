package com.example.seniordesign2;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Room database and associated classes and methods to control database flow
 */
@Database(entities = {DeviceLog.Entry.class}, version = 1)
public abstract class DeviceLog extends RoomDatabase {

    public abstract LogDao logDao();

    @Entity
    public static class Entry {
        @PrimaryKey(autoGenerate = true)
        public int id;

        @ColumnInfo(name = "device_address")
        public String deviceAddress;

        @ColumnInfo(name = "date")
        public String date;

        @ColumnInfo(name = "time")
        public String time;
    }

    @Dao
    public interface LogDao {
        @Query("SELECT * FROM entry")
        List<Entry> getAll();

        @Query("SELECT * FROM entry WHERE device_address LIKE :address")
        List<Entry> loadByAddress(String address);

        @Insert
        void insertAll(Entry... entries);

        @Delete
        void deleteEntry(Entry entry);

        @Delete
        void deleteEntries(Entry... entries);
    }


}
