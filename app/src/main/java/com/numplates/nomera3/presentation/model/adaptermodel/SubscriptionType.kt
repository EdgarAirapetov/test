package com.numplates.nomera3.presentation.model.adaptermodel

enum class SubscriptionType {
    // Юзера нет в друзьях
    TYPE_FRIEND_NONE,
    // Юзер кинул мне заявку в друзья
    TYPE_INCOMING_FRIEND_REQUEST,
    // Юзер подписался на меня
    TYPE_SUBSCRIBED,
    //Что-то другое
    DEFAULT
}