package com.yahoo.inmind.commons.control;

import java.util.HashMap;

/**
 * Created by oscarr on 12/18/14.
 */
public final class Constants {
    /** Messages **/
    public static final int MSG_LAUNCH_BASE_NEWS_ACTIVITY = 1;
    public static final int MSG_LAUNCH_EXT_NEWS_ACTIVITY = 2;
    public static final int MSG_REQUEST_NEWS_ITEMS = 3;
    public static final int MSG_SHOW_MODIFIED_NEWS = 4;
    public static final int MSG_LOGIN_NEWS = 5;
    public static final int MSG_GET_USER_PROFILE = 6;
    public static final int MSG_UPDATE_NEWS = 7;
    public static final int MSG_GET_NEWS_ITEMS = 8;
    public static final int MSG_APPLY_FILTERS = 9;
    public static final int MSG_SHOW_NEWS_ARTICLE = 10;
    public static final int MSG_EXPAND_NEWS_ARTICLE = 11;
    public static final int MSG_LAUNCH_ACTIVITY = 12;
    public static final int MSG_GET_ARTICLE_POSITION = 13;
    public static final int MSG_SHOW_CURRENT_NEWS_ARTICLE = 14;
    public static final int MSG_SHOW_NEXT_NEWS_ARTICLE = 15;
    public static final int MSG_SHOW_PREVIOUS_NEWS_ARTICLE = 16;
    public static final int MSG_START_AUDIO_RECORD = 17;
    public static final int MSG_STOP_AUDIO_RECORD = 18;
    public static final int MSG_UPLOAD_TO_SERVER = 19;
    public static final int MSG_FILTER_NEWS_BY_EMAIL = 20;
    public static final int MSG_GET_MODEL_DISTRIBUTIONS = 21;
    public static final int MSG_PROCESS_EVENTS_CALENDAR = 22;
    public static final int MSG_CHOOSE_ACCOUNT = 23;
    public static final int MSG_DEVICE_ONLINE = 24;
    public static final int MSG_GET_SELECTED_ACCOUNT_NAME = 25;
    public static final int MSG_GET_AWARE_SETTINGS = 26;
    public static final int MSG_START_AWARE_PLUGIN = 27;
    public static final int MSG_STOP_AWARE_PLUGIN = 28;
    public static final int MSG_WEATHER_CHANGE_FORECAST_MODE = 30;
    public static final int MSG_SET_LOCATION = 33;
    public static final int MSG_WEATHER_SUBSCRIBE_TO_FORECAST = 34;
    public static final int MSG_SEARCH_HOTEL = 35;
    public static final int MSG_STOP_SENSORS = 36;
    public static final int MSG_STOP_PLUGINS = 37;
    public static final int MSG_STATUS_BATTERY = 38;
    public static final int MSG_CONFIG_ACCOUNT_NAME = 39;
    public static final int MSG_GOOGLE_PLAY_AVAILABLE = 40;
    public static final int MSG_GOOGLE_PLAY_AVAILABLE_ERROR = 41;
    public static final int MSG_GET_CALENDAR_EVENTS = 42;
    public static final int MSG_GET_AR_NAME = 43;
    public static final int MSG_SENSOR_SETTINGS = 44;
    public static final int MSG_WEATHER_GET_REPORT = 46;
    public static final int MSG_WEATHER_UNSUBSCRIBE_TO_FORECAST = 47;
    public static final int MSG_GET_LOCATION = 48;
    public static final int MSG_RESET_CURRENT_LOCATION = 49;
    public static final int MSG_SET_LOCATION_SETTINGS = 50;
    public static final int MSG_GET_HISTORY_LOCATIONS = 51;
    public static final int MSG_RESET_HISTORY_LOCATIONS = 52;
    public static final int MSG_SET_STREAMING_SETTINGS = 53;
    public static final int MSG_GET_CAMERA_STREAMING = 54;
    public static final int MSG_STREAMING_UNSUBSCRIBE = 55;
    public static final int MSG_STREAMING_SUBSCRIBE = 56;
    public static final int MSG_STREAMING_START_PREVIEW = 57;
    public static final int MSG_STREAMING_STOP_STREAM = 58;
    public static final int MSG_CREATE_DECISION_RULE = 59;
    public static final int MSG_REMOVE_DECISION_RULE = 60;
    public static final int MSG_ALARM_PLAY_RINGTONE = 61;

