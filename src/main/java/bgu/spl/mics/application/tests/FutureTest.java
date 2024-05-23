package bgu.spl.mics.application.tests;

import bgu.spl.mics.Future;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {
    private static Future future;
    private static TestModelEvent event;
    private static Model model;
    private static Data data;

    @Before
    public void setUp() throws Exception {
        data = new Data("Images", 20000);
        model = new Model("name",data);
        future = new Future();
    }

    @Test
    public void get() {
        future.resolve(model);
        assertTrue(future.isDone());
        assertSame(model , future.get());
    }

    @Test
    public void resolve() {
        assertFalse(future.isDone());
        future.resolve(model);
        assertTrue(future.isDone());
    }

    @Test
    public void isDone() {
        future.resolve(model);
        assertTrue(future.isDone());
    }

    @Test
    public void testGet() {
        Thread t1 = new Thread(()->{
            try {   Thread.sleep(2000); }
            catch (InterruptedException e) {    e.printStackTrace();    }
            future.resolve(model);
        });
        t1.start();
        assertNotSame(model , future.get(500,TimeUnit.MILLISECONDS));
        assertSame(model , future.get(2500,TimeUnit.MILLISECONDS));
    }
}