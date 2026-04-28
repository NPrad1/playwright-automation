package com.playwright.qa.pages;

import java.nio.file.Path;
import java.util.List;

import org.testng.Assert;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.WaitForSelectorState;

public class ManageCoursesPage {

	private Locator manageCoursesIconLocator;
	private Locator searchCoursesBoxLocator;
	private Locator courseNameInTableResult;
	private Locator priceInTableResult;
	private Locator addNewCourseButtonLocator;
	private Locator chooseUploadFileButtonLocator;
	private Locator newCourseNameTextBoxLocator;
	private Locator descriptionNewCourseTextBoxLocator;
	private Locator instructorNameNewCourseTextBoxLocator;
	private Locator priceNewCourseTextBoxLocator;
	private Locator startDateNewCourseTextBoxLocator;
	private Locator endDateNewCourseTextBoxLocator;
	private Locator checkboxPermanentNewCourseLocator;
	private Locator selectCategoryButtonNewCourseLocator;
	private Locator seleniumCategoryButtonNewCourseLocator;
	private Locator saveNewCourseButtonLocator;
	private Locator loadMoreButtonLocator;
	
	
	private Page page;

	public ManageCoursesPage(Page page) {
		this.page = page;
		manageCoursesIconLocator = page.locator("//h1[@class='title']");
		searchCoursesBoxLocator = page.locator("//input[@name='searchText']");
		courseNameInTableResult = page.locator("//table[@class='courses-table table table-borderless']//tr/td[2]");
		priceInTableResult = page.locator("//table[@class='courses-table table table-borderless']//tr/td[3]");
		addNewCourseButtonLocator=page.locator("//div[@class=\"manage-btns\"]/button[normalize-space()='Add New Course']");
		chooseUploadFileButtonLocator=page.locator("#thumbnail");
		newCourseNameTextBoxLocator=page.locator("#name");
		descriptionNewCourseTextBoxLocator=page.locator("#description");
		instructorNameNewCourseTextBoxLocator=page.locator("#instructorNameId");
		priceNewCourseTextBoxLocator=page.locator("#price");
		startDateNewCourseTextBoxLocator=page.locator("//input[@name='startDate']");
		endDateNewCourseTextBoxLocator=page.locator("//input[@name='endDate']");
		checkboxPermanentNewCourseLocator=page.locator("#isPermanent");
		selectCategoryButtonNewCourseLocator=page.locator("//button[@class='menu-btn']");
		seleniumCategoryButtonNewCourseLocator=page.locator("//div[@class='menu-items']/button[normalize-space()='Selenium']");
		saveNewCourseButtonLocator=page.locator("//button[@class='action-btn']");
		loadMoreButtonLocator=page.locator("//button[normalize-space()='Load More']");
		
		
		
		

	}

	public boolean isPageLoaded() {
		return page.url().contains("/course/manage") && manageCoursesIconLocator.isVisible();
	}
	
	public List<String> searchCourse(String expectedCourseName) {
		
		

		searchCoursesBoxLocator.fill(expectedCourseName);
		courseNameInTableResult.first().waitFor();
		
		return courseNameInTableResult.allInnerTexts();
	}

	public List<String> getCoursePrices(String expectedPrice) {
		priceInTableResult.first().waitFor();
		return priceInTableResult.allInnerTexts();
	}
	
	public void loadAllCoursesOnManageCoursesTable() {

	    while (true) {

	        if (!loadMoreButtonLocator.isVisible()) {
	            break;
	        }

	        loadMoreButtonLocator.click();

	        // IMPORTANT: wait for new data to load
	        page.waitForTimeout(1000);

	        // optional safety break if needed later
	    }
	}
	public void waitForTableToStabilize() {

	    Locator rows = courseNameInTableResult.first();

	    // wait until at least one row exists
	    rows.waitFor();

	    // extra stability wait (important for React apps)
	    page.waitForTimeout(800);
	}

	public void isCourseDisplayed(String courseName, String expectedPrice) {
		/* // Step 1: Search your course
	    searchCoursesBoxLocator.fill(courseName);

	    // Step 2: Find the row using course name
	    Locator row = page.locator("//tr[td[normalize-space()='" + courseName + "']]");

	    // Step 3: Verify course name is present (this itself confirms name)
	    PlaywrightAssertions.assertThat(row).isVisible();

	    // Step 4: Get price from same row
	    String actualPrice = row.locator("//td[3]").textContent().trim();

	    // Step 5: Normalize price (remove ₹, spaces, commas)
	    String actual = actualPrice.replaceAll("[^0-9]", "");
	    String expected = expectedPrice.replaceAll("[^0-9]", "");

	    // Step 6: Verify price
	    Assert.assertEquals(actual, expected, "Price mismatch for course: " + courseName);
	*/

	    // STEP 1: load full table first
	    loadAllCoursesOnManageCoursesTable();
	    waitForTableToStabilize();

	    // STEP 2: now search
	    searchCoursesBoxLocator.fill(courseName);

	    // STEP 3: WAIT for table update (important)
	    page.waitForTimeout(1000);

	    // STEP 4: now locate row safely
	    Locator row = page.locator(
	        "//tr[td[normalize-space()='" + courseName + "']]"
	    );

	    // STEP 5: DO NOT use waitFor() blindly
	    if (row.count() == 0) {
	        Assert.fail("Course not found in table: " + courseName);
	    }

	    // STEP 6: get price safely
	    String actualPrice = row.locator("td:nth-child(3)").innerText();

	    String actual = actualPrice.replaceAll("[^0-9]", "");
	    String expected = expectedPrice.replaceAll("[^0-9]", "");

	    Assert.assertEquals(actual, expected,
	            "Price mismatch for course: " + courseName);
	}
	
	
	public String addNewCourse(String courseName,String Descritption, String instructor,String price,String startDate,String endDate,String filePath) {
		addNewCourseButtonLocator.click();
		
		chooseUploadFileButtonLocator.setInputFiles(Path.of(filePath));
		newCourseNameTextBoxLocator.fill(courseName);
		descriptionNewCourseTextBoxLocator.fill(Descritption);
		instructorNameNewCourseTextBoxLocator.fill(instructor);
		priceNewCourseTextBoxLocator.fill(price);
		startDateNewCourseTextBoxLocator.fill(startDate);
		endDateNewCourseTextBoxLocator.fill(endDate);
		
		 // ✅ Close calendar properly
	    page.locator("body").click();

	    // ✅ Ensure it's gone
	    page.locator(".react-datepicker")
	        .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));

	    // ✅ Now safe
	    checkboxPermanentNewCourseLocator.check();
		
	
		selectCategoryButtonNewCourseLocator.click();
		seleniumCategoryButtonNewCourseLocator.click();
		saveNewCourseButtonLocator.click();
		
		// Wait until new course is visible (strong sync)
		Locator newCourse = page.locator("//td[normalize-space()='" + courseName + "']");

		PlaywrightAssertions.assertThat(newCourse).isVisible();
		return newCourse.textContent().trim();
		
	}
	
}
