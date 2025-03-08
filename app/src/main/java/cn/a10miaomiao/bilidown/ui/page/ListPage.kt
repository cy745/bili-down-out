package cn.a10miaomiao.bilidown.ui.page

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.a10miaomiao.bilidown.common.BiliDownUtils
import cn.a10miaomiao.bilidown.common.datastore.DataStoreKeys
import cn.a10miaomiao.bilidown.common.datastore.rememberDataStorePreferencesFlow
import cn.a10miaomiao.bilidown.common.molecule.collectAction
import cn.a10miaomiao.bilidown.common.molecule.rememberPresenter
import cn.a10miaomiao.bilidown.entity.BiliAppInfo
import cn.a10miaomiao.bilidown.ui.BiliDownScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


data class ListPageState(
    val appList: List<BiliAppInfo>,
)

sealed class ListPageAction {
    object Add : ListPageAction()
}

@Composable
fun ListPagePresenter(
    context: Context,
    action: Flow<ListPageAction>,
): ListPageState {
    var appList by remember { mutableStateOf<List<BiliAppInfo>>(emptyList()) }
    val selectedAppPackageNameSet by rememberDataStorePreferencesFlow(
        context = context,
        key = DataStoreKeys.appPackageNameSet,
        initial = emptySet(),
    ).collectAsState(emptySet())
    LaunchedEffect(selectedAppPackageNameSet) {
        appList = BiliDownUtils.biliAppList.filter {
            selectedAppPackageNameSet.contains(it.packageName)
        }
    }
    action.collectAction {
        when (it) {
            ListPageAction.Add -> {
            }
        }
    }
    return ListPageState(
        appList,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPage(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val (state, channel) = rememberPresenter {
        ListPagePresenter(context, it)
    }
    if (state.appList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 空布局
                Text(text = "还未添加APP信息")
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        navController.navigate(BiliDownScreen.AddApp.route)
                    }
                ) {
                    Text(text = "添加")
                }
            }
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { state.appList.size })
        val contentToShow = remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
        val dividerColor = MaterialTheme.colorScheme.outlineVariant

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AnimatedContent(
                modifier = Modifier.drawWithContent {
                    drawContent()
                    drawLine(
                        color = dividerColor,
                        strokeWidth = 1.dp.toPx(),
                        start = Offset(0f, size.height - 1.dp.toPx() / 2),
                        end = Offset(size.width, size.height - 1.dp.toPx() / 2),
                    )
                },
                targetState = contentToShow.value,
            ) { content ->
                if (content != null) {
                    content.invoke()
                } else {
                    PrimaryTabRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background),
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        containerColor = Color.Transparent,
                        selectedTabIndex = pagerState.currentPage,
                        indicator = {
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .tabIndicatorOffset(
                                        pagerState.currentPage,
                                        matchContentSize = true
                                    ),
                                height = 4.dp,
                                width = Dp.Unspecified,
                            )
                        },
                        divider = {}
                    ) {
                        state.appList.forEachIndexed { index, app ->
                            Tab(
                                text = { Text(text = app.name) },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                            )
                        }
                    }
                }
            }

            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = pagerState,
                userScrollEnabled = contentToShow.value == null
            ) { page ->
                DownloadListPage(
                    navController = navController,
                    packageName = state.appList[page].packageName,
                    onSelectBarVisible = { contentToShow.value = it }
                )
            }
        }
    }
}