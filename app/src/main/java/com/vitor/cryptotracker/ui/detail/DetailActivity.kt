package com.vitor.cryptotracker.ui.detail

import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.vitor.cryptotracker.R
import com.vitor.cryptotracker.data.models.OHLCData
import com.vitor.cryptotracker.data.models.Resource
import com.vitor.cryptotracker.databinding.ActivityDetailBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity : AppCompatActivity(), OnChartValueSelectedListener {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    private var coinId: String = ""
    private var coinName: String = ""
    private var coinSymbol: String = ""

    // Tipo de gráfico atual (0 = linha, 1 = velas)
    private var currentChartType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtendo os dados da intent
        coinId = intent.getStringExtra("coin_id") ?: ""
        coinName = intent.getStringExtra("coin_name") ?: ""
        coinSymbol = intent.getStringExtra("coin_symbol") ?: ""

        // Configuração da toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configuração inicial
        setupInitialUI()
        setupChartTypeSelection()
        setupPeriodSelection()
        setupObservers()

        // Carregar dados
        viewModel.fetchMarketChart(coinId)
        viewModel.fetchOHLCData(coinId)
    }

    private fun setupInitialUI() {
        // Configurações iniciais da UI
        binding.textCoinName.text = coinName
        binding.textCoinSymbol.text = coinSymbol

        // Configuração do ShimmerFrameLayout
        binding.shimmerChart.startShimmer()

        // Listener para voltar na toolbar
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Configurando os charts
        setupLineChart(binding.chartPrices)
        setupCandleStickChart(binding.chartCandles)
    }

    private fun setupChartTypeSelection() {
        binding.tabChartType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentChartType = tab?.position ?: 0
                updateChartVisibility()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateChartVisibility() {
        when (currentChartType) {
            0 -> { // Gráfico de linha
                binding.chartPrices.visibility = View.VISIBLE
                binding.chartCandles.visibility = View.GONE
            }
            1 -> { // Gráfico de candles
                binding.chartPrices.visibility = View.GONE
                binding.chartCandles.visibility = View.VISIBLE
            }
        }
    }

    private fun setupPeriodSelection() {
        val chipClickListener = View.OnClickListener { view ->
            val chip = view as Chip
            val period = when (chip.id) {
                R.id.chip1d -> "1"
                R.id.chip7d -> "7"
                R.id.chip30d -> "30"
                R.id.chip90d -> "90"
                R.id.chip1y -> "365"
                else -> "7"
            }

            // DEBOUNCE: só troca o período se não estiver carregando
            if (!viewModel.isLoading) {
                viewModel.changePeriod(coinId, period)

                // Atualizar o formato da data de acordo com o período selecionado
                when (period) {
                    "1" -> binding.chartPrices.xAxis.valueFormatter = createTimeFormatter()
                    else -> binding.chartPrices.xAxis.valueFormatter = createDateFormatter()
                }

                // Mostrar shimmer durante o carregamento
                binding.shimmerChart.visibility = View.VISIBLE
                binding.chartPrices.visibility = View.GONE
                binding.chartCandles.visibility = View.GONE
                binding.shimmerChart.startShimmer()
            }
        }

        binding.chip1d.setOnClickListener(chipClickListener)
        binding.chip7d.setOnClickListener(chipClickListener)
        binding.chip30d.setOnClickListener(chipClickListener)
        binding.chip90d.setOnClickListener(chipClickListener)
        binding.chip1y.setOnClickListener(chipClickListener)

        // Marcar o chip 7D por padrão
        binding.chip7d.isChecked = true
    }

    private fun setupObservers() {
        // Observer para os dados do gráfico de linha
        viewModel.marketChart.observe(this) { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    resource.data?.let { marketChart ->
                        updateLineChart(marketChart.prices)
                        updatePriceInfo(marketChart.prices)
                    }
                    stopShimmer()
                }
                Resource.Status.LOADING -> {
                    startShimmer()
                }
                Resource.Status.ERROR -> {
                    stopShimmer()
                    // Mostrar mensagem de erro
                    resource.message?.let { msg ->
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Observer para os dados do gráfico de velas
        viewModel.ohlcData.observe(this) { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    resource.data?.let { ohlcData ->
                        updateCandleStickChart(ohlcData)
                    }
                    stopShimmer()
                }
                Resource.Status.LOADING -> {
                    startShimmer()
                }
                Resource.Status.ERROR -> {
                    stopShimmer()
                    // Mostrar mensagem de erro
                    resource.message?.let { msg ->
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Observer para o período selecionado
        viewModel.selectedPeriod.observe(this) { period ->
            // Atualiza o título com o período
            val periodText = when (period) {
                "1" -> "Últimas 24 horas"
                "7" -> "Últimos 7 dias"
                "30" -> "Últimos 30 dias"
                "90" -> "Últimos 90 dias"
                "365" -> "Último ano"
                else -> "Últimos 7 dias"
            }
        }
    }

    private fun startShimmer() {
        binding.shimmerChart.visibility = View.VISIBLE
        binding.chartPrices.visibility = View.GONE
        binding.chartCandles.visibility = View.GONE
        binding.shimmerChart.startShimmer()
    }

    private fun stopShimmer() {
        binding.shimmerChart.stopShimmer()
        binding.shimmerChart.visibility = View.GONE
        updateChartVisibility()
    }

    private fun setupLineChart(chart: LineChart) {
        // Configurações básicas do gráfico
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.description.isEnabled = false
        chart.setNoDataText("Carregando dados...")
        chart.setOnChartValueSelectedListener(this)

        // Efeito de sombra
        val shadow = chart.renderer.paintRender
        shadow.setShadowLayer(3f, 0f, 2f, Color.parseColor("#20000000"))

        // Configuração do eixo X
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(this, R.color.text_secondary)
        xAxis.valueFormatter = createDateFormatter()
        xAxis.labelCount = 5

        // Configuração do eixo Y
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#E0E0E0")
        leftAxis.textColor = ContextCompat.getColor(this, R.color.text_secondary)

        // Desabilita o eixo Y direito
        chart.axisRight.isEnabled = false

        // Configuração da legenda
        val legend = chart.legend
        legend.form = Legend.LegendForm.NONE
        legend.textColor = ContextCompat.getColor(this, R.color.text_secondary)
    }

    private fun setupCandleStickChart(chart: CandleStickChart) {
        // Configurações básicas do gráfico de candles
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.description.isEnabled = false
        chart.setNoDataText("Carregando dados...")
        chart.setOnChartValueSelectedListener(this)

        // Configuração do eixo X
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(this, R.color.text_secondary)
        xAxis.valueFormatter = createDateFormatter()
        xAxis.labelCount = 5

        // Configuração do eixo Y
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#E0E0E0")
        leftAxis.textColor = ContextCompat.getColor(this, R.color.text_secondary)

        // Desabilita o eixo Y direito
        chart.axisRight.isEnabled = false

        // Configuração da legenda
        val legend = chart.legend
        legend.form = Legend.LegendForm.NONE
        legend.textColor = ContextCompat.getColor(this, R.color.text_secondary)
    }

    private fun updateLineChart(prices: List<List<Double>>) {
        // Preparar os dados para o gráfico
        val entries = prices.map {
            Entry(it[0].toFloat(), it[1].toFloat())
        }

        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Preço")

            // Configuração do visual do gráfico
            dataSet.color = ContextCompat.getColor(this, R.color.chart_line)
            dataSet.lineWidth = 2.5f
            dataSet.setDrawCircles(false)
            dataSet.setDrawValues(false)
            dataSet.setDrawFilled(true)

            // Configuração do preenchimento com gradiente
            val drawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    ContextCompat.getColor(this, R.color.chart_fill_start),
                    ContextCompat.getColor(this, R.color.chart_fill_end)
                )
            )
            dataSet.fillDrawable = drawable

            // Tipo de linha suave
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

            // Destacar pontos selecionados
            dataSet.highLightColor = ContextCompat.getColor(this, R.color.accent_color)
            dataSet.highlightLineWidth = 1.5f
            dataSet.setDrawHorizontalHighlightIndicator(true)
            dataSet.setDrawVerticalHighlightIndicator(true)

            // Configuração final do gráfico
            val lineData = LineData(dataSet)
            binding.chartPrices.data = lineData
            binding.chartPrices.invalidate()

            // Animar o gráfico
            binding.chartPrices.animateX(1000)
        }
    }

    private fun updateCandleStickChart(ohlcData: List<OHLCData>) {
        val entries = ohlcData.map { data ->
            CandleEntry(
                data.timestamp.toFloat(),
                data.high.toFloat(),
                data.low.toFloat(),
                data.open.toFloat(),
                data.close.toFloat()
            )
        }

        if (entries.isNotEmpty()) {
            val dataSet = CandleDataSet(entries, "OHLC")

            // Configuração visual dos candles
            dataSet.shadowWidth = 1.5f
            dataSet.decreasingColor = ContextCompat.getColor(this, R.color.candle_red)
            dataSet.decreasingPaintStyle = Paint.Style.FILL
            dataSet.increasingColor = ContextCompat.getColor(this, R.color.candle_green)
            dataSet.increasingPaintStyle = Paint.Style.FILL
            dataSet.neutralColor = ContextCompat.getColor(this, R.color.chart_line)
            dataSet.shadowColorSameAsCandle = true
            dataSet.setDrawValues(false)
            dataSet.highLightColor = ContextCompat.getColor(this, R.color.accent_color)

            // Configuração final do gráfico
            val candleData = CandleData(dataSet)
            binding.chartCandles.data = candleData
            binding.chartCandles.invalidate()

            // Animar o gráfico
            binding.chartCandles.animateX(1000)
        }
    }

    private fun updatePriceInfo(prices: List<List<Double>>) {
        if (prices.isNotEmpty()) {
            // Último preço
            val latestPrice = prices.last()[1]
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
            binding.textCurrentPrice.text = currencyFormatter.format(latestPrice)

            // Data do último preço
            val timestamp = prices.last()[0].toLong()
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            binding.textPriceDate.text = dateFormat.format(Date(timestamp))

            // Variação de preço em 24h
            if (prices.size > 1) {
                val firstPrice = prices.first()[1]
                val priceChange = latestPrice - firstPrice
                val priceChangePercent = (priceChange / firstPrice) * 100

                val changeFormatted = String.format("%.2f%%", priceChangePercent)

                if (priceChangePercent >= 0) {
                    binding.textPriceChange.setTextColor(ContextCompat.getColor(this, R.color.green))
                    binding.textPriceChange.text = "+$changeFormatted"
                    binding.textPriceChange.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_arrow_up_green, 0, 0, 0
                    )
                } else {
                    binding.textPriceChange.setTextColor(ContextCompat.getColor(this, R.color.red))
                    binding.textPriceChange.text = changeFormatted
                    binding.textPriceChange.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_arrow_down_red, 0, 0, 0
                    )
                }

                binding.badgePriceChange.setCardBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        if (priceChangePercent >= 0) R.color.light_green else R.color.light_red
                    )
                )
            }
        }
    }

    private fun createDateFormatter(): ValueFormatter {
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        return object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val millis = value.toLong()
                return try {
                    dateFormat.format(Date(millis))
                } catch (e: Exception) {
                    ""
                }
            }
        }
    }

    private fun createTimeFormatter(): ValueFormatter {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val millis = value.toLong()
                return try {
                    timeFormat.format(Date(millis))
                } catch (e: Exception) {
                    ""
                }
            }
        }
    }

    // Implementação do OnChartValueSelectedListener
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        e?.let { entry ->
            val timestamp = entry.x.toLong()
            val price = entry.y

            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val priceFormatter = NumberFormat.getCurrencyInstance(Locale.US)

            binding.textPriceDate.text = dateFormat.format(Date(timestamp))
            binding.textCurrentPrice.text = priceFormatter.format(price.toDouble())
        }
    }

    override fun onNothingSelected() {
        // Restaurar para o último preço quando nada estiver selecionado
        val prices = (viewModel.marketChart.value?.data?.prices) ?: return
        updatePriceInfo(prices)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}