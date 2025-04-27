package com.meera.core.extensions

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.time.Duration

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}

/**
 * Execute task on a fragment or activity on UI Thread with delay
 */
fun LifecycleOwner.doDelayed(delayMillis: Long, executeUi: () -> Unit): Job {
    return this.lifecycleScope.launch(Dispatchers.Main) {
        delay(delayMillis)
        try {
            executeUi.invoke()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

fun Lifecycle.doDelayed(delayMillis: Long, executeUi: () -> Unit): Job {
    return this.coroutineScope.launch(Dispatchers.Main) {
        delay(delayMillis)
        try {
            executeUi.invoke()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

fun ViewModel.doDelayed(delayMillis: Long, executeUi: () -> Unit): Job {
    return this.viewModelScope.launch(Dispatchers.Main) {
        delay(delayMillis)
        try {
            executeUi.invoke()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}


inline fun View.doDelayed(delayMillis: Long, crossinline executeUi: () -> Unit): Job {
    val job = CoroutineScope(Dispatchers.Main).launch {
        delay(delayMillis)
        try {
            executeUi()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    this.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(p0: View) = Unit
        override fun onViewDetachedFromWindow(p0: View) {
            try {
                job.cancelChildren()
                removeOnAttachStateChangeListener(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    })
    return job
}

fun <T> ViewModel.doAsyncViewModel(backgroundTask: () -> T, result: (T) -> Unit) {
    this.viewModelScope.launch(Dispatchers.Main) {
        try {
            val task = withContext(Dispatchers.IO) {
                backgroundTask.invoke()
            }
            result.invoke(task)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

fun <T> Lifecycle.doAsync(backgroundTask: () -> T, result: (T) -> Unit) {
    this.coroutineScope.launch(Dispatchers.Main) {
        val task = async(Dispatchers.IO) {
            backgroundTask.invoke()
        }
        try {
            result.invoke(task.await())
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

@Deprecated("Не использовать т.к. используется GlobalScope. Использовать doAsync() с lifecycle")
fun <T> Any.doAsync(backgroundTask: () -> T, result: (T) -> Unit) {
    GlobalScope.launch(Dispatchers.Main) {
        val task = async(Dispatchers.IO) {
            backgroundTask.invoke()
        }
        try {
            result.invoke(task.await())
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

fun <T> Any.doAsyncWithJob(backgroundTask: () -> T, result: (T) -> Unit) =
    GlobalScope.launch(Dispatchers.Main) {
        val task = async(Dispatchers.IO) {
            backgroundTask.invoke()
        }
        try {
            result.invoke(task.await())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

fun <T> View.doAsyncOnView(backgroundTask: () -> T, result: (T) -> Unit): Job {
    val job = CoroutineScope(Dispatchers.Main).launch {
        val data = async(Dispatchers.Default) {
            backgroundTask.invoke()
        }
        try {
            result.invoke(data.await())
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    this.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(p0: View) = Unit
        override fun onViewDetachedFromWindow(p0: View) {
            try {
                job.cancelChildren()
                removeOnAttachStateChangeListener(this)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    })

    return job
}

fun ViewModel.doOnUIThread(action: () -> Unit) {
    viewModelScope.launch(Dispatchers.Main) { action() }
}

fun LifecycleOwner.doOnUIThread(action: () -> Unit) {
    lifecycleScope.launch(Dispatchers.Main) { action() }
}

fun CoroutineScope.debouncedAction(delay: Long, action: () -> Unit): () -> Unit {
    var job: Job? = null
    return action@{
        job?.cancel()
        job = launch {
            delay(delay)
            action()
        }
    }
}

fun <T> CoroutineScope.debouncedAction1(delay: Long, action: (T) -> Unit): (T) -> Unit {
    var job: Job? = null
    return action@{
        job?.cancel()
        job = launch {
            delay(delay)
            action(it)
        }
    }
}
