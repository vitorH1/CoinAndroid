package com.vitor.cryptotracker.di

import android.content.Context
import androidx.room.Room
import com.vitor.cryptotracker.data.db.AppDatabase
import com.vitor.cryptotracker.data.db.CoinDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // As dependências viverão enquanto o app viver
object DatabaseModule {

    // Provê uma instância única (Singleton) do nosso AppDatabase
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "crypto_database" // Nome do arquivo do banco de dados
        ).build()
    }

    // Provê uma instância única (Singleton) do nosso CoinDao
    @Provides
    @Singleton
    fun provideCoinDao(appDatabase: AppDatabase): CoinDao {
        return appDatabase.coinDao()
    }

}