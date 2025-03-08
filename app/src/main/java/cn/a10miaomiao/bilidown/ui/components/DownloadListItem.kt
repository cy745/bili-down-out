package cn.a10miaomiao.bilidown.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilidown.common.UrlUtil
import cn.a10miaomiao.bilidown.entity.DownloadInfo
import cn.a10miaomiao.bilidown.entity.DownloadType
import coil.compose.AsyncImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadListItem(
    item: DownloadInfo,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.padding(5.dp),
    ) {
        val bgColor = animateColorAsState(
            if (isSelected) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.secondaryContainer,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
        val textColor = animateColorAsState(
            if (isSelected) MaterialTheme.colorScheme.onSecondary
            else MaterialTheme.colorScheme.onSecondaryContainer,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = bgColor.value
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                        .padding(10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = UrlUtil.autoHttps(item.cover) + "@672w_378h_1c_",
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 120.dp, height = 80.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .padding(horizontal = 10.dp),
                    ) {
                        Text(
                            text = item.title,
                            maxLines = 2,
                            modifier = Modifier.weight(1f),
                            overflow = TextOverflow.Ellipsis,
                            color = textColor.value
                        )

                        val status = remember(item) {
                            val status = if (item.is_completed) {
                                "已完成下载"
                            } else {
                                "暂停中"
                            }
                            "${item.items.size}个视频 • $status"
                        }

                        Text(
                            text = status,
                            maxLines = 1,
                            color = textColor.value.copy(0.2f),
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DownloadListItemPreview() {
    DownloadListItem(
        DownloadInfo(
            "", 1,
            has_dash_audio = true,
            is_completed = true,
            total_bytes = 0L,
            downloaded_bytes = 0L,
            title = "标题",
            cover = "",
            id = 0L,
            cid = 0L,
            type = DownloadType.VIDEO,
            items = mutableListOf()
        ),
        isSelected = false,
        onClick = {}
    )
}

@Preview
@Composable
private fun DownloadListItemSelectedPreview() {
    DownloadListItem(
        DownloadInfo(
            "", 1,
            has_dash_audio = true,
            is_completed = true,
            total_bytes = 0L,
            downloaded_bytes = 0L,
            title = "标题",
            cover = "",
            id = 0L,
            cid = 0L,
            type = DownloadType.VIDEO,
            items = mutableListOf()
        ),
        isSelected = true,
        onClick = {}
    )
}