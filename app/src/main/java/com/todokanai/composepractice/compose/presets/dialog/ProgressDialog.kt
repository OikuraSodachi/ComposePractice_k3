package com.todokanai.composepractice.compose.presets.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDialog(
    modifier:Modifier,
    progress:Float
) {
    var showDialog by remember { mutableStateOf(true) }
    var progressState by remember{mutableStateOf(progress)}     // var 로 사용하는거 맞음. val로 수정하지 말것
    if(showDialog) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = {showDialog = false},
            content = {
                Column() {
                    LinearProgressIndicator(
                        modifier = Modifier.size(48.dp), // 원하는 크기로 조정합니다.
                        color = Color.Blue, // 원하는 색상으로 조정합니다.
                        progress = progressState,
                        strokeCap = StrokeCap.Round,
                        trackColor = Color.Red
                    )

                    CustomLinearProgressIndicator(progress = progress)

                }
            }
        )
    }
}
@Composable
fun CustomLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    progressColor: Color = Color.Red,
    backgroundColor: Color = Color.Red.copy(0.24f),
    clipShape: Shape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .clip(clipShape)
            .background(backgroundColor)
            .height(8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(progressColor)
                .fillMaxHeight()
                .fillMaxWidth(progress)
        )
    }
}

@Preview
@Composable
private fun ProgressDialogPreview(){
    Surface() {
        ProgressDialog(
            modifier = Modifier,
            progress = 0.3f
        )
    }
}

@Preview
@Composable
private fun LPreview(){
    Surface() {
        CustomLinearProgressIndicator(
            modifier = Modifier
                .width(200.dp)
                .height(10.dp),
            progress = 0.9f)
    }
}