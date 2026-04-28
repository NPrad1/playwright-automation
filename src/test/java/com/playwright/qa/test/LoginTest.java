package com.playwright.qa.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.playwright.qa.base.BaseTest;
import com.playwright.qa.base.ConfigReader;
import com.playwright.qa.pages.DashboardPage;
import com.playwright.qa.pages.LandingPage;
import com.playwright.qa.pages.LoginPage;

public class LoginTest extends BaseTest {
	LandingPage landingPage;

	@BeforeMethod(groups = {"smoke", "regression"}, alwaysRun = true)
	public void setUpLoginPage() {
		landingPage = new LandingPage(page());

	}

	@Test(groups = { "smoke", "regression" })
	public void verifyLoginLogOut() {

		String email = ConfigReader.get("user.email");
		String password = ConfigReader.get("user.password");
		LoginPage loginPage = landingPage.clickloginButton();
		Assert.assertTrue(loginPage.loginPageURL().contains("/login"));
		DashboardPage dashboardPage = loginPage.loginToApplication(email, password);
		Assert.assertTrue(dashboardPage.isLoginSuccessful(), "Login failed - cart button not visible");
		loginPage = dashboardPage.logOutApplication();
		Assert.assertTrue(loginPage.isLogoutSuccessfull(),"Log out not Successful");
		

	}

	@Test(priority = 1 ,groups = { "smoke", "regression" })
	public void verifyLoginPageURLTest() {

		LoginPage loginPage = landingPage.clickloginButton();
		// 🔥 ensure navigation completed
		page().waitForURL("**/login");

		Assert.assertTrue(loginPage.loginPageURL().contains("/login"), "Login page URL no matching");

	}

	@Test(priority=2,groups = { "smoke", "regression" })
	public void loginPageNewUserSignUpButtonTest() {

		LoginPage loginPage = landingPage.clickloginButton();

		Assert.assertTrue(loginPage.isNewUserSignUpButtonDisplayed(), "New User SignUp Button is not Present");
	}

	@Test(priority=3,groups = "regression")
	public void loginPageSocialMediaButtonTest() {
		// landingPage=new LandingPage(page);
		LoginPage loginPage = landingPage.clickloginButton();

		// loginPage=new LoginPage(page);
		Assert.assertTrue(loginPage.socialMediaButtonDisplayed() > 0, "Social Media Button not present on login page");
	}
	
	@Test(priority=4,groups="regression")
	public void loginWithOutEmailPasswordTest()
	{
		LoginPage loginPage=landingPage.clickloginButton();
		Assert.assertTrue(loginPage.isEmailAndPasswordErrorDisplayed(),"Error message should be displayed when signing in without email and password");
	}
	
	@Test(priority=5,groups="regression")
	public void unregisteredEmailUserTest() {
		
		String email = ConfigReader.get("user.unregemail");
		String password = ConfigReader.get("user.password");
		LoginPage loginPage = landingPage.clickloginButton();
		Assert.assertTrue(loginPage.loginUnregisteredUser(email, password));
		
	}
	
	@Test(priority=6,groups="regression")
	public void loginWithEmailAndEmptyPasswordTest()
	{
		String email=ConfigReader.get("user.email");
		LoginPage loginPage=landingPage.clickloginButton();
		Assert.assertTrue(loginPage.loginWithEmail(email), "Login with only Email method failed");
		
	}
	
	@Test(priority=7,groups="regression")
	public void loginWithPasswordAndEmptyEmailTest()
	{
		String password=ConfigReader.get("user.password");
		LoginPage loginPage=landingPage.clickloginButton();
		Assert.assertTrue(loginPage.loginWithPassword(password), "Login with only Password method failed");
	}
}
