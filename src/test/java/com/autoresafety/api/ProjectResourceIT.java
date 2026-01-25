package com.autoresafety.api;

import io.quarkus.test.junit.QuarkusIntegrationTest;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@QuarkusIntegrationTest
@EnabledIfSystemProperty(named = "quarkus.it", matches = "true")
class ProjectResourceIT extends ProjectResourceTest {
    // Execute the same tests but in packaged mode.
}
