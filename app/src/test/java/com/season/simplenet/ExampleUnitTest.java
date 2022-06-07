package com.season.simplenet;

import com.season.net.Retrofit;
import com.season.net.http.GET;
import com.season.net.http.Path;
import com.season.net.http.Query;

import org.junit.Before;
import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private Service service;

    interface Service {
        @GET("/login/{id}")
        String test(@Path ("id")String id, @Query("name") String name);
        @GET
        String user(@Query ("nat")String nat, @Query("results") String results);
    }

    @Before
    public void setUp() {
        Retrofit retrofit =
                new Retrofit();
        service = retrofit.create(Service.class);
    }

    @Test
    public void test() {
        System.out.println(service.user("US","1"));
    }
}