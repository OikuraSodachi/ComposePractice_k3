package com.todokanai.composepractice.compose.frag

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todokanai.composepractice.compose.StorageMenuButtons
import com.todokanai.composepractice.compose.activity.MainActivity
import com.todokanai.composepractice.compose.holder.StorageHolder
import com.todokanai.composepractice.viewmodel.StorageViewModel
import java.io.File

@Composable
fun StorageFrag(
    modifier: Modifier,
    activity: MainActivity,
    viewModel: StorageViewModel,
    exitStorageFrag:()->Unit,
    setInitialPath: (File)->Unit
){
    val storageList = viewModel.storageList.collectAsStateWithLifecycle()

    Column {
        StorageMenuButtons(
            modifier = Modifier,
            button1 = {viewModel.button1()},
            exit = {viewModel.exit(activity)}
        )

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
        ) {
            items(storageList.value.size) {
                val storage = storageList.value[it]
                StorageHolder(
                    modifier = Modifier
                        .clickable {
                            viewModel.setInitialPath({setInitialPath(storage.storage)})
                            exitStorageFrag()
                        },
                    storage = storage
                )
            }
        }
    }

    println("Recomposition: StorageFrag")
}