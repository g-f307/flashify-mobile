package com.example.flashify.di

import android.content.Context
import com.example.flashify.model.manager.GoogleAuthManager
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.model.manager.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideThemeManager(@ApplicationContext context: Context): ThemeManager {
        return ThemeManager(context)
    }

    @Provides
    fun provideGoogleAuthManager(@ApplicationContext context: Context): GoogleAuthManager {
        return GoogleAuthManager(context)
    }
}