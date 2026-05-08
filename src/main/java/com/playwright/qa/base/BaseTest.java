package com.playwright.qa.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.testng.ITestResult;
import org.testng.annotations.*;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ViewportSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Listeners({
        com.playwright.qa.listener.ExtentListener.class,
        com.playwright.qa.listener.RetryListener.class
})
public class BaseTest {

    private static final Logger logger = LogManager.getLogger(BaseTest.class);

    private Playwright playwright;
    private Browser browser;

    // ✅ ThreadLocal for both Page and BrowserContext (parallel-safe)
    private static final ThreadLocal<Page> tlPage = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlContext = new ThreadLocal<>();

    // ✅ Public getters for listeners
    public static Page getPage() {
        return tlPage.get();
    }

    public static BrowserContext getContext() {
        return tlContext.get();
    }

    protected Page page() {
        return tlPage.get();
    }

    private static final String BASE_URL = ConfigReader.get("base.url");

    private static final boolean HEADLESS =
            Boolean.parseBoolean(System.getProperty("headless",
                    System.getenv().getOrDefault("CI", "false").equals("true")
                            ? "true"
                            : ConfigReader.get("headless")));

    private static final int TIMEOUT = ConfigReader.getInt("timeout");

    // ✅ FIX #6 — Set allure dir once at suite level, not every test
    @BeforeSuite(alwaysRun = true)
    public static void globalSetup() {
        System.setProperty("allure.results.directory",
                System.getProperty("user.dir") + "/target/allure-results");
        logger.info("Allure results dir set to: {}/target/allure-results",
                System.getProperty("user.dir"));
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional("chromium") String browserName) {

        logger.info("Thread {} running on browser: {}",
                Thread.currentThread().getId(), browserName);
        logger.info("HEADLESS MODE: {}", HEADLESS);

        playwright = Playwright.create();

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(HEADLESS);

        browser = switch (browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit"  -> playwright.webkit().launch(options);
            default        -> playwright.chromium().launch(options);
        };

        // ✅ FIX #4 — Video dir uses method name, not just thread ID
        //    Prevents video overwrite when threads are reused across tests
        // NOTE: method name resolved in tearDown via ITestResult — here we use
        //       thread ID only for the recording dir; final file moved in tearDown
        String videoDir = System.getProperty("user.dir")
                + "/test-output/videos/thread-" + Thread.currentThread().getId() + "/";

        try {
            Files.createDirectories(Paths.get(videoDir));
        } catch (IOException e) {
            logger.warn("Could not create video dir: {}", e.getMessage());
        }

        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(new ViewportSize(1440, 900))
                        .setRecordVideoDir(Paths.get(videoDir))  // ✅ Always record
                        .setRecordVideoSize(1440, 900)
        );

        // ✅ Start tracing — screenshots + DOM snapshots + sources
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true)
        );

        tlContext.set(context);

        Page newPage = context.newPage();
        tlPage.set(newPage);

        page().setDefaultTimeout(TIMEOUT);
        page().navigate(BASE_URL);
        page().waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {

        BrowserContext context = tlContext.get();

        // ✅ FIX #4 — Unique artifact path: methodName + retryCount + threadId
        //    No collision in parallel runs even when threads are reused
        String safeTestName = result.getMethod().getMethodName()
                + "_retry" + result.getMethod().getCurrentInvocationCount()
                + "_t" + Thread.currentThread().getId();

        String traceDir = System.getProperty("user.dir")
                + "/test-output/traces/" + safeTestName + "/";

        try {
            Files.createDirectories(Paths.get(traceDir));
        } catch (IOException e) {
            logger.warn("Could not create trace dir: {}", e.getMessage());
        }

        // ── Trace: save on fail, discard on pass ──────────────────────────────
        if (context != null) {
            try {
                if (!result.isSuccess()) {
                    Path tracePath = Paths.get(traceDir + "trace.zip");
                    context.tracing().stop(new Tracing.StopOptions()
                            .setPath(tracePath));
                    logger.info("Trace saved → {}", tracePath.toAbsolutePath());

                    // ✅ FIX #2 — Set BEFORE context.close() so ArtifactReporter reads it
                    result.setAttribute("tracePath", tracePath.toAbsolutePath().toString());

                } else {
                    context.tracing().stop(); // discard — zero overhead on pass
                    logger.info("PASSED TEST: {} — trace discarded", result.getName());
                }
            } catch (Exception e) {
                logger.warn("Tracing stop failed: {}", e.getMessage());
            }
        }

        // ── CRITICAL: close context BEFORE reading video path ─────────────────
        //    Playwright finalizes the .webm file only after context.close()
        try {
            if (context != null) context.close();
        } catch (Exception ignored) {}

        // ── Video: read path after context close ──────────────────────────────
        try {
            Page p = page();
            if (p != null && p.video() != null) {
                Path videoPath = p.video().path();

                if (!result.isSuccess()) {
                    // ✅ Rename video to match test name for traceability
                    Path namedVideo = Paths.get(System.getProperty("user.dir")
                            + "/test-output/videos/" + safeTestName + ".webm");
                    Files.createDirectories(namedVideo.getParent());
                    Files.move(videoPath, namedVideo,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Video saved → {}", namedVideo.toAbsolutePath());
                    result.setAttribute("videoPath", namedVideo.toAbsolutePath().toString());

                } else {
                    // ✅ Delete video on pass to save disk space
                    p.video().delete();
                    logger.info("Video deleted for passed test: {}", result.getName());
                }
            }
        } catch (Exception e) {
            logger.warn("Could not handle video: {}", e.getMessage());
        }

        // ── Safe cleanup ──────────────────────────────────────────────────────
        try { if (page() != null && !page().isClosed()) page().close(); } catch (Exception ignored) {}
        try { if (browser != null) browser.close(); }                    catch (Exception ignored) {}
        try { if (playwright != null) playwright.close(); }              catch (Exception ignored) {}

        // ✅ Prevent ThreadLocal memory leaks in parallel execution
        tlPage.remove();
        tlContext.remove();
    }
}
