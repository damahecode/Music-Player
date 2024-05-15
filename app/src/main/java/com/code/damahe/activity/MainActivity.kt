package com.code.damahe.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.code.damahe.app.Activity
import com.code.damahe.app.MainContent
import com.code.damahe.res.R
import com.code.damahe.material.theme.DCodeAppTheme
import com.code.damahe.material.viewmodel.ThemeUiState.Loading
import com.code.damahe.material.viewmodel.ThemeUiState.Success
import com.code.damahe.screen.MainScreen
import com.code.damahe.util.Permissions
import com.code.damahe.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : Activity() {

    private val playerViewModel: PlayerViewModel by viewModels()
    private var ongoingRequest: AlertDialog? = null
    private var isPermissionShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash screen on-screen until the UI state is loaded. This condition is
        // evaluated each time the app needs to be redrawn so it should be fast to avoid blocking
        // the UI.
        splashScreen.setKeepOnScreenCondition {
            when (themeUiState) {
                Loading -> true
                is Success -> false
            }
        }

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)

        playerViewModel.bindService()

        setContent {
            MainContent(themeUiState = themeUiState) {
                MainScreen(playerViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!Permissions.checkRunningConditions(this, Permissions.getAllPermission())) {
            requestRequiredPermissions()
        } else
            playerViewModel.fetchAllAudio(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        isPermissionShowing = false
        ongoingRequest?.takeIf { it.isShowing }?.dismiss()

        playerViewModel.unBindService()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isPermissionShowing = false
        requestRequiredPermissions(!isGranted)
        if (isGranted) {
            playerViewModel.fetchAllAudio(this)
        }
    }

    private fun requestRequiredPermissions(deniedAfterRequest: Boolean = false) {
        if (ongoingRequest?.isShowing == true || isPermissionShowing) return

        for (permission in Permissions.getAllPermission()) {
            val id = permission.id
            val showRationale = shouldShowRequestPermissionRationale(id)
            val granted = checkSelfPermission(id) == PackageManager.PERMISSION_GRANTED
            val request = {
                requestPermissionLauncher.launch(permission.id)
            }

            if (granted) continue

            if (deniedAfterRequest && !showRationale && permission.isRequired) {
                AlertDialog.Builder(this).apply {
                    setCancelable(false)
                    setTitle(R.string.msg_permission_required)
                    setMessage(R.string.msg_permission_required_summary)
                    setPositiveButton(R.string.ok) { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    setNegativeButton(R.string.txt_reject) { _, _ -> finish() }
                    ongoingRequest = show()
                }
            } else if (showRationale) {
                AlertDialog.Builder(this).apply {
                    setCancelable(false)
                    setTitle(permission.title)
                    setMessage(permission.description)
                    setPositiveButton(R.string.txt_grant) { _, _ -> request() }
                    setNegativeButton(R.string.txt_reject) { _, _ -> finish() }
                    ongoingRequest = show()
                }
            } else {
                request()
                isPermissionShowing = true
            }
            break
        }
    }
}


@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Composable
fun PreviewScreen() {
    DCodeAppTheme {
       // MainScreen()
    }
}