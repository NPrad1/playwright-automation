package com.playwright.qa.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TestNG imports
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

// Playwright imports
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
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

    protected Page page() {
        return tlPage.get();
    }

    private static final String BASE_URL = ConfigReader.get("base.url");
    private static final int TIMEOUT = ConfigReader.getInt("timeout");

    @BeforeMethod(groups = {"smoke", "regression"}, alwaysRun = true)
    @Parameters("browser")
    public void setUp(org.testng.ITestContext testContext,
                      @Optional("chromium") String browserName) {

        logger.info("🔥 Thread {} running on browser: {}",
                Thread.currentThread().getId(), browserName);

        playwright = Playwright.create();

        // ============================
        // ✅ FIX 1: Runtime Headless (CRITICAL)
        // ============================
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", ConfigReader.get("headless"))
        );

        logger.info("🚀 Headless mode: {}", headless);
        logger.info("System headless property: {}", System.getProperty("headless"));
        logger.info("Config headless value: {}", ConfigReader.get("headless"));
        // ============================

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(headless);

        browser = switch (browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            default -> playwright.chromium().launch(options);
        };

        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(new ViewportSize(1440, 900))
        );

        tlPage.set(context.newPage());

        page().setDefaultTimeout(TIMEOUT);
        page().navigate(BASE_URL);
        page().waitForLoadState(LoadState.DOMCONTENTLOADED);

        testContext.setAttribute("page", page());
    }

    @AfterMethod(groups = {"smoke", "regression"}, alwaysRun = true)
    public void tearDown(ITestResult result) {

        if (!result.isSuccess()) {
            logger.error("❌ FAILED TEST: {}", result.getName());

            // ============================
            // ✅ FIX 2: Null Safety (CRITICAL)
            // ============================
            if (page() != null) {
                logger.error("👉 URL at failure: {}", page().url());
            } else {
                logger.error("⚠️ Page is null — browser likely failed to launch");
            }
            // ============================

        } else {
            logger.info("✅ PASSED TEST: {}", result.getName());
        }

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Cleanup
        if (page() != null) page().close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();

        tlPage.remove(); // ✅ prevent memory leaks
    }
}