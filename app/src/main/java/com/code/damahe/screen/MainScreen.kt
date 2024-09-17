package com.code.damahe.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.code.damahe.res.R
import com.code.damahe.res.icon.DCodeIcon.ImageVectorIcon
import com.code.damahe.res.icon.MyIcons
import com.code.damahe.material.dialogs.ThemeDialog
import com.code.damahe.material.model.ThemeString
import com.code.damahe.material.theme.DCodeBackground
import com.code.damahe.material.theme.DCodeGradientBackground
import com.code.damahe.service.PlayerListener
import com.code.damahe.viewmodel.PlayerViewModel

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MainScreen(viewModel: PlayerViewModel = hiltViewModel()) {

    val mBound = viewModel.mBound.collectAsState()

    if (mBound.value) {
        viewModel.getService()?.setListener(object : PlayerListener {
            override fun updateOnChange() {
                viewModel.updateMusicState()
            }
        })
    }

    val showThemeSettingsDialog = remember { mutableStateOf(false) }

    if (showThemeSettingsDialog.value) {
        ThemeDialog(
            string = ThemeString(R.string.title_app_theme, R.string.loading, R.string.ok, R.string.brand_default,
                R.string.brand_dynamic, R.string.gradient_colors_preference, R.string.gradient_colors_yes,
                R.string.gradient_colors_no, R.string.dark_mode_preference, R.string.dark_mode_config_system_default,
                R.string.dark_mode_config_light, R.string.dark_mode_config_dark),
            onDismiss = {showThemeSettingsDialog.value = false},
        )
    }

    val openBottomSheet = remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    DCodeBackground {
        DCodeGradientBackground {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                contentWindowInsets = WindowInsets(0),
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.app_name)) },
                        navigationIcon = {
                            IconButton(onClick = { showThemeSettingsDialog.value = true }) {
                                Icon(ImageVectorIcon(MyIcons.Settings).imageVector, contentDescription = stringResource(id = R.string.txt_preferences))
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                    )
                },
                floatingActionButton = {
                    val musicUiState = viewModel.musicControllerUiState
                    if (musicUiState.playerState  != null) {
                        FloatingActionButton(
                            modifier = Modifier.systemBarsPadding(),
                            onClick = { openBottomSheet.value = true },
                        ) {
                            Icon(Icons.Rounded.Face, "Localized description")
                        }
                    }
                }
            ) { padding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal,
                            ),
                        ),
                ) {
                    AllSongsList(modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                        viewModel
                    ) { index, list ->
                        viewModel.setMusic(index, list)
                    }

                    // Sheet content
                    if (openBottomSheet.value) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                viewModel.updateCurrentPosition(false)
                                openBottomSheet.value = false
                            },
                            sheetState = bottomSheetState,
                            windowInsets = WindowInsets(0)
                        ) {
                            PlayerLayout(viewModel)
                        }
                    }
                }
            }
        }
    }
}


//@Preview(
//    showBackground = true
//)
//@Composable
//fun DefaultPreview() {
//    DCodeAppTheme {
//        MainScreen()
//    }
//}