    public static HashMap<Integer, String> IDS = new HashMap<>();
    static{
        IDS.put( MSG_LAUNCH_BASE_NEWS_ACTIVITY, "MSG_LAUNCH_BASE_NEWS_ACTIVITY");
        IDS.put( MSG_LAUNCH_EXT_NEWS_ACTIVITY, "MSG_LAUNCH_EXT_NEWS_ACTIVITY");
        IDS.put( MSG_REQUEST_NEWS_ITEMS, "MSG_REQUEST_NEWS_ITEMS");
        IDS.put( MSG_SHOW_MODIFIED_NEWS, "MSG_SHOW_MODIFIED_NEWS");
        IDS.put( MSG_LOGIN_NEWS, "MSG_LOGIN_NEWS");
        IDS.put( MSG_GET_USER_PROFILE, "MSG_GET_USER_PROFILE");
        IDS.put( MSG_UPDATE_NEWS, "MSG_UPDATE_NEWS");
        IDS.put( MSG_GET_NEWS_ITEMS, "MSG_GET_NEWS_ITEMS");
        IDS.put( MSG_APPLY_FILTERS, "MSG_APPLY_FILTERS");
        IDS.put( MSG_SHOW_NEWS_ARTICLE, "MSG_SHOW_NEWS_ARTICLE");
        IDS.put( MSG_EXPAND_NEWS_ARTICLE, "MSG_EXPAND_NEWS_ARTICLE");
        IDS.put( MSG_LAUNCH_ACTIVITY, "MSG_LAUNCH_ACTIVITY");
        IDS.put( MSG_GET_ARTICLE_POSITION, "MSG_GET_ARTICLE_POSITION");
        IDS.put( MSG_SHOW_CURRENT_NEWS_ARTICLE, "MSG_SHOW_CURRENT_NEWS_ARTICLE");
        IDS.put( MSG_SHOW_NEXT_NEWS_ARTICLE, "MSG_SHOW_NEXT_NEWS_ARTICLE");
        IDS.put( MSG_SHOW_PREVIOUS_NEWS_ARTICLE, "MSG_SHOW_PREVIOUS_NEWS_ARTICLE");
        IDS.put( MSG_START_AUDIO_RECORD, "MSG_START_AUDIO_RECORD");
        IDS.put( MSG_STOP_AUDIO_RECORD, "MSG_STOP_AUDIO_RECORD");
        IDS.put( MSG_UPLOAD_TO_SERVER, "MSG_UPLOAD_TO_SERVER");
        IDS.put( MSG_FILTER_NEWS_BY_EMAIL, "MSG_FILTER_NEWS_BY_EMAIL");
        IDS.put( MSG_GET_MODEL_DISTRIBUTIONS, "MSG_GET_MODEL_DISTRIBUTIONS");
        IDS.put( MSG_PROCESS_EVENTS_CALENDAR, "MSG_PROCESS_EVENTS_CALENDAR");
        IDS.put( MSG_CHOOSE_ACCOUNT, "MSG_CHOOSE_ACCOUNT");
        IDS.put( MSG_DEVICE_ONLINE, "MSG_DEVICE_ONLINE");
        IDS.put( MSG_GET_SELECTED_ACCOUNT_NAME, "MSG_GET_SELECTED_ACCOUNT_NAME");
        IDS.put( MSG_GET_AWARE_SETTINGS, "MSG_GET_SELECTED_ACCOUNT_NAME");
        IDS.put( MSG_START_AWARE_PLUGIN, "MSG_START_AWARE_PLUGIN");
        IDS.put( MSG_STOP_AWARE_PLUGIN, "MSG_STOP_AWARE_PLUGIN");
        IDS.put( MSG_WEATHER_CHANGE_FORECAST_MODE, "MSG_WEATHER_CHANGE_FORECAST_MODE");
        IDS.put( MSG_SET_LOCATION, "MSG_SET_LOCATION");
        IDS.put( MSG_WEATHER_SUBSCRIBE_TO_FORECAST, "MSG_WEATHER_SUBSCRIBE_TO_FORECAST");
        IDS.put( MSG_SEARCH_HOTEL, "MSG_SEARCH_HOTEL");
        IDS.put( MSG_STOP_SENSORS, "MSG_STOP_SENSORS");
        IDS.put( MSG_STOP_PLUGINS, "MSG_STOP_PLUGINS");
        IDS.put( MSG_STATUS_BATTERY, "MSG_STATUS_BATTERY");
        IDS.put( MSG_CONFIG_ACCOUNT_NAME, "MSG_CONFIG_ACCOUNT_NAME");
        IDS.put( MSG_GOOGLE_PLAY_AVAILABLE, "MSG_GOOGLE_PLAY_AVAILABLE");
        IDS.put( MSG_GOOGLE_PLAY_AVAILABLE_ERROR, "MSG_GOOGLE_PLAY_AVAILABLE_ERROR");
        IDS.put( MSG_GET_CALENDAR_EVENTS, "MSG_GET_CALENDAR_EVENTS");
        IDS.put( MSG_GET_AR_NAME, "MSG_GET_AR_NAME");
        IDS.put( MSG_SENSOR_SETTINGS, "MSG_SENSOR_SETTINGS");
        IDS.put( MSG_WEATHER_GET_REPORT, "MSG_WEATHER_GET_REPORT");
        IDS.put( MSG_WEATHER_UNSUBSCRIBE_TO_FORECAST, "MSG_WEATHER_UNSUBSCRIBE_TO_FORECAST");
        IDS.put( MSG_GET_LOCATION, "MSG_GET_LOCATION");
        IDS.put( MSG_RESET_CURRENT_LOCATION, "MSG_RESET_CURRENT_LOCATION");
        IDS.put( MSG_SET_LOCATION_SETTINGS, "MSG_SET_LOCATION_SETTINGS");
        IDS.put( MSG_GET_HISTORY_LOCATIONS, "MSG_GET_HISTORY_LOCATIONS");
        IDS.put( MSG_RESET_HISTORY_LOCATIONS, "MSG_RESET_HISTORY_LOCATIONS");
        IDS.put( MSG_SET_STREAMING_SETTINGS, "MSG_SET_STREAMING_SETTINGS");
        IDS.put( MSG_GET_CAMERA_STREAMING, "MSG_GET_CAMERA_STREAMING");
        IDS.put( MSG_STREAMING_UNSUBSCRIBE, "MSG_STREAMING_UNSUBSCRIBE");
        IDS.put( MSG_STREAMING_SUBSCRIBE, "MSG_STREAMING_SUBSCRIBE");
        IDS.put( MSG_STREAMING_START_PREVIEW, "MSG_STREAMING_START_PREVIEW");
        IDS.put( MSG_STREAMING_STOP_STREAM, "MSG_STREAMING_STOP_STREAM");
        IDS.put( MSG_CREATE_DECISION_RULE, "MSG_CREATE_DECISION_RULE");
        IDS.put( MSG_REMOVE_DECISION_RULE, "MSG_REMOVE_DECISION_RULE");
        IDS.put( MSG_ALARM_PLAY_RINGTONE, "MSG_ALARM_PLAY_RINGTONE");
    }

