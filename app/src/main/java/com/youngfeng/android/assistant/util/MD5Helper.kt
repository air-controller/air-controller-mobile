package com.youngfeng.android.assistant.util

import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.SequenceInputStream
import java.util.Arrays
import java.util.Vector

object MD5Helper {

    private fun calcMD5HashForDir(dirToHash: File, includeHiddenFiles: Boolean): String? {
        val fileStreams: Vector<FileInputStream> = Vector<FileInputStream>()
        println("Found files for hashing:")
        collectInputStreams(dirToHash, fileStreams, includeHiddenFiles)
        val seqStream = SequenceInputStream(fileStreams.elements())
        return try {
            val md5Hash: String = DigestUtils.md5Hex(seqStream)
            seqStream.close()
            md5Hash
        } catch (e: IOException) {
            throw e
        }
    }

    private fun collectInputStreams(
        dir: File,
        foundStreams: MutableList<FileInputStream>,
        includeHiddenFiles: Boolean
    ) {
        val fileList: Array<File> = dir.listFiles()
        Arrays.sort(fileList)

        for (f in fileList) {
            if (!includeHiddenFiles && f.name.startsWith(".")) {
                // Skip it
            } else if (f.isDirectory) {
                collectInputStreams(f, foundStreams, includeHiddenFiles)
            } else {
                try {
                    foundStreams.add(FileInputStream(f))
                } catch (e: FileNotFoundException) {
                    throw e
                }
            }
        }
    }

    fun md5(file: File): String {
        return if (file.isFile) {
            DigestUtils.md5Hex(FileInputStream(file))
        } else {
            calcMD5HashForDir(file, true) ?: ""
        }
    }

    fun md5(str: String): String {
        return DigestUtils.md5Hex(str)
    }
}
