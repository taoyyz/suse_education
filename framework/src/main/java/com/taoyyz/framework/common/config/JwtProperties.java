package com.taoyyz.framework.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/4 1:22
 */
@Component
@ConfigurationProperties(prefix = "jwt.token")
@Data
public class JwtProperties {
    private String secretKey;
    private Long expiration;
    private String pathPatterns;
    private List<String> excludePathPatterns;
}
