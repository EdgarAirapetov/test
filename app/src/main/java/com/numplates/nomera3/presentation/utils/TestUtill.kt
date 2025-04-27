package com.numplates.nomera3.presentation.utils

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

fun setUpRxSchedulers() {
    val immediate: Scheduler = object : Scheduler() {
        override fun scheduleDirect(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
            // this prevents StackOverflowErrors when scheduling with a delay
            return super.scheduleDirect(run, 0, unit)
        }

        override fun createWorker(): Worker {
            return ExecutorScheduler.ExecutorWorker({ obj: Runnable -> obj.run() }, false)
        }
    }
    RxJavaPlugins.setInitIoSchedulerHandler { scheduler: Callable<Scheduler?>? -> immediate }
    RxJavaPlugins.setInitComputationSchedulerHandler { scheduler: Callable<Scheduler?>? -> immediate }
    RxJavaPlugins.setInitNewThreadSchedulerHandler { scheduler: Callable<Scheduler?>? -> immediate }
    RxJavaPlugins.setInitSingleSchedulerHandler { scheduler: Callable<Scheduler?>? -> immediate }
    RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler: Callable<Scheduler?>? -> immediate }
}
