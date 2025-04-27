package com.meera.core.di.modules

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.meera.db.DataStore
import com.meera.db.dao.DraftsDao
import com.meera.db.dao.MediakeyboardFavoritesDao
import com.meera.db.dao.PeopleApprovedUsersDao
import com.meera.db.dao.PeopleRelatedUsersDao
import com.meera.db.dao.RegistrationCountriesDao
import com.meera.db.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

private const val DB_NAME = "roomdb"

@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDataStore(context: Context): DataStore {
        return Room
            .databaseBuilder(context, DataStore::class.java, DB_NAME)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.setMaxSqlCacheSize(SQLiteDatabase.MAX_SQL_CACHE_SIZE)
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideRegistrationCountriesDao(dataStore: DataStore): RegistrationCountriesDao {
        return dataStore.registrationCountriesDao()
    }

    @Provides
    fun provideMediakeyboardFavoritesDao(dataStore: DataStore): MediakeyboardFavoritesDao {
        return dataStore.mediakeyboardFavoritesDao()
    }

    @Provides
    fun provideDraftsDao(dataStore: DataStore): DraftsDao {
        return dataStore.draftsDao()
    }

    @Provides
    fun providePeopleApprovedUsersDao(dataStore: DataStore): PeopleApprovedUsersDao {
        return dataStore.peopleApprovedUsersDao()
    }

    @Provides
    fun providePeopleRelatedUsersDao(dataStore: DataStore): PeopleRelatedUsersDao {
        return dataStore.peopleRelatedUsersDao()
    }

    @Provides
    fun provideUserProfileDao(dataStore: DataStore): UserProfileDao {
        return dataStore.userProfileDao()
    }
}
