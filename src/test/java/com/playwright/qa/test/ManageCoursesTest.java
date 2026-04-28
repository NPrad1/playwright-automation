package com.playwright.qa.test;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.playwright.qa.base.BaseTest;
import com.playwright.qa.base.ConfigReader;
import com.playwright.qa.pages.DashboardPage;
import com.playwright.qa.pages.LandingPage;
import com.playwright.qa.pages.LoginPage;
import com.playwright.qa.pages.ManageCoursesPage;
import com.playwright.qa.utils.TestDataProvider;

public class ManageCoursesTest extends BaseTest {

	LandingPage landingPage;
	LoginPage loginPage;
	DashboardPage dashboardPage;
	ManageCoursesPage manageCoursesPage;

	@BeforeMethod(groups = {"smoke", "regression"}, alwaysRun = true)
	public void setUpDashboardPage() {
		landingPage = new LandingPage(page());
		String email = ConfigReader.get("user.email");
		String password = ConfigReader.get("user.password");

		loginPage = landingPage.clickloginButton();
		dashboardPage = loginPage.loginToApplication(email, password);
		manageCoursesPage = dashboardPage.manageCourses();

	}

	@Test(groups= {"smoke","regression"})
	public void verifyManageCourseNavigation() {

		Assert.assertTrue(manageCoursesPage.isPageLoaded(),
				"User not landed on Manage Course Page. URL: " + manageCoursesPage.isPageLoaded());
	}

	@Test(dataProvider = "coursesAndPricesData", dataProviderClass = TestDataProvider.class, groups = { "regression" })
	public void searchCourseNameTest(String courseName,String price) {
		
		manageCoursesPage.isCourseDisplayed(courseName, price);

	}
	
	@Test(dataProvider="newCourseData", dataProviderClass=TestDataProvider.class, groups= {"smoke","regression"},enabled=false)
	public void addNewCourseTest(String courseNameExcel,String description, String insructor,String price,String startDate,String endDate) {
		String filePath="screenshots\\loginPageSocialMeadiaButtonTest.png";
		
		String uniqueCourseName = courseNameExcel + System.currentTimeMillis();
		String actualCourseName=manageCoursesPage.addNewCourse(uniqueCourseName ,description, insructor, price, startDate, endDate,filePath);
	
		Assert.assertEquals(actualCourseName, uniqueCourseName,
		        "Course name mismatch. Expected: " + uniqueCourseName + " but found: " + actualCourseName);
	}
}
