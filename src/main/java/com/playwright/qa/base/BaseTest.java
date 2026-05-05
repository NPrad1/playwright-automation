
package com.playwright.qa.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ViewportSize;

@Listeners({
        com.playwright.qa.listener.ExtentListener.class,
        com.playwright.qa.listener.RetryListener.class
})
public class BaseTest {

    private static final Logger logger = LogManager.getLogger(BaseTest.class);

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;

    // ✅ Thread-safe Page
    private static ThreadLocal<Page> tlPage = new ThreadLocal<>();

    // ✅ PUBLIC getter for listener
    public static Page getPage() {
        return tlPage.get();
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

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional("chromium") String browserName) {

        logger.info("🔥 Thread {} running on browser: {}",
                Thread.currentThread().getId(), browserName);

        logger.info("🚀 HEADLESS MODE: {}", HEADLESS);

        playwright = Playwright.create();

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(HEADLESS);

        browser = switch (browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            default -> playwright.chromium().launch(options);
        };

        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(new ViewportSize(1440, 900))
        );

        Page newPage = context.newPage();
        tlPage.set(newPage);

        page().setDefaultTimeout(TIMEOUT);
        page().navigate(BASE_URL);
        page().waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {

        if (page() != null) {
            try {
                if (!result.isSuccess()) {
                    logger.error("❌ FAILED TEST: {}", result.getName());
                    logger.error("👉 URL at failure: {}", page().url());
                } else {
                    logger.info("✅ PASSED TEST: {}", result.getName());
                }
            } catch (Exception e) {
                logger.warn("Error while logging test result: {}", e.getMessage());
            }
        }

        // ✅ Safe cleanup (no crash)
        try { if (page() != null && !page().isClosed()) page().close(); } catch (Exception ignored) {}
        try { if (context != null) context.close(); } catch (Exception ignored) {}
        try { if (browser != null) browser.close(); } catch (Exception ignored) {}
        try { if (playwright != null) playwright.close(); } catch (Exception ignored) {}

        tlPage.remove(); // prevent memory leaks
    }
}

