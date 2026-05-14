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
 * WHY THIS EXISTS: TestNG fires ITestListener.onTestFailure()
 * BEFORE @AfterMethod runs. This means tracePath and videoPath attributes set
 * in BaseTest.tearDown() are always null inside ExtentListener.onTestFailure().
 *
 * IReporter.generateReport() fires after ALL tests and ALL @AfterMethods have
 * completed — so attributes are guaranteed to be populated here.
 *
 * RESULT: Trace and video links are reliably appended to each failed test node
 * in the Extent Report.
 */

public class ArtifactReporter implements IReporter {

	private static final Logger logger = LogManager.getLogger(ArtifactReporter.class);

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

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

			String methodName = result.getMethod().getMethodName();

			ExtentTest artifactNode = extent.createTest("[Artifacts] " + methodName);

			String testName = (String) result.getAttribute("testName");

			if (testName == null) {
				continue;
			}

// =========================
// VIDEO
// =========================

			String videoPath = System.getProperty("user.dir") + "/test-output/videos/" + testName + ".webm";

			java.io.File videoFile = new java.io.File(videoPath);

			if (videoFile.exists()) {

				artifactNode.info("<a href='file:///" + videoPath.replace("\\", "/") + "'>🎥 Open Failure Video</a>");
			}

// =========================
// TRACE
// =========================

			String tracePath = System.getProperty("user.dir") + "/test-output/traces/" + testName + "/trace.zip";

			java.io.File traceFile = new java.io.File(tracePath);

			if (traceFile.exists()) {

				artifactNode.info("<a href='file:///" + tracePath.replace("\\", "/") + "'>📂 Open Trace</a>");
			}
		}
	}
}
