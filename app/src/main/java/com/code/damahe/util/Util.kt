package com.code.damahe.util

object Util {

    fun formatDurationTimeStyle(duration: Long): String {
        val stringBuffer = StringBuffer()

        val minutes = (duration / 60000).toInt()
        val seconds = (duration % 60000 / 1000).toInt()

        stringBuffer
            .append(String.format("%02d", minutes))
            .append(":")
            .append(String.format("%02d", seconds))

        return stringBuffer.toString()
    }
}