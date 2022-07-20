package com.taogen.commons.okhttp.vo;

import com.taogen.commons.okhttp.enums.HttpMethod;
import lombok.*;
import lombok.experimental.SuperBuilder;
import okhttp3.Headers;

import java.util.List;
import java.util.Map;

/**
 * @author Taogen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OkHttpRequest {
    private String url;
    private HttpMethod method;
    private Headers headers;
    private Map<String, List<Object>> queryStringParams;
}
