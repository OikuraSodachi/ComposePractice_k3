package com.todokanai.composepractice.compose.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.todokanai.composepractice.compose.frag.FileListFrag
import com.todokanai.composepractice.compose.frag.StorageFrag
import com.todokanai.composepractice.ui.theme.ComposePracticeTheme
import com.todokanai.composepractice.viewmodel.FileListViewModel
import com.todokanai.composepractice.viewmodel.MainViewModel
import com.todokanai.composepractice.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var activityResult: ActivityResultLauncher<String>
    private val mViewModel: MainViewModel by viewModels()
    private val fViewModel: FileListViewModel by viewModels()
    private val sViewModel : StorageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen(
                activity = this,
                mViewModel = mViewModel,
                fViewModel = fViewModel,
                sViewModel = sViewModel
            )
        }

        mViewModel.getPermission(this)

        activityResult =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted)
                    finish()
            }
    }
}

@Composable
private fun HomeScreen(
    activity: MainActivity,
    mViewModel:MainViewModel,
    fViewModel:FileListViewModel,
    sViewModel:StorageViewModel
){
    ComposePracticeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            var isStorageFrag by remember{mutableStateOf(true)}
            val onBack = activity.onBackPressedDispatcher

            if(isStorageFrag){
                StorageFrag(
                    modifier =Modifier,
                    activity = activity,
                    viewModel = sViewModel,
                    exitStorageFrag ={isStorageFrag = false},
                    setInitialPath = {fViewModel.updateCurrentPath(it)}
                )
                onBack.addCallback() {
                    mViewModel.alreadyMain()
                }

            }else {
                FileListFrag(
                    modifier = Modifier,
                    activity = activity,
                    viewModel = fViewModel
                )
                onBack.addCallback() {
                    fViewModel.onBackPressed({isStorageFrag = true})
                }
            }
        }
    }

}


