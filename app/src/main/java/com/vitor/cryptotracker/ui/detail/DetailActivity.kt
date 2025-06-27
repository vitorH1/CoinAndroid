package com.vitor.cryptotracker.ui.detail

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.vitor.cryptotracker.R
import com.vitor.cryptotracker.data.models.CoinInfoContainer
import com.vitor.cryptotracker.data.models.OHLCData
import com.vitor.cryptotracker.databinding.ActivityDetailBinding
import com.vitor.cryptotracker.utils.Constants
import com.vitor.cryptotracker.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()
    private var coin: CoinInfoContainer? = null

    companion object {
        const val EXTRA_COIN = "extra_coin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }


        coin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_COIN, CoinInfoContainer::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_COIN)
        }


        if (coin == null) {
            Toast.makeText(this, "Erro: Moeda não encontrada", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUI()
        setupChartPeriodSelector()
        setupObservers()

        binding.toggleGroupPeriod.check(R.id.btn30d)
    }

    private fun setupUI() {
        coin?.let {
            supportActionBar?.title = "${it.coinInfo.fullName} (${it.coinInfo.name})"
            binding.tvDetailPrice.text = it.display.usd.price
            binding.tvMarketCap.text = "Capitalização de Mercado: ${it.display.usd.marketCap}"
            binding.tvVolume24h.text = "Volume (24h): ${it.display.usd.totalVolume24h}"
            binding.tvSupply.text = "Fornecimento em Circulação: ${it.display.usd.supply}"
        }
    }

    private fun setupChartPeriodSelector() {
        binding.toggleGroupPeriod.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val days = when (checkedId) {
                    R.id.btn7d -> 7
                    R.id.btn30d -> 30
                    R.id.btn90d -> 90
                    else -> 30
                }
                fetchChartData(days)
            }
        }
    }

    private fun fetchChartData(days: Int) {
        coin?.coinInfo?.name?.let { symbol ->
            viewModel.fetchHistoricalData(symbol, days)
        }
    }

    private fun setupObservers() {
        viewModel.historicalData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.detailProgressBar.visibility = View.VISIBLE
                    binding.lineChart.visibility = View.INVISIBLE
                }
                is Resource.Success -> {
                    binding.detailProgressBar.visibility = View.GONE
                    binding.lineChart.visibility = View.VISIBLE
                    resource.data?.let {
                        setupChart(it)
                        binding.lineChart.marker = ChartMarkerView(this, it)
                    }
                }
                is Resource.Error -> {
                    binding.detailProgressBar.visibility = View.GONE
                    Toast.makeText(this, "Erro ao carregar gráfico: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupChart(ohlcData: List<OHLCData>) {
        val entries = ohlcData.mapIndexed { index, data ->
            Entry(index.toFloat(), data.close)
        }

        val dataSet = LineDataSet(entries, "").apply {
            color = ContextCompat.getColor(this@DetailActivity, R.color.primary)
            valueTextColor = Color.TRANSPARENT
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 2f
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@DetailActivity, R.drawable.chart_gradient)
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            axisLeft.apply {
                textColor = ContextCompat.getColor(this@DetailActivity, R.color.on_surface_variant)
                gridColor = ContextCompat.getColor(this@DetailActivity, R.color.surface)
                setDrawAxisLine(false)
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(this@DetailActivity, R.color.on_surface_variant)
                gridColor = ContextCompat.getColor(this@DetailActivity, R.color.surface)
                setDrawAxisLine(false)

                // ******** A CORREÇÃO ESTÁ AQUI ********
                valueFormatter = object : ValueFormatter() {
                    // O método correto é 'getFormattedValue' e ele só precisa do 'value'
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < ohlcData.size) {
                            val timestamp = ohlcData[index].time * 1000L
                            val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                            sdf.format(Date(timestamp))
                        } else {
                            ""
                        }
                    }
                }
            }
            animateX(1000)
            invalidate()
        }
    }
}