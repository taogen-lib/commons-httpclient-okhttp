package com.taogen.commons.okhttp.vo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author Taogen
 */
@Data
@ToString(callSuper = true)
@SuperBuilder
public class OkHttpRequestWithFormData extends OkHttpRequest{
    Map<String, List<Object>> formData;

}
