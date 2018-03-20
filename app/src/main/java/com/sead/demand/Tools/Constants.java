package com.sead.demand.Tools;

public class Constants {
    // Server Url
    public static final String BASE_URL = "http://sead2.univasf.edu.br/demanda";
    public static final String BASE_URL_12 = "http://192.168.42.201:8000";
    public static final String BASE_URL_11 = "http://10.87.139.253:8000";
    public static final String BASE_URL_10 = "http://192.168.3.183:8000";
    public static final String BASE_URL_9 = "http://192.168.42.197:8000";
    public static final String BASE_URL_8 = "http://192.168.42.107:8000";
    public static final String BASE_URL_4 = "http://192.168.0.110:8000";
    public static final String BASE_URL_2 = "http://192.168.100.12:8000";
    public static final String BASE_URL_7 = "http://192.168.3.181:8000";
    public static final String BASE_URL_6 = "http://192.168.3.182:8000";
    public static final String BASE_URL_3 = "http://192.168.3.184:8000";
    public static final String BASE_URL_5 = "http://192.168.1.3:8000";
    public static final String BASE_URL_1 = "http://192.168.42.86:8000";

    // User's types.
    public static final String INTERNAL_USER = "int";
    public static final String EXTERNAL_USER = "ext";
    public static final String UNIVASF_USER = "uni";

    // Menu references.
    public static final int SHOW_TRIO_MENU = 1; // Accept, Postpone, Reject.
    public static final int SHOW_NO_MENU = 2;
    public static final int SHOW_REOPEN_MENU = 3;
    public static final int SHOW_CANCEL_MENU = 4;
    public static final int SHOW_RESEND_MENU = 5;
    public static final int SHOW_DONE_MENU = 6;

    // Menu references (new).
    public static final int NO_MENU = 0;
    public static final int RECEIVER_MENU = 1; // 1-pode atender?, 2-concluir, 3-solicitar prazo, 4-repassar por competência.
    public static final int SUPERIOR_MENU = 2; // 1-repassar, 2-indeferir.
    public static final int SENDER_MENU = 3; // 1-finalizar, 2-reabrir.

    // Page references.
    public static final int CREATE_PAGE = 0;
    public static final int RECEIVED_PAGE = 1;
    public static final int SENT_PAGE = 2;
    public static final int ADMIN_PAGE = 3;
    public static final int STATUS_PAGE = 4;
    public static final int ARCHIVE_PAGE = 5;
    public static final int SEARCH_PAGE = 6;

    // Shared Preferences.
    public static final String USER_PREFERENCES = "user_preferences";
    public static final String JOB_PREFERENCES = "job_preferences";
    public static final String SUPERIOR_PREFERENCES = "superior_preferences";
    public static final String IS_LOGGED = "is_logged";
    public static final String LOGGED_USER_EMAIL = "user_email";
    public static final String LOGGED_USER_ID = "user_id";
    public static final String LOGGED_USER_JOB_POSITION = "user_job_position";
    public static final String GCM_TOKEN = "gcm_token";

    // Predefined Variables.
    public static String[] JOB_POSITIONS = {"Ponta", "Coordenador", "Diretor", "Secretário"};
    public static int[] POSTPONE_OPTIONS = {1, 3, 5, 7}; // Amount in days.
    public static int[] DUE_TIME = {3, 8, 15, 20}; // According to Demand Prior. 3, 8, 15, >= 15 (inclusive).
    public static String[] DEMAND_PRIOR_NAME = {
            "Muito Alta",
            "Alta",
            "Média",
            "Baixa"
    };

    // Demand Prior.
    public static final String VERY_HIGH_PRIOR_TAG = "very_high_level";
    public static final String HIGH_PRIOR_TAG = "high_level";
    public static final String MEDIUM_PRIOR_TAG = "medium_level";
    public static final String LOW_PRIOR_TAG = "low_level";

    // Demand Status.
    public static final String ACCEPT_STATUS = "A"; // Pode atender a demanda.
    public static final String DONE_STATUS = "D"; // Demanda concluída pelo solicitado.
    public static final String REJECT_STATUS = "X"; // Indeferida pelo superior.
    public static final String CANCEL_STATUS = "C"; // Cancelada pelo solicitante.
    public static final String REOPEN_STATUS = "R"; // Reaberta pelo solicitante (caso especial).
    public static final String UNDEFINE_STATUS = "U"; // Indefinido (default).
    public static final String LATE_STATUS = "L"; // Demanda atrasada (automaticamente).
    public static final String DELETE_STATUS = "E";
    public static final String FINISH_STATUS = "F"; // Demanda finalizada pelo solicitante.
    public static final String TRANSFER_STATUS = "T"; // Demanda Repassada.

