package cn.a10miaomiao.bilidown.ui.page

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import cn.a10miaomiao.bilidown.R
import cn.a10miaomiao.bilidown.common.BiliDownFile
import cn.a10miaomiao.bilidown.common.MiaoLog
import cn.a10miaomiao.bilidown.common.lifecycle.LaunchedLifecycleObserver
import cn.a10miaomiao.bilidown.common.localStoragePermission
import cn.a10miaomiao.bilidown.common.molecule.collectAction
import cn.a10miaomiao.bilidown.common.molecule.rememberPresenter
import cn.a10miaomiao.bilidown.entity.DownloadInfo
import cn.a10miaomiao.bilidown.entity.DownloadItemInfo
import cn.a10miaomiao.bilidown.entity.DownloadType
import cn.a10miaomiao.bilidown.shizuku.localShizukuPermission
import cn.a10miaomiao.bilidown.ui.BiliDownScreen
import cn.a10miaomiao.bilidown.ui.components.BatchExportConfirmDialog
import cn.a10miaomiao.bilidown.ui.components.DownloadListItem
import cn.a10miaomiao.bilidown.ui.components.PermissionDialog
import cn.a10miaomiao.bilidown.ui.components.SelectBar
import cn.a10miaomiao.bilidown.ui.components.SwipeToRefresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.navigation.BackHandler
import rikka.shizuku.ShizukuProvider


data class DownloadListPageState(
    val list: List<DownloadInfo>,
    val path: String,
    val canRead: Boolean,
    val loading: Boolean,
    val refreshing: Boolean,
    val failMessage: String,

    val isSelecting: Boolean,
    val selectItems: List<DownloadInfo>,
    val confirmingBatchExport: Boolean
)

sealed class DownloadListPageAction {
    class GetList(
        val packageName: String,
        val enabledShizuku: Boolean,
    ) : DownloadListPageAction()

    class RefreshList(
        val packageName: String,
        val enabledShizuku: Boolean,
    ) : DownloadListPageAction()

    class SelectItem(val item: DownloadInfo) : DownloadListPageAction()
    data object SelectAllItem : DownloadListPageAction()
    data object ClearSelectItem : DownloadListPageAction()
    data object ExitSelecting : DownloadListPageAction()
    data object ShowBatchConfirm : DownloadListPageAction()
    data object DismissBatchConfirm : DownloadListPageAction()
}

