package com.todokanai.composepractice.tools.fileaction

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class RenameAction {

    fun renameAction(
        selectedFile: File,
        name:String,
     //   onComplete:()->Unit
    ){

        selectedFile.renameTo(File("${selectedFile.parent}/$name"))

    }       // Done
}