package com.vitor.cryptotracker.ui.main

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.vitor.cryptotracker.R
import java.text.SimpleDateFormat
import java.util.*

class ChartMarkerView(
    context: Context,
    layoutResource: Int,
    private val prices: List<List<Double>>
) : MarkerView(context, layoutResource) {

    private val tvPrice: TextView = findViewById(R.id.tvPrice)
    private val tvDate: TextView = findViewById(R.id.tvDate)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            tvPrice.text = context.getString(R.string.price_format, it.y)
            // Busca o timestamp mais próximo
            val millis = it.x.toLong()
            tvDate.text = dateFormat.format(Date(millis))
        }
        super.refreshContent(e, highlight)
    }
}