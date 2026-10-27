package com.bowerzlabs.kraftpulse.shared.store;

import com.bowerzlabs.kraftpulse.shared.config.StorageSettings;

public interface EventStoreProvider {

    boolean supports(StorageSettings settings);

    KraftPulseEventStore create(StorageSettings settings);
}