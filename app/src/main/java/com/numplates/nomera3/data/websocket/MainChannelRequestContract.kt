package com.numplates.nomera3.data.websocket

const val PAYLOAD_STATUS_KEY = "status"
const val STATUS_OK = "ok"
const val STATUS_ERROR = "error"

// Observing
const val REQUEST_TYPING = "typing"
const val REQUEST_ONLINE = "online"
const val REQUEST_UPDATE_USER = "update_user"

const val REQUEST_CREATE_ROOM = "create_room"
const val REQUEST_CHECK_ROOM = "check_room"
const val REQUEST_MUTE_ROOM = "mute_room"
const val REQUEST_UNMUTE_ROOM = "unmute_room"
const val REQUEST_NEW_MESSAGE = "new_message"
const val REQUEST_GET_MESSAGES = "get_messages"
const val REQUEST_REMOVE_MESSAGE = "delete_message"

const val REQUEST_GET_ROOMS = "get_rooms"
const val REQUEST_DELETE_ROOM = "delete_room"
const val REQUEST_RELOAD_DIALOGS = "reload_dialogs"

const val REQUEST_MESSAGE_READ = "readed"
const val REQUEST_SYNC_EVENTS_UPDATE = "sync_events_update"
const val REQUEST_NOTIFICATION_UPDATES = "notification:update"
const val SUBSCRIPTION_NEW_POST_UPDATE = "feed_subscriptions_update"

// GIFTS
const val REQUEST_GET_ALL_GIFTS = "get_gifts"
const val REQUEST_GET_USER_GIFTS = "get_user_gifts"

// USER
const val REQUEST_MAP_USER_STATE = "map_user_state"
const val REQUEST_UPDATE_PROFILE = "update_profile"
const val REQUEST_IS_USERNAME_UNIQUE = "check_uniqname"
const val REQUEST_GET_PRIVACY_SETTINGS = "get_settings"
const val REQUEST_SET_PRIVACY_SETTINGS = "set_settings"
const val REQUEST_PROFILE_STATISTICS = "show_profile_statistics"

const val REQUEST_GET_MEMBERS = "get_members"
const val REQUEST_GET_ADMINS = "get_admins"
const val REQUEST_GET_ROOM_GROUP_STATUS = "get_group_room_status"

// const val REQUEST_GET_MEMBERS_ONLY_IDS = "get_members_only_ids"

const val REQUEST_ADD_USERS = "add_users"
const val REQUEST_REMOVE_USER = "remove_user"

const val REQUEST_ADD_ADMINS = "add_admins"
const val REQUEST_REMOVE_ADMIN = "remove_admin"

const val REQUEST_CHANGE_TITLE = "change_title"
const val REQUEST_CHANGE_DESCRIPTION = "change_description"

// ALBUM
const val REQUEST_GET_ALBUM = "get_album"

// FRIENDS
const val REQUEST_GET_FRIENDS = "get_friends"
const val REQUEST_ADD_FRIENDS = "add_friends"
const val REQUEST_REMOVE_FRIENDS = "remove_friends"
const val REQUEST_UPDATE_FRIENDSHIP = "update_friendship"

const val REQUEST_GET_PROFILE = "get_profile"
const val REQUEST_GET_USER_INFO = "get_user_info"

//PhoneNumberVerify
const val REQUEST_CONTACTS_SEND_CODE = "contact_send_code"
const val REQUEST_CONTACT_VERIFY = "contact_verify"

//contact
const val REQUEST_CONTACTS_SYNC = "contact_sync"
const val REQUEST_CONTACT_CHECK = "contact_check"

//vehicle
const val REQUEST_VEHICLE_INFO = "get_wizard"
const val REQUEST_GET_BRANDS_BY_TYPE = "get_brands_by_type"
const val REQUEST_GET_MODELS_BY_BRAND = "get_models_by_brand"
const val REQUEST_GET_VEHICLE_TYPES = "get_vehicles_type"

const val REQUEST_TEST_DISCONNECT = "disconnect_test"
const val REQUEST_SET_CALL_PRIVACY_FOR_USER = "set_call_privacy_for_user"

const val REQUEST_CALL_STARTED = "call_started"
const val REQUEST_CALL_FINISHED = "call_stopped"
const val REQUEST_NOTIFICATION_PRIVACY = "set_message_notification_privacy"

//SL__:
const val REQUEST_SIGNALING = "signaling"

//Shake
const val REQUEST_SHAKE = "shake"
