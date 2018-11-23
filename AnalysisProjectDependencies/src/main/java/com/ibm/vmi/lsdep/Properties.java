package com.ibm.vmi.lsdep;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

public class Properties {
	public static final String JARPATHES="jarPathes";
	public static final String JAVAPATHES="javaPathes";
	public static final String COMPONENTS="components";
	public static final String OUPUTPATH="outputPath";

	private ResourceBundle resourceBundle = null;
	public Properties(String file) {
		String proFilePath =file;
		System.out.printf("properties file %s\n", proFilePath);
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(proFilePath));
			resourceBundle = new PropertyResourceBundle(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getString(String key) {
        if (resourceBundle==null || key == null || key.equals("") || key.equals("null")) {
            return "";
        }
        String result = "";
        try {
            result = resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
        return result;
    }
	
}
