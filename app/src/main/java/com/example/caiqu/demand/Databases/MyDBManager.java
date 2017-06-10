package com.example.caiqu.demand.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caiqu on 29/05/2017.
 */

// TODO: Check created_at and updated_at for correct type.
// TODO: Once in a while check if there are users not attached to any demand: delete them!
// TODO: Create method to list all rows for each table.

public class MyDBManager {
    private String TAG = getClass().getSimpleName();

    private FeedReaderDBHelper mMyDbHelper;
    private SQLiteDatabase mDB;

    public MyDBManager(Context context){
        mMyDbHelper = new FeedReaderDBHelper(context);
    }

    public long addDemand(Demand demand){
        // Fist: Add sender and receiver if they don't exist.
        if(addUser(demand.getSender()) < 0) Log.e(TAG, "Sender not added! Prob it exists already.");
        if(addUser(demand.getReceiver()) < 0) Log.e(TAG, "Receiver not added! Prob it exists already.");

        if(findDemandByServerId(demand.getId()) == null) {
            ContentValues values = new ContentValues();

            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID, demand.getId());
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID, demand.getSender().getId());
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID, demand.getReceiver().getId());
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT, demand.getSubject());
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION, demand.getDescription());
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS, demand.getStatus());
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_IMPORTANCE, demand.getImportance());
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN, demand.getSeen());
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_CREATED_AT,
                    new Timestamp(demand.getCreatedAt().getTime()).toString());
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_UPDATED_AT,
                    new Timestamp(demand.getUpdatedAt().getTime()).toString());

            mDB = mMyDbHelper.getWritableDatabase();

            long newRowId = mDB.insert(FeedReaderContract.DemandEntry.TABLE_NAME, null, values);

            mDB.close();

            return newRowId;
        } else {
            mDB.close();
            return -1;
        }
    }

    public long addUser(User user){

        if(findUserByServerId(user.getId()) == null) {

            ContentValues values = new ContentValues();

            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID, user.getId());
            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_NAME, user.getName());
            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_EMAIL, user.getEmail());
            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_JOB_POSITION, user.getPosition());
            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR, user.getSuperior());
            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_STATUS, user.getStatus());
            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_FCM, user.getGcm());
            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_CREATED_AT,
                    new Timestamp(user.getCreatedAt().getTime()).toString());
            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_UPDATED_AT,
                    new Timestamp(user.getUpdatedAt().getTime()).toString());

            mDB = mMyDbHelper.getWritableDatabase();
            long newRowId = mDB.insert(FeedReaderContract.UserEntry.TABLE_NAME, null, values);

            mDB.close();

            return newRowId;
        }else{
            mDB.close();
            return -1;
        }
    }

    // TODO: include parameter updated_at, since every time a column changes this one does too.
    public int updateDemandColumn(int demandId, String column, String value){

        ContentValues values = new ContentValues();
        values.put(column,value);

        String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID + " = ?";
        String[] selectionArgs = {"" + demandId};

        mDB = mMyDbHelper.getReadableDatabase();

        int count = mDB.update(
                FeedReaderContract.DemandEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        mDB.close();

        return count;
    }

    public int updateUserColumn(int userId, String column, String value) {

        ContentValues values = new ContentValues();
        values.put(column,value);

        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {"" + userId};

        mDB = mMyDbHelper.getReadableDatabase();

        int count = mDB.update(
                FeedReaderContract.UserEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        mDB.close();

        return count;
    }

    public int updateDemand(int demandId, ContentValues values){

        String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID + " = ?";
        String[] selectionArgs = {"" + demandId};

        mDB = mMyDbHelper.getReadableDatabase();

        int count = mDB.update(
                FeedReaderContract.DemandEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        mDB.close();

        return count;
    }

    public int updateUser(int userId, ContentValues values) {

        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {"" + userId};

        mDB = mMyDbHelper.getReadableDatabase();

        int count = mDB.update(
                FeedReaderContract.UserEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        mDB.close();

        return count;
    }

    public Demand findDemandByServerId(int id){
        List<Demand> demands;
        String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID + " = ? ";
        String [] args = {"" + id};

        demands = searchDemands(selection,args);

        if(!demands.isEmpty())
            for (int i = 0; i <= demands.size(); i ++)
                return demands.get(i);

        return null;
    }

    public User findUserByServerId(int id){
            List<User> users;
            String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " = ? ";
            String [] args = {"" + id};

            users = searchUsers(selection,args);

            if(!users.isEmpty())
                for (int i = 0; i <= users.size(); i ++)
                    return users.get(i);

            return null;
        }

    public List<Demand> searchDemands(String selection, String[] selectionArgs){

        String[] projection = {
                FeedReaderContract.DemandEntry._ID,
                FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID,
                FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID,
                FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID,
                FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT,
                FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION,
                FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS,
                FeedReaderContract.DemandEntry.COLUMN_NAME_IMPORTANCE,
                FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN,
                FeedReaderContract.DemandEntry.COLUMN_NAME_CREATED_AT,
                FeedReaderContract.DemandEntry.COLUMN_NAME_UPDATED_AT
        };

        String sortOrder =
                FeedReaderContract.DemandEntry.COLUMN_NAME_UPDATED_AT + " DESC";

        mDB = mMyDbHelper.getReadableDatabase();

        Cursor cursor = mDB.query(
                FeedReaderContract.DemandEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<Demand> demands = new ArrayList<>();

        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry._ID));
            int demandId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID));
            int senderId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID));
            int receiverId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID));
            String subject = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS));
            String importance = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_IMPORTANCE));
            String seen = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN));
            String created_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_CREATED_AT));
            String updated_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_UPDATED_AT));

            // Search for local instances of users
            User sender = findUserByServerId(senderId);
            User receiver = findUserByServerId(receiverId);

            if(sender != null && receiver != null){
                Demand demand = new Demand(
                        itemId,
                        demandId,
                        sender,
                        receiver,
                        importance,
                        subject,
                        description,
                        status,
                        seen,
                        created_at,
                        updated_at
                );

                demands.add(demand);
            }

        }

        cursor.close();

        mDB.close();

        return demands;
    }

    public List<User> searchUsers(String selection, String[] selectionArgs){

        String[] projection = {
                FeedReaderContract.UserEntry._ID,
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID,
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_NAME,
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_EMAIL,
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_JOB_POSITION,
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_STATUS,
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR,
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_FCM,
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_CREATED_AT,
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_UPDATED_AT
        };

        String sortOrder =
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_UPDATED_AT + " DESC";

        mDB = mMyDbHelper.getReadableDatabase();

        Cursor cursor = mDB.query(
                FeedReaderContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<User> users = new ArrayList<>();

        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry._ID));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_NAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_EMAIL));
            int superior = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_STATUS));
            String position = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_JOB_POSITION));
            String fcm = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_FCM));
            String created_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_CREATED_AT));
            String updated_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_UPDATED_AT));

            User user = new User(
                    itemId,
                    userId,
                    email,
                    name,
                    position,
                    status,
                    superior,
                    fcm,
                    created_at,
                    updated_at
            );

            users.add(user);
        }

        cursor.close();

        mDB.close();

        return users;
    }

    public void deleteDemandById(Demand id){
        String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID + " = ?";
        String[] selectionArgs = {"" + id};
        mDB.delete(FeedReaderContract.DemandEntry.TABLE_NAME, selection, selectionArgs);
        mDB.close();
    }

    public void deleteUserById(User id){
        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {"" + id};
        mDB.delete(FeedReaderContract.UserEntry.TABLE_NAME, selection, selectionArgs);
        mDB.close();
    }
}
