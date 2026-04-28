package com.playwright.qa.pages;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

public class CartPage {

	private Locator cartItemsLocator;
	private Locator totalPriceLocator;
	private Locator enrollNowButtonLocator;
	private Locator enrollNowPageButtonLocator;
	private Locator addressEnrollPageLocator;
	private Locator phoneNumberEnrollPageLocator;
	private Locator enrollConfirmationMessageLocator;
	private Locator toastMessageLocator;

	private Page page;

	public CartPage(Page page) {
		this.page = page;

		cartItemsLocator = page.locator("//div[contains(@class,'course-card')]//h2");
		totalPriceLocator = page.locator("//h3[contains(text(),'Total Price')]");
		enrollNowButtonLocator = page.locator("//div[@class='top-container']//button[normalize-space()='Enroll Now']");
		enrollNowPageButtonLocator = page
				.locator("//div[@class='modal-footer']//button[normalize-space()='Enroll Now']");
		addressEnrollPageLocator = page.locator("#address");
		phoneNumberEnrollPageLocator = page.locator("#phone");
		enrollConfirmationMessageLocator = page.locator("//h4[@class='uniqueId']");
		toastMessageLocator = page.locator(".Toastify__toast-body");

	}

	public int getTotalPrice() {

		String text = totalPriceLocator.innerText();
		System.out.println("Raw Text: " + text);

		String numeric = text.replaceAll("[^0-9]", "");
		System.out.println("Extracted: " + numeric);

		return Integer.parseInt(numeric);
	}

	public boolean isCoursePresent(String courseName) {

		List<String> names = cartItemsLocator.allInnerTexts();

		for (String name : names) {
			if (name.equalsIgnoreCase(courseName)) {
				return true;
			}
		}

		return false;

	}

	public void removeCourseFromCart(String courseName) {

		String xpath = "//div[contains(@class,'course-card')]//h2[contains(text(),'" + courseName
				+ "')]/following::button[1]";

		Locator addToCartBtn = page.locator(xpath);

		addToCartBtn.waitFor();
		addToCartBtn.click();
	}

	public void goToEnrollPage() {
		PlaywrightAssertions.assertThat(enrollNowButtonLocator).isVisible();
		    enrollNowButtonLocator.click();
		
	}
	
	public String toastMessage() {
	    PlaywrightAssertions.assertThat(toastMessageLocator.last()).isVisible();
	    return toastMessageLocator.last().innerText();
	}
	
	public void enrollNow(String address, String phoneNumber) {
		goToEnrollPage();
		PlaywrightAssertions.assertThat(addressEnrollPageLocator).isVisible();
		addressEnrollPageLocator.fill(address);
		phoneNumberEnrollPageLocator.fill(phoneNumber);
		enrollNowPageButtonLocator.click();
	}

	public boolean isCourseEnrolled() {
		PlaywrightAssertions.assertThat(enrollConfirmationMessageLocator).isVisible();
		System.out.println(enrollConfirmationMessageLocator.innerText() + " expected confirmation message");
		return enrollConfirmationMessageLocator.innerText().contains("Your order id is ");
	}

		
		
	
		
	public String addressAlert() {
	  //  goToEnrollPage();
	    enrollNowPageButtonLocator.click();
	   return toastMessage();
	   // verifyToastMessage("Address is empty");
	}
	public String phoneNumberAlert() {
	   
	    addressEnrollPageLocator.fill("Test Address");
	    enrollNowPageButtonLocator.click();
	    return toastMessage();
	   // verifyToastMessage("Phone number is empty");
	}
}
