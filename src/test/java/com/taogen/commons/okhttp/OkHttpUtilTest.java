package com.taogen.commons.okhttp;

import com.taogen.commons.okhttp.enums.HttpMethod;
import com.taogen.commons.okhttp.vo.OkHttpRequest;
import com.taogen.commons.okhttp.vo.OkHttpRequestWithFormData;
import com.taogen.commons.okhttp.vo.OkHttpRequestWithJson;
import com.taogen.commons.okhttp.vo.OkHttpResponse;
import lombok.extern.log4j.Log4j2;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class OkHttpUtilTest extends BaseTest {
    public static final String RANDOM_TOKEN = "123456";

    public static final String CHINESE_TEST = "中文测试";

    public static final String RESPONSE_BODY_1 = "{\"id\": 1, \"name\": \"test\"}";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void requestWithoutBody_get() throws IOException, InterruptedException {
        String url = getMockWebServerUrl("/testRequestWithoutBody_get", RESPONSE_BODY_1);
        Map<String, List<Object>> queryParams = new HashMap<>();
        queryParams.put("id", Arrays.asList(1));
        queryParams.put("name", Arrays.asList("test"));
        OkHttpRequest okHttpRequest = OkHttpRequest.builder()
                .url(url)
                .method(HttpMethod.GET)
                .headers(getOkHttpBasicHeaders())
                .queryStringParams(queryParams)
                .build();
        OkHttpResponse okHttpResponse = OkHttpUtil.requestWithoutBody(okHttpRequest);
        log.info("okHttpResponse: {}", okHttpResponse);
        // validate response
        assertEquals(200, okHttpResponse.getStatusCode());
        assertEquals(RESPONSE_BODY_1, okHttpResponse.getBodyString());
        // validate request
        RecordedRequest request = mockWebServer.takeRequest();
        validateRequestWithQueryString(request, okHttpRequest);
    }


    @Test
    void requestWithJson() throws IOException, InterruptedException {
        String url = getMockWebServerUrl("/testRequestWithJson", RESPONSE_BODY_1);
        String requestBody = "{\"id\": 1}";
        OkHttpRequestWithJson okHttpRequest = OkHttpRequestWithJson.builder()
                .url(url)
                .method(HttpMethod.POST)
                .headers(getOkHttpBasicHeaders())
                .json(requestBody)
                .build();
        OkHttpResponse okHttpResponse = OkHttpUtil.requestWithJson(okHttpRequest);
        log.info("okHttpResponse: {}", okHttpResponse);
        // validate response
        assertEquals(200, okHttpResponse.getStatusCode());
        assertEquals(RESPONSE_BODY_1, okHttpResponse.getBodyString());
        // validate request
        validateRequestWithJson(mockWebServer.takeRequest(), okHttpRequest);
    }


    @Test
    void requestWithFormUrlEncoded() throws InterruptedException, IOException {
        String url = getMockWebServerUrl("/testRequestWithFormUrlEncoded", RESPONSE_BODY_1);
        Map<String, List<Object>> formData = new HashMap<>();
        formData.put("id", Arrays.asList(1));
        formData.put("name", Arrays.asList("test", "test2"));
        OkHttpRequestWithFormData okHttpRequest = OkHttpRequestWithFormData.builder()
                .url(url)
                .method(HttpMethod.POST)
                .headers(getOkHttpBasicHeaders())
                .formData(formData)
                .build();
        OkHttpResponse okHttpResponse = OkHttpUtil.requestWithFormUrlEncoded(okHttpRequest);
        log.info("okHttpResponse: {}", okHttpResponse);
        // validate response
        assertEquals(200, okHttpResponse.getStatusCode());
        assertEquals(RESPONSE_BODY_1, okHttpResponse.getBodyString());
        // validate request
        validateRequestWithUrlEncodedForm(mockWebServer.takeRequest(), okHttpRequest);
    }

    @Test
    void requestWithFormData() throws InterruptedException, IOException {
        String url = getMockWebServerUrl("/testRequestWithFormData", RESPONSE_BODY_1);
        Map<String, List<Object>> formData = new HashMap<>();
        formData.put("id", Arrays.asList(1));
        formData.put("name", Arrays.asList("test", "test2"));
        OkHttpRequestWithFormData okHttpRequest = OkHttpRequestWithFormData.builder()
                .url(url)
                .method(HttpMethod.POST)
                .headers(getOkHttpBasicHeaders())
                .formData(formData)
                .build();
        OkHttpResponse okHttpResponse = OkHttpUtil.requestWithFormData(okHttpRequest);
        log.info("okHttpResponse: {}", okHttpResponse);
        // validate response
        assertEquals(200, okHttpResponse.getStatusCode());
        assertEquals(RESPONSE_BODY_1, okHttpResponse.getBodyString());
        // validate request
        validateRequestWithMultipartForm(mockWebServer.takeRequest(), okHttpRequest);
    }
}