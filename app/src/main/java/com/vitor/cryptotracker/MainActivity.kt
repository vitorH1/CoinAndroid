package com.vitor.cryptotracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vitor.cryptotracker.data.models.CoinInfoContainer
import com.vitor.cryptotracker.databinding.ActivityMainBinding
import com.vitor.cryptotracker.ui.detail.DetailActivity
import com.vitor.cryptotracker.ui.main.CryptoAdapter
import com.vitor.cryptotracker.ui.main.MainViewModel
import com.vitor.cryptotracker.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var cryptoAdapter: CryptoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        cryptoAdapter = CryptoAdapter { coin ->
            // Ação de clique: abrir a DetailActivity passando o objeto da moeda
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_COIN, coin)
            }
            startActivity(intent)
        }
        binding.recyclerCryptos.apply {
            adapter = cryptoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupObservers() {
        viewModel.cryptocurrencies.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.mainProgressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.mainProgressBar.visibility = View.GONE
                    cryptoAdapter.differ.submitList(resource.data)
                }
                is Resource.Error -> {
                    binding.mainProgressBar.visibility = View.GONE
                    Toast.makeText(this, "Erro: ${resource.message}", Toast.LENGTH_LONG).show()
                    Log.e("MainActivity", "Hilt error: ${resource.message}")
                }
            }
        }
    }
}