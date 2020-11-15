package com.ficko.runnisticapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.ficko.runnisticapp.db.Database
import com.ficko.runnisticapp.other.Constants.DATABASE_NAME
import com.ficko.runnisticapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.ficko.runnisticapp.other.Constants.KEY_NAME
import com.ficko.runnisticapp.other.Constants.KEY_WEIGHT
import com.ficko.runnisticapp.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module // Tells Dagger that this will be our Module object
@InstallIn(ApplicationComponent::class) // Because of this annotation, the objects created within AppModule will live until the whole application is destroyed
object AppModule { // The purpose of AppModule is to tell Dagger how to create certain objects (which it wouldn't know how to do otherwise)

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPrefs: SharedPreferences) = sharedPrefs.getString(KEY_NAME, "") ?: ""
    // getString sometimes returns null, that's why we do an additional null-check

    @Singleton
    @Provides
    fun provideWeight(sharedPrefs: SharedPreferences) = sharedPrefs.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPrefs: SharedPreferences) =
        sharedPrefs.getBoolean(KEY_FIRST_TIME_TOGGLE, true)

    @Singleton
    @Provides
    // Instruction on how to create the database instance
    fun provideDatabase(
        @ApplicationContext appContext: Context // Dagger will automatically insert the needed context here
    ) = Room.databaseBuilder(
        appContext,
        Database::class.java,
        DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    // Instruction on how to create the instance of DAO
    fun provideRunDao(db: Database) = db.getRunDao()

    // We don't need to explicitly write a 'provide' function for our MainRepository because Dagger will automatically know how to create it,
    // since it knows how to create a RunDAO object, and this is the only dependency of the MainRepository class
}