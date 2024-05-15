package com.code.damahe.di

import android.content.Context
import com.code.damahe.service.PlayerBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun providesPlayerBuilder(@ApplicationContext context: Context): PlayerBuilder {
        return PlayerBuilder(context)
    }
}