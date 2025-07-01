package com.vitor.cryptotracker.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// Classe principal que encapsula a resposta da API para a lista de moedas
data class TopCoinsResponse(
    @SerializedName("Data")
    val data: List<CoinInfoContainer>
)

// Container que possui as informações completas da moeda
@Parcelize
data class CoinInfoContainer(
    @SerializedName("CoinInfo")
    val coinInfo: CoinInfo,
    @SerializedName("DISPLAY")
    val display: DisplayData?
) : Parcelable

// Informações básicas da moeda (ID, Nome, Símbolo, Imagem)
@Parcelize
data class CoinInfo(
    @SerializedName("Id")
    val id: String,
    @SerializedName("Name")
    val name: String,
    @SerializedName("FullName")
    val fullName: String,
    @SerializedName("ImageUrl")
    val imageUrl: String?
) : Parcelable

// Dados formatados para exibição na UI (preços, etc.)
@Parcelize
data class DisplayData(
    @SerializedName("USD")
    val usd: UsdDisplayData
) : Parcelable

// Dados específicos em USD para exibição
@Parcelize
data class UsdDisplayData(
    @SerializedName("PRICE")
    val price: String,
    @SerializedName("MKTCAP")
    val marketCap: String,
    @SerializedName("SUPPLY")
    val supply: String,
    @SerializedName("TOTALVOLUME24H")
    val totalVolume24h: String,
    @SerializedName("CHANGEPCT24HOUR")
    val changePct24Hour: String,
    @SerializedName("CHANGE24HOUR")
    val change24Hour: String

) : Parcelable