    public static String getID(int id){
        String idS = IDS.get(id);
        return idS == null? ""+id : idS;
    }


    /** Bundle fields **/
    public static final String BUNDLE_ACTIVITY_NAME = "BUNDLE_ACTIVITY_NAME";
    public static final String BUNDLE_MODIFIED_NEWS = "BUNDLE_MODIFIED_NEWS";
    public static final String BUNDLE_FILTERS = "BUNDLE_FILTERS";
    public static final String BUNDLE_ARTICLE_ID = "BUNDLE_ARTICLE_ID";
    public static final String BUNDLE_TYPE_FILTER = "BUNDLE_TYPE_FILTER";
    public static final String BUNDLE_DRAWER_MANAGER = "BUNDLE_DRAWER_MANAGER";
    public static final String BUNDLE_CLEAR_FILTERS = "BUNDLE_CLEAR_FILTERS";
    public static final String BUNDLE_MESSAGE_TYPE = "BUNDLE_MESSAGE_TYPE";
    public static final String BUNDLE_MAIN_LAYOUT_ID = "BUNDLE_MAIN_LAYOUT_ID";
    public static final String BUNDLE_RESET_SAVED_INSTANCE = "BUNDLE_RESET_SAVED_INSTANCE";


    /** qualifiers **/
    public static final String QUALIFIER_NEWS = "QUALIFIER_NEWS";

    /** Flags **/
    public static final String FLAG_FORCE_RELOAD = "FORCE_RELOAD";
    public static final String FLAG_RETURN_JSON = "FLAG_RETURN_JSON";
    public static final String FLAG_UPDATE_NEWS = "FLAG_UPDATE_NEWS";
    public static final String FLAG_SEND_EVENT = "FLAG_SEND_EVENT";
    public static final String FLAG_REFRESH = "FLAG_REFRESH";


