@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package com.ojhdtapp.parabox.ui.message

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ojhdtapp.parabox.core.util.toTimeUntilNow
import com.ojhdtapp.parabox.domain.model.Contact
import com.ojhdtapp.parabox.ui.MainSharedViewModel
import com.ojhdtapp.parabox.ui.destinations.ChatPageDestination
import com.ojhdtapp.parabox.ui.setting.EditUserNameDialog
import com.ojhdtapp.parabox.ui.util.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.navigate
import com.valentinilk.shimmer.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class,
    ExperimentalPermissionsApi::class
)
@Destination
@MessageNavGraph(start = true)
@Composable
fun MessagePage(
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
    mainNavController: NavController,
    mainSharedViewModel: MainSharedViewModel,
    sizeClass: WindowSizeClass,
    drawerState: DrawerState,
    onEvent: (ActivityEvent) -> Unit
) {
    val viewModel: MessagePageViewModel = hiltViewModel()
    val listState = rememberLazyListState()
    val snackBarHostState = remember { SnackbarHostState() }
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val expandedFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    val hoverSearchBar by remember{
        derivedStateOf {
            listState.firstVisibleItemIndex > 1
        }
    }
    val messageState by mainSharedViewModel.messageStateFlow.collectAsState()
    val contactState by viewModel.contactStateFlow.collectAsState()
    val archivedContact by viewModel.archivedContactStateFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showDeleteGroupedContactConfirm by remember {
        mutableStateOf(false)
    }
    val notificationPermissionState = rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
    val notificationPermissionDeniedDialog = remember { mutableStateOf(false) }
    LaunchedEffect(true) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionState.status.isGranted){
            notificationPermissionDeniedDialog.value = true
        }
        viewModel.uiEventFlow.collectLatest { it ->
            when (it) {
                is MessagePageUiEvent.ShowSnackBar -> {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(it.message, it.label).also { result ->
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    it.callback?.invoke()
                                }
                                SnackbarResult.Dismissed -> {}
                                else -> {}
                            }
                        }
                    }
                }
                is MessagePageUiEvent.UpdateMessageBadge -> {
                    mainSharedViewModel.setMessageBadge(it.value)
                }
            }
        }
    }
    BackHandler(enabled = viewModel.areaState.value != AreaState.MessageArea
            || viewModel.searchBarActivateState.value != SearchAppBar.NONE) {
        viewModel.setSearchBarActivateState(SearchAppBar.NONE)
        viewModel.clearSelectedContactStateList()
        viewModel.setAreaState(AreaState.MessageArea)
    }
    Row() {
        // Left
        val contactWidthModifier = if(sizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) modifier.width(400.dp) else modifier.weight(1f)
        if (notificationPermissionDeniedDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    notificationPermissionDeniedDialog.value = false
                },
                icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                title = {
                    Text(text = "权限申请")
                },
                text = {
                    Text(
                        "在 Android 13 (Tiramisu) 及更高版本上，通知发送需要额外运行时权限。该权限对于即时通讯应用是必要的。\n您亦可前往设置页面手动授权。"
                    )
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            notificationPermissionDeniedDialog.value = false
                            notificationPermissionState.launchPermissionRequest()
                        }
                    ) {
                        Text("尝试授权")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            notificationPermissionDeniedDialog.value = false
                        }
                    ) {
                        Text("转到设置")
                    }
                }
            )
        }
        GroupActionDialog(
            showDialog = viewModel.showGroupActionDialogState.value,
            state = viewModel.groupInfoState.value,
            sizeClass = sizeClass,
            onDismiss = {
                viewModel.setShowGroupActionDialogState(false)
            }, onConfirm = viewModel::groupContact
        )
        EditActionDialog(
            showDialog = viewModel.showEditActionDialogState.value,
            contact = contactState.data.findLast { it.contactId == viewModel.selectedContactStateList.firstOrNull()?.contactId },
            sizeClass = sizeClass,
            onDismiss = { viewModel.setShowEditActionDialogState(false) },
            onConfirm = {},
            onEvent = {
                when (it) {
                    is EditActionDialogEvent.ProfileAndTagUpdate -> {
                        viewModel.setContactProfileAndTag(it.contactId, it.profile, it.tags)
                        viewModel.addContactTag(it.tags)
                    }
                    is EditActionDialogEvent.EnableNotificationStateUpdate -> {
                        viewModel.setContactNotification(it.contactId, it.value)
                    }
                    is EditActionDialogEvent.PinnedStateUpdate -> {
                        viewModel.setContactPinned(it.contactId, it.value)
                    }
                    is EditActionDialogEvent.ArchivedStateUpdate -> {
                        viewModel.setContactArchived(it.contactId, it.value)
                    }
                    is EditActionDialogEvent.DeleteGrouped -> {
                        showDeleteGroupedContactConfirm = true
                    }
                }
            }
        )
        TagEditAlertDialog(
            showDialog = viewModel.showTagEditAlertDialogState.value,
            contact = contactState.data.findLast { it.contactId == viewModel.selectedContactStateList.firstOrNull()?.contactId },
            sizeClass = sizeClass,
            onDismiss = { viewModel.setShowTagEditAlertDialogState(false) },
            onConfirm = { id: Long, tags: List<String> ->
                viewModel.setContactTag(id, tags)
                viewModel.addContactTag(tags)
                viewModel.setShowTagEditAlertDialogState(false)
                viewModel.clearSelectedContactStateList()
            }
        )
        UserProfileDialog(
            openDialog = mainSharedViewModel.showUserProfileDialogState.value,
            userName = mainSharedViewModel.userNameFlow.collectAsState(initial = "User").value,
            avatarUri = mainSharedViewModel.userAvatarFlow.collectAsState(initial = null).value,
            pluginList = mainSharedViewModel.pluginListStateFlow.collectAsState().value,
            sizeClass = sizeClass,
            onUpdateName = {
                mainSharedViewModel.setEditUserNameDialogState(true)
            },
            onUpdateAvatar = {
                onEvent(ActivityEvent.SetUserAvatar)
            },
            onDismiss = { mainSharedViewModel.setShowUserProfileDialogState(false) })
        EditUserNameDialog(
            openDialog = mainSharedViewModel.editUserNameDialogState.value,
            userName = mainSharedViewModel.userNameFlow.collectAsState(initial = "User").value,
            onConfirm = {
                mainSharedViewModel.setEditUserNameDialogState(false)
                mainSharedViewModel.setUserName(it)
            },
            onDismiss = { mainSharedViewModel.setEditUserNameDialogState(false) }
        )
        if (showDeleteGroupedContactConfirm) {
            androidx.compose.material3.AlertDialog(onDismissRequest = {
                showDeleteGroupedContactConfirm = false
            },
                title = { Text(text = "确认删除") },
                text = { Text(text = "该编组将从会话列表移除，但不会影响任何已关联会话及聊天记录。") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        viewModel.selectedContactStateList.firstOrNull()?.let {
                            viewModel.deleteGroupedContact(it)
                        }
                        showDeleteGroupedContactConfirm = false
                    }) {
                        Text(text = "确认")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        showDeleteGroupedContactConfirm = false
                    }) {
                        Text(text = "取消")
                    }
                })
        }
        Scaffold(
            modifier = contactWidthModifier
                .shadow(8.dp)
                .zIndex(1f),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            topBar = {
                SearchAppBar(
                    text = viewModel.searchText.value,
                    onTextChange = viewModel::setSearchText,
                    placeholder = "搜索会话",
                    activateState = viewModel.searchBarActivateState.value,
                    avatarUri = mainSharedViewModel.userAvatarFlow.collectAsState(initial = null).value,
                    shouldHover = hoverSearchBar,
                    onActivateStateChanged = {
                        viewModel.setSearchBarActivateState(it)
                        viewModel.clearSelectedContactStateList()
                        when (it) {
                            SearchAppBar.SEARCH -> {
                                viewModel.setAreaState(AreaState.SearchArea)
                            }
                            SearchAppBar.NONE, SearchAppBar.SELECT, SearchAppBar.ARCHIVE_SELECT -> {
                                viewModel.setAreaState(AreaState.MessageArea)
                            }
                            else -> {}
                        }
                    },
                    selection = viewModel.selectedContactStateList,
                    onGroupAction = {
                        viewModel.getGroupInfoPack()
                        viewModel.setShowGroupActionDialogState(true)
                    },
                    onDropdownMenuItemEvent = {
                        when (it) {
                            is DropdownMenuItemEvent.Info -> {
                                viewModel.setShowEditActionDialogState(true)
                            }
                            is DropdownMenuItemEvent.NewTag -> {
                                viewModel.setShowTagEditAlertDialogState(true)
                            }
                            is DropdownMenuItemEvent.Hide -> {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = "会话已暂时隐藏",
                                        actionLabel = "取消",
                                        duration = SnackbarDuration.Short
                                    )
                                        .also { result ->
                                            when (result) {
                                                SnackbarResult.ActionPerformed -> {
                                                    viewModel.cancelContactHidden()
                                                }
                                                SnackbarResult.Dismissed -> {}
                                                else -> {}
                                            }
                                        }
                                }
                                viewModel.setContactHidden(
                                    viewModel.selectedContactStateList.toList()
                                        .map { it.contactId })
                            }
                            is DropdownMenuItemEvent.HideArchive -> {
                                viewModel.hideArchiveContact()
                            }
                            is DropdownMenuItemEvent.Pin -> {
                                viewModel.setContactPinned(
                                    viewModel.selectedContactStateList.toList()
                                        .map { it.contactId }, it.value
                                )
                            }
                            is DropdownMenuItemEvent.Archive -> {
                                viewModel.setContactArchived(
                                    viewModel.selectedContactStateList.toList()
                                        .map { it.contactId }, it.value
                                )
                            }
                            is DropdownMenuItemEvent.UnArchiveALl -> {
                                viewModel.setContactArchived(
                                    viewModel.archivedContactStateFlow.value.map { it.contactId },
                                    false
                                )
                            }
                            is DropdownMenuItemEvent.MarkAsRead -> {
                                if (it.value) {
                                    viewModel.clearContactUnreadNum(
                                        viewModel.selectedContactStateList.toList()
                                            .map { it.contactId })
                                } else {
                                    viewModel.restoreContactUnreadNum(
                                        viewModel.selectedContactStateList.toList()
                                            .map { it.contactId })
                                }
                            }
                            is DropdownMenuItemEvent.DeleteGrouped -> {
                                showDeleteGroupedContactConfirm = true
                            }
                        }
                    },
                    onExpandAction = {},
                    sizeClass = sizeClass,
                    onMenuClick = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    },
                    onAvatarClick = {
                        mainSharedViewModel.setShowUserProfileDialogState(true)
                    }
                )
            },
            bottomBar = {

            },
            floatingActionButton = {
                if (sizeClass.widthSizeClass != WindowWidthSizeClass.Medium) {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "发起会话") },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "new contact"
                            )
                        },
                        expanded = expandedFab,
                        onClick = {
                            viewModel.testFun()
                        })
                }
            },
        ) { paddingValues ->
            AnimatedContent(targetState = viewModel.areaState.value,
                transitionSpec = {
                    if (targetState == AreaState.SearchArea && initialState == AreaState.MessageArea) {
                        expandVertically(expandFrom = Alignment.Top).with(
                            scaleOut(
                                tween(200),
                                0.9f
                            ) + fadeOut(tween(200))
                        ).apply {
                            targetContentZIndex = 2f
                        }
                    } else if (targetState == AreaState.MessageArea && initialState == AreaState.SearchArea) {
                        (scaleIn(tween(200), 0.9f) + fadeIn(tween(200))).with(
                            shrinkVertically(
                                shrinkTowards = Alignment.Top
                            )
                        ).apply {
                            targetContentZIndex = 1f
                        }
                    } else if (targetState == AreaState.ArchiveArea && initialState == AreaState.MessageArea) {
                        (slideInHorizontally { 100 } + fadeIn() with slideOutHorizontally { -100 } + fadeOut()).apply {
                            targetContentZIndex = 2f
                        }
//                        slideInHorizontally { it }.with(
//                            scaleOut(tween(200), 0.9f) + fadeOut(
//                                tween(
//                                    200
//                                )
//                            )
//                        ).apply {
//                            targetContentZIndex = 2f
//                        }
                    } else if (targetState == AreaState.MessageArea && initialState == AreaState.ArchiveArea) {
                        (slideInHorizontally { -100 } + fadeIn() with slideOutHorizontally { 100 } + fadeOut()).apply {
                            targetContentZIndex = 1f
                        }
//                        (scaleIn(
//                            tween(200),
//                            0.9f
//                        ) + fadeIn(tween(200))).with(slideOutHorizontally { it }).apply {
//                            targetContentZIndex = 1f
//                        }
                    } else {
                        fadeIn() with fadeOut()
                    }
                }) {
                when (it) {
                    AreaState.MessageArea -> MessageArea(
                        Modifier,
                        listState,
                        paddingValues,
                        viewModel,
                        messageState,
                        contactState,
                        archivedContact,
                        coroutineScope,
                        snackBarHostState,
                        mainSharedViewModel,
                        sizeClass,
                        shimmerInstance,
                        mainNavController
                    )
                    AreaState.SearchArea -> SearchArea(
                        Modifier,
                        paddingValues,
                        viewModel,
                        coroutineScope,
                        snackBarHostState,
                        mainSharedViewModel,
                        sizeClass,
                        shimmerInstance,
                        mainNavController
                    )
                    AreaState.ArchiveArea -> ArchiveArea(
                        Modifier,
                        paddingValues,
                        viewModel,
                        archivedContact,
                        coroutineScope,
                        snackBarHostState,
                        mainSharedViewModel,
                        messageState,
                        sizeClass,
                        shimmerInstance,
                        mainNavController
                    )
                    else -> {}
                }
            }

        }
        // Right
        if (sizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            ChatPage(
                modifier = Modifier.weight(1f),
                navigator = navigator,
                mainNavController = mainNavController,
                mainSharedViewModel = mainSharedViewModel,
                sizeClass = sizeClass,
                onEvent = onEvent,
                isInSplitScreen = true
            )
        }
    }

