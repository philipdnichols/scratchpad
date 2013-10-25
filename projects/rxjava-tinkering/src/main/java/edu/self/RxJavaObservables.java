package edu.self;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;

public class RxJavaObservables {
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);
    
    public static void main(String[] args) {
        /*Observable<Integer> o = Observable.from(1, 2, 3, 4, 5, 6);
        Observable<String> s = Observable.from("a", "b", "c");
        
        List<Double> doubles = new ArrayList<Double>();
        doubles.add(1.1);
        doubles.add(1.2);
        Observable<Double> d = Observable.from(doubles);
        
        Observable<String> j = Observable.just("one object");*/
        
        /*customObservableBlocking().subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }

            @SuppressWarnings("unused")
            @Override
            public void onError(Throwable e) {
                System.out.println("onError");
            }

            @Override
            public void onNext(String args) {
                System.out.println("onNext: " + args);
            }
        });
        customObservableNonBlocking().subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }

            @SuppressWarnings("unused")
            @Override
            public void onError(Throwable e) {
                System.out.println("onError");
            }

            @Override
            public void onNext(String args) {
                System.out.println("onNext: " + args);
            }
        });
        System.out.println("Done!");*/
        
        /*Observable<String> obs = customObservableNonBlockingFuture().skip(10).take(5).map(new Func1<String, String>() {
            @Override
            public String call(String string) {
               return string + "_transformed";
            }
        });
        Subscription sub = obs.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onError(Throwable e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onNext(String args) {
                System.out.println("onNext => " + args);
            }
        });*/
        
        fetchWikipediaArticleAsynchronously(new String[] {"Tiger", /*"NonExistantTitle"*/ "Elephant"}).subscribe(
            new Action1<String>() {
                @Override
                public void call(String t1) {
                    System.out.println("--- Article ---\n" + t1.substring(0, 150));
                }
            },
            new Action1<Throwable>() {
                @Override
                public void call(Throwable t1) {
                    System.out.println("--- Error ---\n" + t1.getMessage());
                }
            }
        );
        
        pool.shutdown();
    }
    
    public static Observable<String> customObservableBlocking() {
        return Observable.create(new OnSubscribeFunc<String>() {
            @Override
            public Subscription onSubscribe(Observer<? super String> observer) {
                for (int i = 0; i < 50; i++) {
                    observer.onNext("value_" + i);
                }
                
                // After send all values we complete the sequence
                observer.onCompleted();
                
                // return an empty Subscription since this blocks and thus can't be unsubscribed from
                return Subscriptions.empty();
            }
        });
    }
    
    public static Observable<String> customObservableNonBlocking() {
        return Observable.create(new OnSubscribeFunc<String>() {
            @Override
            public Subscription onSubscribe(final Observer<? super String> observer) {
                // For simplicity this example uses a Thread instead of an ExecutorService/ThreadPool
                final Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 75; i++) {
                            observer.onNext("anotherValue_" + i);
                        }
                        // After sending all values we complete the sequence
                        observer.onCompleted();
                    }
                });
                t.start();
                
                return new Subscription() {
                    @Override
                    public void unsubscribe() {
                        // Ask the thread to stop doing work.
                        // For this simple example it just interrupts.
                        t.interrupt();
                    }
                };
            }
        });
    }
    
    public static Observable<String> customObservableNonBlockingFuture() {
        return Observable.create(new OnSubscribeFunc<String>() {
            @Override
            public Subscription onSubscribe(final Observer<? super String> observer) {
                return Subscriptions.from(pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 75; i++) {
                            observer.onNext("anotherValue_" + i);
                        }
                        // After sending all values we complete the sequence
                        observer.onCompleted();
                    }
                }));
            }
        });
    }
    
    public static Observable<String> fetchWikipediaArticleAsynchronously(final String[] wikipediaArticles) {
        return Observable.create(new OnSubscribeFunc<String>() {
            @Override
            public Subscription onSubscribe(final Observer<? super String> observer) {
                return Subscriptions.from(pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (String articleName : wikipediaArticles) {
                                observer.onNext(IOUtils.toString((InputStream) new URL("http://en.wikipedia.org/wiki/" + articleName).getContent()));
                            }
                            observer.onCompleted();
                        } catch (Exception e) {
                            observer.onError(e);
                        }
                    }
                }));
            }
        });
    }
}