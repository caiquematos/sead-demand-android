package com.example.caiqu.demand.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by caiqu on 29/05/2017.
 */

public class FeedReaderDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sead.db";

    // Table demands queries.
    private static final String SQL_CREATE_DEMANDS_TABLE =
            "CREATE TABLE " + FeedReaderContract.DemandEntry.TABLE_NAME + " (" +
                    FeedReaderContract.DemandEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID + " INTEGER UNIQUE," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " INTEGER," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID + " INTEGER," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT + " VARCHAR(60)," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " CHAR(2)," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN + " CHAR(2)," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_IMPORTANCE + " VARCHAR(12)," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_CREATED_AT + " TIMESTAMP," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_UPDATED_AT + " TIMESTAMP)";

    private static final String SQL_DELETE_DEMANDS_TABLE =
            "DROP TABLE IF EXISTS " + FeedReaderContract.DemandEntry.TABLE_NAME;

 // Table users queries.
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + FeedReaderContract.UserEntry.TABLE_NAME + " (" +
                    FeedReaderContract.UserEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " INTEGER UNIQUE," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_NAME + " VARCHAR(60)," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_EMAIL + " VARCHAR(60)," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_JOB_POSITION + " VARCHAR(60)," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_STATUS + " CHAR(2)," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR + " INTEGER," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_FCM + " TEXT," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_CREATED_AT + " TIMESTAMP," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_UPDATED_AT + " TIMESTAMP)";

    private static final String SQL_DELETE_USERS_TABLE =
            "DROP TABLE IF EXISTS " + FeedReaderContract.UserEntry.TABLE_NAME;

    public FeedReaderDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DEMANDS_TABLE);
        db.execSQL(SQL_CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_DEMANDS_TABLE);
        db.execSQL(SQL_DELETE_USERS_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
