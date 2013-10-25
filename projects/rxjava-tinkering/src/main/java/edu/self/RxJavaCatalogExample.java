package edu.self;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

public class RxJavaCatalogExample {
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);
    
    public static void main(String[] args) {
        BankAccount myBankAccount = new BankAccount();
        
        Observable.zip(myBankAccount.getBalance(),
                       getCatalog("Montgomery Ward")
                           .mapMany(
                                new Func1<Catalog, Observable<? extends Jeans>>() {
                                    @Override
                                    public Observable<? extends Jeans> call(Catalog catalog) {
                                        return catalog.findJeans("38W", "38L", "blue");
                                    }
                                }
                            ),
                       new Func2<Double, Jeans, Boolean>() {
                           @Override
                           public Boolean call(Double cash, Jeans jeans) {
                               if (cash > jeans.getPurchasePrice()) {
                                   return jeans.purchase();
                               }
                               return Boolean.FALSE;
                           }
                       })
                       .subscribe(
                           new Action1<Boolean>() {
                               @Override
                               public void call(Boolean success) {
                                   System.out.println("We purchased the jeans: " + success);
                               }
                           },
                           new Action1<Throwable>() {
                               @Override
                               public void call(Throwable t1) {
                                   System.out.println(t1);
                               }
                           }
                       );
        
        pool.shutdown();
    }
    
    public static Observable<Catalog> getCatalog(final String catalogName) {
        return Observable.create(new OnSubscribeFunc<Catalog>() {
            @Override
            public Subscription onSubscribe(final Observer<? super Catalog> observer) {
                return Subscriptions.from(pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        observer.onNext(new Catalog(catalogName));
                        observer.onCompleted();
                    }
                }));
                /*observer.onNext(new Catalog(catalogName));
                observer.onCompleted();
                
                return Subscriptions.empty();*/
            }
        });
    }
    
    public static class BankAccount {
        public Observable<Double> getBalance() {
            return Observable.create(new OnSubscribeFunc<Double>() {
                @Override
                public Subscription onSubscribe(final Observer<? super Double> observer) {
                    return Subscriptions.from(pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            observer.onNext(3000.0);
                            observer.onCompleted();
                        }
                    }));
                    /*observer.onNext(3000.0);
                    observer.onCompleted();
                    
                    return Subscriptions.empty();*/
                }
            });
        }
    }
    
    public static class Catalog {
        public String catalogName;
        
        public Catalog(String catalogName) {
            this.catalogName = catalogName;
        }
        
        public Observable<Jeans> findJeans(final String width, final String length, final String color) {
            return Observable.create(new OnSubscribeFunc<Jeans>() {
                @Override
                public Subscription onSubscribe(final Observer<? super Jeans> observer) {
                    /*return Subscriptions.from(pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            observer.onNext(new Jeans(width, length, color));
                            observer.onCompleted();
                        }
                    }));*/
                    observer.onNext(new Jeans(width, length, color));
                    observer.onCompleted();
                    
                    return Subscriptions.empty();
                }
            });
        }
    }
    
    public static class Jeans {
        public String width, length, color;
        
        public Jeans(String width, String length, String color) {
            this.width = width;
            this.length = length;
            this.color = color;
        }
        
        public Double getPurchasePrice() {
            return 300.0;
        }
        
        public Boolean purchase() {
            return Boolean.TRUE;
        }
        
        @Override
        public String toString() {
            return "Width: " + width + " Length: " + length + " Color: " + color;
        }
    }
}