@Composable
fun DownloadListPagePresenter(
    context: Context,
    action: Flow<DownloadListPageAction>,
): DownloadListPageState {
    var list by remember {
        mutableStateOf(emptyList<DownloadInfo>())
    }
    var path by remember {
        mutableStateOf("")
    }
    var canRead by remember {
        mutableStateOf(true)
    }
    var failMessage by remember {
        mutableStateOf("")
    }
    var loading by remember {
        mutableStateOf(true)
    }
    var refreshing by remember {
        mutableStateOf(false)
    }
    var isSelecting by remember {
        mutableStateOf(false)
    }
    var selectItems by remember {
        mutableStateOf(emptyList<DownloadInfo>())
    }
    var confirmingBatchExport by remember {
        mutableStateOf(false)
    }

    suspend fun getList(
        packageName: String,
        enabledShizuku: Boolean,
    ) {
        try {
            MiaoLog.debug { "getList(packageName:$packageName, enabledShizuku: $enabledShizuku)" }
            val biliDownFile = BiliDownFile(context, packageName, enabledShizuku)
            canRead = biliDownFile.canRead()
            if (!canRead) {
                return
            }
            loading = true
            failMessage = ""
            val newList = mutableListOf<DownloadInfo>()
            biliDownFile.readDownloadList().forEach {
                val biliEntry = it.entry
                var indexTitle = ""
                var itemTitle = ""
                var id = biliEntry.avid ?: 0L
                var cid = 0L
                var epid = 0L
                var type = DownloadType.VIDEO
                val page = biliEntry.page_data
                if (page != null) {
                    id = biliEntry.avid!!
                    indexTitle = page.download_title ?: page.part ?: "${page.page}P"
                    cid = page.cid
                    type = DownloadType.VIDEO
                    itemTitle = biliEntry.title
                }
                val ep = biliEntry.ep
                val source = biliEntry.source
                if (ep != null && source != null) {
                    id = biliEntry.season_id!!.toLong()
                    indexTitle = ep.index_title
                    epid = ep.episode_id
                    cid = source.cid
                    type = DownloadType.BANGUMI
                    itemTitle = if (ep.index_title.isNotBlank()) {
                        ep.index_title
                    } else {
                        ep.index
                    }
                }
                val item = DownloadItemInfo(
                    dir_path = it.entryDirPath,
                    media_type = biliEntry.media_type,
                    has_dash_audio = biliEntry.has_dash_audio,
                    is_completed = biliEntry.is_completed,
                    total_bytes = biliEntry.total_bytes,
                    downloaded_bytes = biliEntry.downloaded_bytes,
                    title = itemTitle,
                    cover = biliEntry.cover,
                    id = id,
                    type = type,
                    cid = cid,
                    epid = epid,
                    index_title = indexTitle,
                )
                val last = newList.lastOrNull()
                if (last != null
                    && last.type == item.type
                    && last.id == item.id
                ) {
                    if (last.is_completed && !item.is_completed) {
                        last.is_completed = false
                    }
                    last.items.add(item)
                } else {
                    newList.add(
                        DownloadInfo(
                            dir_path = it.pageDirPath,
                            media_type = biliEntry.media_type,
                            has_dash_audio = biliEntry.has_dash_audio,
                            is_completed = biliEntry.is_completed,
                            total_bytes = biliEntry.total_bytes,
                            downloaded_bytes = biliEntry.downloaded_bytes,
                            title = biliEntry.title,
                            cover = biliEntry.cover,
                            cid = cid,
                            id = id,
                            type = type,
                            items = mutableListOf(item)
                        )
                    )
                }
            }
            list = newList.toList()
        } catch (e: TimeoutCancellationException) {
            e.printStackTrace()
            failMessage = if (enabledShizuku) {
                "连接Shizuku服务超时，建议您尝试停止并重新激活Shizuku！"
            } else {
                "读取缓存列表超时！"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            failMessage = "读取列表异常：" + (e.message ?: e.toString())
        } finally {
            loading = false
        }
    }

    action.collectAction {
        when (it) {
            is DownloadListPageAction.RefreshList -> {
                refreshing = true
                withContext(Dispatchers.IO) {
                    getList(it.packageName, it.enabledShizuku)
                }
                refreshing = false
            }

            is DownloadListPageAction.GetList -> {
                withContext(Dispatchers.IO) {
                    getList(it.packageName, it.enabledShizuku)
                }
            }

            is DownloadListPageAction.SelectItem -> {
                if (selectItems.contains(it.item)) {
                    selectItems -= it.item
                } else {
                    if (!isSelecting) {
                        isSelecting = true
                    }
                    selectItems += it.item
                }
            }

            is DownloadListPageAction.ClearSelectItem -> {
                selectItems = emptyList()
            }

            is DownloadListPageAction.SelectAllItem -> {
                if (!isSelecting) {
                    isSelecting = true
                }
                selectItems = list
            }

            is DownloadListPageAction.ExitSelecting -> {
                isSelecting = false
                selectItems = emptyList()
            }

            is DownloadListPageAction.ShowBatchConfirm -> {
                if (selectItems.isEmpty()) {
                    return@collectAction
                }
                confirmingBatchExport = true
            }

            is DownloadListPageAction.DismissBatchConfirm -> {
                confirmingBatchExport = false
            }
        }
    }
    return DownloadListPageState(
        list,
        path,
        canRead,
        loading,
        refreshing,
        failMessage,
        isSelecting,
        selectItems,
        confirmingBatchExport
    )
}

@Composable
fun DownloadListPage(
    navController: NavHostController,
    packageName: String,
    onSelectBarVisible: ((@Composable () -> Unit)?) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    val storagePermission = localStoragePermission()
    val permissionState = storagePermission.collectState()
    val shizukuPermission = localShizukuPermission()
    val shizukuPermissionState by shizukuPermission.collectState()

    val (state, channel) = rememberPresenter(listOf(packageName, permissionState)) {
        DownloadListPagePresenter(context, it)
    }

    val selectItems = rememberUpdatedState(state.selectItems)

    LaunchedEffect(state.isSelecting) {
        if (state.isSelecting) {
            onSelectBarVisible {
                SelectBar(
                    selectedCount = selectItems.value.size,
                    onSelectAll = { channel.trySend(DownloadListPageAction.SelectAllItem) },
                    onClear = { channel.trySend(DownloadListPageAction.ClearSelectItem) },
                    onCancel = { channel.trySend(DownloadListPageAction.ExitSelecting) },
                    onDone = { channel.trySend(DownloadListPageAction.ShowBatchConfirm) }
                )
            }
        } else {
            onSelectBarVisible(null)
        }
    }

    BackHandler(enabled = state.isSelecting) {
        channel.trySend(DownloadListPageAction.ExitSelecting)
    }

    BatchExportConfirmDialog(
        visible = state.confirmingBatchExport,
        count = selectItems.value.size,
        onConfirm = {
            channel.trySend(DownloadListPageAction.DismissBatchConfirm)
            channel.trySend(DownloadListPageAction.ExitSelecting)
        },
        onDismiss = {
            channel.trySend(DownloadListPageAction.DismissBatchConfirm)
        }
    )

    LaunchedEffect(
        packageName,
        permissionState.isGranted,
        permissionState.isExternalStorage,
        shizukuPermissionState.isRunning,
        shizukuPermissionState.isEnabled,
    ) {
        if (state.list.isEmpty()
            && permissionState.isGranted
            && permissionState.isExternalStorage
        ) {
            channel.send(
                DownloadListPageAction.GetList(
                    packageName = packageName,
                    shizukuPermissionState.isEnabled,
                )
            )
        }
    }

    LaunchedLifecycleObserver(
        onResume = {
            if (state.list.isEmpty()) {
                channel.trySend(
                    DownloadListPageAction.GetList(
                        packageName = packageName,
                        shizukuPermissionState.isEnabled,
                    )
                )
            }
        }
    )

    fun resultCallBack() {
        if (!permissionState.isGranted || !permissionState.isExternalStorage) {
            showPermissionDialog = true
        }
    }

    fun openShizuku() {
        try {
            val packageManager = context.packageManager
            val intent =
                packageManager.getLaunchIntentForPackage(ShizukuProvider.MANAGER_APPLICATION_ID)
            if (intent == null) {
                Toast.makeText(context, "未找到Shizuku", Toast.LENGTH_LONG)
                    .show()
            } else {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Shizuku启动失败", Toast.LENGTH_LONG)
                .show()
            e.printStackTrace()
        }
    }

    PermissionDialog(
        showPermissionDialog = showPermissionDialog,
        isGranted = permissionState.isGranted,
        onDismiss = { showPermissionDialog = false }
    )
    SwipeToRefresh(
        refreshing = state.refreshing,
        onRefresh = {
            channel.trySend(
                DownloadListPageAction.RefreshList(
                    packageName = packageName,
                    enabledShizuku = shizukuPermissionState.isEnabled,
                )
            )
        },
    ) {
        if (state.list.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 4.dp,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "正在读取列表",
                        color = MaterialTheme.colorScheme.outline,
                    )
                } else if (!permissionState.isGranted || !permissionState.isExternalStorage) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (permissionState.isGranted) {
                            Text(text = "请授予所有文件的存储权限")
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    storagePermission.requestPermissions(::resultCallBack)
                                }
                            ) {
                                Text(text = "授予所有文件的权限")
                            }
                        } else {
                            Text(text = "请授予存储权限")
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    storagePermission.requestPermissions(::resultCallBack)
                                }
                            ) {
                                Text(text = "授予权限")
                            }
                        }
                    }
                } else if (!state.canRead) {
                    Text(text = "请授予文件夹权限")
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val biliDownFile =
                                BiliDownFile(context, packageName, shizukuPermissionState.isEnabled)
                            biliDownFile.startFor(2)
                        }
                    ) {
                        Text(text = "授予权限")
                    }
                    TextButton(
                        onClick = {
                            navController.navigate(BiliDownScreen.More.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) {
                        Text(text = "或使用Shizuku")
                    }
                } else if (state.failMessage.isNotBlank()) {
                    Text(
                        text = state.failMessage,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp)
                    )
                    if ("Shizuku" in state.failMessage) {
                        TextButton(
                            onClick = ::openShizuku,
                        ) {
                            Text(text = "跳转Shizuku")
                        }
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_movie_pay_area_limit),
                        contentDescription = "空空如也",
                        modifier = Modifier.size(200.dp, 200.dp)
                    )
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = "空空如也",
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
            ) {
                items(state.list, { it.dir_path }) {
                    DownloadListItem(
                        item = it,
                        isSelected = state.selectItems.contains(it),
                        onClick = {
                            if (!state.isSelecting) {
                                val dirPath = Uri.encode(it.dir_path)
                                navController.navigate(
                                    BiliDownScreen.Detail.route + "?packageName=${packageName}&dirPath=${dirPath}"
                                )
                            } else {
                                channel.trySend(DownloadListPageAction.SelectItem(it))
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            channel.trySend(DownloadListPageAction.SelectItem(it))
                        }
                    )
                }
            }
        }
    }
}