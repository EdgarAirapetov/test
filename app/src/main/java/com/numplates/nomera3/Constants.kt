package com.numplates.nomera3

const val WEBRTC_ROOM_URL = "https://api2.dev.noomera.ru"
const val NOOMEERA_USER_AGREEMENT_URL = "https://noomeera.com/politics/terms_of_use/"

///////////////////////////////////////////////////////////////////////////
// HTTP codes
///////////////////////////////////////////////////////////////////////////
const val HTTP_CODE_SUCCESS = 200
const val HTTP_CODE_BAD_REQUEST = 400
const val HTTP_CODE_NOT_FOUND = 404
const val HTTP_SCHEME = "http"
const val HTTPS_SCHEME = "https"
const val NOOMEERA_SCHEME = "noomeera"
const val MEERA_SCHEME = "meera"

/**
 * @link TELECOM CONSTANTS
 */

const val MIN_USER_AGE = 17
const val MAX_USER_AGE = 79

// Тип звонка (входящий/исходящий)
const val TYPE_CALL_KEYS = "TYPE_CALL_KEYS"
const val INCOMING_CALL_KEY = 2001
const val OUTGOING_CALL_KEY = 2002
const val TYPE_CALL_ACTION = "TYPE_CALL_ACTION"
const val CALL_ACTION_ACCEPT_CALL = 2003
const val CALL_ACTION_REJECT_CALL = 2004
const val CALL_ACTION_OPEN_CALL = 2005
const val CALL_REMOTE_USER_OBJECT = "CALL_REMOTE_USER_OBJECT"

// Тип звонка (видео/аудио)
const val DATA_CALL_KEYS = "DATA_CALL_KEYS"
const val VIDEO_CALL_KEY = "video"
const val AUDIO_CALL_KEY = "audio"

// ID неудаляемой пуш-нотификации о звонке
const val CALL_NOTIFICATION_ID = 8888

// Открытие экрана звонка из пуша или из приложения
const val ACTION_INCOMING_CALL = "ACTION_INCOMING_CALL"

// ID NOTIFICATION_CHANNEL
const val CALL_NOTIFICATION_CHANNEL_ID = "CALL_NOTIFICATION_CHANNEL_ID"

// Настройки, передаваемые в дата-канале при установленном соединении
const val WEBRTC_SET_MIC_DISABLED = "WEBRTC_SET_MIC_DISABLED"
const val WEBRTC_SET_MIC_ENABLED = "WEBRTC_SET_MIC_ENABLED"
const val WEBRTC_SET_CAMERA_DISABLED = "WEBRTC_SET_CAMERA_DISABLED"
const val WEBRTC_SET_CAMERA_ENABLED = "WEBRTC_SET_CAMERA_ENABLED"

const val DEFAULT_KEYBOARD_HEIGHT_PX = 688

// Environment External directory
const val DIRECTORY_VOICE = "Voice"

// Lottie animations
const val LOTTIE_LOADER_SPEED = 3f
const val LOTTIE_LOADER_ANIMATION = "speedometer.json"
const val LOTTIE_MELODY_ANIMATION = "melody_note_animation.json"

// Маска для замены матов в постах и комментах на картинки
const val OBSCENE_WORDS_MASK = "ǞǠǢſǤǨǸǬǮǱǺǼǾȀȂȄȆȈȊȌȎȐȒȔȖȜȞȤȦȨ"

// Маска для замены символов реакций на картинки
const val REACTION_SYMBOLS_MASK = "ǬǮǱǺǼǾȀȂȄȆȈȊȌȎȐȒȔȖȜȞȤȦȨ"

// Privacy settings payload keys
const val KEY_SHOW_ON_MAP = "showOnMap"
// ...

// Friend status
const val FRIEND_STATUS_NONE = 0
const val FRIEND_STATUS_OUTGOING = 1
const val FRIEND_STATUS_CONFIRMED = 2
const val FRIEND_STATUS_INCOMING = 3

