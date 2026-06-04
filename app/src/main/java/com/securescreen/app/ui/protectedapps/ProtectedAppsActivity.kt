package com.securescreen.app.ui.protectedapps

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.widget.addTextChangedListener
import com.securescreen.app.R
import com.securescreen.app.data.AppInfo
import com.securescreen.app.data.AppRepository
import com.securescreen.app.databinding.ActivityProtectedAppsBinding
import com.securescreen.app.ui.main.AppSelectionAdapter
import com.securescreen.app.ui.main.MainViewModel

class ProtectedAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProtectedAppsBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var repository: AppRepository
    private lateinit var adapter: AppSelectionAdapter
    private var allApps: List<AppInfo> = emptyList()
    private var protectedPackages: Set<String> = emptySet()
    private var appSearchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProtectedAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AppRepository(applicationContext)
        setupTopBar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()

        viewModel.loadApps()
        viewModel.loadState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadState()
        renderHeader()
    }

    private fun setupTopBar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
        binding.topAppBar.post {
            binding.topAppBar.setContentInsetStartWithNavigation(0)
        }
    }

    private fun setupRecyclerView() {
        adapter = AppSelectionAdapter { packageName, isProtected ->
            viewModel.setPackageProtected(packageName, isProtected)
        }

        binding.appsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.appsRecyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener { editable ->
            appSearchQuery = editable?.toString().orEmpty()
            renderAppList()
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.apps.observe(this) { apps ->
            allApps = apps
            renderAppList()
        }

        viewModel.protectedPackages.observe(this) { packages ->
            protectedPackages = packages
            renderHeader()
            renderAppList()
        }
    }

    private fun renderHeader() {
        val count = protectedPackages.size
        binding.selectedCountText.text = getString(R.string.protected_apps_count, count)
        val systemWide = repository.isAggressiveModeEnabled()
        binding.modeWarningText.isVisible = systemWide
        binding.modeWarningText.text = getString(R.string.systemwide_active_note)
    }

    private fun renderAppList() {
        val query = appSearchQuery.trim()
        val filteredApps = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter { app ->
                app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
            }
        }

        adapter.submit(filteredApps, protectedPackages)
        binding.noAppsText.isVisible = filteredApps.isEmpty()
    }
}
