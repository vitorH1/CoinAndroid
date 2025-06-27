package com.vitor.cryptotracker.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vitor.cryptotracker.data.models.CoinInfoContainer
import com.vitor.cryptotracker.databinding.ItemCryptocurrencyBinding
import com.vitor.cryptotracker.utils.Constants

class CryptoAdapter(
    private val onItemClicked: (CoinInfoContainer) -> Unit
) : RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    inner class CryptoViewHolder(val binding: ItemCryptocurrencyBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<CoinInfoContainer>() {
        override fun areItemsTheSame(oldItem: CoinInfoContainer, newItem: CoinInfoContainer): Boolean {
            return oldItem.coinInfo.id == newItem.coinInfo.id
        }

        override fun areContentsTheSame(oldItem: CoinInfoContainer, newItem: CoinInfoContainer): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val binding = ItemCryptocurrencyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CryptoViewHolder(binding)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val coin = differ.currentList[position]
        holder.binding.apply {
            tvCoinFullName.text = coin.coinInfo.fullName
            tvCoinSymbol.text = coin.coinInfo.name
            tvPrice.text = coin.display.usd.price

            // Lógica para colorir a porcentagem
            val changePct = coin.display.usd.changePct24Hour
            tvChangePct24h.text = "$changePct%"
            if (changePct.startsWith("-")) {
                tvChangePct24h.setTextColor(Color.RED)
            } else {
                tvChangePct24h.setTextColor(Color.parseColor("#388E3C")) // Verde escuro
            }

            Glide.with(root)
                .load(Constants.IMAGE_BASE_URL + coin.coinInfo.imageUrl)
                .into(ivCoinIcon)

            root.setOnClickListener {
                onItemClicked(coin)
            }
        }
    }
}