package rx.demo;

import io.reactivex.Flowable;

import java.io.IOException;
import java.util.concurrent.*;

public class Demo02 {

    public static void main(String[] args) throws IOException {

        Flowable<String> hello = Flowable.just("hello");

        Future<String> stringFuture = Executors.newCachedThreadPool().submit(() ->  {
            Thread.sleep(1000);
            return " cruel ";
        });

        Flowable<String> cruel = Flowable.fromFuture(stringFuture);

        Flowable<String> world = Flowable.defer(() -> Flowable.just("world!"));

        System.in.read();
    }
}
