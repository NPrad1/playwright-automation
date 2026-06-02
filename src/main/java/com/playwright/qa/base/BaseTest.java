package com.playwright.qa.base;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ViewportSize;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Listeners({
        com.playwright.qa.listener.ExtentListener.class,
        com.playwright.qa.listener.RetryListener.class,com.playwright.qa.listener.ArtifactReporter.class,com.playwright.qa.listener.AllureReportListener.class,com.playwright.qa.listener.ExecutionTimeListener.class,io.qameta.allure.testng.AllureTestNg.class
})
public class BaseTest {

    private static final Logger logger =
            LogManager.getLogger(BaseTest.class);

    // =========================================================
    // THREAD LOCAL OBJECTS
    // =========================================================

    private static final ThreadLocal<Playwright> tlPlaywright =
            new ThreadLocal<>();

    private static final ThreadLocal<Browser> tlBrowser =
            new ThreadLocal<>();

    private static final ThreadLocal<BrowserContext> tlContext =
            new ThreadLocal<>();

    private static final ThreadLocal<Page> tlPage =
            new ThreadLocal<>();

    private static final ThreadLocal<String> tlBrowserName =
            new ThreadLocal<>();

    // =========================================================
    // VIDEO EXECUTOR
    // =========================================================

    private static final ExecutorService videoExecutor =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors()
            );

    // =========================================================
    // CONFIG
    // =========================================================

    private static final String BASE_URL =
            ConfigReader.get("base.url");

    private static final int TIMEOUT =
            ConfigReader.getInt("timeout");

    // =========================================================
    // GETTERS
    // =========================================================

    public static Playwright getPlaywright() {
        return tlPlaywright.get();
    }

    public static Browser getBrowser() {
        return tlBrowser.get();
    }

    public static BrowserContext getContext() {
        return tlContext.get();
    }

    public static Page getPage() {
        return tlPage.get();
    }

    protected Page page() {
        return tlPage.get();
    }

    // =========================================================
    // BEFORE SUITE
    // =========================================================

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {

        System.setProperty(
                "allure.results.directory",
                System.getProperty("user.dir")
                        + "/target/allure-results"
        );

        logger.info("=================================================");
        logger.info("Execution Started");
        logger.info("=================================================");
    }

    // =========================================================
    // FIX 1: @BeforeClass -> @BeforeTest
    // ONE BROWSER INSTANCE PER <test> BLOCK, NOT PER CLASS
    // =========================================================

    @BeforeTest(alwaysRun = true)
    @Parameters("browser")
    public void initBrowser(
            @Optional("chromium") String browserName) {

        if (browserName != null && !browserName.isBlank()) {
            tlBrowserName.set(browserName);
        }

        String resolvedBrowser = tlBrowserName.get();

        if (resolvedBrowser == null || resolvedBrowser.isBlank()) {
            resolvedBrowser = "chromium";
            tlBrowserName.set(resolvedBrowser);
            logger.warn(
                    "Thread {} browserName was null, defaulting to chromium",
                    Thread.currentThread().getId()
            );
        }

        logger.info(
                "Thread {} initializing browser: {}",
                Thread.currentThread().getId(),
                resolvedBrowser
        );

        try {

            Playwright playwright = Playwright.create();
            tlPlaywright.set(playwright);

            Browser browser = launchBrowser(playwright, resolvedBrowser);

            if (browser == null) {
                throw new RuntimeException("Browser launch returned null");
            }

            tlBrowser.set(browser);

            logger.info(
                    "Thread {} browser initialized successfully",
                    Thread.currentThread().getId()
            );

        } catch (Exception e) {
            logger.error("Browser initialization failed", e);
            throw e;
        }
    }

    // =========================================================
    // BEFORE METHOD
    // NEW CONTEXT + NEW PAGE PER TEST
    // =========================================================

    @BeforeMethod(alwaysRun = true)
    public void setUp() {

        String browserName = tlBrowserName.get();

        if (browserName == null || browserName.isBlank()) {
            browserName = "chromium";
            tlBrowserName.set(browserName);
            logger.warn(
                    "Thread {} browserName was null in setUp, defaulting to chromium",
                    Thread.currentThread().getId()
            );
        }

        logger.info(
                "Thread {} running test on browser: {}",
                Thread.currentThread().getId(),
                browserName
        );

        try {

            Browser browser = getBrowser();

            if (browser == null || !browser.isConnected()) {
                logger.warn(
                        "Thread {} browser null or disconnected. Reinitializing...",
                        Thread.currentThread().getId()
                );
                Playwright playwright = Playwright.create();
                tlPlaywright.set(playwright);
                browser = launchBrowser(playwright, tlBrowserName.get());
                tlBrowser.set(browser);
            }

            BrowserContext context =
                    browser.newContext(buildContextOptions());
            tlContext.set(context);

            startTracing(context);

            Page page = context.newPage();
            tlPage.set(page);

            boolean isWebKit =
                    "webkit".equalsIgnoreCase(browserName);

            page.setDefaultTimeout(
                    isWebKit ? TIMEOUT * 2 : TIMEOUT
            );
            page.setDefaultNavigationTimeout(
                    isWebKit ? 60000 : 30000
            );

            page.navigate(BASE_URL);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        } catch (Exception e) {
            // FIX 2: Clean up partial state on setUp failure
            logger.error("Thread {} setUp failed",
                    Thread.currentThread().getId(), e);
            try {
                Playwright pw = tlPlaywright.get();
                if (pw != null) pw.close();
            } catch (Exception ignored) {}
            tlPlaywright.remove();
            tlBrowser.remove();
            tlPage.remove();
            tlContext.remove();
            throw e;
        }
    }

    // =========================================================
    // AFTER METHOD
    // =========================================================

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {

        // FIX 3: Replaced broken isBeforeMethodConfiguration()
        // check with a null check that actually works
        if (tlPage.get() == null && tlContext.get() == null) {
            logger.warn(
                    "Thread {} page/context null, setUp likely failed for: {}",
                    Thread.currentThread().getId(),
                    result.getMethod().getMethodName()
            );
            tlPage.remove();
            tlContext.remove();
            return;
        }

        String testName = buildTestName(result);
        result.setAttribute("testName", testName);

        Path videoPath = captureVideoPath();

        try {
            handleTracing(result, testName);
            handleScreenshot(result);
        } catch (Exception e) {
            logger.warn("Artifact capture failed", e);
        }

        try {
            closePage();
        } catch (Exception e) {
            logger.warn("Page close failed", e);
        }

        try {
            closeContext();
        } catch (Exception e) {
            logger.warn("Context close failed", e);
        }

        submitVideoHandling(result, testName, videoPath);

        tlPage.remove();
        tlContext.remove();
    }

    // =========================================================
    // FIX 1: @AfterClass -> @AfterTest
    // CLOSES BROWSER ONCE PER <test> BLOCK
    // tlBrowserName cleaned here instead of @AfterSuite
    // =========================================================

    @AfterTest(alwaysRun = true)
    public void closeBrowser() {

        try {
            Browser browser = getBrowser();
            if (browser != null) {
                browser.close();
                logger.info(
                        "Thread {} browser closed",
                        Thread.currentThread().getId()
                );
            }
        } catch (Exception e) {
            logger.warn("Browser close failed", e);
        }

        try {
            Playwright playwright = getPlaywright();
            if (playwright != null) {
                playwright.close();
                logger.info(
                        "Thread {} playwright closed",
                        Thread.currentThread().getId()
                );
            }
        } catch (Exception e) {
            logger.warn("Playwright close failed", e);
        }

        tlBrowser.remove();
        tlPlaywright.remove();
        tlBrowserName.remove();
    }

    // =========================================================
    // AFTER SUITE
    // =========================================================

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {

        waitForVideoExecutor();

        // tlBrowserName.remove() moved to @AfterTest

        logger.info("=================================================");
        logger.info("Execution Completed");
        logger.info("=================================================");
    }

    // =========================================================
    // FIX 4: HOVER AND CLICK HELPER
    // Use for any nav hover -> click patterns
    // =========================================================

    protected void hoverAndClick(String hoverLocator, String clickLocator) {
        page().locator(hoverLocator).waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15_000));
        page().locator(hoverLocator).hover();
        page().locator(clickLocator).waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(10_000));
        page().locator(clickLocator).click();
    }

    // =========================================================
    // LAUNCH BROWSER
    // =========================================================

    private Browser launchBrowser(
            Playwright playwright,
            String browserName) {

        if (browserName == null || browserName.isBlank()) {
            logger.warn(
                    "launchBrowser received null browserName, defaulting to chromium"
            );
            browserName = "chromium";
        }

        BrowserType.LaunchOptions options =
                new BrowserType.LaunchOptions()
                        .setHeadless(ArtifactConfig.HEADLESS);

        if ("webkit".equalsIgnoreCase(browserName)) {
            options.setSlowMo(100);
        }

        return switch (browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit"  -> playwright.webkit().launch(options);
            default        -> playwright.chromium().launch(options);
        };
    }

    // =========================================================
    // CONTEXT OPTIONS
    // =========================================================

    private Browser.NewContextOptions buildContextOptions() {

        Browser.NewContextOptions options =
                new Browser.NewContextOptions()
                        .setViewportSize(new ViewportSize(1920, 1080));

        if (ArtifactConfig.RECORD_VIDEO) {

            String videoDir =
                    System.getProperty("user.dir")
                            + "/test-output/videos/thread-"
                            + Thread.currentThread().getId();

            try {
                Files.createDirectories(Paths.get(videoDir));
            } catch (IOException e) {
                logger.warn("Unable to create video directory", e);
            }

            options.setRecordVideoDir(Paths.get(videoDir))
                   .setRecordVideoSize(1440, 900);
        }

        return options;
    }

    // =========================================================
    // START TRACING
    // =========================================================

    private void startTracing(BrowserContext context) {

        boolean fullTrace = ArtifactConfig.TRACE_FIRST_RUN;

        context.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(fullTrace)
                        .setSnapshots(fullTrace)
                        .setSources(false)
        );
    }

    // =========================================================
    // BUILD TEST NAME
    // =========================================================

    private String buildTestName(ITestResult result) {

        int invocationCount =
                result.getMethod().getCurrentInvocationCount();

        String base =
                result.getMethod().getMethodName()
                        + "_t"
                        + Thread.currentThread().getId();

        return invocationCount > 1
                ? base + "_retry" + (invocationCount - 1)
                : base;
    }

    // =========================================================
    // CAPTURE VIDEO PATH
    // =========================================================

    private Path captureVideoPath() {

        try {
            Page page = getPage();
            if (page != null && page.video() != null) {
                return page.video().path();
            }
        } catch (Exception e) {
            logger.warn("Unable to capture video path", e);
        }

        return null;
    }

    // =========================================================
    // HANDLE TRACING
    // =========================================================

    private void handleTracing(
            ITestResult result,
            String testName) {

        BrowserContext context = getContext();

        if (context == null) {
            return;
        }

        Path tracePath = Paths.get(
                System.getProperty("user.dir")
                        + "/test-output/traces/"
                        + testName
                        + "/trace.zip"
        );

        try {

            Files.createDirectories(tracePath.getParent());

            if (!result.isSuccess()) {

                logger.error("FAILED TEST: {}", result.getName());

                context.tracing().stop(
                        new Tracing.StopOptions().setPath(tracePath)
                );

                if (Files.exists(tracePath)) {
                    Allure.addAttachment(
                            "Trace Zip",
                            "application/zip",
                            Files.newInputStream(tracePath),
                            ".zip"
                    );
                }

            } else {
                context.tracing().stop();
            }

        } catch (Exception e) {
            logger.warn("Tracing stop failed", e);
        }
    }

    // =========================================================
    // HANDLE SCREENSHOT
    // =========================================================

    private void handleScreenshot(ITestResult result) {

        Page page = getPage();

        if (result.isSuccess()
                || page == null
                || page.isClosed()) {
            return;
        }

        try {

            byte[] screenshot = page.screenshot(
                    new Page.ScreenshotOptions().setFullPage(true)
            );

            Allure.addAttachment(
                    "Failure Screenshot",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );

        } catch (Exception e) {
            logger.warn("Screenshot attachment failed", e);
        }
    }

    // =========================================================
    // CLOSE PAGE
    // =========================================================

    private void closePage() {

        Page page = getPage();

        if (page != null && !page.isClosed()) {
            page.close();
        }
    }

    // =========================================================
    // CLOSE CONTEXT
    // =========================================================

    private void closeContext() {

        BrowserContext context = getContext();

        if (context != null) {
            context.close();
        }
    }

    // =========================================================
    // SUBMIT VIDEO HANDLING
    // =========================================================

    private void submitVideoHandling(
            ITestResult result,
            String testName,
            Path videoPath) {

        if (!ArtifactConfig.RECORD_VIDEO || videoPath == null) {
            return;
        }

        if (videoExecutor.isShutdown()) {
            logger.warn("Video executor already shut down");
            return;
        }

        final boolean isFailed = !result.isSuccess();

        videoExecutor.submit(() -> {

            if (!waitForVideoFile(videoPath)) {
                logger.warn("Video file not found: {}", videoPath);
                return;
            }

            try {

                if (isFailed) {

                    Path destination = Paths.get(
                            System.getProperty("user.dir")
                                    + "/test-output/videos/"
                                    + testName
                                    + ".webm"
                    );

                    Files.createDirectories(destination.getParent());

                    Files.move(
                            videoPath,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    Allure.addAttachment(
                            "Failure Video",
                            "video/webm",
                            new ByteArrayInputStream(
                                    Files.readAllBytes(destination)
                            ),
                            ".webm"
                    );

                    logger.info("Video saved: {}", destination);

                } else {

                    Files.deleteIfExists(videoPath);
                    logger.info(
                            "Video deleted for passed test: {}",
                            testName
                    );
                }

            } catch (Exception e) {
                logger.warn("Video handling failed", e);
            }
        });
    }

    // =========================================================
    // WAIT FOR VIDEO FILE
    // =========================================================

    private boolean waitForVideoFile(Path videoPath) {

        for (int i = 0; i < 25; i++) {

            if (Files.exists(videoPath)
                    && videoPath.toFile().length() > 0) {
                return true;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    // =========================================================
    // WAIT FOR VIDEO EXECUTOR
    // =========================================================

    public static void waitForVideoExecutor() {

        if (videoExecutor == null || videoExecutor.isShutdown()) {
            logger.info("Video executor already shut down");
            return;
        }

        logger.info("Waiting for video tasks to complete...");

        videoExecutor.shutdown();

        try {

            boolean completed =
                    videoExecutor.awaitTermination(60, TimeUnit.SECONDS);

            if (completed) {
                logger.info("All video tasks completed successfully");
            } else {
                logger.warn("Video executor timeout exceeded");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error(
                    "Interrupted while waiting for video executor",
                    e
            );
        }
    }
}