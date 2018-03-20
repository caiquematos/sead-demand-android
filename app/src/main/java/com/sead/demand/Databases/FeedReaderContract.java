package com.sead.demand.Databases;

import android.provider.BaseColumns;

/**
 * Created by caiqu on 29/05/2017.
 */

public class FeedReaderContract {

    private FeedReaderContract(){}

    public static class DemandEntry implements BaseColumns {
        public static final String TABLE_NAME = "demands";
        public static final String COLUMN_NAME_DEMAND_ID = "demand_id"; // Online db id.
        public static final String COLUMN_NAME_SENDER_ID = "sender_id";
        public static final String COLUMN_NAME_RECEIVER_ID = "receiver_id";
        public static final String COLUMN_NAME_REASON_ID = "reason_id";
        public static final String COLUMN_NAME_TYPE_ID = "type_id";
        public static final String COLUMN_NAME_SUBJECT = "subject";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_SEEN = "seen";
        public static final String COLUMN_NAME_ARCHIVE = "archive";
        public static final String COLUMN_NAME_POSTPONED = "postponed";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_a";
    }

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME_USER_ID = "user_id"; // Online db id.
        public static final String COLUMN_NAME_USER_NAME = "name";
        public static final String COLUMN_NAME_USER_EMAIL = "email";
        public static final String COLUMN_NAME_USER_JOB_POSITION = "job_position";
        public static final String COLUMN_NAME_USER_STATUS = "status";
        public static final String COLUMN_NAME_USER_SUPERIOR = "superior";
        public static final String COLUMN_NAME_USER_JOB = "job";
        public static final String COLUMN_NAME_USER_FCM = "fcm";
        public static final String COLUMN_NAME_USER_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_USER_UPDATED_AT = "updated_at";
    }

    public static class ReasonEntry implements BaseColumns {
        public static final String TABLE_NAME = "reasons";
        public static final String COLUMN_NAME_REASON_ID = "reason_id"; // Online db id.
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_USER_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_USER_UPDATED_AT = "updated_at";
    }

    public static class AuthorityEntry implements BaseColumns {
        public static final String TABLE_NAME = "authorities";
        public static final String COLUMN_NAME_AUTH_ID = "auth_id";
        public static final String COLUMN_NAME_USER = "user";
        public static final String COLUMN_NAME_SUPERIOR = "superior";
        public static final String COLUMN_NAME_DEMAND = "demand";
        public static final String COLUMN_NAME_LEVEL = "level";
        public static final String COLUMN_NAME_AUTH_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_AUTH_UPDATED_AT = "updated_at";
    }

    public static class JobEntry implements BaseColumns {
        public static final String TABLE_NAME = "jobs";
        public static final String COLUMN_NAME_JOB_ID = "job_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_JOB_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_JOB_UPDATED_AT = "updated_at";
    }

    public static class DemandTypeEntry implements BaseColumns {
        public static final String TABLE_NAME = "demand_type";
        public static final String COLUMN_NAME_TYPE_ID = "type_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_COMPLEXITY = "complexity";
        public static final String COLUMN_NAME_PRIORITY = "priority";
        public static final String COLUMN_NAME_TYPE_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_TYPE_UPDATED_AT = "updated_at";
    }
}
