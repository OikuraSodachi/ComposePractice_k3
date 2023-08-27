package com.todokanai.composepractice.tools

/** ~Action -> ViewModel에서 호출하는 method
 *
 *  웬만하면 여기서는 CoroutineScope가 등장하지 않도록 작성
 */
import android.content.Context
import com.todokanai.composepractice.data.datastore.DataStoreRepository
import com.todokanai.composepractice.tools.fileaction.CopyAction
import com.todokanai.composepractice.tools.fileaction.DeleteAction
import com.todokanai.composepractice.tools.fileaction.MoveAction
import com.todokanai.composepractice.tools.fileaction.NewFolderAction
import com.todokanai.composepractice.tools.fileaction.OpenAction
import com.todokanai.composepractice.tools.fileaction.RenameAction
import com.todokanai.composepractice.tools.fileaction.UnzipAction
import com.todokanai.composepractice.tools.fileaction.ZipAction
import com.todokanai.composepractice.tools.independent.dirTree_td
import com.todokanai.composepractice.tools.independent.getFileNumber_td
import com.todokanai.composepractice.variables.Variables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.zip.ZipFile

class FileAction() {

    private val myNoti = MyNotification()


    /** CoroutineScope 감싸고 invokeOnCompletion? */
    fun openAction(
        context:Context,
        selected: File
    ){
        if (selected.isDirectory) {
            onComplete(selected,null)   // Folder 열기
        } else {
            OpenAction().openFile(context, selected)   // 파일이면 열기
        }
    }       // Done

    fun renameAction(
        selectedFile:File,
        name:String
    ){
        actionWrapper(
            action = {
                RenameAction().renameAction(
                    selectedFile = selectedFile,
                    name = name
                )
                     },
            completionCallback = {onComplete(selectedFile.parentFile,null)}
        )
    }       // Done

    fun copyAction(
        files:Array<File>,
        currentPath: File
    ){


        actionWrapper(
            action = {
                CopyAction().copyFiles(
                    files = files,
                    target = currentPath,
                    progressCallback = { myNoti.copyProgressNoti(it) },
                    onSpaceRequired = { onSpaceRequired() }
                )
            },
            completionCallback = { onComplete(currentPath, "copied ${files.size} files") }
        )
    }       // Done

    fun moveAction(
        files:Array<File>,
        currentPath: File
    ){
        actionWrapper(
            action = {
                MoveAction().moveAction(
                files = files,
                path = currentPath,
                progressCallback = {myNoti.moveProgressNoti(it)},
                //onComplete = { onComplete(currentPath,"Moved ${files.size} files")},
                onSpaceRequired = {onSpaceRequired()}
            )
                     },
            completionCallback = { onComplete(currentPath,"Moved ${files.size} files")}

        )

    }           // Done

    fun deleteAction(
        files:Array<File>
    ){
        val totalNumber = getFileNumber_td(files)
        actionWrapper(
            action =  {
                DeleteAction().deleteFiles(
                    files = files,
                    progressCallback = {myNoti.deleteProgressNoti(it,totalNumber)}
            )
                      },
            completionCallback = {
                onComplete(files.first().parentFile,"deleted ${files.size} files and its subdirectories")
            }
        )

    }       // Done
    fun newFolderAction(
        currentPath:File,
        folderName:String
    ){
        actionWrapper(
            action = {
                NewFolderAction().newFolderAction(
                    path = currentPath,
                    folderName = folderName
                )
            },
            completionCallback = {onComplete(currentPath, null)}
        )

        /*
        NewFolderAction().newFolderAction(
            path = currentPath,
            folderName = folderName,
            onComplete = {onComplete(currentPath,null)}
        )

         */
    }       // Done

    suspend fun zipAction(
        files:Array<File>,
        zipFileName:String
    ){
        val targetPath = files.first().parentFile       // 압축할 목록의 parent 경로
      //  println("targetPath: $targetPath")
        val zipFile = File("$targetPath/$zipFileName.zip")

        /*
        ZipAction().zipFiles(
            files = files,
            zipFile = zipFile,
            progressCallback = {  myNoti.zipProgressNoti(it) },
            onComplete = { onComplete(targetPath,"zipped ${files.size} files") },
            onSpaceRequired = { onSpaceRequired() }
        )

         */


        actionWrapper(
            action = {
                ZipAction().compressToZip(
                    files = files.toList(),
                    zipFile = zipFile,
                    progressCallback = { myNoti.zipProgressNoti(it) },

                    onSpaceRequired = { onSpaceRequired() }
                )
            },
            completionCallback = { onComplete(targetPath, "zipped ${files.size} files") },
        )
    }

    fun unzipAction(
        zipFile:File,
        currentPath:File,
        unzipHere:Boolean
    ){
        val file = ZipFile(zipFile)
        if(unzipHere){
            actionWrapper(
            action = {
                UnzipAction().unzipHere_Wrapper(
                    zipFile = file,
                    target = currentPath,
                    progressCallback = { myNoti.unzipProgressNoti(it) },
                    onSpaceRequired = { onSpaceRequired() }
                )
            },
                completionCallback = { onComplete(currentPath.parentFile, "unzipped") },
            )
        } else {
            actionWrapper(
                action = {
                    UnzipAction().unzip_Wrapper(
                        zipFile = file,
                        target = currentPath,
                        progressCallback = {myNoti.unzipProgressNoti(it)},

                        onSpaceRequired = {onSpaceRequired()}
                    )
                },
                completionCallback = {onComplete(currentPath,"unzipped")},
            )
        }
    }      // Done

    //-----------------------
    // 여기부터 Private

    /** Action 완료후 onComplete 호출 용도 */
    private fun actionWrapper(action:()->Unit, completionCallback:()->Unit){
        CoroutineScope(Dispatchers.IO).launch {
            action()
        }.invokeOnCompletion {
            completionCallback()
        }
    }

    private fun onSpaceRequired() = LogTool().makeShortToast("Not enough space")

    suspend fun updateCurrentPath(file:File)  {
        val vars = Variables()
        val dsRepo = DataStoreRepository()
        file.listFiles()?.let {         // file.listFiles()?.let -> 접근 가능한 경로일 경우
            vars.setCurrentPath(file)                       // setter 적용

            // -- LiveData 구간?

            vars.setDirTree(dirTree_td(file))
            vars.setFileHolderItemList(dsRepo.sortBy())
            //setFileHolderItemList2(dsRepo.sortBy())
        }
    }

    /** path에 null 입력시 update 미발생
     *
     * message에 null 입력시 알림은 미발생
     */

    private fun onComplete(
        path:File?,
        message:String?,
    ){

        CoroutineScope(Dispatchers.IO).launch {
            path?.let {
                updateCurrentPath(path)
                //updateCurrentPath(path)
            }
            message?.let {
                myNoti.completedNotification(
                    title = "",
                    message = it
                )
            }
        }
    }






}