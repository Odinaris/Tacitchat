package cn.odinaris.tacitchat.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object ImageUtils{
    fun readBitMap(resId: Int,mContext: Context): Bitmap {
        val opt = BitmapFactory.Options()
        opt.inPreferredConfig = Bitmap.Config.RGB_565
        val `is` = mContext.resources.openRawResource(resId)
        return BitmapFactory.decodeStream(`is`, null, opt)
    }

    /**
     * 质量压缩方法
     *
     * @param image
     * *
     * @return
     */
    fun compressImage(image: Bitmap): Bitmap {

        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 90

        while (baos.toByteArray().size / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset() // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos)// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10// 每次都减少10
        }
        val isBm = ByteArrayInputStream(baos.toByteArray())// 把压缩后的数据baos存放到ByteArrayInputStream中
        val bitmap = BitmapFactory.decodeStream(isBm, null, null)// 把ByteArrayInputStream数据生成图片
        return bitmap
    }
}