    /** layouts **/
    public static final String UI_PORTRAIT_LAYOUT = "UI_PORTRAIT_LAYOUT";
    public static final String UI_LANDSCAPE_LAYOUT = "UI_LANDSCAPE_LAYOUT";
    public static final String UI_NEWS_RANK = "UI_NEWS_RANK";
    public static final String UI_NEWS_TITLE = "UI_NEWS_TITLE";
    public static final String UI_NEWS_SCORE = "UI_NEWS_SCORE";
    public static final String UI_NEWS_SUMMARY = "UI_NEWS_SUMMARY";
    public static final String UI_NEWS_FEAT = "UI_NEWS_FEAT";
    public static final String UI_NEWS_FEAT2 = "UI_NEWS_FEAT2";
    public static final String UI_NEWS_PUBLISHER = "UI_NEWS_PUBLISHER";
    public static final String UI_NEWS_REASON = "UI_NEWS_REASON";
    public static final String UI_NEWS_IMG = "UI_NEWS_IMG";
    public static final String UI_NEWS_SHARE_FB = "UI_NEWS_SHARE_FB";
    public static final String UI_NEWS_SHARE_TWITTER = "UI_NEWS_SHARE_TWITTER";
    public static final String UI_NEWS_SHARE_TMBLR = "UI_NEWS_SHARE_TMBLR";
    public static final String UI_NEWS_SHARE_MORE = "UI_NEWS_SHARE_MORE";
    public static final String UI_NEWS_LIKE = "UI_NEWS_LIKE";
    public static final String UI_NEWS_DISLIKE = "UI_NEWS_DISLIKE";
    public static final String UI_NEWS_COMMENTS = "UI_NEWS_COMMENTS";


    /** news reader **/
    //Constants of JSON paths for properties of user profile
    public static final String JSON_YAHOO_USER_PROFILE_PATH = "yahoo-coke:*/yahoo-coke:debug-scoring/feature-response/result";
    public static final String JSON_USER_GENDER = "JSON_USER_GENDER";
    public static final String JSON_USER_AGE = "JSON_USER_AGE";
    public static final String JSON_POSITIVE_DEC_WIKIID = "POSITIVE_DEC WIKIID";
    public static final String JSON_POSITIVE_DEC_YCT = "POSITIVE_DEC YCT";
    public static final String JSON_NEGATIVE_DEC_WIKIID = "NEGATIVE_DEC WIKIID";
    public static final String JSON_NEGATIVE_DEC_YCT = "NEGATIVE_DEC YCT";
    public static final String JSON_FB_WIKIID = "FB WIKIID";
    public static final String JSON_FB_YCT = "FB YCT";
    public static final String JSON_CAP_ENTITY_WIKI = "JSON_CAP_ENTITY_WIKI";
    public static final String JSON_CAP_YCT_ID = "JSON_CAP_YCT_ID";
    public static final String JSON_NEGATIVE_INF_WIKIID = "NEGATIVE_INF WIKIID";
    public static final String JSON_NEGATIVE_INF_YCT = "NEGATIVE_INF YCT";
    public static final String JSON_USER_PROPUSAGE = "JSON_USER_PROPUSAGE";
    //Constants of JSON paths for properties of news content
    public static final String JSON_YAHOO_COKE_STREAM_ELEMENTS = "yahoo-coke:stream/elements";
    public static final String JSON_SS_FAKE_USER_PROFILE_PARAM_NAME = "profile";


    /** news item attributes **/
    public static final String ARTICLE_INDEX = "index";
    public static final String ARTICLE_TITLE = "title";
    public static final String ARTICLE_UUID = "uuid";
    public static final String ARTICLE_REASON = "explain/reason";
    public static final String ARTICLE_URL = "snippet/url";
    public static final String ARTICLE_CATEGORIES = "snippet/categories";
    public static final String ARTICLE_SCORE = "score";
    public static final String ARTICLE_CAP_FEATURES = "cap_features";
    public static final String ARTICLE_RAW_SCORE_MAP = "raw_score_map";
    public static final String ARTICLE_PUBLISHER = "publisher";
    public static final String ARTICLE_IMAGE_URL = "snippet/image/original/url";
    public static final String ARTICLE_SUMMARY = "snippet/summary";
    public static final String ARTICLE_PREVIOUS_POSITION = "ARTICLE_PREVIOUS_POSITION";
    public static final String ARTICLE_NEXT_POSITION = "ARTICLE_NEXT_POSITION";
    public static final String NEWS = "NEWS";

    /** rule validation operators **/
    public static final String OPERATOR_HIGHER_THAN = "OPERATOR_HIGHER_THAN";
    public static final String OPERATOR_EQUALS_TO = "OPERATOR_EQUALS_TO";
    public static final String OPERATOR_LOWER_THAN = "OPERATOR_LOWER_THAN";
    public static final String OPERATOR_CONTAINS_STRING = "OPERATOR_CONTAINS_STRING";
    public static final String OPERATOR_DATE_BEFORE = "OPERATOR_DATE_BEFORE";
    public static final String OPERATOR_DATE_AFTER = "OPERATOR_DATE_AFTER";
    public static final String OPERATOR_DATE_EQUAL = "OPERATOR_DATE_EQUAL";
    public static final String OPERATOR_TIME_EQUAL = "OPERATOR_TIME_EQUAL";
    public static final String OPERATOR_TIME_BEFORE = "OPERATOR_TIME_BEFORE";
    public static final String OPERATOR_TIME_AFTER = "OPERATOR_TIME_AFTER";

