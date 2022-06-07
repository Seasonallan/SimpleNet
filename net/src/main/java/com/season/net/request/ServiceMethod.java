package com.season.net.request;


import com.season.net.Retrofit;
import com.season.net.http.GET;
import com.season.net.http.POST;
import com.season.net.http.Query;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
        private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
        private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);

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
                if (parameterAnnotationsArray[p] != null) {
                    queryUrl += "?";
                    for (Annotation annotation : parameterAnnotationsArray[p]) {
                        if (annotation instanceof Query) {
                            Query query = (Query) annotation;
                            String name = query.value();
                            System.out.println(name);
                            if (p == lastParameter) {
                                queryUrl += name + "=" + args[p];
                            } else {
                                queryUrl += name + "=" + args[p];
                                queryUrl += "&";
                            }
                        }
                    }
                }
            }

            try {
                URL url = new URL(retrofit.baseUrl + relativeUrl + queryUrl);
                System.out.println(retrofit.baseUrl + relativeUrl + queryUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod(httpMethod);
                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("请求url失败");
                }
                InputStream inStream = conn.getInputStream();
                byte[] bt = read(inStream);
                inStream.close();
                return new String(bt);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return httpMethod + "," + relativeUrl;
        }

        //从流中读取数据
        public static byte[] read(InputStream inStream) throws Exception {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            return outStream.toByteArray();
        }


        String httpMethod;
        String relativeUrl = "";
        String queryUrl= "";

        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof GET) {
                parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
            } else if (annotation instanceof POST) {
                parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
            }
        }

        private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {
            this.httpMethod = httpMethod;

            if (value.isEmpty()) {
                return;
            }

            // Get the relative URL path and existing query string, if present.
            int question = value.indexOf('?');
            if (question != -1 && question < value.length() - 1) {
                // Ensure the query string does not have any named parameters.
                String queryParams = value.substring(question + 1);
                Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(queryParams);
                if (queryParamMatcher.find()) {
                    throw methodError(
                            method,
                            "URL query string \"%s\" must not have replace block. "
                                    + "For dynamic query parameters use @Query.",
                            queryParams);
                }
            }

            this.relativeUrl = value;
        }

    }


}
