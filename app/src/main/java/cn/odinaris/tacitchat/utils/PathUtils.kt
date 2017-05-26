package cn.odinaris.tacitchat.utils

import android.content.Context
import android.os.Environment

import java.io.File

object PathUtils {

    var ctx: Context? = null

    private val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    private fun availableCacheDir(): File {
            if (isExternalStorageWritable) {
                return ctx?.externalCacheDir as File
            } else {
                return ctx?.cacheDir as File
            }
        }

    fun checkAndMkdirs(dir: String): String {
        val file = File(dir)
        if (!file.exists()) {
            file.mkdirs()
        }
        return dir
    }

    fun avatarCropPath() = File(availableCacheDir(), "avatar_crop").absolutePath

    fun avatarTmpPath() = File(availableCacheDir(), "avatar_tmp").absolutePath

}
