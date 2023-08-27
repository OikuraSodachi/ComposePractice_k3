package com.todokanai.composepractice.compose.presets.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BooleanDialog(
    modifier:Modifier,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    if (showDialog) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = { onCancel() },
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        showDialog = false
                    }
                ) {
                    Text(text = "Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onCancel()
                        showDialog = false
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Preview
@Composable
private fun BooleanDialogPreview(){
    Surface() {
        BooleanDialog(
            modifier = Modifier,
            title = "BooleanDialog",
            message = "Message",
            onConfirm = {  },
            onCancel = { }
        )
    }
}