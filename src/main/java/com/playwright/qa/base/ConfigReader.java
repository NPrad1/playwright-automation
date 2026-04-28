package com.playwright.qa.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
	
	 private static Properties properties = new Properties();
	
	 

	 static {
	        try {
	            FileInputStream fis = new FileInputStream(
	                "src/test/resources/config.properties"
	            );
	            properties.load(fis);
	        } catch (IOException e) {
	            throw new RuntimeException("config.properties not found!", e);
	        }
	    }

	    public static String get(String key) {
	        String value = properties.getProperty(key);
	        if (value == null) throw new RuntimeException("Key not found: " + key);
	        return value.trim();
	    }

	    public static boolean getBoolean(String key) {
	        return Boolean.parseBoolean(get(key));
	    }

	    public static int getInt(String key) {
	        return Integer.parseInt(get(key));
	    }

}
