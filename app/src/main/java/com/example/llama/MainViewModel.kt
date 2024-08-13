package com.example.llama

import android.llama.cpp.LLamaAndroid
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainViewModel(private val llamaAndroid: LLamaAndroid = LLamaAndroid.instance()): ViewModel() {
    companion object {
        @JvmStatic
        private val NanosPerSecond = 1_000_000_000.0
    }

    private val tag: String? = this::class.simpleName

    var messages by mutableStateOf(listOf("初始化...."))
        private set

    var message by mutableStateOf("你好呀~")
        private set

    var system_message by mutableStateOf("你是黑丝御姐")
        private set

    override fun onCleared() {
        super.onCleared()

        viewModelScope.launch {
            try {
                llamaAndroid.unload()
            } catch (exc: IllegalStateException) {
                messages += exc.message!!
            }
        }
    }

    fun send() {
        // Add to messages console.
        messages += message
        messages += ""

        viewModelScope.launch {
            var chattext = "<|im_start|>user\n" + message + "<|im_end|>\n<|im_start|>character\n";
            llamaAndroid.send(chattext)
                .catch {
                    Log.e(tag, "send() failed", it)
                    messages += it.message!!
                }
                .collect { messages = messages.dropLast(1) + (messages.last() + it) }
        }
    }

    fun bench(pp: Int, tg: Int, pl: Int, nr: Int = 1) {
        viewModelScope.launch {
            try {
                val start = System.nanoTime()
                val warmupResult = llamaAndroid.bench(pp, tg, pl, nr)
                val end = System.nanoTime()

                messages += warmupResult

                val warmup = (end - start).toDouble() / NanosPerSecond
                messages += "Warm up time: $warmup seconds, please wait..."

                if (warmup > 5.0) {
                    messages += "Warm up took too long, aborting benchmark"
                    return@launch
                }

                messages += llamaAndroid.bench(512, 128, 1, 3)
            } catch (exc: IllegalStateException) {
                Log.e(tag, "bench() failed", exc)
                messages += exc.message!!
            }
        }
    }

    fun load(pathToModel: String) {
        viewModelScope.launch {
            try {
                llamaAndroid.load(pathToModel)
                messages += "加载 $pathToModel"
            } catch (exc: IllegalStateException) {
                Log.e(tag, "load() failed", exc)
                messages += exc.message!!
            }
        }
    }

    fun updateMessage(newMessage: String) {
        message = newMessage
    }

    fun updateSystemMessage(newMessage: String) {
        system_message = newMessage
    }

    fun initchart() {
        //messages = listOf()
        messages += "初始化人设:"
        messages += system_message;
        messages += ""

        viewModelScope.launch {
            var systext = "<|im_start|>system\n" + system_message + "<|im_end|>\n";
            llamaAndroid.send(systext)
                .catch {
                    Log.e(tag, "send() failed", it)
                    messages += it.message!!
                }
                .collect { messages = messages.dropLast(1) + (messages.last() + it) }
        }
    }

    fun inittutu() {
        //messages = listOf()
        system_message = "你是天真烂漫,活泼开朗的“兔兔”";
        initchart();
    }

    fun inityujie() {
        //messages = listOf()
        system_message = "你是黑丝御姐";
        initchart();
    }

    fun initluoli() {
        system_message = "你是傲娇萝莉";
        initchart();
    }

    fun log(message: String) {
        messages += message
    }
}
