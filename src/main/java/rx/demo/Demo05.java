package rx.demo;

import com.google.common.collect.Maps;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Demo05 {

    private static Map<String, String> codes;

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        codes = loadCodes();
        new Form();
    }


    private static Map<String, String> loadCodes() throws URISyntaxException, FileNotFoundException {
        URL resource = Demo05.class.getClassLoader().getResource("kody.csv");
        File file = new File(resource.toURI());
        Map<String, String> codes = Maps.newHashMap();
        try (Reader rdr = new FileReader(file)){
            CSVParser parser = new CSVParser(rdr, CSVFormat.newFormat(';'));
            parser.forEach(record -> codes.put(record.get(0), record.get(2)));
            return codes;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return codes;
    }

    private static Flowable<String> lookupCityByCode(String codePrefix) {

        return Flowable.create(emitter -> {
            if (codePrefix == null) {
                emitter.onError(new IllegalAccessException("codePrefix"));
                return;
            }

            if (codePrefix.length() == 0) {
                emitter.onComplete();
                return;
            }

            Iterator<Map.Entry<String, String>> iterator = codes.entrySet().iterator();

            Disposable expensiveLookupHandle = Schedulers.computation().schedulePeriodicallyDirect(() -> {

                for (int i = 0; i < 10; ++i) {
                    if (iterator.hasNext()) {
                        Map.Entry<String, String> next = iterator.next();
                        if (next.getKey().startsWith(codePrefix)) {
                            emitter.onNext(next.getValue());
                        }
                    } else {
                        System.out.println("lookup complete!");
                        emitter.onComplete();
                        break;
                    }
                }

            }, 100L, 1L, TimeUnit.MILLISECONDS);

            Disposable notify = Disposables.fromAction(() -> System.out.println("action completed!"));

            CompositeDisposable compositeDisposable = new CompositeDisposable(expensiveLookupHandle, notify);

            emitter.setDisposable(compositeDisposable);

        }, BackpressureStrategy.ERROR);

    }

    private static class Form extends JFrame{
        public Form() {
            TextField query = new TextField();
            query.setBounds(5, 15, 420, 25);
            add(query);

            DefaultListModel<String> model = new DefaultListModel<>();


            Flowable<String> userInput = Flowable.create(emitter -> {
                query.addTextListener(e -> {
                    String queryText = ((TextField) e.getSource()).getText();
                    model.clear();
                    emitter.onNext(queryText);
                });

            }, BackpressureStrategy.ERROR);

            setupRx(model, userInput);

            JList<String> results = new JList<>(model);
            results.setBounds(5, 40, 420, 210);
            add(results);

            setSize(450, 300);
            setLayout(null);
            setVisible(true);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setTitle("Demo 05 - complex flowables");
        }

        private void setupRx(DefaultListModel<String> model, Flowable<String> userInput) {

            Flowable<Flowable<String>> complexFlowable = userInput
                    .map(query ->
                            lookupCityByCode(query)
                    )
                ;

            Flowable<String> resultFlowable = Flowable.switchOnNext(complexFlowable);

            resultFlowable
                    .doOnNext(p -> System.out.println("got - " + p))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation())
                    .subscribe(item -> SwingUtilities.invokeLater(() -> model.addElement(item)));
        }
    }
}












/*
        private void setupRx(DefaultListModel<String> model, Flowable<String> userInput) {
            Flowable<Flowable<String>> complexFlowable = userInput
                    .debounce(500, TimeUnit.MILLISECONDS, Schedulers.computation())
                    .map(query ->
                            lookupCityByCode(query)
                            .take(1000)
                            .buffer(1000)
                            .map(p -> {
                                 Collections.sort(p);
                                 return p;
                            })
                            .flatMap(p -> Flowable.fromIterable(p))
                            .distinctUntilChanged()
                            .take(5)
                    )
                ;
            Flowable<String> resultFlowable = Flowable.switchOnNext(complexFlowable);

            resultFlowable
                    .doOnNext(p -> System.out.println("got - " + p))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation())
                    .subscribe(item -> SwingUtilities.invokeLater(() -> model.addElement(item)));
        }

 */