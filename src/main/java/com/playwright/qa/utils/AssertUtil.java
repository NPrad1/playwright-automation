package com.playwright.qa.utils;

import org.testng.Assert;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

public class AssertUtil {
	// 🔹 Element visible
    public static void verifyElementVisible(Locator locator) {
        PlaywrightAssertions.assertThat(locator)
                .isVisible();
    }

    // 🔹 Element not visible
    public static void verifyElementNotVisible(Locator locator) {
        PlaywrightAssertions.assertThat(locator)
                .not()
                .isVisible();
    }

    // 🔹 Text equals (safe)
    public static void verifyTextEquals(String actual, String expected, String message) {
        Assert.assertNotNull(actual, "Actual value is null");
        Assert.assertEquals(actual.trim(), expected.trim(), message);
    }

    // 🔹 Text contains
    public static void verifyTextContains(String actual, String expected, String message) {
        Assert.assertNotNull(actual, "Actual value is null");
        Assert.assertTrue(actual.contains(expected), message);
    }

    // 🔹 Boolean condition
    public static void verifyTrue(boolean condition, String message) {
        Assert.assertTrue(condition, message);
    }

    // 🔹 Boolean false
    public static void verifyFalse(boolean condition, String message) {
        Assert.assertFalse(condition, message);
    }

}
