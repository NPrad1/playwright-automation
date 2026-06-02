package com.playwright.qa.listener;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentManager {

    private static ExtentReports extent;

    // ✅ CHANGED: Added synchronized
    //
    // BEFORE: extent == null check is NOT thread-safe
    //         Two parallel threads could both see extent==null
    //         at the same time and create two separate instances
    //         Result: some tests logged to one instance,
    //         others to another → incomplete report
    //
    // AFTER:  synchronized ensures only one thread enters
    //         at a time — single ExtentReports instance
    //         guaranteed across all parallel threads
    public static synchronized ExtentReports getInstance() {

        if (extent == null) {

            ExtentSparkReporter reporter =
                new ExtentSparkReporter(
                    "test-output/ExtentReport.html"
                );

            reporter.config().setReportName(
                "Automation Report"
            );
            reporter.config().setDocumentTitle(
                "Playwright Test Report"
            );

            extent = new ExtentReports();
            extent.attachReporter(reporter);
        }

        return extent;
    }
}
