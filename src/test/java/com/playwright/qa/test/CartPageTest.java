package com.playwright.qa.test;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.playwright.qa.base.BaseTest;
import com.playwright.qa.base.ConfigReader;
import com.playwright.qa.pages.CartPage;
import com.playwright.qa.pages.DashboardPage;
import com.playwright.qa.pages.LandingPage;
import com.playwright.qa.pages.LoginPage;
import com.playwright.qa.utils.ExcelUtil;

public class CartPageTest extends BaseTest{
	
	LandingPage landingPage;
	LoginPage loginPage;
	DashboardPage dashboardPage;
	
	
	
	@BeforeMethod(groups = {"smoke", "regression"}, alwaysRun = true)
	public void setUpCartPage() {
		landingPage = new LandingPage(page());
		String email = ConfigReader.get("user.email");
		String password = ConfigReader.get("user.password");

		loginPage = landingPage.clickloginButton();
		dashboardPage = loginPage.loginToApplication(email, password);
	}
	
	
	
	
	
	@Test(groups= {"smoke","regression"})
	public void verifyCartEndToEndFlowTest() {

	    Object[][] data = ExcelUtil.getTestData("CoursesAndPrices");

	    int expectedTotal = 0;
	    List<String> expectedCourses = new ArrayList<>();
	   

	    // 🔥 LOOP ALL DATA (this is the key change)
	    for (Object[] course : data) {

	        String courseName = course[0].toString();
	        String priceText = course[1].toString();

	        int price = Integer.parseInt(priceText.replaceAll("[^0-9]", ""));

	        dashboardPage.addCourseToCart(courseName);

	        // small wait for stability
	      //  page.waitForTimeout(500);

	        expectedTotal += price;
	        expectedCourses.add(courseName);
	    }

	    // 👉 Navigate to Cart
	    CartPage cartPage = dashboardPage.goToCart();

	    // 👉 Verify total price
	    Assert.assertEquals(
	        cartPage.getTotalPrice(),
	        expectedTotal,
	        "Total price mismatch!"
	    );

	    // 👉 Verify all courses present
	    for (String course : expectedCourses) {
	        Assert.assertTrue(
	            cartPage.isCoursePresent(course),
	            "Course missing in cart: " + course
	        );
	    }
	}
	
	@Test(groups= {"regression"})
	public void UpdateAndRemoveCartTest()
	{
		Object[][] data = ExcelUtil.getTestData("CoursesAndPrices");
		
		int expectedTotal=0;
		List<String> expectedCourse=new ArrayList<>();
		
		for(Object[] course:data)
		{
			String courseName=course[0].toString();
			String priceText=course[1].toString();
			int price=Integer.parseInt(priceText.replaceAll("[^0-9]",""));
			dashboardPage.addCourseToCart(courseName);
			// page.waitForTimeout(500);
			 
			 expectedTotal +=price;
			 expectedCourse.add(courseName);
			
			
			
			
		}
		
		
		
		CartPage cartPage = dashboardPage.goToCart();
		
		
		Assert.assertEquals(cartPage.getTotalPrice(), expectedTotal, "Total Price Mismatch");
		
		for(String course:expectedCourse)
		{
			Assert.assertTrue(cartPage.isCoursePresent(course),"Course missing "+course);
		}
		int updatedTotal=expectedTotal;
		List<String>courseToBeRemoved=new ArrayList<>(); 
		
		
		for(Object[] course:data)
		{
			
			
			String courseNametoBeRemoved=course[0].toString();
			String priceTextofCourseToBeRemoved=course[1].toString();
			int priceofCourseToBeRemoved=Integer.parseInt(priceTextofCourseToBeRemoved.replaceAll("[^0-9]",""));
			cartPage.removeCourseFromCart(courseNametoBeRemoved);
		//	page.waitForTimeout(500);
			
			updatedTotal-=priceofCourseToBeRemoved;
			
			
		}
		Assert.assertEquals(cartPage.getTotalPrice(),updatedTotal,"price not matching after removal of cart");
		
	}
	
	
	@Test(groups= {"smoke","regression"})
	public void enrollNowCourseTest()
	{
		  Object[][] data = ExcelUtil.getTestData("CoursesAndPrices");

		    int expectedTotal = 0;
		    List<String> expectedCourses = new ArrayList<>();
		   

		    // 🔥 LOOP ALL DATA (this is the key change)
		    for (Object[] course : data) {

		        String courseName = course[0].toString();
		        String priceText = course[1].toString();

		        int price = Integer.parseInt(priceText.replaceAll("[^0-9]", ""));

		        dashboardPage.addCourseToCart(courseName);

		        // small wait for stability
		     //   page.waitForTimeout(500);

		        expectedTotal += price;
		        expectedCourses.add(courseName);
		    }

		    // 👉 Navigate to Cart
		    CartPage cartPage = dashboardPage.goToCart();

		    // 👉 Verify total price
		    Assert.assertEquals(
		        cartPage.getTotalPrice(),
		        expectedTotal,
		        "Total price mismatch!"
		    );
		    
		    cartPage.enrollNow("HIPL", "998877665544");
		   Assert.assertTrue(cartPage.isCourseEnrolled(), "Courses not enrollled");
	}
	
	@Test
	public void toastMessageTest() {
		
		  Object[][] data = ExcelUtil.getTestData("CoursesAndPrices");

	    int expectedTotal = 0;
	    List<String> expectedCourses = new ArrayList<>();
	   

	    // 🔥 LOOP ALL DATA (this is the key change)
	    for (Object[] course : data) {

	        String courseName = course[0].toString();
	        String priceText = course[1].toString();

	        int price = Integer.parseInt(priceText.replaceAll("[^0-9]", ""));

	        dashboardPage.addCourseToCart(courseName);

	        // small wait for stabilit
	     //   page.waitForTimeout(500);

	        expectedTotal += price;
	        expectedCourses.add(courseName);
	    }

	    // 👉 Navigate to Cart
	    CartPage cartPage = dashboardPage.goToCart();

	    // 👉 Verify total price
	    Assert.assertEquals(
	        cartPage.getTotalPrice(),
	        expectedTotal,
	        "Total price mismatch!"
	    );
	    
	    cartPage.goToEnrollPage();
	 
	    Assert.assertTrue(cartPage.addressAlert().contains("Address is empty"));
	    Assert.assertTrue(cartPage.phoneNumberAlert().contains("Phone number is empty"));
	    
	}
	 
}
