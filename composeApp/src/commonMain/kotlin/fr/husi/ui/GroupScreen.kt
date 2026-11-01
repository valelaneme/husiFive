package fr.husi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import fr.husi.compose.CapsuleTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ernestoyaquello.dragdropswipelazycolumn.AllowedSwipeDirections
import com.ernestoyaquello.dragdropswipelazycolumn.DragDropSwipeLazyColumn
import com.ernestoyaquello.dragdropswipelazycolumn.DraggableSwipeableItem
import com.ernestoyaquello.dragdropswipelazycolumn.DraggableSwipeableItemScope
import com.ernestoyaquello.dragdropswipelazycolumn.config.DraggableSwipeableItemColors
import com.ernestoyaquello.dragdropswipelazycolumn.state.rememberDragDropSwipeLazyColumnState
import fr.husi.GroupType
import fr.husi.compose.BoxedVerticalScrollbar
import fr.husi.compose.QRCodeDialog
import fr.husi.compose.SheetActionRow
import fr.husi.compose.SheetSectionTitle
import fr.husi.compose.SimpleIconButton
import fr.husi.compose.TextButton
import fr.husi.compose.fadingEdge
import fr.husi.compose.material3.Icon
import fr.husi.compose.material3.Text
import fr.husi.compose.setPlainText
import fr.husi.compose.withNavigation
import fr.husi.database.SagerDatabase
import fr.husi.fmt.toUniversalLink
import fr.husi.ktx.blankAsNull
import fr.husi.ktx.DisplayTime
import fr.husi.ktx.formatBytes
import fr.husi.repository.resolveRepository
import fr.husi.resources.Res
import fr.husi.resources.action_export
import fr.husi.resources.action_export_clipboard
import fr.husi.resources.action_export_file
import fr.husi.resources.arrow_back
import fr.husi.resources.back
import fr.husi.resources.cancel
import fr.husi.resources.clear_profiles
import fr.husi.resources.clear_profiles_message
import fr.husi.resources.confirm
import fr.husi.resources.content_copy
import fr.husi.resources.copy_success
import fr.husi.resources.delete
import fr.husi.resources.drag_indicator
import fr.husi.resources.edit
import fr.husi.resources.file_export
import fr.husi.resources.group_create
import fr.husi.resources.group_status_empty
import fr.husi.resources.group_status_empty_subscription
import fr.husi.resources.group_status_proxies
import fr.husi.resources.group_status_updated_empty
import fr.husi.resources.internal_link
import fr.husi.resources.link
import fr.husi.resources.menu
import fr.husi.resources.menu_group
import fr.husi.resources.mop
import fr.husi.resources.more_vert
import fr.husi.resources.ok
import fr.husi.resources.playlist_add
import fr.husi.resources.qr_code
import fr.husi.resources.removed
import fr.husi.resources.share
import fr.husi.resources.share_qr_nfc
import fr.husi.resources.share_subscription
import fr.husi.resources.subscription_expire
import fr.husi.resources.subscription_last_updated
import fr.husi.resources.subscription_traffic
import fr.husi.resources.subscription_used
import fr.husi.resources.undo
import fr.husi.resources.update
import fr.husi.resources.update_all_subscription
import io.github.oikvpqya.compose.fastscroller.material3.defaultMaterialScrollbarStyle
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.write
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import kotlin.math.abs
import kotlin.time.Instant