    /** Contents **/
    public static final String CONTENT_NEWS_LIST = "CONTENT_NEWS_LIST";
    public static final String CONTENT = "CONTENT";

    /** Set constant values **/
    public static final int SET_NEWS_LIST_SIZE = 0;
    public static final int SET_REFRESH_TIME = 1;
    public static final int SET_UPDATE_TIME = 2;
    public static final String SET_AUDIO_SAMPLE_RATE = "SET_AUDIO_SAMPLE_RATE";
    public static final String SET_AUDIO_CHANNEL_CONFIG = "SET_AUDIO_CHANNEL_CONFIG";
    public static final String SET_AUDIO_ENCODING = "SET_AUDIO_ENCODING";
    public static final String SET_SUBSCRIBER = "SET_SUBSCRIBER";
    public static final String SET_AUDIO_BUFFER_ELEMENTS_TO_REC = "SET_AUDIO_BUFFER_ELEMENTS_TO_REC";
    public static final String SET_AUDIO_BYTES_PER_ELEMENT = "SET_AUDIO_BYTES_PER_ELEMENT";
    public static final String SET_VIDEO_QUALITY = "SET_VIDEO_QUALITY";



    /** Configuration variables **/
    public static final String CONFIG_PERSONALIZATION_MULTIBANDIT_LEARNING = "CONFIG_PERSONALIZATION_MULTIBANDIT_LEARNING";
    public static final String CONFIG_PERSONALIZATION_LEARNING_FROM_ADVISE = "CONFIG_PERSONALIZATION_LEARNING_FROM_ADVISE";
    public static final String CONFIG_FEEDBACK_MULTIBANDIT_LEARNING = "CONFIG_FEEDBACK_MULTIBANDIT_LEARNING";
    public static final String CONFIG_FEEDBACK_LEARNING_FROM_ADVISE = "CONFIG_FEEDBACK_LEARNING_FROM_ADVISE";
    public static final String CONFIG_NEWS_FILTERED_BY_EMAIL = "CONFIG_NEWS_FILTERED_BY_EMAIL";
    public static final String CONFIG_NEWS_PROPERTIES = "news_config.properties";
    public static final String CONFIG_ID_PERSONALIZATION = "CONFIG_ID_PERSONALIZATION";
    public static final String CONFIG_ID_FEEDBACK = "CONFIG_ID_FEEDBACK";
    public static final String CONFIG_NEWS_RANKING_OPTION = "CONFIG_NEWS_RANKING_OPTION";
    public static final String CONFIG_NEWS_MODELS_DISTRIBUTION = "CONFIG_NEWS_MODELS_DISTRIBUTION";


    /** HTTP **/
    public static final String HTTP_REQUEST_SERVER_URL = "HTTP_REQUEST_SERVER_URL";
    public static final String HTTP_REQUEST_BODY = "HTTP_REQUEST_BODY";
    public static final String HTTP_MIME_TYPE = "HTTP_MIME_TYPE";
    public static final String HTTP_RESOURCE_NAME = "HTTP_RESOURCE_NAME";
    public static final String HTTP_FILE_EXTENSION = "HTTP_FILE_EXTENSION";
    public static final String IMG_YUV_FORMAT = "IMG_YUV_FORMAT";
    public static final String IMG_QUALITY = "IMG_QUALITY";
    public static final String IMG_COMPRESS_FORMAT = "IMG_COMPRESS_FORMAT";
    public static final String IMG_HEIGHT = "IMG_HEIGHT";
    public static final String IMG_WITH = "IMG_WITH";


    /** GOOGLE SERVICES **/
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;


