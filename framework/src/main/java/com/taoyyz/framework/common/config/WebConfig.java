package com.taoyyz.framework.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taoyyz.framework.web.interceptor.AuthInterceptor;
import com.taoyyz.framework.web.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/4 0:32
 */
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    private static final String DATETIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMATTER = "yyyy-MM-dd";
    private static final String TIME_FORMATTER = "HH:mm:ss";

    @Autowired
    private JwtInterceptor jwtInterceptor;
    @Autowired
    private JwtProperties jwtProperties;
    @Resource
    private AuthInterceptor authInterceptor;


    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        //JWT Token拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns(jwtProperties.getPathPatterns())
                .excludePathPatterns(jwtProperties.getExcludePathPatterns());

        //方法权限控制拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/webjars/springfox-swagger-ui/css/typography.css")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/css/typography.css");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        super.addResourceHandlers(registry);
    }

    //添加转换器
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer());
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer());
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        objectMapper.registerModule(javaTimeModule);
        converter.setObjectMapper(objectMapper);
        converters.add(converter);
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }


    public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(value.format(ofPattern(DATETIME_FORMATTER)));
        }
    }

    public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext deserializationContext)
                throws IOException {
            return LocalDateTime.parse(p.getValueAsString(), ofPattern(DATETIME_FORMATTER));
        }
    }

    public static class LocalDateSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(value.format(ofPattern(DATE_FORMATTER)));
        }
    }

    public static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
            return LocalDate.parse(p.getValueAsString(), ofPattern(DATE_FORMATTER));
        }
    }

    public static class LocalTimeSerializer extends JsonSerializer<LocalTime> {
        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(value.format(ofPattern(TIME_FORMATTER)));
        }
    }

    public static class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
            return LocalTime.parse(p.getValueAsString(), ofPattern(TIME_FORMATTER));
        }
    }

}
