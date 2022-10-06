package com.ojhdtapp.parabox.ui.setting

import android.os.Build
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.ojhdtapp.parabox.ui.MainSharedViewModel
import com.ojhdtapp.parabox.ui.theme.Theme
import com.ojhdtapp.parabox.ui.util.ActivityEvent
import com.ojhdtapp.parabox.ui.util.PreferencesCategory
import com.ojhdtapp.parabox.ui.util.SimpleMenuPreference
import com.ojhdtapp.parabox.ui.util.SwitchPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterfacePage(
    modifier: Modifier = Modifier,
    viewModel: SettingPageViewModel,
    mainSharedViewModel: MainSharedViewModel,
    sizeClass: WindowSizeClass,
    onEvent: (ActivityEvent) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    // Dynamic Color
    val enableDynamicColor by viewModel.enableDynamicColorFlow.collectAsState(initial = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val colorTransitionFraction = scrollBehavior.state.collapsedFraction
            val appBarContainerColor by rememberUpdatedState(
                lerp(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    FastOutLinearInEasing.transform(colorTransitionFraction)
                )
            )
            LargeTopAppBar(
                modifier = Modifier
                    .background(appBarContainerColor)
                    .statusBarsPadding(),
                title = { Text("用户界面") },
                navigationIcon = {
                    if (sizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                        IconButton(onClick = {

                        }) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = "back"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) {
        LazyColumn(
            contentPadding = it
        ) {
            item {
                PreferencesCategory(text = "主题")
            }
            item {
                SwitchPreference(
                    title = "Monet 动态取色",
                    subtitleOn = "应用色彩将响应壁纸变化",
                    subtitleOff = "应用色彩响应壁纸变化（需要系统版本为 Android 12 或更高）",
                    checked = enableDynamicColor,
                    onCheckedChange = viewModel::setEnableDynamicColor
                )
            }
            item {
                SimpleMenuPreference(
                    title = "主题色",
                    selectedKey = viewModel.themeFlow.collectAsState(initial = Theme.DEFAULT).value,
                    optionsMap = mapOf(
                        Theme.DEFAULT to "藤紫",
                        Theme.SAKURA to "樱花",
                        Theme.GARDENIA to "栀子",
                        Theme.WATER to "清水"
                    ),
                    enabled = !enableDynamicColor,
                    onSelect = viewModel::setTheme
                )
            }
            item {
                PreferencesCategory(text = "语言")
            }
            item {
                SimpleMenuPreference(
                    title = "语言",
                    optionsMap = mapOf("zh-rCN" to "中文（中国）", "en" to "英语", "ja" to "日语"),
                    onSelect = {})
            }
        }
    }
}