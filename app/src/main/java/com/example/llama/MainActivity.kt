package com.example.llama

import android.app.ActivityManager
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.text.format.Formatter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.example.llama.ui.theme.LlamaAndroidTheme
import java.io.File

class MainActivity(
    activityManager: ActivityManager? = null,
    downloadManager: DownloadManager? = null,
    clipboardManager: ClipboardManager? = null,
): ComponentActivity() {
    private val tag: String? = this::class.simpleName

    private val activityManager by lazy { activityManager ?: getSystemService<ActivityManager>()!! }
    private val downloadManager by lazy { downloadManager ?: getSystemService<DownloadManager>()!! }
    private val clipboardManager by lazy { clipboardManager ?: getSystemService<ClipboardManager>()!! }

    private val viewModel: MainViewModel by viewModels()

    // Get a MemoryInfo object for the device's current memory status.
    private fun availableMemory(): ActivityManager.MemoryInfo {
        return ActivityManager.MemoryInfo().also { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build()
        )

        val free = Formatter.formatFileSize(this, availableMemory().availMem)
        val total = Formatter.formatFileSize(this, availableMemory().totalMem)

        viewModel.log("当前内存: 可用: $free / 总: $total")
        viewModel.log("模型下载目录: ${getExternalFilesDir(null)}")

        val extFilesDir = getExternalFilesDir(null)

        val models = listOf(
            Downloadable(
                "Peach-9B-8k-Roleplay(Q4_0, 4.7 GiB)",
                Uri.parse("https://modelscope.cn/models/chentyjpm/Peach-9B-8k-Roleplay-GGML/resolve/master/Peach-9B-8k-Roleplay-Q4_0.gguf"),
                File(extFilesDir, "Peach-9B-8k-Roleplay-Q4_0.gguf"),
            ),
        )

        setContent {
            LlamaAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainCompose(
                        viewModel,
                        clipboardManager,
                        downloadManager,
                        models,
                    )
                }

            }
        }
    }
}

@Composable
fun MainCompose(
    viewModel: MainViewModel,
    clipboard: ClipboardManager,
    dm: DownloadManager,
    models: List<Downloadable>
) {

    Column {
        Row {
            for (model in models) {
                Downloadable.Button(viewModel, dm, model)
            }
        }
        val scrollState = rememberLazyListState()
        Row {
            OutlinedTextField(
                value = viewModel.system_message,
                onValueChange = { viewModel.updateSystemMessage(it) },
                label = { Text("人设") },
                modifier = Modifier
                    .height(64.dp)
                    .width(320.dp),
            )
            Button(
                modifier = Modifier
                    .height(56.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(5.dp)),
                onClick = { viewModel.initchart() }
            ) { Text("设定") }
        }
        Row {
            Button(
                modifier = Modifier
                    .height(32.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(5.dp)),
                onClick = { viewModel.inittutu() }
            ) { Text("可爱兔兔") }
            Button(
                modifier = Modifier
                    .height(32.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(5.dp)),
                onClick = { viewModel.inityujie() }
            ) { Text("黑丝御姐") }
            Button(
                modifier = Modifier
                    .height(32.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(5.dp)),
                onClick = { viewModel.initluoli() }
            ) { Text("傲娇萝莉") }
            Button(
                modifier = Modifier
                    .height(32.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(5.dp)),
                onClick = {
                    viewModel.messages.joinToString("\n").let {
                        clipboard.setPrimaryClip(ClipData.newPlainText("", it))
                    }
                }) { Text("复制") }
        }
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(state = scrollState) {

                items(viewModel.messages) {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        Row {
            OutlinedTextField(
                value = viewModel.message,
                onValueChange = { viewModel.updateMessage(it) },
                label = { Text("对话") },
                modifier = Modifier
                    .width(320.dp)
                    .height(64.dp),
            )

            Button(
                modifier = Modifier
                    .height(56.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(5.dp)),
                onClick = { viewModel.send() },
            ) { Text("发送") }

        }

    }
}
