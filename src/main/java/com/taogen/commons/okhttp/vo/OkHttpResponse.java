package com.taogen.commons.okhttp.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.Headers;

import java.nio.charset.StandardCharsets;

/**
 * @author Taogen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OkHttpResponse {
    private Integer statusCode;
    private Headers headers;
    /**
     * byte[] array for text response body or binary response body
     */
    private byte[] body;
    private String bodyString;

    public OkHttpResponse(Integer statusCode, Headers headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public String getBodyString() {
        if (this.body == null) {
            return null;
        }
        if (this.bodyString == null) {
            this.bodyString = new String(body, StandardCharsets.UTF_8);
        }
        return bodyString;
    }
}