// Friendship update
const val UPDATE_ADD = "add"
const val UPDATE_CONFIRM = "confirm"
const val UPDATE_DELETE = "delete"

//Comments availability
const val COMMENTS_AVAILABILITY_FRIENDS = "friends"
const val COMMENTS_AVAILABILITY_NOBODY = "nobody"

// Данный флаг приходит тогда, когда мне пришла заявка в друзья
const val REQUEST_NOT_CONFIRMED_BY_ME = 1
// Данный флаг приходит тогда, когда юзер пока не подтвердил мою заявку в друзья
const val REQUEST_NOT_CONFIRMED_BY_USER = 3
const val USER_GENDER_MALE = 1


// Chat
const val CHAT_VOICE_MESSAGES_PATH = "audio_messages"
const val CHAT_VIDEO_MESSAGE_PATH = "video_messages"
const val CHAT_VOICE_MESSAGE_EXTENSION = ".m4a"

//Application info constants
const val SHOW_CALL_POPUP = "show_call_popup" // used in application info request. It say's if it worth to show call popup
const val SHOW_FRIENDS_SUBSCRIBERS_POPUP = "show_friends_and_subscribers_popup"
const val ADMIN_SUPPORT_ID_NAME = "admin_support_id"

const val WHO_CAN_CALL = "howCanCall" // who can call privacy setting
const val SHOW_FRIENDS_AND_SUBSCRIBERS = "showFriendsAndSubscribers" // who can see my friends and followers
@Suppress("detekt:MaxLineLength")
//Ключ для работы с покупками
var rsaKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmSoOSEr4oEk/Y0Zsc07AxAUUxOjYy1eJR2erIBNZNU/P5aQlnk+5FO7GNY7kiZxgfd4VfHpwzB1oMg07z6Lv7D+DUrwee1RBLy3T1h5nDqoQxysWBTrRAITH7CdbVVPTmtAEUVpPOM3LMWDGDn4RrINNfOETIubUuwyZdE3ia10rivjaHBhuagvcYPnIEDneZ5NRY1FCt0K4BPE2rYlCM9PskkkCxoSgJEt3Oy8lrl8JlAKUug2PVy4gu3CahcrQNdYeyod98ihW7+QIJ/5elgpD3iwNvQysjE/UrnJkQSG2P5HycNz1YIY7/kp0R0woud1aQ5VTC/4j7QyQxqzbDwIDAQAB"


