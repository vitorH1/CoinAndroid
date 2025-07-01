package com.vitor.cryptotracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vitor.cryptotracker.data.models.CoinEntity

// Anotação que define que esta é uma classe de banco de dados
@Database(
    entities = [CoinEntity::class], // Lista de todas as "tabelas" (Entidades)
    version = 1, // Versão do banco. Se mudarmos a estrutura, teremos que incrementar.
    exportSchema = false // Não precisamos exportar o esquema para um arquivo
)
abstract class AppDatabase : RoomDatabase() {

    // Função abstrata que o Room usará para nos dar acesso ao nosso DAO
    abstract fun coinDao(): CoinDao

}