    /** CALENDAR CONSTANTS **/
    public static final String CALENDAR_AFTER_CREATE_EVENT = "CALENDAR_AFTER_CREATE_EVENT";
    public static final String CALENDAR_AFTER_UPDATE_EVENT = "CALENDAR_AFTER_UPDATE_EVENT";
    public static final String CALENDAR_AFTER_DELETE_EVENTS = "CALENDAR_AFTER_DELETE_EVENTS";
    public static final String CALENDAR_AFTER_DELETE_ALL_EVENTS = "CALENDAR_AFTER_DELETE_ALL_EVENTS";
    public static final String CALENDAR_AFTER_QUERY_EVENTS = "CALENDAR_AFTER_QUERY_EVENTS";
    public static final String CALENDAR_AVAILABILITY_EXCEPTION = "CALENDAR_AVAILABILITY_EXCEPTION";
    public static final String CALENDAR_USER_RECOVERABLE_EXCEPTION = "CALENDAR_USER_RECOVERABLE_EXCEPTION";
    public static final String CALENDAR_MODE = "CALENDAR_MODE";
    public static final String CALENDAR_EVENT_DATA = "CALENDAR_EVENT_DATA";
    public static final String CALENDAR_CHECK_REFRESH_CALENDAR = "CALENDAR_CHECK_REFRESH_CALENDAR";
    public static final String CALENDAR_FROM_DATE = "CALENDAR_FROM_DATE";
    public static final String CALENDAR_NUMBER_OF_MONTHS = "CALENDAR_NUMBER_OF_MONTHS";
    public static final String CALENDAR_START_DATE = "CALENDAR_START_DATE";
    public static final String CALENDAR_END_DATE = "CALENDAR_END_DATE";
    public static final String CALENDAR_EVENT_NOW = "CALENDAR_EVENT_NOW";
    public static final String CALENDAR_TODAY = "CALENDAR_TODAY";
    public static final String CALENDAR_TOMORROW = "CALENDAR_TOMORROW";
    public static final String CALENDAR_START_TIME = "CALENDAR_START_TIME";
    public static final String CALENDAR_END_TIME = "CALENDAR_END_TIME";
    public static final String CALENDAR = "CALENDAR";
    public static final int CALENDAR_GET_EVENTS = 0;
    public static final int CALENDAR_INSERT_EVENT = 1;
    public static final int CALENDAR_DELETE_EVENTS = 2;
    public static final int CALENDAR_UPDATE_EVENT = 3;
    public static final int CALENDAR_DELETE_ALL_EVENTS = 4;



    /** SERVICES **/
    public static final String SERVICE_SETTINGS = "SERVICE_SETTINGS";
    public static final String SERVICE_NAME = "SERVICE_NAME";
    public static final String ACCOUNT_NAME = "ACCOUNT_NAME";
    public static final String SERVICE_LOCATION = "Location";
    public static final String SERVICE_AR = "SERVICE_AR";
    public static final String AWARE_SENSOR_SETTING = "AWARE_SENSOR_SETTING";

    /** SENSORS **/
    public static final String SENSOR_SETTING = "SENSOR_SETTING";
    public static final String SENSOR_NAME = "SENSOR_NAME";
    public static final String SENSOR_STOP = "SENSOR_STOP";
    public static final String SENSOR_START = "SENSOR_START";
    public static final String SENSOR_ACCELEROMETER = "SENSOR_ACCELEROMETER";



    /** LOCATION **/
    public static final String LOCATION_LONGITUDE = "LOCATION_LONGITUDE";
    public static final String LOCATION_LATITUDE = "LOCATION_LATITUDE";
    public static final String LOCATION_COUNTRY = "LOCATION_COUNTRY";
    public static final String LOCATION_CITY = "LOCATION_CITY";
    public static final String LOCATION_COUNTRY_CODE = "LOCATION_COUNTRY_CODE";
    public static final String LOCATION_STATE_CODE = "LOCATION_STATE_CODE";
    public static final String LOCATION_SUB_AREA = "LOCATION_SUB_AREA";
    public static final String LOCATION_CURRENT_PLACE = "LOCATION_CURRENT_PLACE";
    public static final String LOCATION_WRITE_READ_DATA_FROM_FILE = "LOCATION_WRITE_READ_DATA_FROM_FILE";
    public static final String LOCATION_PLACE_NAME = "LOCATION_PLACE_NAME";
    public static final String LOCATION_HISTORY = "LOCATION_HISTORY";
    public static final String LOCATION_MAX_HISTORY = "LOCATION_MAX_HISTORY";


    /** HOTEL **/
    public static final String HOTEL_URL = "HOTEL_URL";
    public static final String HOTEL_CRITERIA = "HOTEL_CRITERIA";


    /** GOOGLEAPIS **/
    public static final String GOOGLE_CONNECTION_STATUS = "GOOGLE_CONNECTION_STATUS";

    /** COMMON **/
    public static final String APP_CONTEXT = "APP_CONTEXT";
    public static final String RESULTS_LOGIN = "RESULTS_LOGIN";

    /** TOAST **/
    public static final String TOAST = "TOAST";
    public static final String TOAST_MESSAGE = "TOAST_MESSAGE";