//        Column(
//            modifier = modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Button(onClick = {
//                onConnectBtnClicked()
//            }, enabled = viewModel.pluginInstalledState.value && !viewModel.sendAvailableState.value) {
//                Text(text = "Connect")
//            }
//            Button(
//                onClick = { onSendBtnClicked() },
//                enabled = viewModel.sendAvailableState.value
//            ) {
//                Text(text = "Send")
//            }
//            Text(text = viewModel.message.value)
//        }
}

@Composable
fun RowScope.SearchArea(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    viewModel: MessagePageViewModel,
    coroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    mainSharedViewModel: MainSharedViewModel,
    sizeClass: WindowSizeClass,
    shimmerInstance: Shimmer,
    mainNavController: NavController
) {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {

        }
    }
}

@Composable
fun RowScope.ArchiveArea(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    viewModel: MessagePageViewModel,
    archivedContact: List<Contact>,
    coroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    mainSharedViewModel: MainSharedViewModel,
    messageState: MessageState,
    sizeClass: WindowSizeClass,
    shimmerInstance: Shimmer,
    mainNavController: NavController
) {
//    androidx.compose.material3.Surface(
//        color = MaterialTheme.colorScheme.surface,
//        tonalElevation = 1.dp
//    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            item{
                Spacer(modifier = Modifier.height(16.dp))
            }
            itemsIndexed(
                items = archivedContact,
                key = { _, item -> item.contactId }
            ) { index, item ->
                val isFirst = index == 0
                val isLast = index == archivedContact.lastIndex
                val topRadius by animateDpAsState(targetValue = if (isFirst) 28.dp else 0.dp)
                val bottomRadius by animateDpAsState(targetValue = if (isLast) 28.dp else 0.dp)
                val isSelected =
                    viewModel.selectedContactStateList.map { it.contactId }
                        .contains(item.contactId)
                ContactItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contact = item,
                    topRadius = topRadius,
                    bottomRadius = bottomRadius,
                    isTop = false,
                    isLoading = false,
                    isSelected = isSelected,
                    isEditing = sizeClass.widthSizeClass != WindowWidthSizeClass.Compact && item.contactId == messageState.contact?.contactId,
                    isExpanded = sizeClass.widthSizeClass == WindowWidthSizeClass.Expanded,
                    noBackground = false,
                    shimmer = shimmerInstance,
                    onClick = {
                        if (viewModel.searchBarActivateState.value == SearchAppBar.SELECT) {
                            viewModel.addOrRemoveItemOfSelectedContactStateList(item)
                        } else {
                            if (viewModel.searchBarActivateState.value != SearchAppBar.ARCHIVE_SELECT) {
                                viewModel.clearContactUnreadNum(item.contactId)
                                mainSharedViewModel.loadMessageFromContact(item)
//                                mainSharedViewModel.receiveAndUpdateMessageFromContact(
//                                    contact = item
//                                )
                                if (sizeClass.widthSizeClass != WindowWidthSizeClass.Expanded) {
                                    mainNavController.navigate(ChatPageDestination())
                                }
                            }
                        }
                    },
                    onLongClick = {
                        if (viewModel.searchBarActivateState.value != SearchAppBar.ARCHIVE_SELECT) {
                            viewModel.setSearchBarActivateState(SearchAppBar.SELECT)
                            viewModel.addOrRemoveItemOfSelectedContactStateList(item)
                        }
                    }
                ) {
                    if (viewModel.searchBarActivateState.value != SearchAppBar.ARCHIVE_SELECT) {
                        viewModel.setSearchBarActivateState(SearchAppBar.SELECT)
                        viewModel.addOrRemoveItemOfSelectedContactStateList(item)
                    }
                }
                if(!isLast){
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
        }
