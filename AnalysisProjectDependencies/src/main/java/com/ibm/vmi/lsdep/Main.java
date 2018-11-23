package com.ibm.vmi.lsdep;

import java.io.IOException;


public class Main {

	/**
	 * @param args
	 */
	static String configFile = "C:\\Users\\zj\\workspace\\AnalysisProjectDependencies\\src\\main\\resources\\config.properties";
	/*
	public static boolean test(){
		if(0==0)
			return true;
		Class2RefClassMap crmap=new Class2RefClassMap();
		try {
			crmap.append(new java.io.File("E:\\\\workspace\\vmi\\src\\com.ibm.vmi.updates.appliance\\java\\src\\com\\ibm\\vmi\\updates\\appliance\\UpdateAppliance.java"));
			System.out.println("hello");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}*/

	public static void main(String[] args) {
		View view=new View();
		view.createView(configFile);
		try {
			view.createDetailClassView(new java.io.File("C:\\Users\\zj\\workspace\\LogbackTest\\out\\detail.xml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
