package com.playwright.qa.pages;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DashboardPage {

	private Locator cartButtonLocator;
	private Locator menuButtonLocator;
	private Locator signOutButtonLocator;
	private Locator coursesLocator;
	private Locator coursePriceLocator;
	private Locator manageButtonLocator;
	private Locator manageCoursesButtonLocator;

	
	private Page page;

	public DashboardPage(Page page) {
		this.page = page;
		cartButtonLocator = page.locator("//button[@class='cartBtn']");
		menuButtonLocator=page.locator("//img[@alt='menu']");
		signOutButtonLocator=page.locator("//button[normalize-space()='Sign out']");
		coursesLocator=page.locator("//div[@class='course-content']/h2");
		coursePriceLocator=page.locator("//div[@class='course-card row']/div[2]/span/b");
		manageButtonLocator=page.locator("//div[@class='nav-menu-item-manage']");
		manageCoursesButtonLocator=page.locator("//a/img[@alt='manage course']");
		
		
	}

	// verification method
	public boolean isLoginSuccessful() {
		cartButtonLocator.waitFor();
		return cartButtonLocator.isVisible();
	}
	
	public LoginPage logOutApplication() {
		menuButtonLocator.waitFor();
		menuButtonLocator.click();
		
		signOutButtonLocator.waitFor();
		signOutButtonLocator.click();
		 return new LoginPage(page);
		
	}
	
	public int getCourseCount() {
		coursesLocator.first().waitFor();
		return coursesLocator.count();
		
	}
	
	public List<String> getCourseNames() {
	    coursesLocator.first().waitFor();
	    return coursesLocator.allInnerTexts();
	}

	public List<String> getCoursePrices() {
	    coursePriceLocator.first().waitFor();
	    return coursePriceLocator.allInnerTexts();
	}
	
	public ManageCoursesPage manageCourses() {
		manageButtonLocator.hover();
		manageCoursesButtonLocator.click();
		return new ManageCoursesPage(page);
	}
	public CartPage goToCart() {
	    cartButtonLocator.click();
	    return new CartPage(page);
	}
	
	public void addCourseToCart(String courseName) {

		  String xpath = "//div[contains(@class,'course-card')]//h2[contains(text(),'" + courseName + "')]/following::button[1]";
		    
		    Locator addToCartBtn = page.locator(xpath);
		    
		    addToCartBtn.waitFor();
		    addToCartBtn.click();
	}
	
	
	
	
	
}
