package com.ficko.runnisticapp.di

import android.content.Context
import androidx.room.Room
import com.ficko.runnisticapp.db.Database
import com.ficko.runnisticapp.other.Constants.DATABASE_NAME
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
}