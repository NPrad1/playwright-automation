package com.playwright.qa.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ViewportSize;

@Listeners({
        com.playwright.qa.listener.ExtentListener.class,
        com.playwright.qa.listener.RetryListener.class
})
public class BaseTest {

    private static final Logger logger =
            LogManager.getLogger(BaseTest.class);

    // =========================================================
    // THREAD SAFE OBJECTS
    // =========================================================

    private static final ThreadLocal<Playwright> tlPlaywright =
            new ThreadLocal<>();

    private static final ThreadLocal<Browser> tlBrowser =
            new ThreadLocal<>();

    private static final ThreadLocal<BrowserContext> tlContext =
            new ThreadLocal<>();

    private static final ThreadLocal<Page> tlPage =
            new ThreadLocal<>();

    // =========================================================
    // CONFIG
    // =========================================================

    private static final String BASE_URL =
            ConfigReader.get("base.url");

    private static final int TIMEOUT =
            ConfigReader.getInt("timeout");

    private static final boolean HEADLESS =
            Boolean.parseBoolean(
                    System.getProperty(
                            "headless",
                            System.getenv()
                                    .getOrDefault("CI", "false")
                                    .equals("true")
                                            ? "true"
                                            : ConfigReader.get("headless")
                    )
            );

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
    // SUITE SETUP
    // =========================================================

    @BeforeSuite(alwaysRun = true)
    public static void globalSetup() {

        System.setProperty(
                "allure.results.directory",
                System.getProperty("user.dir")
                        + "/target/allure-results"
        );

        logger.info("Allure Results Directory Configured");
    }

    // =========================================================
    // TEST SETUP
    // =========================================================

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional("chromium") String browserName) {

        logger.info(
                "Thread {} running on browser: {}",
                Thread.currentThread().getId(),
                browserName
        );

        logger.info("HEADLESS MODE: {}", HEADLESS);

        // =====================================================
        // PLAYWRIGHT
        // =====================================================

        Playwright playwright = Playwright.create();
        tlPlaywright.set(playwright);

        // =====================================================
        // BROWSER
        // =====================================================

        BrowserType.LaunchOptions options =
                new BrowserType.LaunchOptions()
                        .setHeadless(HEADLESS);

        Browser browser = switch (browserName.toLowerCase()) {

            case "firefox" ->
                    playwright.firefox().launch(options);

            case "webkit" ->
                    playwright.webkit().launch(options);

            default ->
                    playwright.chromium().launch(options);
        };

        tlBrowser.set(browser);

        // =====================================================
        // VIDEO DIRECTORY
        // =====================================================

        String videoDir =
                System.getProperty("user.dir")
                        + "/test-output/videos/thread-"
                        + Thread.currentThread().getId();

        try {

            Files.createDirectories(Paths.get(videoDir));

        } catch (IOException e) {

            logger.warn("Unable to create video directory", e);
        }

        // =====================================================
        // CONTEXT
        // =====================================================

        BrowserContext context =
                browser.newContext(

                        new Browser.NewContextOptions()
                                .setViewportSize(
                                        new ViewportSize(1440, 900)
                                )
                                .setRecordVideoDir(Paths.get(videoDir))
                                .setRecordVideoSize(1440, 900)

                );

        tlContext.set(context);

        // =====================================================
        // TRACING
        // =====================================================

        context.tracing().start(

                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)

        );

        // =====================================================
        // PAGE
        // =====================================================

        Page page = context.newPage();

        tlPage.set(page);

        page.setDefaultTimeout(TIMEOUT);
        page.setDefaultNavigationTimeout(30000);

        page.navigate(BASE_URL);

        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    // =========================================================
    // TEARDOWN
    // =========================================================

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {

        BrowserContext context = getContext();
        Page page = getPage();
        Browser browser = getBrowser();
        Playwright playwright = getPlaywright();

        String testName =
                result.getMethod().getMethodName()
                        + "_retry"
                        + result.getMethod().getCurrentInvocationCount()
                        + "_t"
                        + Thread.currentThread().getId();

        // =====================================================
        // TRACE PATH
        // =====================================================

        Path tracePath = Paths.get(
                System.getProperty("user.dir")
                        + "/test-output/traces/"
                        + testName
                        + "/trace.zip"
        );

        // =====================================================
        // VIDEO PATH
        // =====================================================

        Path videoPath = null;

        try {

            if (page != null && page.video() != null) {

                videoPath = page.video().path();
            }

        } catch (Exception e) {

            logger.warn("Unable to capture video path", e);
        }

        // =====================================================
        // TRACE STOP
        // =====================================================

        try {

            if (context != null) {

                Files.createDirectories(tracePath.getParent());

                if (!result.isSuccess()) {

                    logger.error("FAILED TEST: {}", result.getName());

                    if (result.getThrowable() != null) {

                        logger.error(
                                "ERROR:",
                                result.getThrowable()
                        );
                    }

                    context.tracing().stop(

                            new Tracing.StopOptions()
                                    .setPath(tracePath)

                    );

                    logger.info(
                            "Trace saved: {}",
                            tracePath.toAbsolutePath()
                    );

                    result.setAttribute(
                            "tracePath",
                            tracePath.toAbsolutePath().toString()
                    );

                } else {

                    context.tracing().stop();

                    logger.info(
                            "PASSED TEST: {} - Trace discarded",
                            result.getName()
                    );
                }
            }

        } catch (Exception e) {

            logger.warn("Tracing stop failed", e);
        }

        // =====================================================
        // PAGE CLOSE
        // =====================================================

        try {

            if (page != null && !page.isClosed()) {

                page.close();
            }

        } catch (Exception e) {

            logger.warn("Page close failed", e);
        }

        // =====================================================
        // CONTEXT CLOSE
        // =====================================================

        try {

            if (context != null) {

                context.close();
            }

        } catch (Exception e) {

            logger.warn("Context close failed", e);
        }

        // =====================================================
        // WAIT FOR VIDEO RELEASE
        // =====================================================

        try {

            Thread.sleep(500);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        // =====================================================
        // VIDEO HANDLING
        // =====================================================

        try {

            if (videoPath != null) {

                if (!result.isSuccess()) {

                    Path finalVideoPath = Paths.get(
                            System.getProperty("user.dir")
                                    + "/test-output/videos/"
                                    + testName
                                    + ".webm"
                    );

                    Files.createDirectories(
                            finalVideoPath.getParent()
                    );

                    Files.move(
                            videoPath,
                            finalVideoPath,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    logger.info(
                            "Video saved: {}",
                            finalVideoPath.toAbsolutePath()
                    );

                    result.setAttribute(
                            "videoPath",
                            finalVideoPath.toAbsolutePath().toString()
                    );

                } else {

                    Files.deleteIfExists(videoPath);

                    logger.info(
                            "Video deleted for passed test: {}",
                            result.getName()
                    );
                }
            }

        } catch (Exception e) {

            logger.warn("Video handling failed", e);
        }

        // =====================================================
        // BROWSER CLOSE
        // =====================================================

        try {

            if (browser != null && browser.isConnected()) {

                browser.close();
            }

        } catch (Exception e) {

            logger.warn("Browser close failed", e);
        }

        // =====================================================
        // PLAYWRIGHT CLOSE
        // =====================================================

        try {

            if (playwright != null) {

                playwright.close();
            }

        } catch (Exception e) {

            logger.warn("Playwright close failed", e);
        }

        // =====================================================
        // THREADLOCAL CLEANUP
        // =====================================================

        tlPage.remove();
        tlContext.remove();
        tlBrowser.remove();
        tlPlaywright.remove();
    }
}