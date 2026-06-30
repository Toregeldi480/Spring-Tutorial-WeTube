package com.wetube.gateway_service.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
        public static final List<String> PUBLIC_ENDPOINTS = List.of(
                        "/user/",
                        "/video/",
                        "/auth/",
                        "/eureka/");

        public static final List<String> PRIVATE_ENDPOINTS = List.of(
                        "/video/upload",
                        "/video-rating/video/like",
                        "/video-rating/video/dislike",
                        "/user/subscribe",
                        "/user/unsubscribe",
                        "/user/me");

        public Predicate<ServerHttpRequest> isSecured = request -> PUBLIC_ENDPOINTS.stream()
                        .noneMatch(uri -> request.getURI().getPath().startsWith(uri)) ||
                        PRIVATE_ENDPOINTS.stream().anyMatch(uri -> request.getURI().getPath().startsWith(uri));
}
