package com.bowerzlabs.kraftpulse.springboot.config;

import com.bowerzlabs.kraftpulse.core.capture.KraftPulseCaptureService;
import com.bowerzlabs.kraftpulse.core.trace.TraceProvider;
import com.bowerzlabs.kraftpulse.springboot.outbound.OutboundCallRecorder;
import com.bowerzlabs.kraftpulse.springboot.outbound.OutboundContextResolver;
import com.bowerzlabs.kraftpulse.springboot.outbound.OutboundRestInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.restclient.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Imported by KraftPulseAutoConfiguration, so it is a plain configuration class:
 * - not an @AutoConfiguration. Do NOT also list it in AutoConfiguration.imports.
 * - no @ConditionalOnBean on KraftPulseCaptureService. An imported class is registered before the importing
 *   class's own bean methods, so that condition is evaluated too early and comes out false.
 *   The dependency is satisfied by injecting KraftPulseCaptureService as a parameter instead.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "kraftpulse.outbound", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KraftPulseOutboundCaptureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OutboundContextResolver kraftPulseOutboundContextResolver(ObjectProvider<TraceProvider> traceProvider) {
        return new OutboundContextResolver(traceProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboundCallRecorder kraftPulseOutboundCallRecorder(
            KraftPulseCaptureService captureService,
            KraftPulseProperties properties,
            @Value("${spring.application.name:kraftpulse-app}") String applicationName) {
        return new OutboundCallRecorder(captureService::captureOutboundCall, properties.getOutbound(), applicationName);
    }

    // Only clients built from Spring Boot's auto-configured builders are instrumented.
    // A plain `new RestTemplate()` or `RestClient.create()` is NOT captured.

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RestTemplateCustomizer.class)
    static class RestTemplateCapture {

        @Bean
        RestTemplateCustomizer kraftPulseRestTemplateCustomizer(ObjectProvider<OutboundCallRecorder> recorder,
                                                                ObjectProvider<OutboundContextResolver> resolver) {
            return restTemplate -> {
                OutboundCallRecorder r = recorder.getIfAvailable();
                OutboundContextResolver c = resolver.getIfAvailable();
                if (r != null && c != null) {
                    restTemplate.getInterceptors().add(new OutboundRestInterceptor(r, c, "RestTemplate"));
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RestClientCustomizer.class)
    static class RestClientCapture {

        @Bean
        RestClientCustomizer kraftPulseRestClientCustomizer(ObjectProvider<OutboundCallRecorder> recorder,
                                                            ObjectProvider<OutboundContextResolver> resolver) {
            return builder -> {
                OutboundCallRecorder r = recorder.getIfAvailable();
                OutboundContextResolver c = resolver.getIfAvailable();
                if (r != null && c != null) {
                    builder.requestInterceptor(new OutboundRestInterceptor(r, c, "RestClient"));
                }
            };
        }
    }
}