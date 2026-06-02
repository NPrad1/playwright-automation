package com.playwright.qa.base;

public class ArtifactConfig {
	

    // ─────────────────────────────────────────────────────
    // HEADLESS
    // Auto-forces true on CI (GitHub Actions, Jenkins etc)
    // locally reads from config.properties headless=false
    // ─────────────────────────────────────────────────────
    public static final boolean HEADLESS = resolveHeadless();

    private static boolean resolveHeadless() {

        // If running on CI server — always headless
        // CI env variable is set automatically by
        // GitHub Actions, Jenkins, GitLab CI etc
        boolean isCi = "true".equalsIgnoreCase(
            System.getenv().getOrDefault("CI", "false")
        );
        if (isCi) return true;

        // Otherwise reads config.properties
        // also respects -Dheadless=true from CLI
        return ConfigReader.getBoolean("headless");
    }

    // ─────────────────────────────────────────────────────
    // RECORD VIDEO
    // true  = record every test, keep only on failure
    // false = skip recording entirely
    // Default in config.properties: record.video=true
    // ─────────────────────────────────────────────────────
    public static final boolean RECORD_VIDEO =
        ConfigReader.getBoolean("record.video");

    // ─────────────────────────────────────────────────────
    // TRACE FIRST RUN
    // false = lightweight trace on first attempt
    //         full trace only saved on failure/retry
    //         RECOMMENDED — saves significant I/O
    // true  = full trace from first attempt
    // Default in config.properties: trace.first.run=false
    // ─────────────────────────────────────────────────────
    public static final boolean TRACE_FIRST_RUN =
        ConfigReader.getBoolean("trace.first.run");

    // Prevent instantiation
    private ArtifactConfig() {}


}
