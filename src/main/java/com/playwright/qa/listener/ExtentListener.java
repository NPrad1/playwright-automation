package com.playwright.qa.listener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Page;
import com.playwright.qa.base.BaseTest;

public class ExtentListener implements ITestListener {

    private static final ExtentReports extent =
            ExtentManager.getInstance();

    // =========================================================
    // THREAD SAFE EXTENT TEST
    // =========================================================

    private static final ThreadLocal<ExtentTest> test =
            new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {

        int retryCount =
                result.getMethod().getCurrentInvocationCount();

        ExtentTest extentTest =
                extent.createTest(
                        result.getMethod().getMethodName()
                                + " (Attempt: "
                                + retryCount
                                + ")"
                );

        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {

        test.get().pass("✅ Test Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {

        int retryCount =
                result.getMethod().getCurrentInvocationCount();

        test.get().fail(
                "❌ Test Failed on Attempt: "
                        + retryCount
        );

        test.get().fail(result.getThrowable());

        try {

            Page page = BaseTest.getPage();

            if (page != null && !page.isClosed()) {

                // =====================================================
                // TAKE SCREENSHOT
                // =====================================================

                byte[] screenshotBytes =
                        page.screenshot(
                                new Page.ScreenshotOptions()
                                        .setFullPage(true)
                        );

                // =====================================================
                // SAVE SCREENSHOT
                // =====================================================

                String screenshotDir =
                        System.getProperty("user.dir")
                                + "/test-output/screenshots/";

                Files.createDirectories(
                        Paths.get(screenshotDir)
                );

                String fileName =
                        result.getMethod().getMethodName()
                                + "_retry_"
                                + retryCount
                                + ".png";

                Path screenshotPath =
                        Paths.get(screenshotDir, fileName);

                Files.write(
                        screenshotPath,
                        screenshotBytes
                );

                // =====================================================
                // EXTENT REPORT ATTACHMENT
                // =====================================================

                test.get().addScreenCaptureFromPath(
                        screenshotPath.toString(),
                        "📸 Failure Screenshot"
                );

            } else {

                test.get().info(
                        "Page already closed - screenshot skipped"
                );
            }

        } catch (Exception e) {

            test.get().warning(
                    "Screenshot capture failed: "
                            + e.getMessage()
            );
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {

        test.get().skip("⚠️ Test Skipped");
    }

    @Override
    public void onFinish(org.testng.ITestContext context) {

        extent.flush();
    }

    // =========================================================
    // GETTERS
    // =========================================================

    static ThreadLocal<ExtentTest> getTestThreadLocal() {

        return test;
    }

    static ExtentReports getExtent() {

        return extent;
    }
}