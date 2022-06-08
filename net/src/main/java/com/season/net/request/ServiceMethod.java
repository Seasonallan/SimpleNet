package com.season.net.request;


import com.season.net.Retrofit;
import com.season.net.http.Field;
import com.season.net.http.GET;
import com.season.net.http.POST;
import com.season.net.http.Path;
import com.season.net.http.Query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.season.net.request.Utils.methodError;


public abstract class ServiceMethod<T> {
    public static <T> ServiceMethod<T> parseAnnotations(Retrofit retrofit, Method method) {
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

        return new HttpServiceMethod(retrofit, method);
    }

    public abstract T invoke(Object[] args);


    public static class HttpServiceMethod extends ServiceMethod {


        final Retrofit retrofit;
        final Method method;
        final Annotation[] methodAnnotations;
        final Annotation[][] parameterAnnotationsArray;
        final Type[] parameterTypes;


        public HttpServiceMethod(Retrofit retrofit, Method method) {
            this.retrofit = retrofit;
            this.method = method;
            this.methodAnnotations = method.getAnnotations();
            this.parameterTypes = method.getGenericParameterTypes();
            this.parameterAnnotationsArray = method.getParameterAnnotations();
        }

        @Override
        public Object invoke(Object[] args) {
            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }

            int parameterCount = parameterAnnotationsArray.length;
            for (int p = 0, lastParameter = parameterCount - 1; p < parameterCount; p++) {
                parseParamsAnnotation(parameterAnnotationsArray[p], args[p], p == lastParameter);
            }

            try {
                String url = retrofit.baseUrl + relativeUrl + queryUrl;
                System.out.println(url);
                if (httpMethod == "Get") {
                    return SimpleRequest.getRequest(url);
                } else {
                    return SimpleRequest.postRequest(url, params);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return httpMethod + "," + relativeUrl;
        }


        String httpMethod;
        String relativeUrl = "";
        String queryUrl = "";
        Map<String, String> params;

        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof GET) {
                parseHttpMethodAndPath("GET", ((GET) annotation).value());
            } else if (annotation instanceof POST) {
                parseHttpMethodAndPath("POST", ((POST) annotation).value());
            }
        }

        private void parseHttpMethodAndPath(String httpMethod, String value) {
            this.httpMethod = httpMethod;
            this.relativeUrl = value;
        }


        private void parseParamsAnnotation(Annotation[] annotations, Object arg, boolean isLast) {
            if (annotations != null) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Query) {
                        Query query = (Query) annotation;
                        String name = query.value();
                        if (queryUrl.isEmpty()) {
                            queryUrl += "?";
                        }
                        if (isLast) {
                            queryUrl += name + "=" + arg;
                        } else {
                            queryUrl += name + "=" + arg;
                            queryUrl += "&";
                        }
                    } else if (annotation instanceof Path) {
                        Path path = (Path) annotation;
                        String name = path.value();
                        relativeUrl.replace("{" + name + "}", arg.toString());
                    } else if (annotation instanceof Field) {
                        Field field = (Field) annotation;
                        String name = field.value();
                        if (params == null) {
                            params = new HashMap<>();
                        }
                        params.put(name, arg.toString());
                    }
                }
            }
        }

    }


}
