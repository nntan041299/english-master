package com.nntan041299.englishmasterservice.common.advice;

import com.nntan041299.englishmasterservice.common.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Wraps every JSON response body (success and error) coming out of any
 * {@code @RestController} / {@code @RestControllerAdvice} in this app into
 * the shape {"data": ...}, without requiring each controller to do it
 * manually.
 */
@RestControllerAdvice
public class ApiResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                   MethodParameter returnType,
                                   MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request,
                                   ServerHttpResponse response) {
        // Nothing to wrap for empty bodies (e.g. 204 No Content) or if it's
        // already wrapped.
        if (body == null || body instanceof ApiResponse) {
            return body;
        }
        // Leave non-JSON payloads (files, raw strings served as-is, etc.) untouched.
        if (selectedContentType != null && !selectedContentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return body;
        }
        return ApiResponse.of(body);
    }
}
