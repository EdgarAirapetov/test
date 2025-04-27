package com.meera.core.network.utils

interface BaseUrlProvider {
    fun provideBaseUrl() : String
    fun provideBaseUrlSocket() : String

    fun provideBaseUrlUploadStorage() : String
}
