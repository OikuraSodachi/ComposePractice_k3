package com.todokanai.composepractice.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todokanai.composepractice.data.dataclass.StorageHolderItem
import com.todokanai.composepractice.tools.independent.exit_td
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor() : ViewModel(){

    /** FileListFrag의 초기 경로값 */
    fun setInitialPath(setPath:()->Unit)  {
        viewModelScope.launch{
            setPath()
        }
    }

    val storageList : StateFlow<List<StorageHolderItem>>
       get() = MainViewModel.physicalStorageList

    fun button1(){

    }

    fun exit(activity:Activity) = exit_td(activity)
}