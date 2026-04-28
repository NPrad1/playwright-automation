package com.playwright.qa.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.WaitForSelectorState;

public class LoginPage {

	private Locator userNameLocator;
	private Locator passwordLocator;
	private Locator submitButtonLocator;
	private Locator newUserSignUpButton;
	private Locator socialMediaButton;
	private Locator emailAndPasswordRequiredMessageLocator;
	private Locator userEmailDoesnotExistMessageLocator;
	private Locator passwordIsRequiredMessageLocator;
	private Locator emailIsRequiredMessageLocator;
	private Locator cartButtonLocator;
	private Page page;

	public LoginPage(Page page) {
		this.page = page;
		userNameLocator = page.getByPlaceholder("Enter Email");
		passwordLocator = page.getByPlaceholder("Enter Password");
		submitButtonLocator = page.locator(".submit-btn");
		newUserSignUpButton = page.locator("//a[text()='New user? Signup']");
		socialMediaButton = page.locator("//div[@class='social-btns']/a");
		emailAndPasswordRequiredMessageLocator = page.locator(".errorMessage");
		userEmailDoesnotExistMessageLocator = page
				.locator("//h2[contains(@class,'errorMessage') and contains(normalize-space(),'Email')]");
		passwordIsRequiredMessageLocator=page.locator("//h2[contains(@class,'errorMessage') and contains(normalize-space(),'Password is required')]");
		emailIsRequiredMessageLocator=page.locator("//h2[contains(@class,'errorMessage') and contains(normalize-space(),'Email is required')]");
		cartButtonLocator = page.locator("//button[@class='cartBtn']");

	}

	// Create login method and pass parameter

	public DashboardPage loginToApplication(String userName, String password) {
		userNameLocator.fill(userName);
		passwordLocator.fill(password);
		submitButtonLocator.click();
	    // Strong, reliable sync
		page.waitForLoadState();
		return new DashboardPage(page);

	}

	public SignUpPage newUserSignUp() {
		newUserSignUpButton.click();

		return new SignUpPage(page);

	}

	public boolean isNewUserSignUpButtonDisplayed() {
		newUserSignUpButton.waitFor();
		return newUserSignUpButton.isVisible();
	}

	public String loginPageURL() {
		return page.url();
	}

	public boolean isEmailAndPasswordErrorDisplayed() {
		submitButtonLocator.waitFor();
		submitButtonLocator.click();
		emailAndPasswordRequiredMessageLocator.waitFor();
		return emailAndPasswordRequiredMessageLocator.isVisible();
	}

	public int socialMediaButtonDisplayed() {
		socialMediaButton.first().waitFor();
		return socialMediaButton.count();
	}

	public boolean isLogoutSuccessfull() {
		userNameLocator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

		return userNameLocator.isVisible() && submitButtonLocator.isVisible();
	}

	public boolean loginUnregisteredUser(String userName, String password) {
		userNameLocator.fill(userName);
		passwordLocator.fill(password);
		submitButtonLocator.click();
		userEmailDoesnotExistMessageLocator.waitFor();
		return userEmailDoesnotExistMessageLocator.isVisible();

	}
	
	public boolean loginWithEmail(String userName) {
		userNameLocator.fill(userName);
		submitButtonLocator.click();
		passwordIsRequiredMessageLocator.waitFor();
		return passwordIsRequiredMessageLocator.isVisible();
		
	}
	
	public boolean loginWithPassword(String password)
	{
		passwordLocator.fill(password);
		submitButtonLocator.click();
		emailIsRequiredMessageLocator.waitFor();
		return emailIsRequiredMessageLocator.isVisible();
	}
}
