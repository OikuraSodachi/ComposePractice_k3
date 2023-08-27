package com.todokanai.composepractice.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todokanai.composepractice.data.dataclass.FileHolderItem
import com.todokanai.composepractice.data.dataclass.StorageHolderItem
import com.todokanai.composepractice.data.datastore.DataStoreRepository
import com.todokanai.composepractice.myobjects.Constants
import com.todokanai.composepractice.myobjects.Constants.CONFIRM_MODE_COPY
import com.todokanai.composepractice.myobjects.Constants.CONFIRM_MODE_MOVE
import com.todokanai.composepractice.myobjects.Constants.CONFIRM_MODE_UNZIP
import com.todokanai.composepractice.myobjects.Constants.CONFIRM_MODE_UNZIP_HERE
import com.todokanai.composepractice.myobjects.Constants.DEFAULT_MODE
import com.todokanai.composepractice.myobjects.Constants.MULTI_SELECT_MODE
import com.todokanai.composepractice.tools.FileAction
import com.todokanai.composepractice.tools.independent.exit_td
import com.todokanai.composepractice.variables.FileListSorter
import com.todokanai.composepractice.variables.Variables
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileListViewModel @Inject constructor():ViewModel(){

    private val vars = Variables()
    private val fAction = FileAction()
    private lateinit var selectedItem : File
    private val dsRepo = DataStoreRepository()
    val dirTree = Variables.dirTree

    private val _selectMode = MutableStateFlow<Int>(DEFAULT_MODE)
    val selectMode:StateFlow<Int>
        get() = _selectMode
    val fileHolderItemList : StateFlow<List<FileHolderItem>> = Variables.fileHolderItemList

    val currentPath = Variables.currentPath

    /*
    fun updateCurrentPath(file:File) {
        viewModelScope.launch {
            file.listFiles()?.let {         // file.listFiles()?.let -> 접근 가능한 경로일 경우
               // vars.setCurrentPath(file)                       // setter 적용


                // -- LiveData 구간?

                vars.setDirTree(dirTree_td(file))
                vars.setFileHolderItemList(dsRepo.sortBy())
            }
        }
    }

     */

    fun updateCurrentPath(file: File){
        viewModelScope.launch {
            fAction.updateCurrentPath(file)
        }
    }

    private fun changeSelectMode(mode:Int) {
        _selectMode.value = mode
    }

    fun onDirectoryClick(path: File) = updateCurrentPath(path)

    fun onItemClick(context: Context, selected: File){
        viewModelScope.launch {
            selectedItem = selected
            when (selectMode.value) {
                DEFAULT_MODE -> {
                    fAction.openAction(context, selected)
                }

                MULTI_SELECT_MODE -> {

                }
                else -> {
                    if (selected.isDirectory) {
                        fAction.openAction(context, selected)
                    }
                }
            }
        }
    }
    /** change to MULTI_SELECT_MODE */
    fun onItemLongClick() {
        if (selectMode.value == DEFAULT_MODE) {
            changeSelectMode(MULTI_SELECT_MODE)
        }
    }
    fun copyMode() = changeSelectMode(CONFIRM_MODE_COPY)

    fun moveMode() = changeSelectMode(CONFIRM_MODE_MOVE)

    fun rename(selectedList:List<File>,name:String){
        viewModelScope.launch {
            fAction.renameAction(selectedList.first(), name)
            changeSelectMode(DEFAULT_MODE)
        }
    }

    fun delete(selectedList:List<File>){
        viewModelScope.launch {
            fAction.deleteAction(selectedList.toTypedArray())
            changeSelectMode(DEFAULT_MODE)
        }
    }

    fun zip(selectedList:List<File>,name:String){
        viewModelScope.launch {

            Log.d("FileListViewModel", "selectedList: $selectedList")
            Log.d("FileListViewModel", "name: $name")
            fAction.zipAction(selectedList.toTypedArray(), name)
            changeSelectMode(DEFAULT_MODE)
        }
    }

    /**
     * 넘겨줄 file이 압축해제 가능한지 체크할것
     * 웬만하면 "압축해제 가능한 단일 파일"이 선택된 상황에만 unzip 활성화하기
     */
    fun unzipMode() = changeSelectMode(CONFIRM_MODE_UNZIP)

    fun unzipHereMode() = changeSelectMode(CONFIRM_MODE_UNZIP_HERE)

    fun confirm(selectedList:List<File>,currentPath:File){
        viewModelScope.launch {
            val list = selectedList.toTypedArray()

            when (selectMode.value) {       // selectMode : StateFlow<Int>
                CONFIRM_MODE_COPY -> {
                    fAction.copyAction(list, currentPath)
                }

                CONFIRM_MODE_MOVE -> {
                    fAction.moveAction(list, currentPath)

                }

                CONFIRM_MODE_UNZIP -> {
                    fAction.unzipAction(list.first(), currentPath, unzipHere = false)

                }

                CONFIRM_MODE_UNZIP_HERE -> {
                    fAction.unzipAction(list.first(), currentPath, unzipHere = true)
                }
            }
            changeSelectMode(DEFAULT_MODE)
        }
    }

    fun cancel() = changeSelectMode(DEFAULT_MODE)

    //--------------------
    //-- optionViewModel 구간

    val storageList = MainViewModel.physicalStorageList

    val sortMode = dsRepo.sortBy.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(0),
        initialValue = Constants.BY_DEFAULT
    )

    fun toPair(storageList: List<StorageHolderItem>) = listToPair(storageList)

    fun newFolder(name:String) = fAction.newFolderAction(currentPath.value, name)

    fun exit(activity: Activity) = exit_td(activity)

    //fun sortModeCallbackList() = vars.sortModeCallbackList()

    private fun onUpdateSortMode(sortMode:String) {
        dsRepo.saveSortBy(sortMode)
        vars.setFileHolderItemList(sortMode)
    }
    fun sortModeCallbackList() = FileListSorter().getSortModeCallbackList({onUpdateSortMode(it)})

    //------------------
    // private

    private fun listToPair(storageList:List<StorageHolderItem>):List<Pair<String,()->Unit>>{
        val result = mutableListOf<Pair<String,()->Unit>>()
        storageList.forEach {
            result.add(Pair(it.absolutePath,{ updateCurrentPath(it.storage)  }))
        }
        return result
    }

    //------------------
    //--- mainViewModel 구역
    /** viewModelScope 적절하게 배치한건지 잘 몰?루 */
    fun onBackPressed(toStorageFrag:()->Unit){
        if(selectMode.value == MULTI_SELECT_MODE){
            _selectMode.value = DEFAULT_MODE
        } else {

            val parentFile = currentPath.value.parentFile
            if (parentFile?.listFiles() == null) {
                toStorageFrag()
            } else {
                updateCurrentPath(parentFile)
            }
        }
    }



}