package com.taogen.commons.okhttp;

import com.taogen.commons.io.FileUtils;
import com.taogen.easyhttpclient.MockWebServerUtils;
import com.taogen.easyhttpclient.enums.HttpMethod;
import com.taogen.easyhttpclient.vo.*;
import lombok.extern.log4j.Log4j2;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class OkHttpUtilTest extends BaseTest {
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
        MockWebServerUtils.enqueueMockedResponse(mockWebServer, RESPONSE_BODY_1.getBytes(StandardCharsets.UTF_8), "application/json");
        String url = MockWebServerUtils.getMockedUrlByUri(mockWebServer, "/testRequestWithoutBody_get");
        log.info("url: {}", url);
        Map<String, List<Object>> queryParams = new HashMap<>();
        queryParams.put("id", Arrays.asList(1));
        queryParams.put("name", Arrays.asList("test"));
        queryParams.put("testChinese", Arrays.asList(CHINESE_TEST));
        HttpRequest okHttpRequest = HttpRequest.builder()
                .url(url)
                .method(HttpMethod.GET)
                .headers(getOkHttpBasicHeaders())
                .queryParams(queryParams)
                .build();
        HttpResponse okHttpResponse = OkHttpUtil.requestWithoutBody(okHttpRequest);
        log.info("okHttpResponse: {}", okHttpResponse);
        // validate response
        assertEquals(200, okHttpResponse.getStatusCode());
        assertEquals(RESPONSE_BODY_1, new String(okHttpResponse.getBody(), StandardCharsets.UTF_8));
        // validate request
        RecordedRequest request = mockWebServer.takeRequest();
        MockWebServerUtils.validateRequestWithQueryString(request, okHttpRequest);
    }


    @Test
    void requestWithJson() throws IOException, InterruptedException {
        MockWebServerUtils.enqueueMockedResponse(mockWebServer, RESPONSE_BODY_1.getBytes(StandardCharsets.UTF_8), "application/json");
        String url = MockWebServerUtils.getMockedUrlByUri(mockWebServer, "/testRequestWithJson");
        log.info("url: {}", url);
        String requestBody = "{\"id\": 1}";
        HttpRequestWithJson okHttpRequest = HttpRequestWithJson.builder()
                .url(url)
                .method(HttpMethod.POST)
                .headers(getOkHttpBasicHeaders())
                .json(requestBody)
                .build();
        HttpResponse okHttpResponse = OkHttpUtil.requestWithJson(okHttpRequest);
        log.info("okHttpResponse: {}", okHttpResponse);
        // validate response
        assertEquals(200, okHttpResponse.getStatusCode());
        assertEquals(RESPONSE_BODY_1, new String(okHttpResponse.getBody(), StandardCharsets.UTF_8));
        // validate request
        MockWebServerUtils.validateRequestWithJson(mockWebServer.takeRequest(), okHttpRequest);
    }


    @Test
    void requestWithFormUrlEncoded() throws InterruptedException, IOException {
        MockWebServerUtils.enqueueMockedResponse(mockWebServer, RESPONSE_BODY_1.getBytes(StandardCharsets.UTF_8), "application/json");
        String url = MockWebServerUtils.getMockedUrlByUri(mockWebServer, "/testRequestWithFormUrlEncoded");
        log.info("url: {}", url);
        Map<String, List<Object>> formData = new HashMap<>();
        formData.put("id", Arrays.asList(1));
        formData.put("name", Arrays.asList("test", "test2"));
        HttpRequestWithForm okHttpRequest = HttpRequestWithForm.builder()
                .url(url)
                .method(HttpMethod.POST)
                .headers(getOkHttpBasicHeaders())
                .formData(formData)
                .build();
        HttpResponse okHttpResponse = OkHttpUtil.requestWithFormUrlEncoded(okHttpRequest);
        log.info("okHttpResponse: {}", okHttpResponse);
        // validate response
        assertEquals(200, okHttpResponse.getStatusCode());
        assertEquals(RESPONSE_BODY_1, new String(okHttpResponse.getBody(), StandardCharsets.UTF_8));
        // validate request
        MockWebServerUtils.validateRequestWithUrlEncodedForm(mockWebServer.takeRequest(), okHttpRequest);
    }

    @Test
    void requestWithFormData() throws InterruptedException, IOException, URISyntaxException {
        MockWebServerUtils.enqueueMockedResponse(mockWebServer, RESPONSE_BODY_1.getBytes(StandardCharsets.UTF_8), "application/json");
        String url = MockWebServerUtils.getMockedUrlByUri(mockWebServer, "/testRequestWithFormData");
        log.info("url: {}", url);
        Map<String, List<Object>> formData = new HashMap<>();
        formData.put("id", Arrays.asList(1));
        formData.put("name", Arrays.asList("test", "test2"));
        formData.put("file", Arrays.asList(
                new File(FileUtils.getFilePathByFileClassPath("test/test.jpg")),
                new File(FileUtils.getFilePathByFileClassPath("test/test.txt"))));
        HttpRequestWithMultipart okHttpRequest = HttpRequestWithMultipart.builder()
                .url(url)
                .method(HttpMethod.POST)
                .headers(getOkHttpBasicHeaders())
                .formData(formData)
                .build();
        HttpResponse okHttpResponse = OkHttpUtil.requestWithFormData(okHttpRequest);
        log.info("okHttpResponse: {}", okHttpResponse);
        // validate response
        assertEquals(200, okHttpResponse.getStatusCode());
        assertEquals(RESPONSE_BODY_1, new String(okHttpResponse.getBody(), StandardCharsets.UTF_8));
        // validate request
        MockWebServerUtils.validateRequestWithMultipartForm(mockWebServer.takeRequest(), okHttpRequest);
    }
}