    /** WEATHER **/
    public static final String WEATHER_MODE = "WEATHER_MODE";
    public static final String WEATHER_PLACE = "WEATHER_PLACE";
    public static final String WEATHER_FORCE_REFRESH = "WEATHER_FORCE_REFRESH";
    public static final String WEATHER_REFRESH_TIME = "WEATHER_REFRESH_TIME";
    public static final String WEATHER_CONDITION = "WEATHER_CONDITION";
    public static final String WEATHER_FEELS_LIKE_ENG = "WEATHER_FEELS_LIKE_ENG";
    public static final String WEATHER_FEELS_LIKE_METRIC = "WEATHER_FEELS_LIKE_METRIC";
    public static final String WEATHER_HIGH_TEMP_ENG = "WEATHER_HIGH_TEMP_ENG";
    public static final String WEATHER_HIGH_TEMP_METRIC = "WEATHER_HIGH_TEMP_METRIC";
    public static final String WEATHER_LOW_TEMP_ENG = "WEATHER_LOW_TEMP_ENG";
    public static final String WEATHER_LOW_TEMP_METRIC = "WEATHER_LOW_TEMP_METRIC";
    public static final String WEATHER_HOUR = "WEATHER_HOUR";
    public static final String WEATHER_HUMIDITY = "WEATHER_HUMIDITY";
    public static final String WEATHER_DAY = "WEATHER_DAY";
    public static final String WEATHER_MONTH = "WEATHER_MONTH";
    public static final String WEATHER_YEAR = "WEATHER_YEAR";
    public static final String WEATHER_SNOW_ENG = "WEATHER_SNOW_ENG";
    public static final String WEATHER_SNOW_METRIC = "WEATHER_SNOW_METRIC";
    public static final String WEATHER_DAY_OF_WEEK = "WEATHER_DAY_OF_WEEK";
    public static final String WEATHER_CONDITION_RULES = "WEATHER_CONDITION_RULES";
    public static final String WEATHER = "WEATHER";
    public static final int WEATHER_FORECAST_DAILY = 0;
    public static final int WEATHER_FORECAST_HOURLY = 1;

    /** ACTIVITY RECOGNITION **/
    public static final String AR_ACTIVITY_TYPE = "AR_ACTIVITY_TYPE";

    /** ALARM **/
    public static final String ALARM_RELATIVE_TIME = "ALARM_RELATIVE_TIME";
    public static final String ALARM_CONDITION_AFTER = "ALARM_CONDITION_AFTER";
    public static final String ALARM_REFERENCE_TIME = "ALARM_REFERENCE_TIME";
    public static final String ALARM_CONDITION_BEFORE = "ALARM_CONDITION_BEFORE";
    public static final String ALARM_ABSOLUTE_TIME = "ALARM_ABSOLUTE_TIME";
    public static final String ALARM_CONDITION_AT = "ALARM_CONDITION_AT";
    public static final String ALARM = "ALARM";
    public static final String ALARM_RINGTONE_TYPE = "ALARM_RINGTONE_TYPE";
    public static final String ALARM_TIME_NOW = "ALARM_TIME_NOW";

    /** PHONE CALL **/
    public static final String PHONECALL_START_TIME = "PHONECALL_START_TIME";
    public static final String PHONECALL = "PHONECALL";

    /** SMS **/
    public static final String SMS_RECIPIENT = "SMS_RECIPIENT";
    public static final Object SMS_MYSELF = "SMS_MYSELF";
    public static final String SMS_CONTENT = "SMS_CONTENT";
    public static final String SMS = "SMS";
    public static final String SMS_WHEN = "SMS_WHEN";

    /** RULES **/
    public static final String DECISION_RULE = "DECISION_RULE";
    public static final String DECISION_RULE_ELEMENT = "DECISION_RULE_ELEMENT";
    public static final String DECISION_RULE_JSON = "DECISION_RULE_JSON";
    public static final String DECISION_RULE_ID = "DECISION_RULE_ID";
    public static final String DECISION_RULE_TERM = "DECISION_RULE_TERM";
    public static final String DECISION_RULE_CONDITION = "DECISION_RULE_CONDITION";

    /** PROPOSITIONS **/
    public static final String PROPOSITION_TYPE = "PROPOSITION_TYPE";
    public static final String PROPOSITION_ATTRIBUTE = "PROPOSITION_ATTRIBUTE";
    public static final String PROPOSITION_OPERATOR = "PROPOSITION_OPERATOR";
    public static final String PROPOSITION_VALUE = "PROPOSITION_VALUE";
    public static final String PROPOSITION_REF_ATTRIBUTE = "PROPOSITION_REF_ATTRIBUTE";


    /** ACTIONS **/
    public static final String ACTION_TYPE = "ACTION_TYPE";
    public static final String ACTION_SET_ALARM = "ACTION_SET_ALARM";
    public static final String ACTION_TEXT_MESSAGE = "ACTION_TEXT_MESSAGE";
    public static final String ACTION_GOOGLE_ACTIVITY_RECOGNITION = "ACTION_GOOGLE_ACTIVITY_RECOGNITION";
    public static final String ACTION_HOTEL_RESERVATION = "ACTION_HOTEL_RESERVATION";
    public static final String ACTION_CALENDAR = "ACTION_CALENDAR";
    public static final String ACTION_WEATHER = "ACTION_WEATHER";


