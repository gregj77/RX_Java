package rx.demo;

import com.google.common.collect.Lists;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.processors.AsyncProcessor;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.PublishSubject;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Demo04 {

    public static void main(String[] args) throws IOException {
        List<Integer> items = IntStream.range(0, 500).boxed().collect(Collectors.toList());

        Flowable.fromIterable(items)
                .filter(p -> (p % 2) == 0)
                .skip(10)
                .take(10)
                .compose(new AverageComposer())
                .subscribe(p -> System.out.println(p));

        System.in.read();
    }

    private static class AverageComposer implements FlowableTransformer<Integer, Double> {

        private double total;
        private double items;

        @Override
        public Publisher<Double> apply(Flowable<Integer> upstream) {
            AsyncProcessor<Double> result = AsyncProcessor.create();
            upstream.subscribe(
                    item -> {
                        total += item;
                        items++;
                    },
                    err -> {
                        System.out.println("ups..." + err);
                    },
                    () -> {
                        Double avg = total / items;
                        result.onNext(avg);
                        result.onComplete();
                    });
            return result;
        }
    }
}
