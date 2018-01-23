package com.sead.demand.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sead.demand.Entities.Authority;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.DemandType;
import com.sead.demand.Entities.Job;
import com.sead.demand.Entities.PredefinedReason;
import com.sead.demand.Entities.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caiqu on 29/05/2017.
 */

public class MyDBManager {
    private String TAG = getClass().getSimpleName();

    private FeedReaderDBHelper mMyDbHelper;
    private SQLiteDatabase mDB;

    public MyDBManager(Context context){
        mMyDbHelper = new FeedReaderDBHelper(context);
    }

    // If demand already exists, update it.
    public long addDemand(Demand demand){
        // Fist: Add sender and receiver if they don't exist.
        if(addUser(demand.getSender()) < 0) Log.e(TAG, "Sender not added! Prob it exists already.");
        if(addUser(demand.getReceiver()) < 0) Log.e(TAG, "Receiver not added! Prob it exists already.");

        ContentValues values = new ContentValues();

        // If there is a reason, add it.
        PredefinedReason reason = demand.getReason();
        if(reason != null){
            if(addReason(reason) < 0) Log.e(TAG, "Reason not added! Prob it exists already.");
            else Log.e(TAG, "Reason added successfully!");
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_REASON_ID, demand.getReason().getServerId());
        }

        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID, demand.getId());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID, demand.getSender().getId());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID, demand.getReceiver().getId());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT, demand.getSubject());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION, demand.getDescription());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS, demand.getStatus());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN, demand.getSeen());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_CREATED_AT,
                new Timestamp(demand.getCreatedAt().getTime()).toString());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_UPDATED_AT,
                new Timestamp(demand.getUpdatedAt().getTime()).toString());

        if(findDemandByServerId(demand.getId()) == null) {
            mDB = mMyDbHelper.getWritableDatabase();
            long newRowId = mDB.insert(FeedReaderContract.DemandEntry.TABLE_NAME, null, values);

            mDB.close();

            return newRowId;
        } else {
            mDB.close();
            return updateDemand(demand.getId(),values);
        }
    }

    // If user already exists, then update it.
    public long addUser(User user){

        ContentValues values = new ContentValues();

        // Fist: Add Job and Superior if they're not null and don't exist in db.
        // Must check if job is not null, usually, a superior user won't have a job object defined.
        if (user.getJob() != null) {
            Log.d(TAG, "job:" + user.getJob().toString());
            if (addJob(user.getJob()) < 0)
                Log.e(TAG, "Job not added! Prob it exists already.");
            else Log.e(TAG, "Job added successfully:" + user.getJob().getId());
            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_JOB, user.getJob().getId());
        }

        // As done right before, must check if Superior is defined.
        if (user.getSuperior() != null) {
            Log.d(TAG, "superior:" + user.getSuperior().toString());
            if (addUser(user.getSuperior()) < 0)
                Log.e(TAG, "Superior not added! Prob it exists already.");
            else Log.e(TAG, "Superior added successfully:" + user.getSuperior().getId());
            values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR, user.getSuperior().getId());
        }

        Log.e(TAG, "User position:" + user.getPosition());

        values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID, user.getId());
        values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_NAME, user.getName());
        values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_EMAIL, user.getEmail());
        values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_JOB_POSITION, user.getPosition());
        values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_STATUS, user.getStatus());
        values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_FCM, user.getGcm());
        values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_CREATED_AT,
                new Timestamp(user.getCreatedAt().getTime()).toString());
        values.put(FeedReaderContract.UserEntry.COLUMN_NAME_USER_UPDATED_AT,
                new Timestamp(user.getUpdatedAt().getTime()).toString());

        if(findUserByServerId(user.getId()) == null) {
            mDB = mMyDbHelper.getWritableDatabase();
            long newRowId = mDB.insert(FeedReaderContract.UserEntry.TABLE_NAME, null, values);

            mDB.close();

            return newRowId;
        }else{
            mDB.close();
            return updateUser(user.getId(),values);
        }
    }

    // If user already exists, then update it.
    public long addAuthority(Authority auth){

        ContentValues values = new ContentValues();

        Log.e(TAG, "Auth User id:" + auth.getUser());

        values.put(FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_ID, auth.getId());
        values.put(FeedReaderContract.AuthorityEntry.COLUMN_NAME_USER, auth.getUser());
        values.put(FeedReaderContract.AuthorityEntry.COLUMN_NAME_SUPERIOR, auth.getSuperior());
        values.put(FeedReaderContract.AuthorityEntry.COLUMN_NAME_LEVEL, auth.getLevel());
        values.put(FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_CREATED_AT,
                new Timestamp(auth.getCreatedAt().getTime()).toString());
        values.put(FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_UPDATED_AT,
                new Timestamp(auth.getUpdatedAt().getTime()).toString());

        if(findAuthByServerId(auth.getId()) == null) {
            mDB = mMyDbHelper.getWritableDatabase();
            long newRowId = mDB.insert(FeedReaderContract.AuthorityEntry.TABLE_NAME, null, values);

            mDB.close();

            return newRowId;
        }else{
            mDB.close();
            return updateAuthority(auth.getId(),values);
        }
    }

    // If job already exists, then update it.
    public long addJob(Job job){

        ContentValues values = new ContentValues();

        Log.e(TAG, "Job :" + job.getTitle());

        values.put(FeedReaderContract.JobEntry.COLUMN_NAME_JOB_ID, job.getId());
        values.put(FeedReaderContract.JobEntry.COLUMN_NAME_TITLE, job.getTitle());
        values.put(FeedReaderContract.JobEntry.COLUMN_NAME_POSITION, job.getPosition());
        values.put(FeedReaderContract.JobEntry.COLUMN_NAME_JOB_CREATED_AT,
                new Timestamp(job.getCreatedAt().getTime()).toString());
        values.put(FeedReaderContract.JobEntry.COLUMN_NAME_JOB_UPDATED_AT,
                new Timestamp(job.getUpdatedAt().getTime()).toString());

        if(findJobByServerId((int) job.getId()) == null) {
            mDB = mMyDbHelper.getWritableDatabase();
            long newRowId = mDB.insert(FeedReaderContract.JobEntry.TABLE_NAME, null, values);

            mDB.close();

            return newRowId;
        }else{
            mDB.close();
            return updateJob((int) job.getId(),values);
        }
    }


    // If reason already exists, then update it.
    public long addReason(PredefinedReason reason){

        ContentValues values = new ContentValues();

        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_REASON_ID, reason.getServerId());
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_TYPE, reason.getType());
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_TITLE, reason.getTitle());
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_DESCRIPTION, reason.getDescription());
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_CREATED_AT,
                new Timestamp(reason.getCreatedAt().getTime()).toString());
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_UPDATED_AT,
                new Timestamp(reason.getUpdatedAt().getTime()).toString());

        if(findReasonByServerId((int) reason.getServerId()) == null) {
            mDB = mMyDbHelper.getWritableDatabase();
            long newRowId = mDB.insert(FeedReaderContract.ReasonEntry.TABLE_NAME, null, values);

            mDB.close();

            return newRowId;
        }else{
            mDB.close();
            return updateReason((int) reason.getServerId(),values);
        }
    }

    // TODO: include parameter updated_at, since every time a column changes this one does too.
    public int updateDemandColumn(Demand demand, String column, String value){

        if ( findDemandByServerId(demand.getId()) == null){
            if(addDemand(demand) >= 0) return 1;
            else return 0;
        }else {
            ContentValues values = new ContentValues();
            values.put(column, value);

            String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID + " = ?";
            String[] selectionArgs = {"" + demand.getId()};

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

    public int updateAuthorityColumn(int authId, String column, String value) {

        ContentValues values = new ContentValues();
        values.put(column,value);

        String selection = FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_ID + " = ?";
        String[] selectionArgs = {"" + authId};

        mDB = mMyDbHelper.getReadableDatabase();

        int count = mDB.update(
                FeedReaderContract.AuthorityEntry.TABLE_NAME,
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

    public int updateAuthority(int authId, ContentValues values) {

        String selection = FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_ID + " = ?";
        String[] selectionArgs = {"" + authId};

        mDB = mMyDbHelper.getReadableDatabase();

        int count = mDB.update(
                FeedReaderContract.AuthorityEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        mDB.close();

        return count;
    }

    public int updateJob(int jobId, ContentValues values) {

        String selection = FeedReaderContract.JobEntry.COLUMN_NAME_JOB_ID + " = ?";
        String[] selectionArgs = {"" + jobId};

        mDB = mMyDbHelper.getReadableDatabase();

        int count = mDB.update(
                FeedReaderContract.JobEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        mDB.close();

        return count;
    }

    public int updateReason(int reasonId, ContentValues values) {

        String selection = FeedReaderContract.ReasonEntry.COLUMN_NAME_REASON_ID + " = ?";
        String[] selectionArgs = {"" + reasonId};

        mDB = mMyDbHelper.getReadableDatabase();

        int count = mDB.update(
                FeedReaderContract.ReasonEntry.TABLE_NAME,
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

    public Authority findAuthByServerId(int id){
        List<Authority> auths;
        String selection = FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_ID + " = ? ";
        String [] args = {"" + id};

        auths = searchAuthorities(selection,args);

        if(!auths.isEmpty())
            for (int i = 0; i <= auths.size(); i ++)
                return auths.get(i);

        return null;
    }

    public Job findJobByServerId(int id){
        List<Job> jobs;
        String selection = FeedReaderContract.JobEntry.COLUMN_NAME_JOB_ID + " = ? ";
        String [] args = {"" + id};

        jobs = searchJobs(selection,args);

        if(!jobs.isEmpty())
            for (int i = 0; i <= jobs.size(); i ++)
                return jobs.get(i);

        return null;
    }

    public PredefinedReason findReasonByServerId(int id){
            List<PredefinedReason> reasons;
            String selection = FeedReaderContract.ReasonEntry.COLUMN_NAME_REASON_ID + " = ? ";
            String [] args = {"" + id};

            reasons = searchReasons(selection,args);

            if(!reasons.isEmpty())
                for (int i = 0; i <= reasons.size(); i ++)
                    return reasons.get(i);

            return null;
    }

    public List<Demand> searchDemands(String selection, String[] selectionArgs){

        String[] projection = {
                FeedReaderContract.DemandEntry._ID,
                FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID,
                FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID,
                FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID,
                FeedReaderContract.DemandEntry.COLUMN_NAME_REASON_ID,
                FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT,
                FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION,
                FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS,
                FeedReaderContract.DemandEntry.COLUMN_NAME_PRIOR,
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
            int demandTypeId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_TYPE_ID));
            int reasonId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_REASON_ID));
            String subject = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS));
            String seen = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN));
            String created_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_CREATED_AT));
            String updated_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.DemandEntry.COLUMN_NAME_UPDATED_AT));

            // Search for local instances of users
            User sender = findUserByServerId(senderId);
            User receiver = findUserByServerId(receiverId);
            DemandType type = findDemandTypeByServerId();
            PredefinedReason reason = findReasonByServerId(reasonId);

            if(sender != null && receiver != null){
                Demand demand = new Demand(
                        itemId,
                        demandId,
                        sender,
                        receiver,
                        reason,
                        type,
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
                FeedReaderContract.UserEntry.COLUMN_NAME_USER_JOB,
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
            int jobId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_JOB));
            int superiorId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_STATUS));
            String position = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_JOB_POSITION));
            String fcm = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_FCM));
            String created_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_CREATED_AT));
            String updated_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry.COLUMN_NAME_USER_UPDATED_AT));

            // Search for local instances of job and superior
            Job job = findJobByServerId(jobId);
            User superior;

            // This prevents infinite loop.
            if (userId == superiorId) superior = new User(
                    itemId,
                    userId,
                    email,
                    name,
                    status,
                    position,
                    fcm,
                    null,
                    null,
                    created_at,
                    updated_at
            );
            else superior = findUserByServerId(superiorId);

            User user = new User(
                    itemId,
                    userId,
                    email,
                    name,
                    status,
                    position,
                    fcm,
                    job,
                    superior,
                    created_at,
                    updated_at
            );

            users.add(user);
        }

        cursor.close();

        mDB.close();

        return users;
    }

    public List<Authority> searchAuthorities(String selection, String[] selectionArgs){

        String[] projection = {
                FeedReaderContract.AuthorityEntry._ID,
                FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_ID,
                FeedReaderContract.AuthorityEntry.COLUMN_NAME_USER,
                FeedReaderContract.AuthorityEntry.COLUMN_NAME_SUPERIOR,
                FeedReaderContract.AuthorityEntry.COLUMN_NAME_LEVEL,
                FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_CREATED_AT,
                FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_UPDATED_AT
        };

        String sortOrder =
                FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_UPDATED_AT + " DESC";

        mDB = mMyDbHelper.getReadableDatabase();

        Cursor cursor = mDB.query(
                FeedReaderContract.AuthorityEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<Authority> authorities = new ArrayList<>();

        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(FeedReaderContract.AuthorityEntry._ID));
            int authId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_ID));
            int user = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.AuthorityEntry.COLUMN_NAME_USER));
            int superior = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.AuthorityEntry.COLUMN_NAME_SUPERIOR));
            int level = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.AuthorityEntry.COLUMN_NAME_LEVEL));
            String created_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_CREATED_AT));
            String updated_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_UPDATED_AT));

            Authority authority = new Authority(
                    itemId,
                    authId,
                    user,
                    superior,
                    level,
                    created_at,
                    updated_at
            );

            authorities.add(authority);
        }

        cursor.close();

        mDB.close();

        return authorities;
    }

    public List<Job> searchJobs(String selection, String[] selectionArgs){

        String[] projection = {
                FeedReaderContract.JobEntry._ID,
                FeedReaderContract.JobEntry.COLUMN_NAME_JOB_ID,
                FeedReaderContract.JobEntry.COLUMN_NAME_TITLE,
                FeedReaderContract.JobEntry.COLUMN_NAME_POSITION,
                FeedReaderContract.JobEntry.COLUMN_NAME_JOB_CREATED_AT,
                FeedReaderContract.JobEntry.COLUMN_NAME_JOB_UPDATED_AT
        };

        String sortOrder =
                FeedReaderContract.JobEntry.COLUMN_NAME_JOB_UPDATED_AT + " DESC";

        mDB = mMyDbHelper.getReadableDatabase();

        Cursor cursor = mDB.query(
                FeedReaderContract.JobEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<Job> jobs = new ArrayList<>();

        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(FeedReaderContract.JobEntry._ID));
            int jobId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.JobEntry.COLUMN_NAME_JOB_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.JobEntry.COLUMN_NAME_TITLE));
            String position = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.JobEntry.COLUMN_NAME_POSITION));
            String created_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.JobEntry.COLUMN_NAME_JOB_CREATED_AT));
            String updated_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.JobEntry.COLUMN_NAME_JOB_UPDATED_AT));

            Job job = new Job(
                    itemId,
                    jobId,
                    title,
                    position,
                    created_at,
                    updated_at
            );

            jobs.add(job);
        }

        cursor.close();

        mDB.close();

        return jobs;
    }

    public List<PredefinedReason> searchReasons(String selection, String[] selectionArgs){

        String[] projection = {
                FeedReaderContract.ReasonEntry._ID,
                FeedReaderContract.ReasonEntry.COLUMN_NAME_REASON_ID,
                FeedReaderContract.ReasonEntry.COLUMN_NAME_TYPE,
                FeedReaderContract.ReasonEntry.COLUMN_NAME_TITLE,
                FeedReaderContract.ReasonEntry.COLUMN_NAME_DESCRIPTION,
                FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_CREATED_AT,
                FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_UPDATED_AT
        };

        String sortOrder =
                FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_UPDATED_AT + " DESC";

        mDB = mMyDbHelper.getReadableDatabase();

        Cursor cursor = mDB.query(
                FeedReaderContract.ReasonEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<PredefinedReason> reasons = new ArrayList<>();

        while(cursor.moveToNext()) {
            long localId = cursor.getLong(cursor.getColumnIndexOrThrow(FeedReaderContract.UserEntry._ID));
            int serverId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.ReasonEntry.COLUMN_NAME_REASON_ID));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.ReasonEntry.COLUMN_NAME_TYPE));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.ReasonEntry.COLUMN_NAME_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.ReasonEntry.COLUMN_NAME_DESCRIPTION));
            String created_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_CREATED_AT));
            String updated_at = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_UPDATED_AT));

            PredefinedReason reason = new PredefinedReason(
                    localId,
                    serverId,
                    type,
                    title,
                    description,
                    created_at,
                    updated_at
            );

            reasons.add(reason);
        }

        cursor.close();

        mDB.close();

        return reasons;
    }

    public int deleteDemandById(int id){
        String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_DEMAND_ID + " = ?";
        String[] selectionArgs = {"" + id};
        mDB = mMyDbHelper.getWritableDatabase();
        int count = mDB.delete(FeedReaderContract.DemandEntry.TABLE_NAME, selection, selectionArgs);
        mDB.close();
        return count;
    }

    public int deleteUserById(int id){
        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {"" + id};
        mDB = mMyDbHelper.getWritableDatabase();
        int count = mDB.delete(FeedReaderContract.UserEntry.TABLE_NAME, selection, selectionArgs);
        mDB.close();
        return count;
    }

    public int deleteAuthById(int id){
        String selection = FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_ID + " = ?";
        String[] selectionArgs = {"" + id};
        mDB = mMyDbHelper.getWritableDatabase();
        int count = mDB.delete(FeedReaderContract.UserEntry.TABLE_NAME, selection, selectionArgs);
        mDB.close();
        return count;
    }

    public void deleteAllTables(){
        mDB = mMyDbHelper.getWritableDatabase();
        String dropQuery = "DELETE FROM " + FeedReaderContract.DemandEntry.TABLE_NAME;
        mDB.execSQL(dropQuery);
        dropQuery = "DELETE FROM " + FeedReaderContract.UserEntry.TABLE_NAME;
        mDB.execSQL(dropQuery);
        dropQuery = "DELETE FROM " + FeedReaderContract.ReasonEntry.TABLE_NAME;
        mDB.execSQL(dropQuery);
        dropQuery = "DELETE FROM " + FeedReaderContract.AuthorityEntry.TABLE_NAME;
        mDB.execSQL(dropQuery);
    }
}
