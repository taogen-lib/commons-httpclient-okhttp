package com.taogen.commons.okhttp;

import com.taogen.commons.okhttp.enums.HttpMethod;
import com.taogen.commons.okhttp.vo.OkHttpRequest;
import com.taogen.commons.okhttp.vo.OkHttpRequestWithFormData;
import com.taogen.commons.okhttp.vo.OkHttpRequestWithJson;
import com.taogen.commons.okhttp.vo.OkHttpResponse;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
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

    public static OkHttpResponse requestWithoutBody(OkHttpRequest request) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(getHttpUrl(request.getUrl(), request.getQueryStringParams()));
        if (request.getHeaders() != null) {
            requestBuilder.headers(request.getHeaders());
        }
        addRequestMethod(requestBuilder, request.getMethod());
        try (Response response = OKHTTP_CLIENT.newCall(requestBuilder.build()).execute()) {
            // after response closed, the response body can't be read again.
            // so we need to save to a wrapper class object
            return new OkHttpResponse(response.code(), response.headers(), response.body().bytes());
        }
    }

    public static OkHttpResponse requestWithJson(OkHttpRequestWithJson request) throws IOException {
        if (request.getMethod() == HttpMethod.GET) {
            throw new IllegalArgumentException("GET method can't use json body");
        }
        RequestBody requestBody = getJsonRequestBody(request.getJson());
        Request.Builder requestBuilder = new Request.Builder()
                .url(getHttpUrl(request.getUrl(), request.getQueryStringParams()));
        if (request.getHeaders() != null) {
            requestBuilder.headers(request.getHeaders());
        }
        addRequestBody(requestBuilder, requestBody, request.getMethod());
        try (Response response = OKHTTP_CLIENT.newCall(requestBuilder.build()).execute()) {
            return new OkHttpResponse(response.code(), response.headers(), response.body().bytes());
        }
    }

    public static OkHttpResponse requestWithFormUrlEncoded(OkHttpRequestWithFormData okHttpRequest) throws IOException {
        if (okHttpRequest.getMethod() == HttpMethod.GET) {
            throw new IllegalArgumentException("GET method can't use form data body");
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(getHttpUrl(okHttpRequest.getUrl(), okHttpRequest.getQueryStringParams()));
        if (okHttpRequest.getHeaders() != null) {
            requestBuilder.headers(okHttpRequest.getHeaders());
        }
        addRequestBody(requestBuilder, getFormDataBody(okHttpRequest.getFormData()), okHttpRequest.getMethod());
        try (Response response = OKHTTP_CLIENT.newCall(requestBuilder.build()).execute()) {
            return new OkHttpResponse(response.code(), response.headers(), response.body().bytes());
        }
    }

    /**
     * multipart fields are File objects
     * @return
     * @throws IOException
     */
    public static OkHttpResponse requestWithFormData(OkHttpRequestWithFormData okHttpRequest) throws IOException {
        if (okHttpRequest.getMethod() == HttpMethod.GET) {
            throw new IllegalArgumentException("GET method can't use form data body");
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(getHttpUrl(okHttpRequest.getUrl(), okHttpRequest.getQueryStringParams()));
        if (okHttpRequest.getHeaders() != null) {
            requestBuilder.headers(okHttpRequest.getHeaders());
        }
        addRequestBody(requestBuilder, getMultipartBody(okHttpRequest.getFormData()), okHttpRequest.getMethod());
        try (Response response = OKHTTP_CLIENT.newCall(requestBuilder.build()).execute()) {
            return new OkHttpResponse(response.code(), response.headers(), response.body().bytes());
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
