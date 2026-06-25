package com.wetube.gateway_service.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
        public static final List<String> openApiEndpoints = List.of(
                        "/video/",
                        "/auth/",
                        "/eureka/");

        public static final List<String> securedApiEndpoints = List.of(
                        "/video/upload",
                        "/video/like",
                        "/video/dislike");

        public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints.stream()
                        .noneMatch(uri -> request.getURI().getPath().startsWith(uri)) ||
                        securedApiEndpoints.stream()
                                        .anyMatch(uri -> request.getURI().getPath().startsWith(uri));
}
