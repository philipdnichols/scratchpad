package edu.self;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

public class FuturesExample {
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);
    
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        final Future<String> contentsFuture = startDownloading(new URL("http://www.google.com"));
        
        // other computation
        
        System.out.println("Are we done yet: " + contentsFuture.isDone());
        System.out.println("Are we cancelled: " + contentsFuture.isCancelled());
        
        while (!contentsFuture.isDone()) {
            System.out.print(".");
            Thread.sleep(1);
        }
        
        try {
            System.out.println(contentsFuture.get(5, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            System.out.println("Timeout!");
            contentsFuture.cancel(true);
        }
        
        /*System.out.println(downloadContents(new URL("http://www.google.com")));*/
        
        // Shutting down the pool will ensure the termination of the program..
        pool.shutdown();
    }
    
    /*public static String downloadContents(URL url) throws IOException {
        InputStream input = null;
        try {
            input = url.openStream();
            
            return IOUtils.toString(input, CharEncoding.UTF_8);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }*/
    
    public static Future<String> startDownloading(final URL url) {
        return pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                InputStream input = null;
                try {
                    input = url.openStream();
                    
                    return IOUtils.toString(input, CharEncoding.UTF_8);
                } finally {
                    if (input != null) {
                        input.close();
                    }
                }
            }
        });
    }
}