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

    companion object {
        // Nomes das chaves para os extras do Intent
        const val EXTRA_COIN_SYMBOL = "extra_coin_symbol"
        const val EXTRA_COIN_FULL_NAME = "extra_coin_full_name"
        const val EXTRA_COIN_PRICE = "extra_coin_price"
        const val EXTRA_COIN_IMAGE_URL = "extra_coin_image_url"
        const val EXTRA_COIN_CHANGE_PCT = "extra_coin_change_pct"
        const val EXTRA_COIN_MKTCAP = "extra_coin_mktcap"
        const val EXTRA_COIN_SUPPLY = "extra_coin_supply"
        const val EXTRA_COIN_VOLUME = "extra_coin_volume"

        const val EXTRA_ICON_TRANSITION_NAME = "extra_icon_transition"
        const val EXTRA_NAME_TRANSITION_NAME = "extra_name_transition"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Pega os dados do Intent
        val coinSymbol = intent.getStringExtra(EXTRA_COIN_SYMBOL)
        val coinFullName = intent.getStringExtra(EXTRA_COIN_FULL_NAME)
        val coinPrice = intent.getStringExtra(EXTRA_COIN_PRICE)
        val coinImageUrl = intent.getStringExtra(EXTRA_COIN_IMAGE_URL)
        val coinMktcap = intent.getStringExtra(EXTRA_COIN_MKTCAP)
        val coinVolume = intent.getStringExtra(EXTRA_COIN_VOLUME)
        val coinSupply = intent.getStringExtra(EXTRA_COIN_SUPPLY)
        val coinChangePct = intent.getStringExtra(EXTRA_COIN_CHANGE_PCT)

        // Pega os nomes da transição passados pelo Intent
        val iconTransitionName = intent.getStringExtra(EXTRA_ICON_TRANSITION_NAME)
        val nameTransitionName = intent.getStringExtra(EXTRA_NAME_TRANSITION_NAME)

        // Aplica os nomes às views desta tela
        binding.toolbarCoinIcon.transitionName = iconTransitionName
        binding.toolbarCoinName.transitionName = nameTransitionName

        if (coinSymbol == null) {
            Toast.makeText(this, "Erro: Moeda não encontrada", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Passa os dados recebidos para a UI
        setupUI(coinFullName, coinSymbol, coinPrice, coinMktcap, coinVolume, coinSupply, coinImageUrl, coinChangePct)
        setupChartPeriodSelector(coinSymbol) // Passando o símbolo aqui
        setupObservers()

        binding.toggleGroupPeriod.check(R.id.btn30d)
    }

    private fun setupUI(fullName: String?, symbol: String?, price: String?, mktcap: String?, volume: String?, supply: String?, imageUrl: String?, changePct: String?) {
        supportActionBar?.title = ""

        binding.toolbarCoinName.text = fullName
        Glide.with(this)
            .load(Constants.IMAGE_BASE_URL + imageUrl)
            .into(binding.toolbarCoinIcon)

        binding.tvDetailPrice.text = price

        // Lógica para o campo de Variação 24h
        val changeText = changePct ?: "0.0"
        if (changeText.contains("-")) {
            binding.tvPriceChange24h.text = String.format("%s%%", changeText)
            binding.tvPriceChange24h.setTextColor(ContextCompat.getColor(this, R.color.text_negative))
        } else {
            binding.tvPriceChange24h.text = String.format("+%s%%", changeText)
            binding.tvPriceChange24h.setTextColor(ContextCompat.getColor(this, R.color.text_positive))
        }

        binding.tvMarketCapValue.text = mktcap
        binding.tvVolume24hValue.text = volume
        binding.tvSupplyValue.text = supply
    }

    // Esta é a função que estava faltando
    private fun setupChartPeriodSelector(coinSymbol: String) {
        binding.toggleGroupPeriod.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val days = when (checkedId) {
                    R.id.btn7d -> 7
                    R.id.btn30d -> 30
                    R.id.btn90d -> 90
                    else -> 30
                }
                fetchChartData(coinSymbol, days)
            }
        }
    }

    private fun fetchChartData(symbol: String, days: Int) {
        viewModel.fetchHistoricalData(symbol, days)
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
                        if (it.isNotEmpty()) {
                            setupChart(it)
                            binding.lineChart.marker = ChartMarkerView(this, it)
                        }
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

                valueFormatter = object : ValueFormatter() {
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