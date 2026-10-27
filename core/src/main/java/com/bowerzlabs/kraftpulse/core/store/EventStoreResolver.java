package com.bowerzlabs.kraftpulse.core.store;

import com.bowerzlabs.kraftpulse.shared.config.StorageSettings;
import com.bowerzlabs.kraftpulse.shared.store.EventStoreProvider;
import com.bowerzlabs.kraftpulse.shared.store.KraftPulseEventStore;

import java.util.ServiceLoader;

public final class EventStoreResolver {

    private EventStoreResolver() {
    }

    public static KraftPulseEventStore create(StorageSettings settings) {
        return ServiceLoader.load(EventStoreProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(provider -> provider.supports(settings))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No event store provider found for: "
                                + settings.getClass().getSimpleName()
                                + ". Add the corresponding KraftPulse storage module."
                ))
                .create(settings);
    }
}