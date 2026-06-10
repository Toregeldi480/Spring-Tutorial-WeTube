package com.wetube.gateway_service;

import com.wetube.gateway_service.config.AuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {
	@Value("${gateway.token}")
	private String gatewayToken;

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder, AuthFilter authFilter) {
		return builder.routes()
				.route("User-Service", r -> r.path("/auth/**", "/user/**")
						.filters(f -> f
								.filter(authFilter.apply(new AuthFilter.Config()))
								.addRequestHeader("X-Gateway-Token", gatewayToken)
						)
						.uri("lb://USER-SERVICE/"))
				.route("Video-Service", r -> r.path("/video/**")
						.filters(f -> f
								.filter(authFilter.apply(new AuthFilter.Config()))
								.addRequestHeader("X-Gateway-Token", gatewayToken)
						)
						.uri("lb://VIDEO-SERVICE/"))
				.build();
	}

	@Bean
	public CorsWebFilter corsWebFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOrigin("http://localhost:5500");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setAllowCredentials(true);
		config.setExposedHeaders(List.of("*"));
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}
}
