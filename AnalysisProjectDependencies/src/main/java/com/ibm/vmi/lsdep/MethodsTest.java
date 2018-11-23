package com.ibm.vmi.lsdep;

public class MethodsTest
{

	public static void main(String[] args)
	{
		String value ="import static java.io.FileNotFoundException;";
		
		String className = value.substring(7);
		className = LsjarUtil.cleanSpaces(className);
		System.out.println("className"+className);
		String tag = LsjarUtil.getLastTag(className);
		System.out.println("tag:"+tag);
		
		String jarPathes = "F:\\ºËÐÄ×é\\2018\\¸²¸ÇÂÊ\\java-project-dependency-master\\java-project-dependency-master\\lib";
		RefClass2JarMap r2jMap=new RefClass2JarMap();
		r2jMap.appendPathes(jarPathes);
		
	}
}