//    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableContact(
    modifier: Modifier = Modifier,
    state: androidx.compose.material.SwipeableState<Boolean>,
    topRadius: Dp,
    bottomRadius: Dp,
    extraSpace: Dp? = 0.dp,
    enabled: Boolean,
    content: @Composable () -> Unit
) = BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
    val extraSpaceInt = with(LocalDensity.current) {
        extraSpace?.toPx() ?: 0f
    }
    val width = constraints.maxWidth.toFloat() + extraSpaceInt
    val anchors = mapOf(0f to false, -width to true)
    val offset = state.offset.value
    val animationProcess = sqrt((-offset * 2 / width).coerceIn(0f, 1f))

    Box(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .swipeable(
                state = state,
                anchors = anchors,
                thresholds = { _, _ -> androidx.compose.material.FractionalThreshold(0.5f) },
                orientation = Orientation.Horizontal,
                enabled = enabled
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(
                        topStart = topRadius,
                        topEnd = topRadius,
                        bottomEnd = bottomRadius,
                        bottomStart = bottomRadius
                    )
                )
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                modifier = Modifier
                    .offset(x = 32.dp * (1f - animationProcess))
                    .alpha(animationProcess),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "隐藏该会话",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.HideSource,
                    contentDescription = "not disturb",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Box(
            modifier = Modifier.offset(offset = { IntOffset(x = offset.roundToInt(), y = 0) }),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactItem(
    modifier: Modifier = Modifier,
    contact: Contact?,
    icon: @Composable() (() -> Unit)? = null,
    title: String? = null,
    subTitle: String? = null,
    timestamp: Long? = null,
    unreadMessagesNum: Int? = null,
    topRadius: Dp,
    bottomRadius: Dp,
//    isFirst: Boolean = false,
//    isLast: Boolean = false,
    isTop: Boolean = false,
    isLoading: Boolean = true,
    isSelected: Boolean = false,
    isEditing: Boolean = false,
    isExpanded: Boolean = false,
    noBackground: Boolean = false,
    shimmer: Shimmer? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
) {
    val backgroundColor by
    animateColorAsState(
        targetValue = if (isEditing && isExpanded) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            if (isTop) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        }
    )
    val editingOnlyBackgroundColor by animateColorAsState(targetValue = if (isEditing && isExpanded) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
    val textColor by animateColorAsState(
        targetValue = if (isEditing && isExpanded) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            if (isTop) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        }
    )
    androidx.compose.material3.Surface(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomEnd = bottomRadius,
                    bottomStart = bottomRadius
                )
            )
            .fillMaxSize()
            .combinedClickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = LocalIndication.current,
                enabled = true,
                onLongClick = onLongClick,
                onClick = onClick
            ),
        color = if (noBackground) editingOnlyBackgroundColor else backgroundColor,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
