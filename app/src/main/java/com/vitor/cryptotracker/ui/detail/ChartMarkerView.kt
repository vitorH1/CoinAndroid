package com.vitor.cryptotracker.ui.detail

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.vitor.cryptotracker.R // <-- A LINHA QUE FALTAVA!
import com.vitor.cryptotracker.data.models.OHLCData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("ViewConstructor")
class ChartMarkerView(
    context: Context,
    private val ohlcData: List<OHLCData>
) : MarkerView(context, R.layout.marker_view) {

    // Usando o bom e velho findViewById para simplificar nesta View customizada
    private val tvPrice: TextView = findViewById(R.id.tvPrice)
    private val tvDate: TextView = findViewById(R.id.tvDate)

    // Chamado toda vez que o marker é redesenhado
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val index = it.x.toInt()
            if (index >= 0 && index < ohlcData.size) {
                val dataPoint = ohlcData[index]
                // Formata o preço e a data e os exibe nos TextViews
                tvPrice.text = String.format("$%.2f", dataPoint.close)
                tvDate.text = formatTimestamp(dataPoint.time)
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        // Centraliza o marker acima do ponto selecionado
        return MPPointF(-(width / 2f), -height.toFloat() - 10f)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val netDate = Date(timestamp * 1000)
        return sdf.format(netDate)
    }
}