    /** STREAMING **/
    public final static String STREAMING_ERROR_CAMERA_ALREADY_IN_USE = "STREAMING_ERROR_CAMERA_ALREADY_IN_USE";
    public final static String STREAMING_ERROR_CONFIGURATION_NOT_SUPPORTED = "STREAMING_ERROR_CONFIGURATION_NOT_SUPPORTED";
    public final static String STREAMING_ERROR_STORAGE_NOT_READY = "STREAMING_ERROR_STORAGE_NOT_READY";
    public final static String STREAMING_ERROR_CAMERA_HAS_NO_FLASH = "STREAMING_ERROR_CAMERA_HAS_NO_FLASH";
    public final static String STREAMING_ERROR_INVALID_SURFACE = "STREAMING_ERROR_INVALID_SURFACE";
    public final static String STREAMING_ERROR_UNKNOWN_HOST = "STREAMING_ERROR_UNKNOWN_HOST";
    public final static String STREAMING_ERROR_OTHER = "STREAMING_ERROR_OTHER";
    public static final String STREAMING_SURFACE_DESTROYED = "STREAMING_SURFACE_DESTROYED";
    public static final String STREAMING_SURFACE_CREATED = "STREAMING_SURFACE_CREATED";
    public static final String STREAMING_SURFACE_CHANGED = "STREAMING_SURFACE_CHANGED";
    public static final String STREAMING_SESSION_STOPPED = "STREAMING_SESSION_STOPPED";
    public static final String STREAMING_SESSION_STARTED = "STREAMING_SESSION_STARTED";
    public static final String STREAMING_PREVIEW_STARTED = "STREAMING_PREVIEW_STARTED";
    public static final String STREAMING_BITRATE_UPDATE = "STREAMING_BITRATE_UPDATE";
    public static final String STREAMING_ERROR_RTSP_CONNECTION_FAILED = "STREAMING_ERROR_RTSP_CONNECTION_FAILED";
    public static final String STREAMING_ERROR_RSTP_WRONG_CREDENTIALS = "STREAMING_ERROR_RSTP_WRONG_CREDENTIALS";
    public static final String SET_TOGGLE_STREAM = "SET_TOGGLE_STREAM";
    public static final String SET_STREAMING_TOGGLE_FLASH = "SET_STREAMING_TOGGLE_FLASH";
    public static final String SET_STREAMING_SWITCH_CAMERA = "SET_STREAMING_SWITCH_CAMERA";
    public static final String STREAMING_SUBSCRIBER = "STREAMING_SUBSCRIBER";
    public static final String STREAMING_TYPE = "STREAMING_TYPE";
    public static final String STREAMING_BOTH = "STREAMING_BOTH";
    public static final String STREAMING_VIDEO = "STREAMING_VIDEO";
    public static final String STREAMING_AUDIO = "STREAMING_AUDIO";
    public static final String STREAMING_SUBCRIPTION_CONFIG = "STREAMING_SUBCRIPTION_CONFIG";
    public final static String STREAMING_VIDEO_ENCODER_NONE = "STREAMING_VIDEO_ENCODER_NONE";
    public final static String STREAMING_VIDEO_ENCODER_H264 = "STREAMING_VIDEO_ENCODER_H264";
    public final static String STREAMING_VIDEO_ENCODER_H263 = "STREAMING_VIDEO_ENCODER_H263";
    public final static String STREAMING_AUDIO_ENCODER_NONE = "STREAMING_AUDIO_ENCODER_NONE";
    public final static String STREAMING_AUDIO_ENCODER_AMRNB = "STREAMING_AUDIO_ENCODER_AMRNB";
    public final static String STREAMING_AUDIO_ENCODER_AAC = "STREAMING_AUDIO_ENCODER_AAC";
    public static final String STREAMING_USERNAME = "STREAMING_USERNAME";
    public static final String STREAMING_PASSWORD = "STREAMING_PASSWORD";
    public static final String STREAMING_DESTINATION = "STREAMING_DESTINATION";
    public static final String STREAMING_SESSION_CONFIGURED = "STREAMING_SESSION_CONFIGURED";

    /** ACCELEROMETER **/
    public static final String ACCELEROMETER_FREQUENCY = "ACCELEROMETER_FREQUENCY";
    public static final String ACCELEROMETER_X_AXIS = "ACCELEROMETER_X_AXIS";
    public static final String ACCELEROMETER_Y_AXIS = "ACCELEROMETER_Y_AXIS";
    public static final String ACCELEROMETER_Z_AXIS = "ACCELEROMETER_Z_AXIS";
    public static final String ACCELEROMETER_VECTOR_SUM = "ACCELEROMETER_VECTOR_SUM";
    public static final String ACCELEROMETER_ACCURACY = "ACCELEROMETER_ACCURACY";
    public static final String ACCELEROMETER = "ACCELEROMETER";

}
