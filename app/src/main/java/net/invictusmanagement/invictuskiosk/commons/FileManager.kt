package net.invictusmanagement.invictuskiosk.commons

import android.content.Context
import java.io.File

object FileManager {

    fun saveImageToCache(appContext: Context, bytes: ByteArray, fileName: String): String {
        val file = File(appContext.filesDir, fileName)
        file.writeBytes(bytes)
        return file.absolutePath
    }

}