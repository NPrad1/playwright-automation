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

	@Test(groups = { "smoke", "regression" })
	public void verifyLoginLogOut() {
		landingPage = new LandingPage(getPage());
		String email = ConfigReader.get("user.email");
		String password = ConfigReader.get("user.password");
		LoginPage loginPage = landingPage.clickLoginButton();
		Assert.assertTrue(loginPage.loginPageURL().contains("/login"));
		DashboardPage dashboardPage = loginPage.loginToApplication(email, password);
		Assert.assertTrue(dashboardPage.isLoginSuccessful(), "Login failed - cart button not visible");
		loginPage = dashboardPage.logOutApplication();
		Assert.assertTrue(loginPage.isLogoutSuccessfull(), "Log out not Successful");

	}

	@Test(priority = 1, groups = { "smoke", "regression" })
	public void verifyLoginPageURLTest() {
		landingPage = new LandingPage(getPage());
		LoginPage loginPage = landingPage.clickLoginButton();
		// 🔥 ensure navigation completed
		getPage().waitForURL("**/login");
		System.out.println("Running Test On Thread : " + Thread.currentThread().getId());
		// Assert.assertFalse(true, "Failing the test intentionally");

		Assert.assertTrue(loginPage.loginPageURL().contains("/login"), "Login page URL no matching");

	}

	@Test(priority = 2, groups = { "smoke", "regression" })
	public void loginPageNewUserSignUpButtonTest() {
		landingPage = new LandingPage(getPage());

		LoginPage loginPage = landingPage.clickLoginButton();
		System.out.println("Running Test On Thread : " + Thread.currentThread().getId());
		Assert.assertTrue(loginPage.isNewUserSignUpButtonDisplayed(), "New User SignUp Button is not Present");
	}

	@Test(priority = 3, groups = "regression")
	public void loginPageSocialMediaButtonTest() {
		landingPage = new LandingPage(getPage());
		LoginPage loginPage = landingPage.clickLoginButton();

		// loginPage=new LoginPage(page);

		System.out.println("Running Test On Thread : " + Thread.currentThread().getId());
		Assert.assertTrue(loginPage.socialMediaButtonDisplayed() > 0, "Social Media Button not present on login page");
	}

	@Test(priority = 4, groups = "regression")
	public void loginWithOutEmailPasswordTest() {
		landingPage = new LandingPage(getPage());
		LoginPage loginPage = landingPage.clickLoginButton();
		System.out.println("Running Test On Thread : " + Thread.currentThread().getId());
		Assert.assertTrue(loginPage.isEmailAndPasswordErrorDisplayed(),
				"Error message should be displayed when signing in without email and password");
	}

	@Test(priority = 5, groups = "regression")
	public void unregisteredEmailUserTest() {
		landingPage = new LandingPage(getPage());
		String email = ConfigReader.get("user.unregemail");
		String password = ConfigReader.get("user.password");
		LoginPage loginPage = landingPage.clickLoginButton();
		System.out.println("Running Test On Thread : " + Thread.currentThread().getId());
		Assert.assertTrue(loginPage.loginUnregisteredUser(email, password));

	}

	@Test(priority = 6, groups = "regression")
	public void loginWithEmailAndEmptyPasswordTest() {
		landingPage = new LandingPage(getPage());
		String email = ConfigReader.get("user.email");
		LoginPage loginPage = landingPage.clickLoginButton();
		System.out.println("Running Test On Thread : " + Thread.currentThread().getId());
		Assert.assertTrue(loginPage.loginWithEmail(email), "Login with only Email method failed");

	}

	@Test(priority = 7, groups = "regression")
	public void loginWithPasswordAndEmptyEmailTest() {
		landingPage = new LandingPage(getPage());
		String password = ConfigReader.get("user.password");
		LoginPage loginPage = landingPage.clickLoginButton();
		System.out.println("Running Test On Thread : " + Thread.currentThread().getId());
		Assert.assertTrue(loginPage.loginWithPassword(password), "Login with only Password method failed");
	}
}
