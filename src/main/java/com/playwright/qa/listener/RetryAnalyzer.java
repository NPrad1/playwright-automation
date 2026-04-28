package com.playwright.qa.listener;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int count = 0;
    private static final int maxTry = 2;

    @Override
    public boolean retry(ITestResult result) {

        if (count < maxTry) {
            count++;
            System.out.println("Retrying: " 
                    + result.getName() + " | Attempt: " + count);
            return true;
        }

        return false;
    }

}
