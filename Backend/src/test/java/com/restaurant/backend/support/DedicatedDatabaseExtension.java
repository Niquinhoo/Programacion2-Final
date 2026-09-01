package com.restaurant.backend.support;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Impide que una prueba de integración use accidentalmente la base operativa. */
public final class DedicatedDatabaseExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        String url = System.getProperty("db.url", "");
        if (!url.matches("(?i).*[/:]restomanager_test(?:[?;].*)?$")) {
            throw new IllegalStateException(
                    "Las integraciones requieren -Ddb.url apuntando explícitamente a restomanager_test");
        }
    }
}
