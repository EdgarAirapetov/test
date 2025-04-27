package com.numplates.nomera3.data.network.core;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;


/**
 * Created by c7j on 14.03.18.
 */
public class RetryWithDelay implements Function<Flowable<? extends Throwable>, Flowable<?>> {
    private final int maxRetries;
    private final int retryDelayMillis;
    private int retryCount;

    public RetryWithDelay(final int maxRetries, final int retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
        this.retryCount = 0;
    }

    @Override
    public Flowable<?> apply(final Flowable<? extends Throwable> attempts) {
        return attempts
                .flatMap((Function<Throwable, Flowable<?>>) throwable -> {
                    if (++retryCount < maxRetries) {
                        // When this Observable calls onNext, the original observable will be re-subscribed
                        return Flowable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
                    }
                    return Flowable.error(throwable); // Max retries hit. Just pass the error along.
                });
    }
}