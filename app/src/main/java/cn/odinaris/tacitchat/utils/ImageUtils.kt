package cn.odinaris.tacitchat.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUtils{
    fun readBitMap(resId: Int,mContext: Context): Bitmap {
        val opt = BitmapFactory.Options()
        opt.inPreferredConfig = Bitmap.Config.RGB_565
        val `is` = mContext.resources.openRawResource(resId)
        return BitmapFactory.decodeStream(`is`, null, opt)
    }

    /**
     * 质量压缩方法(循环减少QF，直至文件大小满足需求，失真)
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

    //将Uri指向的图片文件转换到file格式
    fun switchUri2File(activity : Activity, uri : Uri) : File{
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val actualImageCursor = activity.managedQuery(uri, projection, null, null, null)
        val actualImageColumnIndex = actualImageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        actualImageCursor.moveToFirst()
        val img_path = actualImageCursor.getString(actualImageColumnIndex)
        return File(img_path)
    }
}