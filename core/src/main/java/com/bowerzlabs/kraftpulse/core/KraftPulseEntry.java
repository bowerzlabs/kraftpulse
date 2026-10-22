package com.bowerzlabs.kraftpulse.core;

import com.bowerzlabs.kraftpulse.core.capture.KraftPulseCaptureService;
import com.bowerzlabs.kraftpulse.core.enrichment.EnrichmentPipeline;
import com.bowerzlabs.kraftpulse.core.masking.MaskingEngine;
import com.bowerzlabs.kraftpulse.core.store.EventStoreResolver;
import com.bowerzlabs.kraftpulse.shared.config.KraftPulseSettings;
import com.bowerzlabs.kraftpulse.shared.enrichment.NPlusOneDetector;
import com.bowerzlabs.kraftpulse.shared.store.KraftPulseEventStore;

public final class KraftPulseEntry {

    private KraftPulseEntry() {
    }

    public static KraftPulseCaptureService start(KraftPulseSettings settings) {
        if (!settings.isEnabled()) {
            System.out.println("[KraftPulse] disabled — skipping startup");
            return null;
        }

        System.out.println("[KraftPulse] starting — storage=" + settings.getStorage());

        KraftPulseEventStore store = EventStoreResolver.create(settings.getStorage());

        EnrichmentPipeline pipeline = new EnrichmentPipeline(
                store,
                new MaskingEngine(settings.getMasking()),
                new NPlusOneDetector(settings.getNPlusOneThreshold())
        );

        return new KraftPulseCaptureService(store, pipeline);
    }



}