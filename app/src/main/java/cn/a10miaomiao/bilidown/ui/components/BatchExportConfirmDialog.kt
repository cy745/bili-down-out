package cn.a10miaomiao.bilidown.ui.components


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BatchExportConfirmDialog(
    visible: Boolean = false,
    count: Int = 0,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("导出${count}个视频")
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors()
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}

@Preview
@Composable
private fun BatchExportConfirmDialogPreview() {
    BatchExportConfirmDialog()
}