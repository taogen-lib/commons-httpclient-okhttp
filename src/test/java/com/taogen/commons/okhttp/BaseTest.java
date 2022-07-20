package com.taogen.commons.okhttp;

import com.taogen.commons.collection.MapUtils;
import com.taogen.commons.network.HttpRequestUtil;
import com.taogen.commons.okhttp.vo.OkHttpRequest;
import com.taogen.commons.okhttp.vo.OkHttpRequestWithFormData;
import com.taogen.commons.okhttp.vo.OkHttpRequestWithJson;
import lombok.extern.log4j.Log4j2;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.RecordedRequest;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.taogen.commons.collection.MapUtils.multiValueMapEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Taogen
 */
@Log4j2
public class BaseTest {
    public static final String APP_HEADER_KEY = "my-app-id";
    public static final String APP_HEADER_VALUE = "java-http-clients";
    /**
     * validate request
     * <p>
     * - url
     * - method
     * - headers
     * - query string params
     * - body
     *
     * @param mockedRealRequest
     * @param okHttpRequest
     */
    protected void validateRequestWithQueryString(RecordedRequest mockedRealRequest, OkHttpRequest okHttpRequest) {
        log.debug("mocked real request: {}", mockedRealRequest);
        String actualUrl = mockedRealRequest.getRequestUrl().toString();
        if (actualUrl.indexOf("?") > 0) {
            actualUrl = actualUrl.substring(0, actualUrl.indexOf("?"));
        }
        // validate URL
        assertEquals(okHttpRequest.getUrl(), actualUrl);
        // validate method
        assertEquals(okHttpRequest.getMethod().name(), mockedRealRequest.getMethod().toString());
        // validate headers
        log.debug("okHttpRequest header: {}", okHttpRequest.getHeaders().toMultimap());
        log.debug("mockedRealRequest header: {}", mockedRealRequest.getHeaders().toMultimap());
        assertTrue(MapUtils.multiStringValueMapContains(mockedRealRequest.getHeaders().toMultimap(), okHttpRequest.getHeaders().toMultimap()));
        // validate query string params
        Map<String, List<Object>> actualQueryStringParams = getQueryStringParamsByRecordedRequest(mockedRealRequest.getRequestUrl());
        log.debug("okHttpRequest query param: {}", okHttpRequest.getQueryStringParams());
        log.debug("mockedRealRequest query param: {}", actualQueryStringParams);
        assertTrue(multiValueMapEquals(okHttpRequest.getQueryStringParams(), actualQueryStringParams));
        // validate body
    }

    protected void validateRequestWithJson(RecordedRequest mockedRealRequest,
                                           OkHttpRequestWithJson okHttpRequestWithJson) {
        validateRequestWithQueryString(mockedRealRequest, (OkHttpRequest) okHttpRequestWithJson);
        // validate body
        log.debug("okHttpRequestWithJson body: {}", okHttpRequestWithJson.getJson());
        String actualBody = mockedRealRequest.getBody().readUtf8();
        log.debug("mockedRealRequest body: {}", actualBody);
        assertEquals(okHttpRequestWithJson.getJson(), actualBody);
    }

    protected void validateRequestWithUrlEncodedForm(RecordedRequest mockedRealRequest, OkHttpRequestWithFormData okHttpRequestWithFormData) {
        validateRequestWithQueryString(mockedRealRequest, (OkHttpRequest) okHttpRequestWithFormData);
        // validate body
        log.debug("okHttpRequestWithFormData formData: {}", okHttpRequestWithFormData.getFormData());
        String actualFormData = mockedRealRequest.getBody().readUtf8();
        String contentType = mockedRealRequest.getHeader("Content-Type");
        log.debug("content type: {}", contentType);
        log.debug("mockedRealRequest formData: {}", actualFormData);
        LinkedHashMap<String, List<Object>> mockedFormDataMap = HttpRequestUtil.queryStringToMultiValueMap(actualFormData);
        log.debug("mockedRealRequest formDataMap: {}", mockedFormDataMap);
        Map<String, List<Object>> requestFormDataMap = okHttpRequestWithFormData.getFormData();
        log.debug("okHttpRequestWithFormData formDataMap: {}", requestFormDataMap);
        assertTrue(MapUtils.multiValueMapEquals(requestFormDataMap, mockedFormDataMap));
    }
    protected void validateRequestWithMultipartForm(RecordedRequest mockedRealRequest,
                                                    OkHttpRequestWithFormData okHttpRequestWithFormData) {
        validateRequestWithQueryString(mockedRealRequest, (OkHttpRequest) okHttpRequestWithFormData);
        // validate body
        log.debug("okHttpRequestWithFormData formData: {}", okHttpRequestWithFormData.getFormData());
        String actualFormData = mockedRealRequest.getBody().readUtf8();
        String contentType = mockedRealRequest.getHeader("Content-Type");
        log.debug("content type: {}", contentType);
        log.debug("mockedRealRequest formData: {}", actualFormData);
        LinkedHashMap<String, List<Object>> mockedFormDataMap = HttpRequestUtil.multipartDataToMultiValueMap(
                actualFormData.getBytes(StandardCharsets.UTF_8), "--" + HttpRequestUtil.getBoundaryByContentType(contentType));
        log.debug("mockedRealRequest formDataMap: {}", mockedFormDataMap);
        Map<String, List<Object>> requestFormDataMap = okHttpRequestWithFormData.getFormData();
        log.debug("okHttpRequestWithFormData formDataMap: {}", requestFormDataMap);
        assertTrue(MapUtils.multiValueMapEquals(requestFormDataMap, mockedFormDataMap));
    }
    protected Map<String, List<Object>> getQueryStringParamsByRecordedRequest(HttpUrl requestUrl) {
        Map<String, List<Object>> queryParams = requestUrl.queryParameterNames().stream()
                .collect(HashMap::new, (map, key) -> map.put(key, new ArrayList<Object>(requestUrl.queryParameterValues(key))), Map::putAll);
        return queryParams;
    }

    protected Headers getOkHttpBasicHeaders() {
        Headers headers = new Headers.Builder()
                .add(APP_HEADER_KEY, APP_HEADER_VALUE)
                .build();
        return headers;
    }

}
