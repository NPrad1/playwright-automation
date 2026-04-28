package com.playwright.qa.listener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Page;
import com.playwright.qa.base.BaseTest;

public class ExtentListener implements ITestListener{
	
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

	        test.get().fail("Test Failed - Attempt: " + retryCount);
	        test.get().fail(result.getThrowable());

	        try {
	        	Page page = (Page) result.getTestContext().getAttribute("page");

	            if (page != null) {

	                String screenshotDir = System.getProperty("user.dir") + "/test-output/screenshots/";
	                Files.createDirectories(Paths.get(screenshotDir));

	                String fileName = result.getMethod().getMethodName()
	                        + "_retry_" + retryCount + ".png";

	                String filePath = screenshotDir + fileName;

	                // Screenshot BEFORE browser closes
	                page.screenshot(new Page.ScreenshotOptions()
	                        .setPath(Paths.get(filePath))
	                        .setFullPage(true));

	                String relativePath = "screenshots/" + fileName;

	                test.get().addScreenCaptureFromPath(relativePath, "Failure Screenshot");
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
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
