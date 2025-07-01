package com.vitor.cryptotracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coins") // Define o nome da nossa tabela
data class CoinEntity(
    @PrimaryKey
    val id: String, // ID da moeda, ex: "1182" para Bitcoin
    val symbol: String, // Símbolo, ex: "BTC"
    val fullName: String, // Nome completo, ex: "Bitcoin"
    val imageUrl: String?, // URL da imagem
    val price: String, // Preço formatado, ex: "$ 60,123.45"
    val changePct24Hour: String, // Variação em %, ex: "1.25"
    val marketCap: String, // Capitalização de mercado formatada
    val volume24h: String, // Volume em 24h formatado
    val supply: String // Fornecimento formatado
)