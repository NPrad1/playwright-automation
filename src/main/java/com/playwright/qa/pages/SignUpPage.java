package com.playwright.qa.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class SignUpPage {
	
	private Locator signUpIconLocator;
	private Locator nameLocator;
	private Locator emailLocator;
	private Locator passwordLocator;
	private Locator playwrightCheckBoxLocator;
	private Locator awsCheckBoxLocator;
	private Locator javaScriptCheckBoxLocator;
	private Locator seleniumCheckBoxLocator;
	private Locator maleCheckBoxLocator;
	private Locator femaleCheckBoxLocator;
	private Locator stateDropdownLocator;
	private Locator hobbiesDropdownLocator;
	private Locator signUpButtonLocator;
	private Locator emailAlreadyRegisteredMessageLocator;
	private Locator passwordMinimumLengthMessageLocator;
	
	
	
	
	private Page page;
	
	public SignUpPage(Page page)
	{ 
		
		this.page=page;
		signUpIconLocator=page.locator("//h2[text()='Sign Up']");
		nameLocator=page.getByPlaceholder("Name");
		emailLocator=page.getByPlaceholder("Email");
		passwordLocator=page.getByPlaceholder("Password");
		javaScriptCheckBoxLocator=page.locator("//label[text()='JavaScript']//preceding::input[1]");
		playwrightCheckBoxLocator=page.locator("//label[normalize-space()='PlayWright']//preceding::input[1]");
		awsCheckBoxLocator=page.locator("//label[text()='AWS']//preceding::input[1]");
		seleniumCheckBoxLocator=page.locator("//label[text()='Selenium']//preceding::input[1]");
		maleCheckBoxLocator=page.locator("//input[@value='Male']");
		femaleCheckBoxLocator=page.locator("//input[@value='Female']");
		stateDropdownLocator=page.locator("#state");
		hobbiesDropdownLocator=page.locator("#hobbies");
		signUpButtonLocator=page.locator(".submit-btn");
		emailAlreadyRegisteredMessageLocator=page.locator(".errorMessage");
		passwordMinimumLengthMessageLocator=page.locator("//h2[@class='errorMessage false']");
		
		
		
	}
	

	
	  //  PAGE VALIDATION
    public boolean isSignUpPageOpened() {
        signUpIconLocator.waitFor();
        return signUpIconLocator.isVisible();
    }

    //  ACTION: Enter user details
    public void enterUserDetails(String name, String email, String password, String state, String[] hobbies) {

        nameLocator.fill(name);
        emailLocator.fill(email);
        passwordLocator.fill(password);

        // Selecting skills (can be dynamic later)
        javaScriptCheckBoxLocator.check();
       // playwrightCheckBoxLocator.check();

        // Gender selection
        maleCheckBoxLocator.check();

        // Dropdowns
        selectDropdownByValue(state);
        selectHobbies(hobbies);
    }

    //  ACTION: Select state
    public void selectDropdownByValue(String value) {
        stateDropdownLocator.selectOption(value);
    }

    //  ACTION: Select hobbies
    public void selectHobbies(String[] hobbies) {
        hobbiesDropdownLocator.selectOption(hobbies);
    }

    //  ACTION: Click SignUp
    public void clickSignUp() {
        signUpButtonLocator.waitFor();
        signUpButtonLocator.click();
    }

    // VALIDATION: Error message
    public boolean isEmailAlreadyRegisteredErrorDisplayed() {
        emailAlreadyRegisteredMessageLocator.waitFor();
        return emailAlreadyRegisteredMessageLocator.isVisible();
    }

    
    public boolean isPasswordMinimumLengthErrorDisplayed() {
    	passwordMinimumLengthMessageLocator.waitFor();
    	return passwordMinimumLengthMessageLocator.isVisible();
    }
	/*
	public boolean signUpEmailAlreadyRegistered(String name, String email,String password,String state,String []hobbies)
	{
		nameLocator.fill(name);
		emailLocator.fill(email);
		passwordLocator.fill(password);
		javaScriptCheckBoxLocator.check();
		playwrightCheckBoxLocator.click();
		awsCheckBoxLocator.click();
		seleniumCheckBoxLocator.click();
		
		maleCheckBoxLocator.click();
		selectDropdownByValue(state);
		selectHobbies(hobbies);
		signUpButtonLocator.waitFor();
		signUpButtonLocator.click();
		emailAlreadyRegisteredMessageLocator.waitFor();
		return emailAlreadyRegisteredMessageLocator.isVisible();
		
	}
	*/
}
