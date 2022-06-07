package com.season.net;

import com.season.net.request.ServiceMethod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetRequest {

    public <T> T create(final Class<T> service) {
        return (T)
                Proxy.newProxyInstance(
                        service.getClassLoader(),
                        new Class<?>[] {service},
                        new InvocationHandler() {
                            private final Object[] emptyArgs = new Object[0];

                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args)
                                    throws Throwable {
                                // If the method is a method from Object then defer to normal invocation.
                                if (method.getDeclaringClass() == Object.class) {
                                    return method.invoke(this, args);
                                }
                                args = args != null ? args : emptyArgs;
                                return loadServiceMethod(method).invoke(args);
                            }
                        });
    }


    private final Map<Method, ServiceMethod<?>> serviceMethodCache = new ConcurrentHashMap<>();
    ServiceMethod<?> loadServiceMethod(Method method) {
        ServiceMethod<?> result = serviceMethodCache.get(method);
        if (result != null) return result;

        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = ServiceMethod.parseAnnotations(this, method);
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    public String test(){
        return "test";
    }


}
