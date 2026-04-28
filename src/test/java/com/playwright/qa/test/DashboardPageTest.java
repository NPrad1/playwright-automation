package com.playwright.qa.test;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.playwright.qa.base.BaseTest;
import com.playwright.qa.base.ConfigReader;
import com.playwright.qa.pages.CartPage;
import com.playwright.qa.pages.DashboardPage;
import com.playwright.qa.pages.LandingPage;
import com.playwright.qa.pages.LoginPage;
import com.playwright.qa.utils.ExcelUtil;
import com.playwright.qa.utils.TestDataProvider;

public class DashboardPageTest extends BaseTest {

	LandingPage landingPage;
	LoginPage loginPage;
	DashboardPage dashboardPage;

	@BeforeMethod(groups = {"smoke", "regression"}, alwaysRun = true)
	public void setUpDashboardPage() {
		landingPage = new LandingPage(page());
		String email = ConfigReader.get("user.email");
		String password = ConfigReader.get("user.password");

		loginPage = landingPage.clickloginButton();
		dashboardPage = loginPage.loginToApplication(email, password);

	}

	@Test(groups = {"regression","smoke"})
	public void CourseTest() {
	
		 System.out.println(dashboardPage.getCourseCount());
		Assert.assertTrue(dashboardPage.getCourseCount() > 0, "No Course present on Dashboard");

	}

	@Test(groups = {"regression","smoke"})
	public void CourseNameAndPriceTest() {
	

		List<String> names = dashboardPage.getCourseNames();
		List<String> prices = dashboardPage.getCoursePrices();

		Assert.assertEquals(names.size(), prices.size(), "Course name and price count mismatch");

		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i).trim();
			String price = prices.get(i).trim();

			Assert.assertFalse(name.isEmpty(), "Course name is missing at index " + i);
			Assert.assertFalse(price.isEmpty(), "Course price is missing for course: " + name);
		}

	}

	@Test(dataProvider = "coursesAndPricesData", dataProviderClass = TestDataProvider.class, groups = { "regression" })
	public void verifyCourseNameAndPriceFromTestDataTest(String expectedName, String expectedPrice) {

		List<String> names = dashboardPage.getCourseNames();
		List<String> prices = dashboardPage.getCoursePrices();


		boolean courseFound = false;

		for (int i = 0; i < names.size(); i++) {

			String actualName = names.get(i).trim();
			String actualPrice = prices.get(i).trim();

			if (actualName.equalsIgnoreCase(expectedName)) {

				courseFound = true;

				Assert.assertEquals(actualPrice, expectedPrice, "Price mismatch for course: " + expectedName);

				break;
			}
		}

		Assert.assertTrue(courseFound, "Course not found in UI: " + expectedName);
	}
	
	@Test(dataProvider = "coursesAndPricesData", 
		      dataProviderClass = TestDataProvider.class, groups= {"regression"})
		public void verifyAddSpecificCourseToCart(String courseName, String price) {

		    dashboardPage.addCourseToCart(courseName);

		//    String text = dashboardPage.isCourseAdded(courseName, price);
		    	//	System.out.println(text);
		}
	
	
	
}
