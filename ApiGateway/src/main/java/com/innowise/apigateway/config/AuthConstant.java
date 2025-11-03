package com.innowise.apigateway.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @ClassName AuthConstant
 * @Description Represents constant values used across authentication and security components.
 * @Author dshparko
 * @Date 08.10.2025 15:25
 * @Version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthConstant {

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final int BEARER_PREFIX_LENGTH = TOKEN_PREFIX.length();

}