const val SMS_NOMERA_PHONE_NUMBER = "Noomeera"  //используется для парсинга смс (Входящий код)
@Suppress("detekt:MaxLineLength")
const val PESDK_SETTINGS_CHAT = "{\"version\":\"3.8.0\",\"meta\":{\"platform\":\"android\",\"version\":\"7.6.0\",\"createdAt\":\"2020-12-09T12:01:32+03:00\"},\"image\":{\"type\":\"video\"},\"operations\":[{\"type\":\"transform\",\"options\":{\"start\":{\"x\":0.0,\"y\":0.0},\"end\":{\"x\":1.0,\"y\":1.0},\"rotation\":0.0}},{\"type\":\"orientation\",\"options\":{\"rotation\":0,\"flipVertically\":false,\"flipHorizontally\":false}},{\"type\":\"adjustments\",\"options\":{\"brightness\":0.0,\"saturation\":0.012575745582580566,\"contrast\":0.0,\"exposure\":0.0,\"shadows\":0.0,\"highlights\":0.0,\"clarity\":0.0,\"gamma\":0.0,\"sharpness\":0.0,\"blacks\":0.0,\"temperature\":0.0,\"whites\":0.0}},{\"type\":\"sprite\",\"options\":{\"sprites\":[]}}]}"
@Suppress("detekt:MaxLineLength")
object AnimatedAvatarUtils {
    const val DEFAULT_FEMALE_STATE = "{\"assets\":{\"accessories\":{\"color\":\"odezhda_023\",\"id\":\"219\"},\"background\":{\"color\":\"bg_007\",\"id\":\"187\"},\"beard\":{\"id\":\"133\"},\"eyebrows\":{\"color\":\"volosi_brovi_boroda_035\",\"id\":\"73\"},\"eyes\":{\"color\":\"glaza_011\",\"id\":\"53\"},\"hair\":{\"color\":\"volosi_brovi_boroda_019\",\"id\":\"33\"},\"hat\":{\"color\":\"odezhda_033\",\"id\":\"175\"},\"head\":{\"color\":\"golova_001\",\"id\":\"13\"},\"mouth\":{\"color\":\"gubi_013\",\"id\":\"118\"},\"nose\":{\"id\":\"100\"},\"shirt\":{\"color\":\"odezhda_014\",\"id\":\"144\"}},\"gender\":\"female\"}"
    const val DEFAULT_MALE_STATE = "{\"assets\":{\"accessories\":{\"color\":\"odezhda_035\",\"id\":\"206\"},\"background\":{\"color\":\"bg_069\",\"id\":\"195\"},\"beard\":{\"color\":\"volosi_brovi_boroda_001\",\"id\":\"123\"},\"eyebrows\":{\"color\":\"volosi_brovi_boroda_037\",\"id\":\"63\"},\"eyes\":{\"color\":\"glaza_010\",\"id\":\"44\"},\"hair\":{\"color\":\"volosi_brovi_boroda_016\",\"id\":\"26\"},\"hat\":{\"color\":\"odezhda_029\",\"id\":\"155\"},\"head\":{\"color\":\"golova_006\",\"id\":\"6\"},\"mouth\":{\"id\":\"109\"},\"nose\":{\"id\":\"88\"},\"shirt\":{\"color\":\"odezhda_034\",\"id\":\"137\"}},\"gender\":\"male\"}"
}

const val AMPLITUDE_API_KEY = "b9dd0fac91b59affb1374d3557df7fdb"

const val APP_METRICA_API_KEY = "70afecdd-3891-45b8-8a42-893f9c651b61"
// "https://noomeera.com/u/" --- --- для тестирования на prod сервере
// https://dev.noomeera.com/u/testtag  --- для тестирования на dev сервере

const val GIPHY_BRAND_NAME = "giphy"
const val GIPHY_API_KEY = "ES31us8mgUmBZO2rQ4GsMBWGuUynT74z"
const val GIPHY_BASE_URL = "https://api.giphy.com"


const val MEDIA_EXT_MP4 = ".mp4"
const val MEDIA_EXT_M4A = ".m4a"
const val MEDIA_EXT_GIF = ".gif"

const val MEDIA_IMAGE = "image"
const val MEDIA_VIDEO = "video"
const val MEDIA_AUDIO = "audio"

const val ASPECT_16x9 = 1.78
const val MIN_ASPECT = 0.75
const val MAX_ASPECT = 2.55

const val NAME_AVATAR_ASSETS = "android.zip"
const val AVATAR_QUALITY_HIGH = 1.0F
const val AVATAR_QUALITY_LOW = 0.5F
const val USER_SUBSCRIBED = 1
const val NEED_SHOW_DIALOG = 1
const val FIRST_POST_ID = 1L

const val MEDIA_GALLERY_ITEM_COUNT_IN_ROW = 4
const val MEDIA_GALLERY_ITEM_PADDING = 6

const val TRUE_VALUE = "true"
const val FALSE_VALUE = "false"
const val ZERO_VALUE = "0"

const val FALSE_INT = 0
const val TRUE_INT = 1

const val ACTION_AFTER_SUBMIT_LIST_DELAY = 500L
const val FEED_START_VIDEO_DELAY = 500L
const val POST_START_VIDEO_DELAY = 100L
