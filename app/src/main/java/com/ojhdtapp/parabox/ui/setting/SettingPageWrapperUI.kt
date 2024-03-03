package com.ojhdtapp.parabox.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.AnimatedPane
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.ojhdtapp.parabox.ui.MainSharedViewModel
import com.ojhdtapp.parabox.ui.contact.ContactPageViewModel
import com.ojhdtapp.parabox.ui.menu.calculateMyPaneScaffoldDirective
import com.ojhdtapp.parabox.ui.message.MessageLayoutType
import com.ojhdtapp.parabox.ui.message.MessagePageViewModel
import com.ojhdtapp.parabox.ui.navigation.DefaultRootComponent
import com.ojhdtapp.parabox.ui.navigation.RootComponent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingPageWrapperUi(
    modifier: Modifier = Modifier,
    mainSharedViewModel: MainSharedViewModel,
    viewModel: SettingPageViewModel,
    navigation: StackNavigation<DefaultRootComponent.Config>,
    stackState: ChildStack<*, RootComponent.Child>
) {
//    val viewModel = hiltViewModel<SettingPageViewModel>()
    val state by viewModel.uiState.collectAsState()
    val mainSharedState by mainSharedViewModel.uiState.collectAsState()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<Setting>(
        scaffoldDirective = calculateMyPaneScaffoldDirective(
            windowAdaptiveInfo = currentWindowAdaptiveInfo()
        )
    )
    val layoutType by remember{
        derivedStateOf {
            if (scaffoldNavigator.scaffoldState.scaffoldDirective.maxHorizontalPartitions == 1) {
                SettingLayoutType.NORMAL
            } else {
                SettingLayoutType.SPLIT
            }
        }
    }

    ListDetailPaneScaffold(
        modifier = modifier,
        scaffoldState = scaffoldNavigator.scaffoldState,
        windowInsets = WindowInsets(0.dp),
        listPane = {
            AnimatedPane(modifier = Modifier.preferredWidth(352.dp)) {
                SettingPage(
                    viewModel = viewModel,
                    mainSharedState = mainSharedState,
                    layoutType = layoutType,
                    scaffoldNavigator = scaffoldNavigator,
                    onMainSharedEvent = mainSharedViewModel::sendEvent,
                    navigation = navigation,
                    stackState = stackState
                )
            }
        }
    ) {
        AnimatedPane(modifier = Modifier) {
            when(state.selected) {
                Setting.GENERAL -> {

                }
                Setting.ADDONS -> {

                }
                Setting.LABELS -> {

                }
                Setting.APPEARANCE -> {

                }
                Setting.NOTIFICATION -> {

                }
                Setting.STORAGE -> {

                }
                Setting.EXPERIMENTAL -> {

                }
                Setting.HELP -> {

                }
            }
        }
    }
}