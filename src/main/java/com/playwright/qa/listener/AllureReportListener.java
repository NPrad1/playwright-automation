package com.playwright.qa.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IExecutionListener;

import java.io.File;

public class AllureReportListener implements IExecutionListener {

    private static final Logger logger = LogManager.getLogger(AllureReportListener.class);

    @Override
    public void onExecutionStart() {
        logger.info("=====> Test Execution Started...");
    }

    @Override
    public void onExecutionFinish() {
        logger.info("=====> Test Execution Finished. Generating Allure Report...");

        try {
            ProcessBuilder pb = new ProcessBuilder(
                "cmd", "/c",
                "mvn", "allure:serve"
            );
            pb.directory(new File(System.getProperty("user.dir")));
            pb.inheritIO();
            pb.start();

            logger.info("=====> Allure server starting... report will open in browser shortly");

        } catch (Exception e) {
            logger.error("=====> Failed to open Allure Report: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}