package com.playwright.qa.listener;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {


    private int count = 0;

    // ✅ CHANGED: maxTry=2 → maxTry=1
    //
    // BEFORE: maxTry=2 meant 3 total runs per test
    //         (1 original + 2 retries)
    //         Seen in your logs: some tests ran 3 times
    //         This wastes ~20-30s per flaky test
    //
    // AFTER:  maxTry=1 means 2 total runs per test
    //         (1 original + 1 retry)
    //         Industry standard — if it fails twice,
    //         it's a real failure not a flake
    private static final int MAX_RETRY = 1;

    @Override
    public boolean retry(ITestResult result) {

        if (count < MAX_RETRY) {
            count++;
            System.out.println(
                "Retrying: " + result.getName()
                    + " | Attempt: " + count
            );
            return true;
        }
        return false;
    }

}
