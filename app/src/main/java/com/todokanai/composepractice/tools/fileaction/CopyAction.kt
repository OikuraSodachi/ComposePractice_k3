package com.todokanai.composepractice.tools.fileaction

import com.todokanai.composepractice.tools.independent.getTotalSize_td
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/** ViewModel 이상 단계에서 보이면 안됨 */
class CopyAction {

    fun copyFiles(
        files:Array<File>,
        target: File,
        progressCallback:(progress:Int)->Unit,
       // onComplete:()->Unit?,
        onSpaceRequired:()->Unit?
    ){
        val totalSize = getTotalSize_td(files)


        if (totalSize >= target.freeSpace) {
            onSpaceRequired()
        } else {
            var bytesProgress = 0L

            fun copyFileRecursivePart(
                    file: File,
                    target: File,
                    callback: (percent: Int) -> Unit
                ) {
                    var prevProgress = 0

                        if (file.isDirectory) {
                            if (!target.exists()) {
                                target.mkdir()
                            }
                            file.listFiles()?.forEach {
                                val newFile = target.toPath().resolve(it.name)
                                copyFileRecursivePart(it, File("$newFile"), callback)
                            }
                        } else {
                            val inputStream = FileInputStream(file)
                            val outputStream = FileOutputStream(target)
                            val buffer = ByteArray(1024)
                            var bytesRead: Int

                            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                                outputStream.write(buffer, 0, bytesRead)
                                bytesProgress += bytesRead
                                /** */
                                val progress = (100 * bytesProgress / totalSize).toInt()
                                if (prevProgress != progress || bytesProgress == totalSize) {      // 진행도 변경되거나 전체 파일의 끝일때
                                    callback(progress)
                                    prevProgress = progress
                                }
                            }
                            inputStream.close()
                            outputStream.close()
                        }

                }
                files.forEach { file ->
                    val tempTarget = File("${target.toPath()}/${file.name}")
                    copyFileRecursivePart(file, tempTarget) { progressCallback(it) }
                }

        }
    }       // Done
}