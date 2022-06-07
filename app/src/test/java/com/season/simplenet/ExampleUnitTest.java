package com.season.simplenet;

import com.season.net.NetRequest;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private Service service;

    interface Service {
        String test();
    }

    @Before
    public void setUp() {
        NetRequest retrofit =
                new NetRequest();
        service = retrofit.create(Service.class);
    }
    @Test
    public void test() {
        System.out.println(service.test());
    }
}