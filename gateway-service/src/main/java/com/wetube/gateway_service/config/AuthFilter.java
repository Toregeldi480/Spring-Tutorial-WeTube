package com.wetube.gateway_service.config;

import com.wetube.gateway_service.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (request.getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            if (validator.isSecured.test(request)) {
                System.out.println("URI:" + request.getURI());
                String token = extractTokenFromCookie(request);

                if (token == null) {
                    System.out.println("Token: Not Found");
                    return handleUnauthorized(exchange, "Missing Authentication Token");
                }
                System.out.println("Token: " + token);

                try {
                    Claims claims = jwtUtil.extractAllClaims(token);

                    System.out.println("Username: " + claims.get("X-Username").toString());
                    System.out.println("User-Id: " + claims.get("X-User-Id").toString());

                    exchange = exchange.mutate()
                            .request(r -> r
                                    .header("X-Username", claims.get("X-Username").toString())
                                    .header("X-User-Id", claims.get("X-User-Id").toString())
                            ).build(); 
                } catch (ExpiredJwtException e) {
                    return handleUnauthorized(exchange, "Token Expired");
                } catch (JwtException e) {
                    return handleUnauthorized(exchange, "Invalid Token");
                }
            }

            return chain.filter(exchange);
        });
    }

    private String extractTokenFromCookie(ServerHttpRequest request) {
        String cookieHeader = request.getHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookieHeader == null) return null;

        return Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(cookie -> cookie.startsWith("accessToken="))
                .map(cookie -> cookie.substring("accessToken=".length()))
                .findFirst()
                .orElse(null);
    }

    // TODO: add message
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {

    }
}
