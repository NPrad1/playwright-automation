package com.playwright.qa.listener;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import java.util.Collection;
import java.util.List;

/**
 * FIX #2 — Solves the listener timing problem.
 *
 * WHY THIS EXISTS:
 *   TestNG fires ITestListener.onTestFailure() BEFORE @AfterMethod runs.
 *   This means tracePath and videoPath attributes set in BaseTest.tearDown()
 *   are always null inside ExtentListener.onTestFailure().
 *
 *   IReporter.generateReport() fires after ALL tests and ALL @AfterMethods
 *   have completed — so attributes are guaranteed to be populated here.
 *
 * RESULT:
 *   Trace and video links are reliably appended to each failed test node
 *   in the Extent Report.
 */

public class ArtifactReporter implements IReporter{
	
	  private static final Logger logger = LogManager.getLogger(ArtifactReporter.class);

	    @Override
	    public void generateReport(List<XmlSuite> xmlSuites,
	                               List<ISuite> suites,
	                               String outputDirectory) {

	        ExtentReports extent = ExtentManager.getInstance();

	        for (ISuite suite : suites) {
	            for (ISuiteResult suiteResult : suite.getResults().values()) {
	                ITestContext context = suiteResult.getTestContext();

	                // Process all failed tests
	                processResults(context.getFailedTests().getAllResults(), extent);

	                // Also process failed configurations (e.g., @BeforeMethod failures)
	                processResults(context.getFailedConfigurations().getAllResults(), extent);
	            }
	        }

	        // ✅ Flush report after appending artifact links
	        extent.flush();
	        logger.info("ArtifactReporter: Extent report updated with trace & video links");
	    }

	    private void processResults(Collection<ITestResult> results, ExtentReports extent) {
	        for (ITestResult result : results) {
	            if (result.isSuccess()) continue;

	            // ✅ Create a supplementary log node for artifacts
	            //    (We use a new test node since the original thread-local is gone)
	            String testName = result.getMethod().getMethodName()
	                    + " (Attempt: " + result.getMethod().getCurrentInvocationCount() + ")";

	            ExtentTest artifactNode = extent.createTest("[Artifacts] " + testName);

	            // ── Trace ─────────────────────────────────────────────────────────
	            String tracePath = (String) result.getAttribute("tracePath");
	            if (tracePath != null) {
	                artifactNode.info("🔍 <b>Playwright Trace:</b> <a href='file:///"
	                        + tracePath.replace("\\", "/") + "'>trace.zip</a>");
	                artifactNode.info("<code>npx playwright show-trace \""
	                        + tracePath + "\"</code>");
	                logger.info("Trace linked in report: {}", tracePath);
	            } else {
	                artifactNode.info("Trace not available for this test");
	            }

	            // ── Video ─────────────────────────────────────────────────────────
	            String videoPath = (String) result.getAttribute("videoPath");
	            if (videoPath != null) {
	                artifactNode.info("🎥 <b>Video Recording:</b> <a href='file:///"
	                        + videoPath.replace("\\", "/") + "'>video.webm</a>");
	                logger.info("Video linked in report: {}", videoPath);
	            } else {
	                artifactNode.info("Video not available for this test");
	            }
	        }
	    }

	

}
