package cn.a10miaomiao.bilidown.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectBar(
    modifier: Modifier = Modifier,
    selectedCount: Int = 0,
    onSelectAll: () -> Unit = {},
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {},
    onClear: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier,
            text = "已选择: ",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 36.sp
        )
        Text(
            modifier = Modifier.weight(1f),
            text = "$selectedCount",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 36.sp
        )

        TextButton(
            enabled = selectedCount > 0,
            onClick = onClear,
        ) {
            Text(
                text = "清除",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        TextButton(
            onClick = onSelectAll
        ) {
            Text(
                text = "全选",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        IconButton(
            onClick = onCancel,
            colors = IconButtonDefaults.filledTonalIconButtonColors()
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
            )
        }

        IconButton(
            onClick = onDone,
            colors = IconButtonDefaults.filledIconButtonColors()
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Done",
            )
        }
    }
}

@Preview
@Composable
private fun SelectBarPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                SelectBar(selectedCount = 0)
            }

            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                SelectBar(selectedCount = 2)
            }
        }
    }
}