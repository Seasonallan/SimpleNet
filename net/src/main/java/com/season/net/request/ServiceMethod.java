package com.season.net.request;


import com.season.net.NetRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.season.net.request.Utils.methodError;


public abstract class ServiceMethod<T> {
    public static <T> ServiceMethod<T> parseAnnotations(NetRequest netRequest, Method method) {
        Type returnType = method.getGenericReturnType();
        if (Utils.hasUnresolvableType(returnType)) {
            throw methodError(
                    method,
                    "Method return type must not include a type variable or wildcard: %s",
                    returnType);
        }
        if (returnType == void.class) {
            throw methodError(method, "Service methods cannot return void.");
        }

        return new HttpServiceMethod(netRequest, method);
    }

    public abstract T invoke(Object[] args);



    public static class HttpServiceMethod extends ServiceMethod{

        public HttpServiceMethod(NetRequest netRequest, Method method) {
            super();
        }

        @Override
        public Object invoke(Object[] args) {
            return "10086";
        }
    }









}
