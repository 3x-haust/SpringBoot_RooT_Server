package io.github._3xhaust.root_server.global.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ApiResponseResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType, @NonNull org.springframework.http.MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull org.springframework.http.server.ServerHttpRequest request,
                                  @NonNull org.springframework.http.server.ServerHttpResponse response) {
        if (body instanceof ApiResponse<?> apiResponse) {
            int statusCode = apiResponse.getStatusCode();
            HttpStatus status = HttpStatus.resolve(statusCode);
            if (status != null) {
                response.setStatusCode(status);
            }
            return apiResponse;
        }
        return body;
    }
}
