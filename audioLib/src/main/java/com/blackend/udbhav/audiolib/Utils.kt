package com.blackend.udbhav.audiolib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal object Utils {

    fun <T> updateState(
        scope: CoroutineScope,
        state: MutableStateFlow<T>,
        getValue: () -> T,
        isContinuous: Boolean = false,
        delayIntervalMs: Long = 1000L): Job {

        return scope.launch {
            if (isContinuous) {
                while (isActive) {
                    state.update { getValue() }
                    delay(delayIntervalMs)
                }
            } else state.update { getValue() }
        }
    }
}