package com.playwright.qa.listener;

import org.testng.ISuite;
import org.testng.ISuiteListener;

public class ExecutionTimeListener implements ISuiteListener {

	private long startTime;

	@Override
	public void onStart(ISuite suite) {

		startTime = System.currentTimeMillis();

		System.out.println("================================================================");
		System.out.println("Execution Started");
		System.out.println("================================================================");

	}

	@Override
	public void onFinish(ISuite suite) {
		
		long endTime=System.currentTimeMillis();
		
		long totalTime=(endTime-startTime)/1000;
		
		long minutes=totalTime/60;
		long seconds=totalTime%60;
		
		System.out.println("=================================================================");
		
		System.out.println("Execution Completed");
		System.out.println("Total Execution Time : "+minutes+ " minutes "+seconds+ " seconds");
		System.out.println("=================================================================");
		
		

	}

}
