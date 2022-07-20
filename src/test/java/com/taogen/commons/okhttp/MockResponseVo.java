package com.taogen.commons.okhttp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author Taogen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockResponseVo {
    private String responseBody;
    private Map<String, List<Object>> headers;

}
