package com.vitor.cryptotracker

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import com.vitor.cryptotracker.databinding.ActivityMainBinding
import com.vitor.cryptotracker.ui.detail.DetailActivity
import com.vitor.cryptotracker.ui.main.CryptoAdapter
import com.vitor.cryptotracker.ui.main.MainViewModel
import com.vitor.cryptotracker.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import android.util.Pair // IMPORT CORRETO

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var cryptoAdapter: CryptoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupObservers()
        setupSwipeToRefresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = "Pesquisar moeda..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchCoin(newText)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchCryptocurrencies()
        }
    }

    private fun setupRecyclerView() {
        // MUDANÇA AQUI: O lambda agora recebe os 3 parâmetros
        cryptoAdapter = CryptoAdapter { coin, iconView, nameView ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_COIN, coin)
                // Passando os transitionNames únicos para a DetailActivity
                putExtra(DetailActivity.EXTRA_ICON_TRANSITION_NAME, iconView.transitionName)
                putExtra(DetailActivity.EXTRA_NAME_TRANSITION_NAME, nameView.transitionName)
            }

            // A Mágica da Animação Corrigida
            val options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                Pair.create(iconView, iconView.transitionName),
                Pair.create(nameView, nameView.transitionName)
            )

            startActivity(intent, options.toBundle())
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
                    showShimmer()
                }
                is Resource.Success -> {
                    hideShimmer()
                    binding.swipeRefreshLayout.isRefreshing = false
                    resource.data?.let { cryptoAdapter.differ.submitList(it.toList()) }
                }
                is Resource.Error -> {
                    hideShimmer()
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(this, "Erro: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showShimmer() {
        if (cryptoAdapter.differ.currentList.isEmpty()) {
            binding.shimmerLayout.apply {
                startShimmer()
                visibility = View.VISIBLE
            }
            binding.recyclerCryptos.visibility = View.INVISIBLE
        }
    }

    private fun hideShimmer() {
        binding.shimmerLayout.apply {
            stopShimmer()
            visibility = View.GONE
        }
        binding.recyclerCryptos.visibility = View.VISIBLE
    }
}