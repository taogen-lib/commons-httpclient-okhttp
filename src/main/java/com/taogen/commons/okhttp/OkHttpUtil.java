package com.taogen.commons.okhttp;

import com.taogen.easyhttpclient.enums.HttpMethod;
import com.taogen.easyhttpclient.vo.*;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Taogen
 */
public class OkHttpUtil {

    private static OkHttpClient OKHTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    public static HttpResponse requestWithoutBody(HttpRequest request) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(getHttpUrl(request.getUrl(), request.getQueryParams()));
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            requestBuilder.headers(convertMapToOkHttpHeaders(request.getHeaders()));
        }
        addRequestMethod(requestBuilder, request.getMethod());
        try (Response response = OKHTTP_CLIENT.newCall(requestBuilder.build()).execute()) {
            // after response closed, the response body can't be read again.
            // so we need to save to a wrapper class object
            return new HttpResponse(response.code(), convertOkHttpHeadersToMap(response.headers()), response.body().bytes());
        }
    }

    private static Map<String, List<Object>> convertOkHttpHeadersToMap(Headers headers) {
        if (headers == null || headers.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, List<Object>> resultMap = new HashMap<>();
//        Iterator<Pair<String, String>> iterator = headers.iterator();
        for (String name : headers.names()) {
            if (resultMap.get(name) == null) {
                resultMap.put(name, new ArrayList<>());
            }
            resultMap.get(name).add(headers.get(name));
        }
        return resultMap;
    }

    private static Headers convertMapToOkHttpHeaders(Map<String, List<Object>> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Headers.Builder builder = new Headers.Builder();
        for (Map.Entry<String, List<Object>> entry : map.entrySet()) {
            for (Object value : entry.getValue()) {
                builder.add(entry.getKey(), String.valueOf(value));
            }
        }
        return builder.build();
    }

    public static HttpResponse requestWithJson(HttpRequestWithJson request) throws IOException {
        if (request.getMethod() == HttpMethod.GET) {
            throw new IllegalArgumentException("GET method can't use json body");
        }
        RequestBody requestBody = getJsonRequestBody(request.getJson());
        Request.Builder requestBuilder = new Request.Builder()
                .url(getHttpUrl(request.getUrl(), request.getQueryParams()));
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            requestBuilder.headers(convertMapToOkHttpHeaders(request.getHeaders()));
        }
        addRequestBody(requestBuilder, requestBody, request.getMethod());
        try (Response response = OKHTTP_CLIENT.newCall(requestBuilder.build()).execute()) {
            return new HttpResponse(response.code(), convertOkHttpHeadersToMap(response.headers()), response.body().bytes());
        }
    }

    public static HttpResponse requestWithFormUrlEncoded(HttpRequestWithForm request) throws IOException {
        if (request.getMethod() == HttpMethod.GET) {
            throw new IllegalArgumentException("GET method can't use form data body");
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(getHttpUrl(request.getUrl(), request.getQueryParams()));
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            requestBuilder.headers(convertMapToOkHttpHeaders(request.getHeaders()));
        }
        addRequestBody(requestBuilder, getFormDataBody(request.getFormData()), request.getMethod());
        try (Response response = OKHTTP_CLIENT.newCall(requestBuilder.build()).execute()) {
            return new HttpResponse(response.code(), convertOkHttpHeadersToMap(response.headers()), response.body().bytes());
        }
    }

    /**
     * multipart fields are File objects
     *
     * @return
     * @throws IOException
     */
    public static HttpResponse requestWithFormData(HttpRequestWithMultipart request) throws IOException {
        if (request.getMethod() == HttpMethod.GET) {
            throw new IllegalArgumentException("GET method can't use form data body");
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(getHttpUrl(request.getUrl(), request.getQueryParams()));
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            requestBuilder.headers(convertMapToOkHttpHeaders(request.getHeaders()));
        }
        addRequestBody(requestBuilder, getMultipartBody(request.getFormData()), request.getMethod());
        try (Response response = OKHTTP_CLIENT.newCall(requestBuilder.build()).execute()) {
            return new HttpResponse(response.code(), convertOkHttpHeadersToMap(response.headers()), response.body().bytes());
        }
    }

    private static RequestBody getJsonRequestBody(String json) {
        return RequestBody.create(
                json, MediaType.parse("application/json"));
    }

    private static void addRequestBody(Request.Builder requestBuilder, RequestBody requestBody, HttpMethod method) {
        if (method == HttpMethod.POST) {
            requestBuilder.post(requestBody);
        } else if (method == HttpMethod.PUT) {
            requestBuilder.put(requestBody);
        } else if (method == HttpMethod.DELETE) {
            requestBuilder.delete(requestBody);
        }
    }

    private static void addRequestMethod(Request.Builder requestBuilder, HttpMethod method) {
        if (method == HttpMethod.GET) {
            requestBuilder.get();
        } else if (method == HttpMethod.POST) {
            requestBuilder.post(null);
        } else if (method == HttpMethod.PUT) {
            requestBuilder.put(null);
        } else if (method == HttpMethod.DELETE) {
            requestBuilder.delete();
        }
    }

    private static HttpUrl getHttpUrl(String url,
                                      Map<String, List<Object>> queryStringParams) {
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        if (queryStringParams != null) {
            for (Map.Entry<String, List<Object>> entry : queryStringParams.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    for (Object value : entry.getValue()) {
                        builder.addQueryParameter(entry.getKey(), String.valueOf(value));
                    }
                }
            }
        }
        return builder.build();
    }

    private static RequestBody getMultipartBody(Map<String, List<Object>> formData) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (formData != null) {
            for (String key : formData.keySet()) {
                List<Object> values = formData.get(key);
                if (values != null) {
                    values.forEach(item -> {
                        if (item instanceof File) {
                            File file = (File) item;
                            String fileName = file.getName();
                            String mediaType = URLConnection.guessContentTypeFromName(fileName);
                            if (mediaType == null) {
                                mediaType = "application/octet-stream";
                            }
                            builder.addFormDataPart(key, fileName,
                                    RequestBody.create(file, MediaType.parse(mediaType)));
                        } else {
                            builder.addFormDataPart(key, item.toString());
                        }
                    });
                }
            }
        }
        return builder.build();
    }

    private static RequestBody getFormDataBody(Map<String, List<Object>> formData) {
        FormBody.Builder builder = new FormBody.Builder();
        if (formData != null) {
            for (String key : formData.keySet()) {
                List<Object> values = formData.get(key);
                if (values != null) {
                    values.forEach(item -> builder.add(key, String.valueOf(item)));
                }
            }
        }
        return builder.build();
    }
}
