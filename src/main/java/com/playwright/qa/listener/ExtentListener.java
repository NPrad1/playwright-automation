
package com.playwright.qa.listener;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Page;
import com.playwright.qa.base.BaseTest;

public class ExtentListener implements ITestListener {

	 private static final ExtentReports extent = ExtentManager.getInstance();

	    // ✅ ThreadLocal — each parallel thread owns its own ExtentTest node
	    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();

	    @Override
	    public void onTestStart(ITestResult result) {
	        int retryCount = result.getMethod().getCurrentInvocationCount();
	        ExtentTest extentTest = extent.createTest(
	                result.getMethod().getMethodName() + " (Attempt: " + retryCount + ")"
	        );
	        test.set(extentTest);
	    }

	    @Override
	    public void onTestSuccess(ITestResult result) {
	        test.get().pass("✅ Test Passed");
	    }

	    @Override
	    public void onTestFailure(ITestResult result) {
	        int retryCount = result.getMethod().getCurrentInvocationCount();
	        test.get().fail("❌ Test Failed on Attempt: " + retryCount);
	        test.get().fail(result.getThrowable());

	        // ── Screenshot (captured live — page still open here) ─────────────────
	        try {
	            Page page = BaseTest.getPage();
	            if (page != null && !page.isClosed()) {
	                String screenshotDir = System.getProperty("user.dir") + "/test-output/screenshots/";
	                Files.createDirectories(Paths.get(screenshotDir));

	                String fileName = result.getMethod().getMethodName()
	                        + "_retry_" + retryCount + ".png";
	                String filePath = screenshotDir + fileName;

	                page.screenshot(new Page.ScreenshotOptions()
	                        .setPath(Paths.get(filePath))
	                        .setFullPage(true));

	                test.get().addScreenCaptureFromPath(
	                        "screenshots/" + fileName,
	                        "📸 Failure Screenshot"
	                );
	            } else {
	                test.get().info("Page already closed — screenshot skipped");
	            }
	        } catch (Exception e) {
	            test.get().warning("Screenshot failed: " + e.getMessage());
	        }

	        // ── Trace & Video links ────────────────────────────────────────────────
	        // NOTE: These attributes are set in BaseTest.tearDown() which runs AFTER
	        //       this listener. They will be null here. Links are attached in
	        //       ArtifactReporter.generateReport() which runs after all tests.
	        // This block is intentionally left as a no-op — see ArtifactReporter.java
	    }

	    @Override
	    public void onTestSkipped(ITestResult result) {
	        test.get().skip("⚠️ Test Skipped");
	    }

	    @Override
	    public void onFinish(ITestContext context) {
	        extent.flush();
	    }

	    // ✅ Package-private getter so ArtifactReporter can update the same node
	    static ThreadLocal<ExtentTest> getTestThreadLocal() {
	        return test;
	    }

	    static ExtentReports getExtent() {
	        return extent;
	    }

}