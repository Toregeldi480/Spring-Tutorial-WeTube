package com.wetube.video_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {
    @Value("${gateway.token}")
    private String gatewayToken;

    public String getGatewayToken() {
        return this.gatewayToken;
    }
}
