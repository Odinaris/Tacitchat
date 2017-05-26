package cn.odinaris.tacitchat.utils

import android.util.Log

class LogUtils {
    companion object {
        val LOGTAG = "Tacitchat"
        var debugEnabled: Boolean = false

        private val debugInfo: String
            get() {
                val stack = Throwable().fillInStackTrace()
                val trace = stack.stackTrace
                val n = 2
                return trace[n].className + " " + trace[n].methodName + "()" + ":" + trace[n].lineNumber +
                        " "
            }

        private fun getLogInfoByArray(infos: Array<String>): String {
            val sb = StringBuilder()
            for (info in infos) {
                sb.append(info)
                sb.append(" ")
            }
            return sb.toString()
        }

        @JvmStatic fun i(s: Array<String>) {
            if (debugEnabled) {
                Log.i(LOGTAG, debugInfo + getLogInfoByArray(s))
            }
        }

        @JvmStatic fun e(s: Array<String>) {
            if (debugEnabled) {
                Log.e(LOGTAG, debugInfo + getLogInfoByArray(s))
            }
        }

        @JvmStatic fun d(s: Array<String>) {
            if (debugEnabled) {
                Log.d(LOGTAG, debugInfo + getLogInfoByArray(s))
            }
        }

        @JvmStatic fun v(s: Array<String>) {
            if (debugEnabled) {
                Log.v(LOGTAG, debugInfo + getLogInfoByArray(s))
            }
        }

        @JvmStatic fun w(s: Array<String>) {
            if (debugEnabled) {
                Log.w(LOGTAG, debugInfo + getLogInfoByArray(s))
            }
        }

        fun logException(tr: Throwable) {
            if (debugEnabled) {
                Log.e(LOGTAG, debugInfo, tr)
            }
        }
    }
}
