package com.playwright.qa.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.playwright.qa.base.BaseTest;
import com.playwright.qa.pages.LandingPage;

public class LandingPageTest extends BaseTest {

	LandingPage landingPage;
	
	@BeforeMethod(groups = {"smoke", "regression"}, alwaysRun = true)
	public void setUpLandingpage() {
		landingPage = new LandingPage(page());
	}

	@Test(groups = "smoke")
	public void LandingPageCoursesTest() {
		
		Assert.assertTrue(landingPage.getCoursesCount() > 0, "Expected courses on landing page but none found");

	}

	@Test(groups = "regression")
	public void LandingPageSocialMediaIconsTest() {

		Assert.assertTrue(landingPage.getSocialMediaIconsCount() > 0, "Social media icons not present on Landing page");

	}

	@Test(groups = "smoke")
	public void LandingPageTitleTest() {
		String expectedTitle="Learn Automation Courses";
		Assert.assertTrue(landingPage.getLandingPageTitle().contains(expectedTitle),"Landing Page Title is not matching");
	}
}
