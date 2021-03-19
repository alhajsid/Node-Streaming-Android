package com.example.streaming.library.rxjava;

import com.example.streaming.library.rxjava.schedulers.AndroidSchedulers;

import rx.Observable;
import rx.schedulers.Schedulers;

public class RxSchedulers {
    public static <T> Observable.Transformer<T, T> ioToMain() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
