package com.autoresafety.api;

abstract class QuarkusTestBase {
    static {
        // VS Code's JUnit runner does not execute Maven Surefire, so it will not inherit
        // the -Dnet.bytebuddy.experimental=true flag we set there. Quarkus/Hibernate
        // enhancement needs this when running on newer JDKs (e.g., Java 25).
        System.setProperty("net.bytebuddy.experimental", "true");
    }
}
