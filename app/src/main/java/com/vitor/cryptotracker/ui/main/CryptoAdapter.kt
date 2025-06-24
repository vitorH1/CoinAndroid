package com.vitor.cryptotracker.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vitor.cryptotracker.R
import com.vitor.cryptotracker.data.models.Cryptocurrency

class CryptoAdapter(
    private var cryptos: List<Cryptocurrency>,
    private val onItemClick: (Cryptocurrency) -> Unit // Callback para clique
) : RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    inner class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCrypto: ImageView = itemView.findViewById(R.id.image_crypto)
        val textName: TextView = itemView.findViewById(R.id.text_name)
        val textSymbol: TextView = itemView.findViewById(R.id.text_symbol)
        val textPrice: TextView = itemView.findViewById(R.id.text_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cryptocurrency, parent, false)
        return CryptoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val crypto = cryptos[position]
        holder.textName.text = crypto.name
        holder.textSymbol.text = crypto.symbol.uppercase()
        holder.textPrice.text = "US$ ${"%.2f".format(crypto.current_price)}"
        Glide.with(holder.itemView)
            .load(crypto.image)
            .placeholder(R.mipmap.ic_launcher)
            .into(holder.imageCrypto)
        holder.itemView.setOnClickListener {
            onItemClick(crypto) // Chama o callback ao clicar
        }
    }

    override fun getItemCount(): Int = cryptos.size

    fun updateList(newList: List<Cryptocurrency>) {
        cryptos = newList
        notifyDataSetChanged()
    }
}