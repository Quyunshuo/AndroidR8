package com.yunshuo.android.r8.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yunshuo.android.r8.Utils
import com.yunshuo.android.r8.data.MoodStatus
import com.yunshuo.android.r8.data.TextProvider

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun Screen(vm: MainVM = viewModel()) {
    val isPreview = LocalInspectionMode.current
    var text by remember { mutableStateOf("") }

    val textProvider = remember { TextProvider() }

    var isShowAddDialog by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        text = textProvider.getRandomText()
        if (!isPreview) {
            vm.syncHistoryRecord()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .padding(horizontal = 26.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 20.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 10.dp),
        ) {
            items(vm.moodList) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Gray)
                        .clickable {
                            // copy mood json
                        }
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                ) {
                    Text(
                        text = "心情状态：${it.mood.description}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "影响因素：${it.influencingEvent}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "创建时间：${Utils.formatTimestamp(it.timestamp)}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Button(
            onClick = { isShowAddDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text(
                text = "记录当前心情状态",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    if (isShowAddDialog) {
        Dialog(onDismissRequest = {}) {

            var selectMood by remember { mutableStateOf(MoodStatus.NEUTRAL) }
            var influencingEvent by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "#心情状态",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    modifier = Modifier
                        .padding(vertical = 14.dp)
                        .fillMaxWidth(),
                    maxItemsInEachRow = Int.MAX_VALUE,
                    overflow = FlowRowOverflow.Clip,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoodStatus.entries.forEach {
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectMood == it) Color.Black else Color.Gray)
                                .padding(horizontal = 10.dp)
                                .clickable { selectMood = it },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = it.description,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Text(
                    text = "#影响因素（可选）",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                BasicTextField(
                    value = influencingEvent,
                    onValueChange = { influencingEvent = Utils.removeNewLines(it) },
                    textStyle = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = Color.Gray,
                    ),
                    modifier = Modifier
                        .padding(vertical = 14.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                ) { innerTextField ->
                    if (influencingEvent.isEmpty()) {
                        Text(
                            text = "输入影响心情的事情",
                            fontSize = 12.sp,
                            maxLines = 1,
                            color = Color.Gray,
                        )
                    }
                    innerTextField()
                }
                Button(
                    onClick = {
                        vm.save(selectMood, influencingEvent)
                        isShowAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "保存",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}