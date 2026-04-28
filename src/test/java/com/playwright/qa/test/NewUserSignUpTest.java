package com.playwright.qa.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.playwright.qa.base.BaseTest;
import com.playwright.qa.pages.DashboardPage;
import com.playwright.qa.pages.LandingPage;
import com.playwright.qa.pages.LoginPage;
import com.playwright.qa.pages.SignUpPage;
import com.playwright.qa.utils.TestDataProvider;

public class NewUserSignUpTest extends BaseTest {

	LandingPage landingPage;
	SignUpPage signUpPage;

	@BeforeMethod(groups = {"smoke", "regression"}, alwaysRun = true)
	public void setUpnewUserSignUpPage() {
		landingPage = new LandingPage(page());
		signUpPage = landingPage.clickloginButton().newUserSignUp();
	}

	// ✅ Verify SignUp Page opens
	@Test(groups = { "smoke" })
	public void verifyNewUserSignUpPage() {

		Assert.assertTrue(signUpPage.isSignUpPageOpened(), "New User SignUp Page not opened");
	}

	// ✅ Data-driven test using Excel
	@Test(dataProvider = "signupData", dataProviderClass = TestDataProvider.class, groups = { "regression" })
	public void verifyNewUserSignUpEmailAlreadyRegistered(String name, String email, String password, String state,
			String hobbiesStr) {

		String[] hobbies = hobbiesStr.split(",");

		signUpPage.enterUserDetails(name, email, password, state, hobbies);
		signUpPage.clickSignUp();

		Assert.assertTrue(signUpPage.isEmailAlreadyRegisteredErrorDisplayed(),
				"Email already registered message not displayed");
	}
	
	@Test(dataProvider= "signUpPasswordCheckData",dataProviderClass = TestDataProvider.class,groups= {"regression"})
	public void passwordMimimumLengthTest(String name, String email, String password, String state,
			String hobbiesStr)
	{

		String[] hobbies = hobbiesStr.split(",");

		signUpPage.enterUserDetails(name, email, password, state, hobbies);
		signUpPage.clickSignUp();
		Assert.assertTrue(signUpPage.isPasswordMinimumLengthErrorDisplayed(),"Password minimum length error message not displayed");
	}
	
	@Test(dataProvider="signupData",dataProviderClass = TestDataProvider.class,groups={"regression","smoke"})
	public void EndToEndNewUserdRegisterAndLoginSuccessfullyTest(String name, String email, String password,String state, String hobbiesStr)
	{
		String[] hobbies = hobbiesStr.split(",");
		String uniqueEmail =System.currentTimeMillis()+email;
		signUpPage.enterUserDetails(name, uniqueEmail, password, state, hobbies);
		signUpPage.clickSignUp();
		
		LoginPage loginPage=new LoginPage(page());
		DashboardPage dashboardPage = loginPage.loginToApplication(uniqueEmail, password);
		Assert.assertTrue(dashboardPage.isLoginSuccessful(), "Expected user to be logged in, but login failed");
		
	}
}
