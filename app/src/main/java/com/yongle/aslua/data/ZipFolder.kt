package com.yongle.aslua.data

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipFolder {

   fun  zip(sourceFolderPath: String, zipFilePath: String) {
        val srcFolder = File(sourceFolderPath)
        val zipFile = File(zipFilePath)

        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            zipFiles(srcFolder.listFiles(), srcFolder.name, zipOut)
        }
    }

    private fun zipFiles(files: Array<File>?, parentFolderName: String, zipOut: ZipOutputStream) {
        files?.forEach { file ->
            if (file.isDirectory) {
                val folderName = file.name
                val subFiles = file.listFiles()
                val subFolderName = if (parentFolderName == "") folderName else "$parentFolderName/$folderName"
                zipFiles(subFiles, subFolderName, zipOut)
            } else {
                val fileName = file.name
                val fileEntry = ZipEntry("$parentFolderName/$fileName")
                zipOut.putNextEntry(fileEntry)

                file.inputStream().use { input ->
                    input.copyTo(zipOut)
                }
            }
        }
    }

}