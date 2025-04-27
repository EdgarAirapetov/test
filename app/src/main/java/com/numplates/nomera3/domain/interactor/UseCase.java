package com.numplates.nomera3.domain.interactor;


import android.util.Log;

import com.numplates.nomera3.App;
import com.numplates.nomera3.data.network.core.ResponseWrapper;
import com.numplates.nomera3.presentation.view.utils.eventbus.busevents.RxEventsJava;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.adapter.rxjava2.HttpException;
import timber.log.Timber;


/**
 * Created by abelov
 */
public abstract class UseCase<T> {

    private final String TAG = this.getClass().getSimpleName();

    protected Disposable subscription;

    protected abstract Flowable<T> buildUseCaseObservable();

    public void execute(Consumer<T> onSuccess, Consumer<Throwable> onFail) {
        this.subscription = this.buildUseCaseObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.d(TAG, "Result inside UseCase: " + result);

                    if (result instanceof ResponseWrapper) {
                        outLogResponse((ResponseWrapper) result);
                    }
                    onSuccess.accept(result);

                }, error -> {
                    Log.e(TAG, "Error inside UseCase: " + error);
                    error.printStackTrace();
//                    onFail.accept(error);

                    // Refresh Token
                    if (error instanceof HttpException) {
                        Response responce =  ((HttpException) error).response();
                        int responseCode = responce.code();
                        Timber.e("ERROR Response code: " + responseCode);
                        if (responseCode == 401) {
                            App.Companion.getBus().send(new RxEventsJava.MustRefreshToken());
                        }
                    }

                });
    }

    public void unsubscribe() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }


    private void outLogResponse(ResponseWrapper result) {
        if (result.getData() != null) {
            Log.d(TAG, "RESPONSE Data: " + result.getData().toString());
        }
        if (result.getErr() != null) {
            Log.d(TAG, "RESPONSE ERROR: " + result.getErr());
        }
    }

}
