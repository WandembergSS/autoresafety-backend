package com.autoresafety.api;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Order(0)
public final class ByteBuddyExperimentalExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        // Quarkus bootstraps Hibernate enhancement during @QuarkusTest startup.
        // On very new JDKs (e.g., Java 25), Byte Buddy requires this flag.
        System.setProperty("net.bytebuddy.experimental", "true");
    }
}
