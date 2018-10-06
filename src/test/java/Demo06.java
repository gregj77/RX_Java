import com.google.common.collect.Maps;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.schedulers.TestScheduler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo06 {

    @Test
    public void testSchedulerExample() {

        TestScheduler testScheduler = new TestScheduler();

        Map<Integer, Long> result = Maps.newHashMap();

        Flowable.create((FlowableEmitter<Integer> emitter) -> {
            AtomicInteger value = new AtomicInteger(0);
            testScheduler.schedulePeriodicallyDirect(() -> emitter.onNext(value.getAndIncrement()), 100L, 100L, TimeUnit.MILLISECONDS);
        }, BackpressureStrategy.ERROR)
                .skip(10)
                .take(5)
                .subscribe(next -> result.put(next, Long.valueOf(testScheduler.now(TimeUnit.MILLISECONDS))));

        testScheduler.advanceTimeTo(2L, TimeUnit.MINUTES);

        Assert.assertEquals(5, result.size());
        Assert.assertEquals(Long.valueOf(1100L), result.get(10));
        Assert.assertEquals(Long.valueOf(1500L), result.get(14));
    }
}
