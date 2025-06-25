package com.vitor.cryptotracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vitor.cryptotracker.databinding.ActivityMainBinding
import com.vitor.cryptotracker.ui.main.CryptoAdapter
import com.vitor.cryptotracker.ui.main.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var cryptoAdapter: CryptoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adapter agora recebe o callback de clique
        cryptoAdapter = CryptoAdapter(emptyList()) { crypto ->
            val intent = Intent(this, com.vitor.cryptotracker.ui.detail.DetailActivity::class.java)
            intent.putExtra("coin_id", crypto.id)
            intent.putExtra("coin_name", crypto.name)
            startActivity(intent)
        }
        binding.recyclerCryptos.layoutManager = LinearLayoutManager(this)
        binding.recyclerCryptos.adapter = cryptoAdapter

        // Observa os dados da ViewModel
        viewModel.cryptocurrencies.observe(this) { resource ->
            when {
                resource.data != null -> {
                    cryptoAdapter.updateList(resource.data)
                    // Mostra no log o nome das moedas
                    resource.data.forEach {
                        Log.d("MainActivity", "Moeda: ${it.name} (${it.symbol})")
                    }
                }
                resource.message != null -> {
                    Log.e("MainActivity", "Erro: ${resource.message}")
                }
                else -> {
                    Log.d("MainActivity", "Carregando criptomoedas...")
                }
            }
        }

        // Dispara busca das criptomoedas
        viewModel.fetchCryptocurrencies()
    }
}