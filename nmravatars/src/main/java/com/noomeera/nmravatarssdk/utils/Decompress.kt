package com.noomeera.nmravatarssdk.utils

import android.util.Log
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal class Decompress(private val zipStream: InputStream, private val destination: String) {
    @Throws(IOException::class)
    fun unzip() {
        val zipStream = ZipInputStream(zipStream)
        lateinit var zEntry: ZipEntry
        while (zipStream.nextEntry?.also { zEntry = it } != null) {
//            Log.d("Unzip", "Unzipping " + zEntry.name + " at " + destination)

            if (zEntry.isDirectory) {
                hanldeDirectory(zEntry.name)
            } else {
                val fout = FileOutputStream(
                    this.destination + "/" + zEntry.name
                )

                val bufout = BufferedOutputStream(fout)
                val buffer = ByteArray(1024)
                var read = 0
                while (zipStream.read(buffer).also { read = it } != -1) {
                    bufout.write(buffer, 0, read)
                }
                zipStream.closeEntry()
                bufout.close()
                fout.close()
            }
        }

        zipStream.close()

//        Log.d("Unzip", "Unzipping complete. path :  $destination")
    }

    private fun hanldeDirectory(dir: String) {
        val f = File(destination + dir)
        if (!f.isDirectory) {
            f.mkdirs()
        }
    }

    init {
        hanldeDirectory("")
    }
}
