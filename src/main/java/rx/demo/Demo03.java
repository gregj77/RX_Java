package rx.demo;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Demo03 {
    public static void main(String[] args) {
        List<Integer> items = IntStream.range(0, 200).boxed().collect(Collectors.toList());

        PublishSubject<Object> publishSubject = PublishSubject.create();
        Flowable.fromIterable(items).subscribe(publishSubject::onNext);
        System.out.println("PublishSubject");
        publishSubject.subscribe(p -> System.out.println(p));

        System.out.println("BehaviorSubject");
        BehaviorSubject<Integer> behaviorSubject = BehaviorSubject.createDefault(-100);
        Disposable subscribe = behaviorSubject.subscribe(p -> System.out.println(p));
        subscribe.dispose();
        Flowable.fromIterable(items).subscribe(behaviorSubject::onNext);
        behaviorSubject.subscribe(p -> System.out.println(p));

        ReplaySubject<Integer> replaySubject = ReplaySubject.createWithSize(10);
        Flowable.fromIterable(items).subscribe(replaySubject::onNext);
        System.out.println("ReplySubject");
        replaySubject.subscribe(p -> System.out.println(p));

        System.out.println("AsyncSubject");
        AsyncSubject<Integer> asyncSubject = AsyncSubject.create();
        Flowable.fromIterable(items).subscribe(asyncSubject::onNext);
        asyncSubject.subscribe(p -> System.out.println(p));
        asyncSubject.onComplete();

    }
}
