package com.playwright.qa.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//TestNG imports
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

//Playwright imports
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ViewportSize;


@Listeners({com.playwright.qa.listener.ExtentListener.class,com.playwright.qa.listener.RetryListener.class})
public class BaseTest {
	 private static final Logger logger = LogManager.getLogger(BaseTest.class);

	    private Playwright playwright;
	    private Browser browser;
	    private BrowserContext context;

	    // ✅ ONLY SOURCE OF TRUTH
	    private static ThreadLocal<Page> tlPage = new ThreadLocal<>();

	    protected Page page() {
	        return tlPage.get();
	    }

	    private static final String BASE_URL     = ConfigReader.get("base.url");
	  //  private static final String BROWSER_TYPE = ConfigReader.get("browser.type");
	    private static final boolean HEADLESS    = ConfigReader.getBoolean("headless");
	    private static final int TIMEOUT         = ConfigReader.getInt("timeout");

	    @SuppressWarnings("deprecation")
		@BeforeMethod(groups = {"smoke", "regression"},alwaysRun = true)
	    @Parameters("browser")
	    public void setUp(org.testng.ITestContext testContext,@Optional("chromium") String browserName) {
	    	 logger.info("🔥 Thread {} running on browser: {}", 
	    		        Thread.currentThread().getId(), browserName);
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

	        // ✅ Correct way
	        tlPage.set(context.newPage());

	        page().setDefaultTimeout(TIMEOUT);
	        page().navigate(BASE_URL);
	        page().waitForLoadState(LoadState.DOMCONTENTLOADED);
	        

	        testContext.setAttribute("page", page());
	    }

	    @AfterMethod(groups = {"smoke", "regression"},alwaysRun = true)
	    public void tearDown(ITestResult result) {

	        if (!result.isSuccess()) {
	            logger.error("❌ FAILED TEST: {}", result.getName());
	            logger.error("👉 URL at failure: {}", page().url());
	        } else {
	            logger.info("✅ PASSED TEST: {}", result.getName());
	        }

	        try {
	            Thread.sleep(300);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	        // cleanup
	        if (page() != null) page().close();
	        if (context != null) context.close();
	        if (browser != null) browser.close();
	        if (playwright != null) playwright.close();

	        tlPage.remove(); // ✅ VERY IMPORTANT (memory leak prevention)
	    }
}
