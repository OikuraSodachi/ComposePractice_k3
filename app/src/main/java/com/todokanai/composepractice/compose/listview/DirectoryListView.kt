package com.todokanai.composepractice.compose.listview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todokanai.composepractice.compose.holder.DirectoryHolder
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/** Recomposition이 중복되서 발생하고 있음 */
/** 비교군: FileListFrag (정상작동) */
@Composable
fun DirectoryListView(
    modifier:Modifier,
    dirTreeFlow: StateFlow<List<File>>,
    updateCurrentPath:(File)->Unit
){
    val items = dirTreeFlow.collectAsStateWithLifecycle()

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        items(items.value.size) {
            val item = items.value[it]
            DirectoryHolder(
                modifier = Modifier
                    .clickable { updateCurrentPath(item) },
                pathName = item
            )
        }
    }
    println("Recomposition: DirectoryListView")
}