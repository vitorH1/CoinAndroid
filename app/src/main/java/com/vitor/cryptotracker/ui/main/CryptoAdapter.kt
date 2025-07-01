package com.vitor.cryptotracker.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vitor.cryptotracker.data.models.CoinEntity
import com.vitor.cryptotracker.databinding.ItemCryptocurrencyBinding
import com.vitor.cryptotracker.utils.Constants

// A assinatura do clique agora passa a CoinEntity e as views
class CryptoAdapter(
    private val onItemClicked: (coin: CoinEntity, iconView: View, nameView: View) -> Unit
) : RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    inner class CryptoViewHolder(val binding: ItemCryptocurrencyBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<CoinEntity>() {
        override fun areItemsTheSame(oldItem: CoinEntity, newItem: CoinEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CoinEntity, newItem: CoinEntity): Boolean {
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
            ivCoinIcon.transitionName = "transition_icon_${coin.id}"
            tvCoinFullName.transitionName = "transition_name_${coin.id}"

            tvCoinFullName.text = coin.fullName
            tvCoinSymbol.text = coin.symbol
            tvPrice.text = coin.price

            val changePct = coin.changePct24Hour
            tvChangePct24h.text = "$changePct%"
            if (changePct.startsWith("-")) {
                tvChangePct24h.setTextColor(Color.parseColor("#F44336"))
            } else {
                tvChangePct24h.setTextColor(Color.parseColor("#4CAF50"))
            }

            Glide.with(root)
                .load(Constants.IMAGE_BASE_URL + coin.imageUrl)
                .into(ivCoinIcon)

            root.setOnClickListener {
                onItemClicked(coin, ivCoinIcon, tvCoinFullName)
            }
        }
    }
}