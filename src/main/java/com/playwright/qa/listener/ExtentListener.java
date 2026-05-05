
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

    private static ExtentReports extent = ExtentManager.getInstance();
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

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
        test.get().pass("Test Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {

        int retryCount = result.getMethod().getCurrentInvocationCount();

        test.get().fail("Test Failed on Attempt: " + retryCount);
        test.get().fail(result.getThrowable());

        try {
            // ✅ Get thread-safe page
            Page page = BaseTest.getPage();

            if (page != null) {

                String screenshotDir = System.getProperty("user.dir") + "/test-output/screenshots/";
                Files.createDirectories(Paths.get(screenshotDir));

                String fileName = result.getMethod().getMethodName()
                        + "_retry_" + retryCount + ".png";

                String filePath = screenshotDir + fileName;

                // ✅ Prevent crash if page already closed
                if (!page.isClosed()) {

                    page.screenshot(new Page.ScreenshotOptions()
                            .setPath(Paths.get(filePath))
                            .setFullPage(true));

                    test.get().addScreenCaptureFromPath(
                            "screenshots/" + fileName,
                            "Failure Screenshot"
                    );

                } else {
                    test.get().info("Page already closed, screenshot skipped");
                }
            }

        } catch (Exception e) {
            test.get().warning("Screenshot failed: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip("Test Skipped");
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}