    public static final String DEADLINE_REQUESTED_STATUS = "Y"; // Solicitado aumento de prazo.
    public static final String DEADLINE_ACCEPTED_STATUS = "Z"; // Solicitado aumento de prazo.
    public static final String POSTPONE_STATUS = "P"; // Me lembre depois (não muda o estado).

    public static final String RESEND_STATUS = "S"; // Eliminar essa opção.

    // Demand Seen.
    public static final String YES = "Y";
    public static final String NO = "N";

    // Job Service.
    public static final String JOB_TYPE_KEY = "job-type-key";
    public static final String MARK_AS_READ_JOB_TAG = "mark-as-read-job-tag";
    public static final String INSERT_JOB_TAG = "insert-job-tag";
    public static final String REMOVE_JOB_TAG = "remove-job-tag";
    public static final String UPDATE_JOB_TAG = "update-job-tag";

    // Alarm Manager.
    public static final String ALARM_TYPE_KEY = "alarm-type-key";
    public static final int POSTPONE_ALARM_TAG = 999;
    public static final int REMIND_ME_ALARM_TAG = 998;
    public static final int WARN_DUE_TIME_ALARM_TAG = 997;
    public static final int DUE_TIME_ALARM_TAG = 996;
    public static final int DUE_TIME_PREVIOUS_WARNING = 1; // Warn user 1 day before due time.

    // Intent Tags.
    public static final String INTENT_DEMAND = "intent-demand";
    public static final String INTENT_USER = "intent-user";
    public static final String INTENT_DEMAND_SERVER_ID = "intent-demand-server-id";
    public static final String INTENT_DEMAND_STATUS = "intent-demand-status";
    public static final String INTENT_ACTIVITY = "intent-activity";
    public static final String INTENT_PAGE = "intent-page";
    public static final String INTENT_MENU = "intent-menu";
    public static final String INTENT_USER_TYPE = "intent-user-type";
    public static final String INTENT_ADMIN_TYPE = "intent-admin-type";
    public static final String INTENT_STORAGE_TYPE = "intent_storage_type";
    public static final String INTENT_REJECT_PREDEFINED_REASON = "intent_reject_predefined_reason";
    public static final String INTENT_REJECT_REASON_INDEX = "intent_reject_reason_index";
    public static final String INTENT_REJECT_REASON_COMMENT = "intent_reject_reason_comment";

    // Type of FCM messages.
	public static final String INSERT_DEMAND_ADMIN = "add_demand_admin";
	public static final String INSERT_DEMAND_RECEIVED = "add_demand_receiver";
	public static final String INSERT_DEMAND_SENT = "add_demand_sent";
	public static final String UPDATE_DEMAND = "update_demand";
	public static final String UPDATE_STATUS = "update_status";
	public static final String UPDATE_PRIOR = "update_prior";
	public static final String UPDATE_READ = "update_read";
	public static final String UNLOCK_USER = "unlock-user-request";
	public static final String AUTH = "AUTH";
	public static final String ACKNOWLEDGE = "acknowledge";

    // Broadcast parameters.
    public static final String BROADCAST_RECEIVER_FRAG = "broadcast_receiver";
    public static final String BROADCAST_SENT_FRAG = "broadcast_sent";
    public static final String BROADCAST_ADMIN_FRAG = "broadcast_admin";
    public static final String BROADCAST_STATUS_ACT = "broadcast_status";
    public static final String BROADCAST_REQUEST_ACT = "broadcast_request";

    // Activity for result View Demand.
    public static final int REJECT_DEMAND = 1;
    public static final int NOT_ACCEPT_DEMAND = 2;
    public static final int DEADLINE_DEMAND = 3;

    // Predefined Reasons.
    public static final int REASON_OTHER_POSITION = 0;
    public static final String REASON_OTHER_TYPE = "O";
    public static final String REASON_PREDEFINED_TYPE = "P";

}