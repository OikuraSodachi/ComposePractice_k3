package com.todokanai.composepractice.variables

import android.os.Environment
import com.todokanai.composepractice.application.MyApplication
import com.todokanai.composepractice.data.DataConverter
import com.todokanai.composepractice.data.dataclass.FileHolderItem
import com.todokanai.composepractice.tools.independent.dirTree_td
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class Variables {
    companion object{
        private val defaultStorage = Environment.getExternalStorageDirectory()

        private val _fileHolderItemList = MutableStateFlow<List<FileHolderItem>>(emptyList())
        val fileHolderItemList : StateFlow<List<FileHolderItem>>
            get() = _fileHolderItemList

        private val _dirTree = MutableStateFlow<List<File>>(dirTree_td(defaultStorage))     // 초기값은 최초 화면 디렉토리로
        val dirTree : StateFlow<List<File>>
            get() = _dirTree
        private val _currentPath = MutableStateFlow<File>(defaultStorage)
        val currentPath : StateFlow<File>
            get() = _currentPath

    }

    private val converter = DataConverter(MyApplication.appContext)

    //--------------------------------------------------------
    // setter 구간

    /** fileHolderItemList의 setter*/
    fun setFileHolderItemList(sortMode:String) {
        currentPath.value.listFiles()?.let { files ->
            _fileHolderItemList.value = converter.fileHolderItemList(files,sortMode)         // 아직 불안정?
        }
    }

    fun setCurrentPath(file:File){
        _currentPath.value = file
    }


    fun setDirTree(dirTree:List<File>){
        _dirTree.value = dirTree
    }



    fun setDirTreeFromPath(currentPath:File){
        _dirTree.value = dirTree_td(currentPath)
    }
    // setter 구간 끝
    //---------------------------------

}