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
	            throw new RuntimeException(
	                "config.properties not found!", e
	            );
	        }
	    }

	    public static String get(String key) {

	        // ✅ NEW: Check CLI flag first (-Dkey=value)
	        // Example: mvn test -Dheadless=true
	        // This overrides whatever is in config.properties
	        // Without this, CLI flags were silently ignored
	        String cliValue = System.getProperty(key);
	        if (cliValue != null) return cliValue.trim();

	        // Fall back to config.properties
	        String value = properties.getProperty(key);
	        if (value == null) {
	            throw new RuntimeException("Key not found: " + key);
	        }
	        return value.trim();
	    }

	    // ✅ NO CHANGE — already existed
	    public static boolean getBoolean(String key) {
	        return Boolean.parseBoolean(get(key));
	    }

	    // ✅ NO CHANGE — already existed
	    public static int getInt(String key) {
	        return Integer.parseInt(get(key));
	    }
}
