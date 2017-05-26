package cn.odinaris.tacitchat.utils

import kotlin.text.Charsets.UTF_8

/**
 * 编解码工具类
 */
object CodeUtils {

    //将字符串转换成二进制字符串，以UTF_8格式编码
    fun Str2BinStr(str: String): String {
        val strByte = str.toByteArray(Charsets.UTF_8)
        var result = ""
        for (i in strByte.indices) {
            result += Byte2BinStr(strByte[i])
            println(strByte[i].toString())
        }
        return result
    }

    //将二进制字符串转换成字符串，以UTF_8格式编码
    fun BinStr2Str(str:String):String{
        val size = str.length/8
        val bytes = ByteArray(size)
        (0..size-1).forEach { bytes[it] = BinStr2Byte(str.substring(8*it,8*it+8)) }
        return String(bytes,UTF_8)
    }

    //将字节转换位8位二进制字符串
    private fun Byte2BinStr(byte:Byte):String{
        val byte2Int = byte.toInt()+128     //将字节范围从[-127,127]转换到[0，255]
        val result = Integer.toBinaryString(byte2Int)
        val sb = StringBuilder(8)
        if(result.length<8){ (0..8-result.length-1).forEach { sb.append("0") } }
        sb.append(result)
        return sb.toString()
    }

    //将8位二进制字符串转换成字节
    private fun BinStr2Byte(str: String): Byte{
        val byteInt = (0..str.length-1).sumBy {
            if(str[it]!='0') Math.pow(2.toDouble(),(str.length- 1 - it).toDouble()).toInt() else 0
        }
        return (byteInt-128).toByte()
    }

    //将二进制字符串转换成整数
    fun BinStr2Int(str: String): Int{
        val int = (0..str.length-1).sumBy {
            if(str[it]!='0') Math.pow(2.toDouble(),(str.length- 1 - it).toDouble()).toInt() else 0
        }
        return int
    }
}