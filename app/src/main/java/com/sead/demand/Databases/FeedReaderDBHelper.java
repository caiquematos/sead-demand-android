package com.sead.demand.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by caiqu on 29/05/2017.
 */

public class FeedReaderDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "sead.db";

    // Table demands queries.
    private static final String SQL_CREATE_DEMANDS_TABLE =
            "CREATE TABLE " + FeedReaderContract.DemandEntry.TABLE_NAME + " (" +
                    FeedReaderContract.DemandEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID + " INTEGER UNIQUE," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " INTEGER," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID + " INTEGER," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_REASON_ID + " INTEGER," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_TYPE_ID + " INTEGER," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT + " VARCHAR(60)," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " CHAR(2)," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN + " CHAR(2)," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_ARCHIVE + " BOOLEAN DEFAULT false," +
                    FeedReaderContract.DemandEntry.COLUMN_NAME_POSTPONED + " INTEGER DEFAULT 0," +
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
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_JOB + " INTEGER," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_FCM + " TEXT," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_CREATED_AT + " TIMESTAMP," +
                    FeedReaderContract.UserEntry.COLUMN_NAME_USER_UPDATED_AT + " TIMESTAMP)";

    private static final String SQL_DELETE_USERS_TABLE =
            "DROP TABLE IF EXISTS " + FeedReaderContract.UserEntry.TABLE_NAME;

    // Table reasons queries.
    private static final String SQL_CREATE_REASONS_TABLE =
            "CREATE TABLE " + FeedReaderContract.ReasonEntry.TABLE_NAME + " (" +
                    FeedReaderContract.ReasonEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.ReasonEntry.COLUMN_NAME_REASON_ID + " INTEGER UNIQUE," +
                    FeedReaderContract.ReasonEntry.COLUMN_NAME_TYPE + " CHAR(2)," +
                    FeedReaderContract.ReasonEntry.COLUMN_NAME_TITLE + " INTEGER," +
                    FeedReaderContract.ReasonEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_CREATED_AT + " TIMESTAMP," +
                    FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_UPDATED_AT + " TIMESTAMP)";

    private static final String SQL_DELETE_REASONS_TABLE =
            "DROP TABLE IF EXISTS " + FeedReaderContract.ReasonEntry.TABLE_NAME;

    // Table authorities queries.
    private static final String SQL_CREATE_AUTH_TABLE =
            "CREATE TABLE " + FeedReaderContract.AuthorityEntry.TABLE_NAME + " (" +
                    FeedReaderContract.AuthorityEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_ID + " INTEGER UNIQUE," +
                    FeedReaderContract.AuthorityEntry.COLUMN_NAME_SUPERIOR + " INTEGER NULL," +
                    FeedReaderContract.AuthorityEntry.COLUMN_NAME_DEMAND + " INTEGER NULL," +
                    FeedReaderContract.AuthorityEntry.COLUMN_NAME_USER + " INTEGER," +
                    FeedReaderContract.AuthorityEntry.COLUMN_NAME_LEVEL + " INTEGER," +
                    FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_CREATED_AT + " TIMESTAMP," +
                    FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_UPDATED_AT + " TIMESTAMP)";

    private static final String SQL_DELETE_AUTH_TABLE =
            "DROP TABLE IF EXISTS " + FeedReaderContract.AuthorityEntry.TABLE_NAME;

    // Table jobs queries.
    private static final String SQL_CREATE_JOB_TABLE =
            "CREATE TABLE " + FeedReaderContract.JobEntry.TABLE_NAME + " (" +
                    FeedReaderContract.JobEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.JobEntry.COLUMN_NAME_JOB_ID + " INTEGER UNIQUE," +
                    FeedReaderContract.JobEntry.COLUMN_NAME_TITLE + " VARCHAR(250)," +
                    FeedReaderContract.JobEntry.COLUMN_NAME_POSITION + " VARCHAR(60)," +
                    FeedReaderContract.JobEntry.COLUMN_NAME_JOB_CREATED_AT + " TIMESTAMP," +
                    FeedReaderContract.JobEntry.COLUMN_NAME_JOB_UPDATED_AT + " TIMESTAMP)";

    private static final String SQL_DELETE_JOB_TABLE =
            "DROP TABLE IF EXISTS " + FeedReaderContract.JobEntry.TABLE_NAME;

    // Table demand types queries.
    private static final String SQL_CREATE_DEMAND_TYPE_TABLE =
            "CREATE TABLE " + FeedReaderContract.DemandTypeEntry.TABLE_NAME + " (" +
                    FeedReaderContract.DemandTypeEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.DemandTypeEntry.COLUMN_NAME_TYPE_ID + " INTEGER UNIQUE," +
                    FeedReaderContract.DemandTypeEntry.COLUMN_NAME_TITLE + " VARCHAR(250)," +
                    FeedReaderContract.DemandTypeEntry.COLUMN_NAME_COMPLEXITY + " INTEGER," +
                    FeedReaderContract.DemandTypeEntry.COLUMN_NAME_PRIORITY + " VARCHAR(60)," +
                    FeedReaderContract.DemandTypeEntry.COLUMN_NAME_TYPE_CREATED_AT + " TIMESTAMP," +
                    FeedReaderContract.DemandTypeEntry.COLUMN_NAME_TYPE_UPDATED_AT + " TIMESTAMP)";

    private static final String SQL_DELETE_DEMAND_TYPE_TABLE =
            "DROP TABLE IF EXISTS " + FeedReaderContract.DemandTypeEntry.TABLE_NAME;

    public FeedReaderDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_REASONS_TABLE);
        db.execSQL(SQL_CREATE_DEMANDS_TABLE);
        db.execSQL(SQL_CREATE_AUTH_TABLE);
        db.execSQL(SQL_CREATE_JOB_TABLE);
        db.execSQL(SQL_CREATE_DEMAND_TYPE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onDelete(db);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    public void onDelete(SQLiteDatabase db){
        db.execSQL(SQL_DELETE_REASONS_TABLE);
        db.execSQL(SQL_DELETE_USERS_TABLE);
        db.execSQL(SQL_DELETE_DEMANDS_TABLE);
        db.execSQL(SQL_DELETE_AUTH_TABLE);
        db.execSQL(SQL_DELETE_JOB_TABLE);
        db.execSQL(SQL_DELETE_DEMAND_TYPE_TABLE);
    }
}
