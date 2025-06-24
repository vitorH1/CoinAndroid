package com.vitor.cryptotracker.ui.main

import com.vitor.cryptotracker.R
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.vitor.cryptotracker.data.repository.CryptoRepository
import com.vitor.cryptotracker.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val coinId = intent.getStringExtra("coin_id") ?: ""
        val coinName = intent.getStringExtra("coin_name") ?: ""
        binding.textCoinName.text = coinName

        lifecycleScope.launch {
            try {
                val repository = CryptoRepository()
                val marketChart = repository.getMarketChart(coinId, days = "7")
                val prices = marketChart.prices

                // Cada price = [timestamp, price]
                val entries = prices.map {
                    Entry(it[0].toFloat(), it[1].toFloat())
                }

                if (entries.isNotEmpty()) {
                    val dataSet = LineDataSet(entries, "Preço")
                    // Estilização profissional
                    dataSet.color = Color.parseColor("#2979FF")
                    dataSet.lineWidth = 3f
                    dataSet.setDrawCircles(false)
                    dataSet.setDrawValues(false)
                    dataSet.setDrawFilled(true)
                    dataSet.fillAlpha = 80
                    dataSet.fillColor = Color.parseColor("#90CAF9")
                    dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                    // Eixo X (datas)
                    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    val xAxis = binding.chartPrices.xAxis
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.textColor = Color.DKGRAY
                    xAxis.setDrawGridLines(false)
                    xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val millis = value.toLong()
                            return try {
                                dateFormat.format(Date(millis))
                            } catch (e: Exception) {
                                ""
                            }
                        }
                    }
                    xAxis.labelRotationAngle = -30f
                    xAxis.labelCount = 5
                    xAxis.granularity = 1f

                    // Eixo Y
                    val leftAxis: YAxis = binding.chartPrices.axisLeft
                    leftAxis.textColor = Color.DKGRAY
                    leftAxis.setDrawGridLines(true)
                    leftAxis.gridColor = Color.parseColor("#E0E0E0")
                    binding.chartPrices.axisRight.isEnabled = false

                    // Fundo branco
                    binding.chartPrices.setBackgroundColor(Color.WHITE)

                    // Legenda
                    val legend: Legend = binding.chartPrices.legend
                    legend.isEnabled = true
                    legend.textColor = Color.GRAY
                    legend.textSize = 13f
                    legend.form = Legend.LegendForm.LINE

                    // Remove descrição padrão
                    val desc = Description()
                    desc.text = ""
                    binding.chartPrices.description = desc

                    // Animação
                    binding.chartPrices.animateX(1000)

                    // (Opcional: MarkerView profissional - ver instruções anteriores)
                    binding.chartPrices.marker = ChartMarkerView(this@DetailActivity, R.layout.marker_view, prices)

                    // Set data e atualiza
                    val lineData = LineData(dataSet)
                    binding.chartPrices.data = lineData
                    binding.chartPrices.invalidate()
                } else {
                    Toast.makeText(this@DetailActivity, "Sem dados para exibir", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DetailActivity", "Erro ao buscar histórico", e)
                Toast.makeText(this@DetailActivity, "Erro ao buscar histórico", Toast.LENGTH_SHORT).show()
            }
        }
    }
}