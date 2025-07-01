package com.vitor.cryptotracker.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitor.cryptotracker.data.models.CoinEntity

@Dao
interface CoinDao {

    // Insere uma lista de moedas. Se uma moeda já existir, ela será substituída.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coins: List<CoinEntity>)

    // Busca todas as moedas da tabela e retorna como um LiveData.
    // A UI vai observar isso e se atualizar automaticamente quando os dados mudarem.
    @Query("SELECT * FROM coins")
    fun getAllCoins(): LiveData<List<CoinEntity>>

    // Apaga todas as moedas da tabela.
    @Query("DELETE FROM coins")
    suspend fun clearAll()

}