package com.bowerzlabs.kraftpulse.springboot.config;

import com.bowerzlabs.kraftpulse.core.KraftPulseEntry;
import com.bowerzlabs.kraftpulse.core.capture.KraftPulseCaptureService;
import com.bowerzlabs.kraftpulse.core.trace.OtelTraceProvider;
import com.bowerzlabs.kraftpulse.core.trace.TraceProvider;
import com.bowerzlabs.kraftpulse.springboot.request.HttpRequestCaptureFilter;
import com.bowerzlabs.kraftpulse.springboot.request.InboundHttpRequestEventBuilder;
import com.bowerzlabs.kraftpulse.springboot.sqlite.KraftPulseSqliteAutoConfiguration;
import com.bowerzlabs.kraftpulse.springboot.web.KraftPulseDashboardController;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

@AutoConfiguration
@EnableConfigurationProperties(KraftPulseProperties.class)
@ConditionalOnProperty(prefix = "kraftpulse", name = {"enabled"}, havingValue = "true", matchIfMissing = true)
@Import({
        KraftPulseWebConfiguration.class,
        KraftPulseDashboardController.class,
        KraftPulseSqliteAutoConfiguration.class,
})
public class KraftPulseAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(KraftPulseAutoConfiguration.class);
    private final KraftPulseProperties properties;
    private final Environment environment;
    private String appName = "kraftpulse";

    public KraftPulseAutoConfiguration(KraftPulseProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;

        // Resolve fallback file path immediately during property binding phase
        KraftPulseProperties.StorageProperties storageProps = properties.getStorageProperties();
        if ("sqlite".equalsIgnoreCase(storageProps.getType()) &&
                (storageProps.getFilePath() == null || storageProps.getFilePath().isBlank())) {

            appName = environment.getProperty("spring.application.name", "kraftpulse");
            String fallbackPath = "./" + sanitizeAppName(appName) + "-data.db";
            storageProps.setFilePath(fallbackPath);
        }
    }

    @PostConstruct
    public void init() {
        LOG.info("KraftPulseAutoConfiguration initialized with storage: {}", properties.getStorageProperties().getType());
    }

    @Bean
    public KraftPulseCaptureService kraftPulseCaptureService() {
        return KraftPulseEntry.start(properties);
    }

    @Bean
    @ConditionalOnMissingBean(TraceProvider.class)
    public TraceProvider traceProvider() {
        // Fallback to GlobalOpenTelemetry if no custom bean is registered
        OpenTelemetry openTelemetry;
        try {
            openTelemetry = GlobalOpenTelemetry.get();
        } catch (IllegalStateException e) {
            openTelemetry = OpenTelemetry.noop();
        }
        return new OtelTraceProvider(openTelemetry);
    }

    @Bean
    public InboundHttpRequestEventBuilder requestEventBuilder(TraceProvider traceProvider) {
        return new InboundHttpRequestEventBuilder(appName, traceProvider);
    }

    @Bean
    public FilterRegistrationBean<HttpRequestCaptureFilter> httpRequestCaptureFilter(
            KraftPulseCaptureService captureService,
            InboundHttpRequestEventBuilder requestEventBuilder
    ) {
        HttpRequestCaptureFilter filter = new HttpRequestCaptureFilter(
                captureService,
                requestEventBuilder,
                properties.getDashboardPath()
        );
        FilterRegistrationBean<HttpRequestCaptureFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    private String sanitizeAppName(String appName) {
        return appName.toLowerCase().replaceAll("[^a-z0-9_-]", "-");
    }
}