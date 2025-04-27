package com.numplates.nomera3.data.fcm;

/**
 * Notification type
 */
public interface IPushInfo {

    // Messenger
    String CHAT_INCOMING_MESSAGE = "messenger-message";
    String CHAT_REQUEST = "chat-request";

    // 8. Хочет добавить Вас в друзья (Другой пользователь хочет добавить “меня“ в друзья)
    // Friends
    String FRIEND_REQUEST = "friend-request";
    // 9. Добавил(а) Вас в друзья (Другой пользователь добавил “меня“ в друзья)
    String FRIEND_CONFIRM = "friend-confirm";          // notificationScreen

    // Add to group chat
    // 13. Пригласил(а) вас в групповой чат (Другой пользователь пригласил меня в групповой чат)
    String ADD_TO_GROUP_CHAT = "add-to-chat";

    // Gifts
    // 12. Отправил(а) Вам подарок (Другой пользователь подарил мне подарок)
    String GIFT_RECEIVED_NOTIFICATION = "gift-received";

    String GIFT_RECEIVED_PUSH = "gift-present";             // notificationScreen

    String POST_COMMENTS_PUSH = "happening-happens";

    String SYSTEM_NOTIFICATION = "system-event";

    String CREATE_ANIMATED_AVATAR = "create-avatar";


    // new notifications
    String PUSH_BACK_TO_APP = "back-to-app"; //back to app push

    String PUSH_OPEN_MAP = "open-map"; // открыть карту
    String PUSH_OPEN_PROFILE = "open-profile"; //open own profile
    String PUSH_OPEN_ROAD = "open-road"; // открыть главную дорогу
    String PUSH_OPEN_EVENTS = "open-events"; // открыть вкладку уведомления
    String PUSH_OPEN_CHAT = "open-chat"; // открыть список чатов
    String PUSH_OPEN_REFERAL = "vip-available"; // открыть реферальный экран
    String PUSH_BIRTHDAY = "birthday"; // открыть экран подарков в профиле именинника
    String PUSH_BIRTHDAY_GROUP = "birthday-group"; // открыть группу уведомлений, связанных с днем рождения
    String PUSH_SELF_BIRTHDAY = "self-birthday";

    // new notifications

    // Comments
    // NOTIFICATIONS (not PUSH)
    // 3. Добавил комментарий к посту (чужой пост)
    String POST_COMMENT = "post-comment";
    // 2. Добавил комментарий к вашему посту  (к моему посту)
    String POST_COMMENT_YOUR = "post-comment-your";
    // 4. Ответил на ваш комментарий (в посте)
    String POST_COMMENT_REPLY = "post-comment-reply";
    // 6. Добавил комментарий к посту в группе (на мой комментарий к чужому посту в группе)
    String GROUP_COMMENT = "group-comment";
    // 5. Добавил комментарий к вашему посту в группе (группа)  (ответил на мой комментарий к моему посту в группе)
    String GROUP_COMMENT_YOUR = "group-comment-your";   // notificationScreen
    // 7. Ответил на ваш комментарий в группе (ответил на “мой” комментарий к посту в группе)
    String GROUP_COMMENT_REPLY = "group-comment-reply";

    String PUSH_GROUP_REQUEST = "group-request";

    String PUSH_CALL_START = "voip";
    // 1. Добавил(а) новый пост
    String SUBSCRIBERS_POST_CREATE = "subscribers-post-create";
    // 0. Обновил(а) аватар
    String SUBSCRIBERS_AVATAR_POST_CREATE = "subscribers-avatar-post-create";
    // 14. Пост помечен как запрещенный (модератором) => “Ваш пост одобрен только для личной «Дороги»”
    String ADD_TO_PRIVATE_BY_MODERATOR_NSFW = "moderation-changed-post-privacy";

    String BIRTHDAY = "birthday";

    // Mentions
    String PUSH_MENTION_GROUP_CHAT = "mention-group-chat";      // Пользователь упомянут в групповом чате
    String MENTION_POST = "mention-post";                                   // упоминание в посте
    String MENTION_COMMENT = "mention-comment";                             // упоминание в комментарии
    String MENTION_MAP_EVENT = "mention-map-event";                         // упоминание в событии

    // Community - Уведомления от сообществ
    //   . Опубликован новый пост в сообществе
    String COMMUNITY_NEW_POST = "community-post-create";

    // Reactions
    String COMMENT_REACTION = "comment-reaction";
    String POST_REACTION = "post-reaction";
    String GALLERY_REACTION = "gallery-reaction";
    String MOMENT = "moment";
    String MOMENT_COMMENT = "moment-comment";
    String MOMENT_REACTION = "moment-reaction";
    String MOMENT_COMMENT_REACTION = "moment-comment-reaction";
    String MOMENT_MENTION_COMMENT = "moment-mention-comment";
    String MOMENT_COMMENT_REPLY = "moment-comment-reply";

    String PEOPLE = "people";

    String NOTIFY_PEOPLE = "notify-people";

    // Запрет публиковать в главную дорогу
    String USER_SOFT_BLOCKED = "soft-blocked";

    String HOLIDAY_DAILY_VISITS = "daily-visit";

    //Map events
    String EVENT_START_SOON = "event-start-soon";
    String EVENT_PARTICIPANT = "event-participant";

    // Events
    String EVENT_CALL_UNAVAILABLE = "call-unavailable";
}
