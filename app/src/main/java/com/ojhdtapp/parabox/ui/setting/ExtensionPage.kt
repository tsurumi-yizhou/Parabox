package com.ojhdtapp.parabox.ui.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIcons
import com.ojhdtapp.parabox.domain.model.AppModel
import com.ojhdtapp.parabox.ui.MainSharedViewModel
import com.ojhdtapp.parabox.ui.util.ActivityEvent
import com.ojhdtapp.parabox.ui.util.NormalPreference
import com.ojhdtapp.parabox.ui.util.PreferencesCategory
import com.ojhdtapp.parabox.ui.util.SettingNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@RootNavGraph(start = false)
@Composable
fun ExtensionPage(
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
    mainNavController: NavController,
    mainSharedViewModel: MainSharedViewModel,
    sizeClass: WindowSizeClass,
    onEvent: (ActivityEvent) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
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
                title = { Text("扩展") },
                navigationIcon = {
                    if (sizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                        IconButton(onClick = {
                            mainNavController.navigateUp()
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
        // Plugin List State
        val pluginList by mainSharedViewModel.pluginListStateFlow.collectAsState()
        LazyColumn(
            contentPadding = it
        ) {
            item() {
                if (pluginList.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 16.dp),
                            text = "未发现可用扩展",
                            style = MaterialTheme.typography.labelLarge
                        )
                        FilledTonalButton(
                            onClick = { /*TODO*/ }) {
                            FaIcon(
                                modifier = Modifier.padding(end = 8.dp),
                                faIcon = FaIcons.GooglePlay,
                                size = ButtonDefaults.IconSize,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Text(text = "从应用商店获取")
                        }
                    }
                }
            }
            items(
                items = pluginList,
                key = { it.packageName }) {
                NormalPreference(
                    title = it.name,
                    subtitle = "${it.packageName}\n版本 ${it.version}",
                    leadingIcon = {
                        AsyncImage(
                            model = it.icon,
                            contentDescription = "icon",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(
                                    CircleShape
                                )
                        )
                    },
                    trailingIcon = {
                        when (it.runningStatus) {
                            AppModel.RUNNING_STATUS_DISABLED -> Icon(
                                imageVector = Icons.Outlined.Block,
                                contentDescription = "disabled"
                            )

                            AppModel.RUNNING_STATUS_ERROR -> Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = "error",
                                tint = MaterialTheme.colorScheme.error
                            )

                            AppModel.RUNNING_STATUS_RUNNING -> Icon(
                                imageVector = Icons.Outlined.CheckCircleOutline,
                                contentDescription = "running",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            AppModel.RUNNING_STATUS_CHECKING -> CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    onClick = {
                        it.launchIntent?.let {
                            onEvent(ActivityEvent.LaunchIntent(it))
                        }
                    }
                )
            }
            item {
                if (pluginList.isNotEmpty()) {
                    NormalPreference(
                        title = "重置扩展连接",
                        subtitle = "断开并重新连接扩展，不会影响扩展运行状态",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LinkOff,
                                contentDescription = "reset link"
                            )
                        },
                        enabled = true
                    ) {
                        onEvent(ActivityEvent.ResetExtension)
                    }
                }
            }
        }
    }
}