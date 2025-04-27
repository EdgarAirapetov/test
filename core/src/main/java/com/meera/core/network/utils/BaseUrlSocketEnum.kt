package com.meera.core.network.utils

enum class BaseUrlSocketEnum (var link: String) {
    DEV_SERVER("wss://api2.rke.dev.noomera.ru/socket/websocket"),
    STAGE_SERVER("wss://api2.stage.noomera.ru/socket/websocket"),
    PROD_SERVER("wss://api.noomera.ru/socket/websocket")
}