//            .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .shimmer(shimmer)
                        .background(MaterialTheme.colorScheme.secondary)
                )
            } else {
                Crossfade(targetState = isSelected) {
                    if (it) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                modifier = Modifier.align(Alignment.Center),
                                imageVector = Icons.Outlined.Done,
                                contentDescription = "selected",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        if (icon != null) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { onAvatarClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                icon()
                            }
                        } else if (contact?.profile?.avatar == null) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                                    .clickable { onAvatarClick() }
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(contact.profile.avatar)
                                    .crossfade(true)
                                    .diskCachePolicy(CachePolicy.ENABLED)// it's the same even removing comments
                                    .build(),
                                contentDescription = "avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable { onAvatarClick() }
                            )
//                            Image(
//                                modifier = Modifier
//                                    .clip(RoundedCornerShape(24.dp))
//                                    .size(48.dp)
//                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
//                                    .clickable { onAvatarClick() },
//                                bitmap = contact.profile.avatar,
//                                contentDescription = "avatar"
//                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                if (isLoading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp, 18.dp)
                            .shimmer(shimmer)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                } else {
                    Text(
                        text = title ?: contact?.profile?.name ?: "会话名称",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (noBackground) MaterialTheme.colorScheme.onSurface else textColor,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (isLoading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(112.dp, 10.dp)
                            .shimmer(shimmer)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                } else {
                    Text(
                        text = buildAnnotatedString {
                            if (subTitle.isNullOrEmpty()) {
                                if (contact?.profile?.name != contact?.latestMessage?.sender) {
                                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                        append(contact?.latestMessage?.sender ?: "")
                                        append(": ")
                                    }
                                }
                                withStyle(style = SpanStyle(color = if (noBackground) MaterialTheme.colorScheme.onSurface else textColor)) {
                                    append(subTitle ?: contact?.latestMessage?.content ?: "")
                                }
                            } else {
                                withStyle(style = SpanStyle(color = if (noBackground) MaterialTheme.colorScheme.onSurface else textColor)) {
                                    append(subTitle)
                                }
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (noBackground) MaterialTheme.colorScheme.onSurface else textColor,
                        maxLines = 1
                    )
                }
            }
            if (!isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Top),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = (timestamp ?: contact?.latestMessage?.timestamp)?.toTimeUntilNow()
                            ?: "",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val unreadMessagesNum =
                        unreadMessagesNum ?: contact?.latestMessage?.unreadMessagesNum ?: 0
                    if (unreadMessagesNum != 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) { Text(text = "$unreadMessagesNum") }
//                    Box(
//                        modifier = Modifier
//                            .height(16.dp)
//                            .defaultMinSize(minWidth = 16.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(MaterialTheme.colorScheme.primary)
//                            .padding(horizontal = 4.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "$unreadMessagesNum",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.onPrimary
//                        )
//                    }
                    }
                }
            }
        }
    }
}