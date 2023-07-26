package com.ojhdtapp.parabox.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import me.saket.swipe.rememberSwipeableActionsState
import kotlin.math.abs

@Composable
fun SwipeableActionsDismissBox(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    threshold: Dp,
    onReachThreshold: () -> Unit,
    startToEndIcon: ImageVector,
    endToStartIcon: ImageVector,
    onDismissedToEnd: () -> Unit,
    onDismissedToStart: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val swipeableActionsState = rememberSwipeableActionsState()
    var isDismissedToStart by remember {
        mutableStateOf(false)
    }
    var isDismissedToEnd by remember {
        mutableStateOf(false)
    }
    val reachThreshold by remember {
        derivedStateOf {
            abs(swipeableActionsState.offset.value) > with(density) { threshold.toPx() }
        }
    }
    LaunchedEffect(reachThreshold) {
        if (reachThreshold) {
            onReachThreshold()
        }
    }
    val scale by animateFloatAsState(
        if (reachThreshold) 1f else 0.75f
    )
    val startToEnd = SwipeAction(
        icon = {
            Icon(
                imageVector = startToEndIcon, contentDescription = "end",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .scale(scale),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        },
        background = MaterialTheme.colorScheme.primary,
        onSwipe = {
            isDismissedToEnd = true
            coroutineScope.launch {
                delay(1000)
                onDismissedToEnd()
            }
        }
    )
    val endToStart = SwipeAction(
        icon = {
            Icon(
                imageVector = endToStartIcon, contentDescription = "start",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .scale(scale),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        },
        background = MaterialTheme.colorScheme.primary,
        onSwipe = {
            isDismissedToStart = true
            coroutineScope.launch {
                delay(1000)
                onDismissedToStart()
            }
        }
    )

    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = !isDismissedToStart,
        exit = slideOutHorizontally { -it },
        enter = expandVertically()
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = !isDismissedToEnd,
            exit = slideOutHorizontally { it }, enter = expandVertically()
        ) {
            SwipeableActionsBox(
                state = swipeableActionsState,
                startActions = if (enabled) listOf(startToEnd) else emptyList(),
                endActions = if (enabled) listOf(endToStart) else emptyList(),
                swipeThreshold = threshold,
                backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.secondary
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissBox(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    startToEndIcon: ImageVector? = null,
    endToStartIcon: ImageVector? = null,
    onDismissedToEnd: () -> Boolean,
    onDismissedToStart: () -> Boolean,
    onVibrate: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmValueChange = {
            when (it) {
                DismissValue.DismissedToEnd -> {
                    onDismissedToEnd()
                }

                DismissValue.DismissedToStart -> {
                    onDismissedToStart()
                }

                else -> false
            }
        },
        positionalThreshold = { distance -> distance * .2f }
    )
    LaunchedEffect(key1 = dismissState.targetValue == DismissValue.Default) {
        if (dismissState.progress != 0f && dismissState.progress != 1f)
            onVibrate()
    }
    LaunchedEffect(key1 = Unit) {
        dismissState.reset()
    }
    val isDismissed = dismissState.isDismissed(DismissDirection.EndToStart)
            || dismissState.isDismissed(DismissDirection.StartToEnd)
    val isDismissedToStart = dismissState.isDismissed(DismissDirection.EndToStart)
    val isDismissedToEnd = dismissState.isDismissed(DismissDirection.StartToEnd)

    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = !isDismissedToStart,
        exit = slideOutHorizontally { -it },
        enter = expandVertically()
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = !isDismissedToEnd,
            exit = slideOutHorizontally { it }, enter = expandVertically()
        ) {
            SwipeToDismiss(
                state = dismissState,
                background = {
                    val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                    val color by animateColorAsState(
                        when (dismissState.targetValue) {
                            DismissValue.Default -> MaterialTheme.colorScheme.secondary
                            DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.primary
                            DismissValue.DismissedToStart -> MaterialTheme.colorScheme.primary
                        }
                    )
                    val textColor by animateColorAsState(
                        when (dismissState.targetValue) {
                            DismissValue.Default -> MaterialTheme.colorScheme.onSecondary
                            DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.onPrimary
                            DismissValue.DismissedToStart -> MaterialTheme.colorScheme.onPrimary
                        }
                    )
                    val alignment = when (direction) {
                        DismissDirection.StartToEnd -> Alignment.CenterStart
                        DismissDirection.EndToStart -> Alignment.CenterEnd
                    }
                    val icon = when (direction) {
                        DismissDirection.StartToEnd -> startToEndIcon ?: Icons.Outlined.Done
                        DismissDirection.EndToStart -> endToStartIcon ?: Icons.Outlined.Done
                    }
                    val scale by animateFloatAsState(
                        if (dismissState.targetValue == DismissValue.Default)
                            0.75f else 1f
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = alignment
                    ) {
                        Icon(
                            icon,
                            contentDescription = "Localized description",
                            modifier = Modifier.scale(scale),
                            tint = textColor
                        )
                    }
                },
                directions = buildSet {
                    if (!enabled) return@buildSet
                    if (startToEndIcon != null) add(DismissDirection.StartToEnd)
                    if (endToStartIcon != null) add(DismissDirection.EndToStart)
                },
                dismissContent = {
                    content()
                }
            )
        }
    }
}