@Composable
fun GroupScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    viewModel: GroupScreenViewModel = viewModel { GroupScreenViewModel() },
    onBackPress: () -> Unit,
    openGroupSettings: (Long) -> Unit,
) {
    val snackbar = LocalSnackbarEmitter.current
    DisposableEffect(Unit) {
        onDispose {
            viewModel.commit()
        }
    }

    var showUpdateAll by remember { mutableStateOf(false) }
    var qrDialogData by remember { mutableStateOf<Pair<String, String>?>(null) } // url:name
    var clearGroupConfirm by remember { mutableStateOf<Long?>(null) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.hiddenGroups) {
        if (uiState.hiddenGroups > 0) {
            snackbar.show(
                StringOrRes.PluralsRes(
                    Res.plurals.removed,
                    uiState.hiddenGroups,
                    uiState.hiddenGroups,
                ),
                StringOrRes.Res(Res.string.undo),
            ) { result ->
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undo()
                }
            }
        }
    }

    var groupToExport by remember { mutableStateOf<Long?>(null) }
    val exportProfiles = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault(),
    ) { file ->
        if (file != null && groupToExport != null) {
            viewModel.exportToFile(
                group = groupToExport!!,
                writeContent = { content ->
                    file.write(content.encodeToByteArray())
                },
                showSnackbar = snackbar::show,
            )
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val windowInsets = WindowInsets.safeDrawing
    val hazeState = rememberHazeState()
    val dragDropListState = rememberDragDropSwipeLazyColumnState()

    // 左右滑动阈值：与配置页面手势一致（80dp）
    val density = LocalDensity.current
    val swipeThresholdPx = remember(density) { with(density) { 80.dp.toPx() } }
    var totalDragAmount by remember { mutableFloatStateOf(0f) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            // 全屏横向滑动返回监听
            .pointerInput(swipeThresholdPx) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDragAmount = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDragAmount += dragAmount
                    },
                    onDragEnd = {
                        if (abs(totalDragAmount) >= swipeThresholdPx) {
                            onBackPress()
                        }
                        totalDragAmount = 0f
                    },
                    onDragCancel = { totalDragAmount = 0f },
                )
            },
        topBar = {
            CapsuleTopBar(
                hazeState = hazeState,
                title = { Text(stringResource(Res.string.menu_group)) },
                navigationIcon = {
                    SimpleIconButton(
                        imageVector = vectorResource(Res.drawable.arrow_back),
                        contentDescription = stringResource(Res.string.back),
                        onClick = onBackPress,
                    )
                },
                actions = {
                    CapsuleActionButton {
                        SimpleIconButton(
                            imageVector = vectorResource(Res.drawable.update),
                            contentDescription = stringResource(Res.string.update_all_subscription),
                            onClick = { showUpdateAll = true },
                        )
                    }
                    CapsuleActionButton {
                        SimpleIconButton(
                            imageVector = vectorResource(Res.drawable.playlist_add),
                            contentDescription = stringResource(Res.string.group_create),
                            onClick = {
                                openGroupSettings(0L)
                            },
                        )
                    }
                },
                windowInsets = windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val contentPadding = innerPadding.withNavigation()
        Row(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
        ) {
            DragDropSwipeLazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .fadingEdge(dragDropListState.lazyListState),
                state = dragDropListState,
                items = uiState.groups.toImmutableList(),
                key = { it.group.id },
                contentType = { 0 },
                userScrollEnabled = true,
                contentPadding = contentPadding,
                onIndicesChangedViaDragAndDrop = { viewModel.submitReorder(it) },
            ) { _, groupState ->
                DraggableSwipeableItem(
                    modifier = Modifier.animateDraggableSwipeableItem(),
                    colors = DraggableSwipeableItemColors.createRemembered(
                        containerBackgroundColor = Color.Transparent,
                        containerBackgroundColorWhileDragged = Color.Transparent,
                    ),
                    // 禁用卡片内部的划出删除，手势交由外层 Scaffold 统一返回
                    allowedSwipeDirections = AllowedSwipeDirections.None,
                    onSwipeDismiss = {},
                ) {
                    GroupCard(
                        mainViewModel = mainViewModel,
                        state = groupState,
                        openGroupSettings = openGroupSettings,
                        snackbar = { message ->
                            snackbar.show(StringOrRes.Direct(message))
                        },
                        showQRDialog = { url, name ->
                            qrDialogData = url to name
                        },
                        showClearGroupDialog = {
                            clearGroupConfirm = groupState.group.id
                        },
                        exportToFile = {
                            groupToExport = groupState.group.id
                            exportProfiles.launch(
                                suggestedName = "profiles_${groupState.group.displayName()}",
                                defaultExtension = "txt",
                            )
                        },
                    )
                }
            }

            BoxedVerticalScrollbar(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = dragDropListState.lazyListState),
                style = defaultMaterialScrollbarStyle().copy(
                    thickness = 3.dp,
                ),
            )
        }
    }

    if (showUpdateAll) AlertDialog(
        onDismissRequest = { showUpdateAll = false },
        confirmButton = {
            TextButton(stringResource(Res.string.ok)) {
                mainViewModel.updateAllSubscriptionGroups()
                showUpdateAll = false
            }
        },
        dismissButton = {
            TextButton(stringResource(Res.string.cancel)) {
                showUpdateAll = false
            }
        },
        icon = { Icon(vectorResource(Res.drawable.update), null) },
        title = { Text(stringResource(Res.string.confirm)) },
        text = { Text(stringResource(Res.string.update_all_subscription)) },
    )

    qrDialogData?.let { (url, name) ->
        QRCodeDialog(
            url = url,
            name = name,
            onDismiss = { qrDialogData = null },
            showSnackbar = { message ->
                snackbar.show(StringOrRes.Direct(message))
            },
        )
    }

    clearGroupConfirm?.let { id ->
        AlertDialog(
            onDismissRequest = { clearGroupConfirm = null },
            confirmButton = {
                TextButton(stringResource(Res.string.ok)) {
                    viewModel.clearGroup(id)
                    clearGroupConfirm = null
                }
            },
            dismissButton = {
                TextButton(stringResource(Res.string.cancel)) {
                    clearGroupConfirm = null
                }
            },
            icon = { Icon(vectorResource(Res.drawable.mop), null) },
            title = { Text(stringResource(Res.string.confirm)) },
            text = { Text(stringResource(Res.string.clear_profiles_message)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DraggableSwipeableItemScope<GroupItemUiState>.GroupCard(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    state: GroupItemUiState,
    openGroupSettings: (Long) -> Unit,
    snackbar: suspend (message: String) -> Unit,
    showQRDialog: (url: String, name: String) -> Unit,
    showClearGroupDialog: () -> Unit,
    exportToFile: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val group = state.group

    var showOptionsSheet by remember { mutableStateOf(false) }
    val optionsSheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    // 解除 48dp 最小触摸占位限制，统一保持在 40.dp
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 3.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // 左侧拖动手柄：保持 40.dp
                    Icon(
                        imageVector = vectorResource(Res.drawable.drag_indicator),
                        contentDescription = "Drag to reorder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp)
                            .dragDropModifier(),
                    )

                    // 中间主体展示区
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        // 第一行：[标题 + 配置数量] --- [编辑 + 更新 + 更多]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = group.displayName(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                )

                                val countText = when (state.group.type) {
                                    GroupType.BASIC -> {
                                        if (state.counts == 0L) {
                                            stringResource(Res.string.group_status_empty)
                                        } else {
                                            pluralStringResource(
                                                Res.plurals.group_status_proxies,
                                                state.counts.toInt(),
                                                state.counts,
                                            )
                                        }
                                    }
                                    GroupType.SUBSCRIPTION -> {
                                        if (state.counts == 0L) {
                                            if (state.group.subscription!!.lastUpdated > 0) {
                                                stringResource(Res.string.group_status_updated_empty)
                                            } else {
                                                stringResource(Res.string.group_status_empty_subscription)
                                            }
                                        } else {
                                            pluralStringResource(
                                                Res.plurals.group_status_proxies,
                                                state.counts.toInt(),
                                                state.counts,
                                            )
                                        }
                                    }
                                    else -> stringResource(Res.string.group_status_empty)
                                }

                                Text(
                                    text = countText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            // 右侧按钮排布：编辑 / 更新（仅订阅） / 更多
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                            ) {
                                if (!group.ungrouped) {
                                    SimpleIconButton(
                                        modifier = Modifier.size(40.dp),
                                        imageVector = vectorResource(Res.drawable.edit),
                                        contentDescription = stringResource(Res.string.edit),
                                        onClick = {
                                            openGroupSettings(group.id)
                                        },
                                    )
                                }

                                if (group.type == GroupType.SUBSCRIPTION) {
                                    SimpleIconButton(
                                        modifier = Modifier.size(40.dp),
                                        imageVector = vectorResource(Res.drawable.update),
                                        contentDescription = stringResource(Res.string.update),
                                        onClick = {
                                            if (!state.isUpdating) {
                                                mainViewModel.updateSubscriptionGroup(group)
                                            }
                                        },
                                    )
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    SimpleIconButton(
                                        modifier = Modifier.size(40.dp),
                                        imageVector = vectorResource(Res.drawable.more_vert),
                                        contentDescription = stringResource(Res.string.menu),
                                        onClick = { showOptionsSheet = true },
                                    )

                                    if (showOptionsSheet) {
                                        ModalBottomSheet(
                                            onDismissRequest = { showOptionsSheet = false },
                                            sheetState = optionsSheetState,
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                val subscriptionLink =
                                                    group.subscription?.link?.blankAsNull()

                                                subscriptionLink?.let { link ->
                                                    SheetSectionTitle(
                                                        text = stringResource(Res.string.share_subscription),
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = vectorResource(Res.drawable.share),
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            )
                                                        },
                                                    )
                                                    SheetActionRow(
                                                        text = stringResource(Res.string.action_export_clipboard),
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = vectorResource(Res.drawable.content_copy),
                                                                contentDescription = null,
                                                            )
                                                        },
                                                        onClick = {
                                                            scope.launch {
                                                                clipboard.setPlainText(link)
                                                                snackbar(
                                                                    resolveRepository().getString(
                                                                        Res.string.copy_success,
                                                                    ),
                                                                )
                                                            }
                                                            showOptionsSheet = false
                                                        },
                                                    )
                                                    SheetActionRow(
                                                        text = stringResource(Res.string.share_qr_nfc),
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = vectorResource(Res.drawable.qr_code),
                                                                contentDescription = null,
                                                            )
                                                        },
                                                        onClick = {
                                                            showQRDialog(link, group.displayName())
                                                            showOptionsSheet = false
                                                        },
                                                    )
                                                }
                                                if (group.subscription != null) {
                                                    HorizontalDivider()
                                                    SheetSectionTitle(
                                                        text = stringResource(Res.string.internal_link),
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = vectorResource(Res.drawable.link),
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            )
                                                        },
                                                    )
                                                    SheetActionRow(
                                                        text = stringResource(Res.string.action_export_clipboard),
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = vectorResource(Res.drawable.content_copy),
                                                                contentDescription = null,
                                                            )
                                                        },
                                                        onClick = {
                                                            scope.launch {
                                                                clipboard.setPlainText(group.toUniversalLink())
                                                                snackbar(
                                                                    resolveRepository().getString(
                                                                        Res.string.copy_success,
                                                                    ),
                                                                )
                                                            }
                                                            showOptionsSheet = false
                                                        },
                                                    )
                                                    SheetActionRow(
                                                        text = stringResource(Res.string.share_qr_nfc),
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = vectorResource(Res.drawable.qr_code),
                                                                contentDescription = null,
                                                            )
                                                        },
                                                        onClick = {
                                                            showQRDialog(
                                                                group.toUniversalLink(),
                                                                group.displayName(),
                                                            )
                                                            showOptionsSheet = false
                                                        },
                                                    )
                                                }
                                                HorizontalDivider()
                                                SheetSectionTitle(
                                                    text = stringResource(Res.string.action_export),
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = vectorResource(Res.drawable.file_export),
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                    },
                                                )
                                                SheetActionRow(
                                                    text = stringResource(Res.string.action_export_clipboard),
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = vectorResource(Res.drawable.content_copy),
                                                            contentDescription = null,
                                                        )
                                                    },
                                                    onClick = {
                                                        scope.launch {
                                                            val links = withContext(Dispatchers.IO) {
                                                                SagerDatabase.proxyDao
                                                                    .getByGroup(group.id)
                                                                    .first()
                                                            }.joinToString("\n") {
                                                                it.toStdLink()
                                                            }
                                                            clipboard.setPlainText(links)
                                                            snackbar(resolveRepository().getString(Res.string.copy_success))
                                                        }
                                                        showOptionsSheet = false
                                                    },
                                                )
                                                SheetActionRow(
                                                    text = stringResource(Res.string.action_export_file),
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = vectorResource(Res.drawable.file_export),
                                                            contentDescription = null,
                                                        )
                                                    },
                                                    onClick = {
                                                        exportToFile()
                                                        showOptionsSheet = false
                                                    },
                                                )
                                                HorizontalDivider()
                                                SheetActionRow(
                                                    text = stringResource(Res.string.clear_profiles),
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = vectorResource(Res.drawable.delete),
                                                            contentDescription = null,
                                                        )
                                                    },
                                                    textColor = MaterialTheme.colorScheme.error,
                                                    iconTint = MaterialTheme.colorScheme.error,
                                                    onClick = {
                                                        showOptionsSheet = false
                                                        showClearGroupDialog()
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 第二行：订阅分组展示 [左: 过期时间] - [中: 已用 / 剩余] - [右: 更新时间]
                        if (group.type == GroupType.SUBSCRIPTION) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val subscription = group.subscription

                                // 左侧：过期时间
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                    if (subscription != null && subscription.expiryDate > 0) {
                                        Text(
                                            text = stringResource(
                                                Res.string.subscription_expire,
                                                DisplayTime.date(Instant.fromEpochSeconds(subscription.expiryDate)),
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                        )
                                    }
                                }

                                // 中间：已用 / 剩余 流量
                                Box(contentAlignment = Alignment.Center) {
                                    if (subscription != null && (subscription.bytesUsed > 0L || subscription.bytesRemaining > 0L)) {
                                        val trafficText = if (subscription.bytesRemaining > 0L) {
                                            "${subscription.bytesUsed.formatBytes()} / ${subscription.bytesRemaining.formatBytes()}"
                                        } else {
                                            stringResource(
                                                Res.string.subscription_used,
                                                subscription.bytesUsed.formatBytes(),
                                            )
                                        }
                                        Text(
                                            text = trafficText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                        )
                                    }
                                }

                                // 右侧：更新时间
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                                    if (state.group.subscription!!.lastUpdated > 0) {
                                        Text(
                                            text = stringResource(
                                                Res.string.subscription_last_updated,
                                                DisplayTime.dateTime(
                                                    Instant.fromEpochSeconds(
                                                        state.group.subscription!!.lastUpdated.toLong(),
                                                    ),
                                                ),
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        } else {
                            // 基本分组空行占位（高度与 bodySmall 保持一致）
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // 3. 卡片最底端条形：更新状态显示更新进度；未更新时若订阅有流量信息则展示剩余比例进度条；无流量/基本分组则展示 4dp 装饰底轨
                val subscription = group.subscription
                val totalBytes = (subscription?.bytesUsed ?: 0L) + (subscription?.bytesRemaining ?: 0L)

                if (state.isUpdating) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(4.dp))
                } else if (state.updateProgress != null) {
                    LinearProgressIndicator(
                        progress = { state.updateProgress.progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                    )
                } else if (group.type == GroupType.SUBSCRIPTION && totalBytes > 0L) {
                    val remainingProgress = (subscription!!.bytesRemaining.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { remainingProgress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    )
                }
            }
        }
    }
}
