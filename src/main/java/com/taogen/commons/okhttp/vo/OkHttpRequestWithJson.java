package com.taogen.commons.okhttp.vo;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author Taogen
 */
@Data
@ToString(callSuper = true)
//@AllArgsConstructor
//@NoArgsConstructor
@SuperBuilder
public class OkHttpRequestWithJson extends OkHttpRequest{
    private String json;
}
