package com.playwright.qa.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class LandingPage {

	private Locator menuLocator;
	private Locator loginButtonLocator;
	private Locator coursesIconsLocator;
	private Locator socialMediaLocator;
	private Page page;

	public LandingPage(Page page) {
		this.page = page;
		menuLocator = page.locator("//img[@alt='menu']");
		loginButtonLocator = page.getByRole(
		        AriaRole.BUTTON,
		        new Page.GetByRoleOptions().setName("Log in"));
		coursesIconsLocator = page.locator(".course-card");
		socialMediaLocator = page.locator("//div[@class='social-btns']/a");

	}

	// Create login method and pass parameter

	public String getLandingPageTitle() {
		return page.title();
	}

	public int getCoursesCount() {
		coursesIconsLocator.first().waitFor();
		return coursesIconsLocator.count();
	}

	public int getSocialMediaIconsCount() {
		socialMediaLocator.first().waitFor();
		return socialMediaLocator.count();
	}

	public LoginPage clickLoginButton() {
		
/*
		menuLocator.click();

		loginButtonLocator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

		loginButtonLocator.click();

		return new LoginPage(page);
		*/
		
/*
         
	    menuLocator.waitFor();
	    menuLocator.click();

	    // 🔥 wait until login button is actually visible and stable
	    loginButtonLocator.waitFor(new Locator.WaitForOptions()
	        .setState(WaitForSelectorState.VISIBLE));

	    // 🔥 ensure clickable (important)
	    loginButtonLocator.scrollIntoViewIfNeeded();

	    loginButtonLocator.click();

	    // 🔥 wait for navigation properly
	 

	  
	   
	
	    return new LoginPage(page);*/

	    // open menu
	    menuLocator.waitFor(new Locator.WaitForOptions()
	            .setState(WaitForSelectorState.VISIBLE));

	    menuLocator.click();

	    // wait for animation completion
	    page.waitForTimeout(1500);

	    // wait for login button
	    loginButtonLocator.waitFor(new Locator.WaitForOptions()
	            .setState(WaitForSelectorState.VISIBLE));

	    // ensure button is inside viewport
	    loginButtonLocator.scrollIntoViewIfNeeded();

	    // safer click
	    loginButtonLocator.click();

	    return new LoginPage(page);
	
	}

}
