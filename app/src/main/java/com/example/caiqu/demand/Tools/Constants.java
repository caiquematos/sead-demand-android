package com.example.caiqu.demand.Tools;

public class Constants {
    // Server Url
    public static final String BASE_URL = "http://192.168.42.107:8000";
    public static final String BASE_URL_2 = "http://192.168.100.12:8000";
    public static final String BASE_URL_4 = "http://192.168.3.181:8000";
    public static final String BASE_URL_ALT_4 = "http://192.168.3.182:8000";
    public static final String BASE_URL_3 = "http://192.168.3.184:8000";
    public static final String BASE_URL_5 = "http://192.168.1.3:8000";
    public static final String BASE_URL_ALT = "http://192.168.42.26:8000";
    public static final String BASE_URL_1 = "http://192.168.42.86:8000";

    // View numbers.
    public static final int RECEIVED_VIEW = 1;
    public static final int SENT_VIEW = 2;
    public static final int ADMIN_VIEW = 3;
    public static final int ACCEPTED_VIEW = 4;
    public static final int POSTPONED_VIEW = 5;
    public static final int CANCELED_VIEW = 6;
    public static final int REJECTED_VIEW = 7;

    // Shared Preferences.
    public static final String IS_LOGGED = "is_logged";
    public static final String LOGGED_USER_EMAIL = "user_email";
    public static final String LOGGED_USER_ID = "user_id";
    public static final String GCM_TOKEN = "gcm_token";

    // Predefined Variables.
    public static String[] JOB_POSITIONS = {"Ponta", "Coordenador", "Diretor", "Secretário"};
    public static int[] POSTPONE_OPTIONS = {1, 2, 3, 4}; // Amount in days.
    public static String[] DEMAND_IMPORTANCE = {"Regular", "Importante", "Urgente"};

    // Demand Status.
    public static final String ACCEPT_STATUS = "A";
    public static final String DONE_STATUS = "D";
    public static final String POSTPONE_STATUS = "P";
    public static final String REJECT_STATUS = "X";
    public static final String CANCEL_STATUS = "C";
    public static final String REOPEN_STATUS = "R";
    public static final String RESEND_STATUS = "S";
    public static final String UNDEFINE_STATUS = "U";

    // Demand Seen.
    public static final String YES_SEEN = "Y";
    public static final String NO_SEEN = "N";

    // Job Service.
    public static final String MARK_AS_READ_JOB_TAG = "mark-as-read-job-tag";
    public static final String INSERT_JOB_TAG = "insert-job-tag";
    public static final String REMOVE_JOB_TAG = "remove-job-tag";
    public static final String UPDATE_JOB_TAG = "update-job-tag";

    // Intent Tags.
    public static final String INTENT_DEMAND = "intent-demand";
    public static final String INTENT_DEMAND_SERVER_ID = "intent-demand-server-id";
    public static final String INTENT_DEMAND_STATUS = "intent-demand-status";
    public static final String INTENT_ACTIVITY = "intent-activity";
    public static final String INTENT_PAGE = "intent-page";
    public static final String INTENT_USER_TYPE = "intent-user-type";
    public static final String INTENT_ADMIN_TYPE = "intent-admin-type";
    public static final String INTENT_STORAGE_TYPE = "intent_storage_type";

    // Type of FCM messages.
	public static final String INSERT_DEMAND_ADMIN = "add_demand_admin";
	public static final String INSERT_DEMAND_RECEIVED = "add_demand_received";
	public static final String INSERT_DEMAND_SENT = "add_demand_sent";
	public static final String UPDATE_DEMAND = "update_demand";
	public static final String UPDATE_STATUS = "update_status";
	public static final String UPDATE_IMPORTANCE = "update_importance";
	public static final String UPDATE_READ = "update_read";

    // Broadcast parameters.
    public static final String BROADCAST_RECEIVER_FRAG = "broadcast_receiver";
    public static final String BROADCAST_SENT_FRAG = "broadcast_sent";
    public static final String BROADCAST_ADMIN_FRAG = "broadcast_admin";
    public static final String BROADCAST_STATUS_ACT = "broadcast_status";

}