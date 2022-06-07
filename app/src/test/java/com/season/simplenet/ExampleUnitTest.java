package com.season.simplenet;

import com.season.net.NetRequest;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test() {
        NetRequest netRequest = new NetRequest();
        System.out.println(netRequest.test());
    }
}