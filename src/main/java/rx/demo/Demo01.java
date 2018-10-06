package rx.demo;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Demo01 {

    public static void main(String[] args) throws IOException {

        Flowable<Long> flowable = Flowable.create((FlowableEmitter<Long> emitter) -> {

            Disposable disposable = Disposables.fromAction(() -> System.out.println("Releasing all resources on thread " + Thread.currentThread().getId()));
            emitter.setDisposable(disposable);

            try {
                System.out.println("producing values on thread " + Thread.currentThread().getId());

                for (int i = 0; i < 5 && !emitter.isCancelled(); ++i) {
                    System.out.println("new value " + i + " on thread " + Thread.currentThread().getId());
                    emitter.onNext(Long.valueOf(i));
                    Thread.sleep(1000);
                }

                if (!emitter.isCancelled()) {
                    emitter.onComplete();
                }

            } catch (Exception err) {
                if (!emitter.isCancelled()) {
                    emitter.onError(err);
                } else {
                    System.out.println("subscription cancelled");
                }
            }

        }, BackpressureStrategy.DROP);

        System.out.println("about to run first subscription - my thread: " + Thread.currentThread().getId());
        Disposable handle = flowable
                .subscribe(Demo01::consume);


        System.in.read();
    }

    private static void consume(Long value){
        System.out.println("got " + value + " on thread " + Thread.currentThread().getId());
    }
    private static void error(Throwable error) {
        System.out.println("ups, " + error + " on thread " + Thread.currentThread().getId());
    }
    private static void completed() {
        System.out.println("consumed all on thread "+ Thread.currentThread().getId());
    }
}
