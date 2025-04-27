package com.meera.core.network.utils

enum class BaseUrlEnum (var link: String) {
    DEV_SERVER("https://api2.rke.dev.noomera.ru"),
    STAGE_SERVER("https://api2.stage.noomera.ru"),
    PROD_SERVER("https://api.noomera.ru")
}

enum class BaseUrlUploadStorageEnum (var link: String) {
    DEV_SERVER("https://upload.dev.noomera.ru"),
    STAGE_SERVER("https://upload.stage.noomera.ru"),
    PROD_SERVER("https://upload.noomera.ru")
}

