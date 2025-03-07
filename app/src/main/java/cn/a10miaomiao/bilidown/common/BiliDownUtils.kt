package cn.a10miaomiao.bilidown.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import cn.a10miaomiao.bilidown.R
import cn.a10miaomiao.bilidown.entity.BiliAppInfo


object BiliDownUtils {

    val biliAppList = listOf(
        BiliAppInfo(
            "哔哩哔哩",
            "tv.danmaku.bili",
            icon = R.drawable.ic_bilibili
        ),
        BiliAppInfo(
            "哔哩哔哩(概念版)",
            "com.bilibili.app.blue",
            icon = R.drawable.ic_bilibili_blue
        ),
        BiliAppInfo(
            "哔哩哔哩(谷歌版)",
            "com.bilibili.app.in",
            icon = R.drawable.ic_bilibili
        ),
        BiliAppInfo(
            "bilimiao",
            "com.a10miaomiao.bilimiao",
            icon = R.drawable.ic_bilimiao
        ),
//        BiliAppInfo(
//            "bilimiao-dev",
//            "cn.a10miaomiao.bilimiao.dev",
//            icon = R.drawable.ic_bilimiao
//        ),
    )

    fun checkSelfPermission(
        context: Context,
        packageName: String,
    ): Boolean {
        if (Build.VERSION.SDK_INT < 23) {  // 5.0
            return true
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) { // 6.0-10.0
            val f = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            return f == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    /**
     * 参考BiliSync的filenamify文件名处理逻辑
     */
    fun filenamify(input: String): String {
        // 匹配 Windows 非法字符和控制字符
        val reserved = Regex("[<>:\"/\\\\|?*\\x00-\\x1F\\x7F\\x80-\\x9F]+")
        // 匹配 Windows 保留设备名称（不区分大小写）
        val windowsReserved = Regex("^(con|prn|aux|nul|com\\d|lpt\\d)$", RegexOption.IGNORE_CASE)
        // 匹配开头或结尾的连续句点
        val outerPeriods = Regex("^\\.+|\\.+$")

        // 分步处理字符串
        var result = reserved.replace(input, "_")
        result = outerPeriods.replace(result, "_")

        // 处理 Windows 保留名称
        if (windowsReserved.matches(result)) {
            result += "_"
        }

        return result.trim()
            .replace(' ', '_')
    }
}