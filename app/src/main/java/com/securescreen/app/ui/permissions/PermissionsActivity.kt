package com.securescreen.app.ui.permissions

import android.Manifest
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.securescreen.app.R
import com.securescreen.app.data.AppRepository
import com.securescreen.app.data.PermissionUtils
import com.securescreen.app.databinding.ActivityPermissionsBinding

class PermissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionsBinding
    private lateinit var repository: AppRepository

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, R.string.notification_permission_required, Toast.LENGTH_LONG)
                    .show()
            }
            refreshState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AppRepository(applicationContext)
        setupTopBar()
        setupListeners()
        refreshState()
    }

    override fun onResume() {
        super.onResume()
        refreshState()
    }

    private fun setupTopBar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.grantUsageAccessButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        binding.grantNotificationPermissionButton.setOnClickListener {
            requestNotificationPermission()
        }

        binding.grantAccessibilityPermissionButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.grantOverlayPermissionButton.setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }

        binding.grantExactAlarmPermissionButton.setOnClickListener {
            requestExactAlarmPermission()
        }

        binding.openBatteryOptimizationButton.setOnClickListener {
            requestBatteryOptimizationExemption()
        }

        binding.autoStartOnBootSwitch.setOnCheckedChangeListener { _, isChecked ->
            repository.setAutoStartOnBootEnabled(isChecked)
        }
    }

    private fun refreshState() {
        val usageGranted = PermissionUtils.hasUsageStatsPermission(this)
        val notificationGranted = PermissionUtils.hasNotificationPermission(this)
        val accessibilityGranted = PermissionUtils.isAccessibilityServiceEnabled(
            this,
            com.securescreen.app.service.SecureAccessibilityService::class.java
        )
        val batteryIgnored = PermissionUtils.isIgnoringBatteryOptimizations(this)

        val exactAlarmAllowed = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            null
        } else {
            getSystemService(android.app.AlarmManager::class.java).canScheduleExactAlarms()
        }
        updatePermissionChip(binding.usageAccessChip, "Usage Access", usageGranted, "Granted", "Not granted")
        updatePermissionChip(binding.notificationChip, "Notifications", notificationGranted, "Granted", "Not granted")
        updatePermissionChip(binding.accessibilityChip, "Accessibility", accessibilityGranted, "Granted", "Not granted")
        updatePermissionChip(binding.batteryOptimizationChip, "Battery", batteryIgnored, "Ignored", "Restricted")
        updatePermissionChip(
            binding.exactAlarmChip,
            "Exact Alarm",
            exactAlarmAllowed,
            "Granted",
            "Not granted",
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) "N/A" else null
        )

        binding.grantUsageAccessButton.visibility = if (usageGranted) android.view.View.GONE else android.view.View.VISIBLE
        binding.grantNotificationPermissionButton.visibility = if (notificationGranted) android.view.View.GONE else android.view.View.VISIBLE
        binding.grantAccessibilityPermissionButton.visibility = if (accessibilityGranted) android.view.View.GONE else android.view.View.VISIBLE
        binding.grantOverlayPermissionButton.visibility = if (PermissionUtils.canDrawOverlays(this)) android.view.View.GONE else android.view.View.VISIBLE
        binding.grantExactAlarmPermissionButton.visibility = if (exactAlarmAllowed == true) android.view.View.GONE else android.view.View.VISIBLE
        binding.openBatteryOptimizationButton.visibility = if (batteryIgnored) android.view.View.GONE else android.view.View.VISIBLE

        binding.autoStartOnBootSwitch.isChecked = repository.isAutoStartOnBootEnabled()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            openAppNotificationSettings()
            return
        }

        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Toast.makeText(this, "Exact alarm is not required on this Android version.", Toast.LENGTH_SHORT).show()
            return
        }

        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
    }

    private fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun openBatteryOptimizationGuide() {
        val intents = listOf(
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")),
            Intent(Settings.ACTION_SETTINGS)
        )

        val opened = intents.firstOrNull { intent ->
            intent.resolveActivity(packageManager) != null
        }?.let { intent ->
            runCatching { startActivity(intent) }.isSuccess
        } ?: false

        if (!opened) {
            Toast.makeText(this, R.string.unable_to_open_settings, Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestBatteryOptimizationExemption() {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:$packageName")
        )

        if (intent.resolveActivity(packageManager) != null) {
            try {
                startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
                // Fall through to the guide below.
            }
        }

        openBatteryOptimizationGuide()
    }

    private fun updatePermissionChip(
        chip: Chip,
        label: String,
        granted: Boolean?,
        positiveLabel: String,
        negativeLabel: String,
        unavailableLabel: String? = null
    ) {
        val (text, colorRes) = when (granted) {
            true -> "✓ $label $positiveLabel" to R.color.surface_container_high
            false -> "$label $negativeLabel" to R.color.surface_container_high
            null -> "$label ${unavailableLabel ?: "N/A"}" to R.color.surface_container_high
        }

        chip.text = text
        chip.isCheckable = false
        chip.isClickable = false
        chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(this, colorRes))
        chip.setTextColor(
            ContextCompat.getColor(
                this,
                when (granted) {
                    true -> R.color.status_active
                    false -> R.color.text_secondary
                    null -> R.color.status_warning
                }
            )
        )
    }
}
