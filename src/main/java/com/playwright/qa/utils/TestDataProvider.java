package com.playwright.qa.utils;

import org.testng.annotations.DataProvider;

public class TestDataProvider {
	
	   @DataProvider(name = "signupData")
	    public static Object[][] getSignupData() {
	        return ExcelUtil.getTestData("signup");
	    }
	   
	   @DataProvider(name="signUpPasswordCheckData")
	   public static Object[][] getPasswordDataLessThanLimit() {
		   return ExcelUtil.getTestData("signUpPasswordCheck");
	   }
	   
	   @DataProvider(name="coursesAndPricesData")
	   public static Object[][] getCoursesAndPricesData()
	   {
		   return ExcelUtil.getTestData("CoursesAndPrices");
	   }
	   
	   @DataProvider(name="newCourseData")
	   public static Object[][] getNewCourseData()
	   {
		   return ExcelUtil.getTestData("NewCourseData");
	   }
	   
	   
	   
}
