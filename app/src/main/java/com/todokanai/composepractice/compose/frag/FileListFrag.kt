package com.todokanai.composepractice.compose.frag

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todokanai.composepractice.compose.OptionButtons
import com.todokanai.composepractice.compose.dialog.InfoDialog
import com.todokanai.composepractice.compose.dialog.RenameDialog
import com.todokanai.composepractice.compose.dialog.ZipDialog
import com.todokanai.composepractice.compose.holder.FileHolder
import com.todokanai.composepractice.compose.listview.BottomButtonListView
import com.todokanai.composepractice.compose.listview.DirectoryListView
import com.todokanai.composepractice.myobjects.Constants
import com.todokanai.composepractice.viewmodel.FileListViewModel
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListFrag(
    modifier: Modifier,
    activity: Activity,
    viewModel: FileListViewModel
) {

    val currentPath = viewModel.currentPath.collectAsStateWithLifecycle()
    val lifeCycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var zipDialog by remember {mutableStateOf(false)}
    var renameDialog by remember {mutableStateOf(false)}
    var infoDialog by remember {mutableStateOf(false)}

    val fileHolderItemList = viewModel.fileHolderItemList.collectAsStateWithLifecycle()

    val selectModeFlow = viewModel.selectMode
    val selectMode = selectModeFlow.collectAsStateWithLifecycle()

    val selectedList = remember{mutableListOf<File>()}

    Column(
        modifier = modifier
    ) {
        val sortModeSelected = viewModel.sortMode.collectAsStateWithLifecycle()
        val storageList = viewModel.storageList.collectAsStateWithLifecycle()

        OptionButtons(
            modifier = Modifier,
            exit = {viewModel.exit(activity)},
            items = viewModel.sortModeCallbackList(),
            selectedItems = sortModeSelected.value,
            storageList = viewModel.toPair(storageList.value),
            onConfirm = {viewModel.newFolder(it)}
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            DirectoryListView(
                modifier = Modifier,
                dirTreeFlow = viewModel.dirTree,
                updateCurrentPath = { viewModel.updateCurrentPath(it) }
            )

            if (fileHolderItemList.value.isEmpty()) {
                /** 비어있는 경로일 경우 */
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .wrapContentSize(),
                    text = "Empty Directory"
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(fileHolderItemList.value.size) { index ->

                        val file = fileHolderItemList.value[index]
                        var isSelected by rememberSaveable { mutableStateOf(false) }
                        FileHolder(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        if (selectMode.value == Constants.MULTI_SELECT_MODE) {
                                            if (isSelected) {
                                                isSelected = false
                                                selectedList.remove(file.file)
                                            } else {
                                                isSelected = true
                                                selectedList.add(file.file)
                                            }
                                            println("sL: $selectedList")
                                        } else {
                                            viewModel.onItemClick(
                                                context, file.file
                                            )
                                        }
                                    },    // onClick을 다른데서 정의해놓고 fileListView로 호출해야할지도? (UI 스레드 경량화 문제?)  ---> 하드웨어 성능에 따른 편차 발견됨
                                    onLongClick = {
                                        if (selectMode.value == Constants.DEFAULT_MODE) {
                                            viewModel.onItemLongClick()
                                            isSelected = true
                                            selectedList.add(file.file)
                                            //      updateSelectedList(selectedList)
                                        }
                                    }
                                ),
                            file = file,
                            isSelected = isSelected,
                        )

                        /** Default Mode로 변경시 isSelected 정보 리셋 부분 */
                        SideEffect {
                            selectModeFlow.asLiveData().observe(lifeCycleOwner) { mode ->
                                if (mode == Constants.DEFAULT_MODE) {
                                    isSelected = false
                                }
                            }
                        }
                    }
                }
            }

            /** Default Mode로 변경시 selectedList 비우기 */
            SideEffect {
                selectModeFlow.asLiveData().observe(lifeCycleOwner) { mode ->
                    if (mode == Constants.DEFAULT_MODE) {
                        selectedList.clear()
                    }
                }
            }

            BottomButtonListView(
                modifier = Modifier,
                selectModeFlow = selectModeFlow,
                zipDialog = { zipDialog = true },
                renameDialog = { renameDialog = true },
                infoDialog = { infoDialog = true },
                moveMode = { viewModel.moveMode() },
                copyMode = { viewModel.copyMode() },
                unzipMode = { viewModel.unzipMode() },
                unzipHereMode = { viewModel.unzipHereMode() },
                delete = { viewModel.delete(selectedList) },
                cancel = { viewModel.cancel() },
                confirm = { viewModel.confirm(selectedList, currentPath.value) },
                selectedList = selectedList
            )
        }

    }
    if(zipDialog){
        ZipDialog(
            onConfirm = { viewModel.zip(selectedList,it) },
            onCancel =  { zipDialog = false }
        )
    }
    if(renameDialog){
        RenameDialog(
            onConfirm = { viewModel.rename(selectedList,it) },
            onCancel = {renameDialog = false}
        )
    }
    if(infoDialog){
        InfoDialog(
            files = selectedList,
            onCancel =  { infoDialog = false }
        )
    }

    println("Recomposition: FileListFrag")
}