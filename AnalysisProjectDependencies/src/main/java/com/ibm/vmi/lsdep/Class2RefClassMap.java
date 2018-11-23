package com.ibm.vmi.lsdep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.util.Iterator;

public class Class2RefClassMap {
	//<"class name", "package;refclass1;refclass2...">
	//private HashMap<String, String> map=new HashMap<String, String>();
	private HashMap<String, List<String>> map = new HashMap<String, List<String>>();
	private String[] components = null;
	public void clean(){
		map.clear();
	}
	public Class2RefClassMap(){

	}
	public HashMap<String, List<String>> getClass2RefClassMap(){
		return map;
	}
	public void setComponents(String[] components){
		this.components = components;
	}
	public List<String> getPackages(){
		List<String> packages = new ArrayList<String>();
		Iterator<Map.Entry<String, List<String>>> iter = map.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String className = entry.getKey();
			String packageName = className2PackageName(className);

			LsjarUtil.append(packages, packageName);
		}
		return packages;
	}
	
	/**
	 * 将源码中的类按包进行分类,将类按包进行分类
	 * 
	 * @return key：包名 value:List<该包下所有类名全路径>
	 */
	public HashMap<String, List<String>> getPackage2ClassMap(){
		HashMap<String, List<String>> prmap = new HashMap<String, List<String>>();
		Iterator<Map.Entry<String, List<String>>> iter = map.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String className = entry.getKey();
			String packageName = className2PackageName(className);
			List<String> savedClasses = prmap.get(packageName);
			if(savedClasses==null){
				savedClasses = new ArrayList<String>();
				prmap.put(packageName, savedClasses);
			}
			savedClasses.add(className);
		}
		return prmap;
	}
	
	/**
	 * 将源码中的类按包进行分类,将类中import类按包进行分类
	 * @return  key：包名 value:List<该包下所有类import类名全路径>
	 */
	public HashMap<String, List<String>> getPackage2RefClassMap(){
		HashMap<String, List<String>> prmap = new HashMap<String, List<String>>();
		Iterator<Map.Entry<String, List<String>>> iter = map.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String className = entry.getKey();
			String packageName = className2PackageName(className);
			List<String> refClasses = entry.getValue();
			List<String> SavedRefClasses = prmap.get(packageName);
			List<String> result = LsjarUtil.append(SavedRefClasses, refClasses);
			if(SavedRefClasses==null){
				prmap.put(packageName, result);
			}
			
		}
		return prmap;
	}
	/**
	 * import类与调用类之间的映射
	 * 
	 * @return key：import 类全路径 value:List<调用该类的全路径>
	 */
	public HashMap<String, List<String>> getRefClass2ClassMap(){
		HashMap<String, List<String>> rcmap = new HashMap<String, List<String>>();
		Iterator<Map.Entry<String, List<String>>> iter = map.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String className = entry.getKey();
			List<String> refClasses = entry.getValue();
			
			for(String refclass:refClasses){
				if(refclass.isEmpty())
					continue;
				List<String> SavedClasses = rcmap.get(refclass);
				if(SavedClasses==null){
					SavedClasses = new ArrayList<String>();
					SavedClasses.add(className);
					rcmap.put(refclass, SavedClasses);
				}
				else{
					LsjarUtil.append(SavedClasses, className);
				}
			}	
		}
		return rcmap;
	}
	public HashMap<String, List<String>> getJar2RefClassMap(HashMap<String, List<String>> rcmap, HashMap<String, String> refClass2JarMap){
		HashMap<String, List<String>> jrmap = new HashMap<String, List<String>>();
		Iterator<Map.Entry<String, String>> iter = refClass2JarMap.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, String> entry = iter.next(); 
			if(entry==null)
				continue;
			String refClass = entry.getKey();
			String jarFile = entry.getValue();
			if(rcmap.get(refClass) == null)
				continue;
			List<String> refClasses = jrmap.get(jarFile);
			if(refClasses==null){
				refClasses = new ArrayList<String>();
				refClasses.add(refClass);
				jrmap.put(jarFile, refClasses);
			}
			else{
				LsjarUtil.append(refClasses, refClass);
			}
			
		}
		return jrmap;
	}
	
	public boolean appendPathes(String pathes){
		String[] pathArray = pathes.split(";");
		for(String path: pathArray){
			try {
				appendPath(new File(path));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}
	public boolean appendPath(File path)throws IOException{
		if(path.isDirectory()){
			File []files = path.listFiles();
			for(File file:files){
				appendPath(file);
			}
			return true;
		}
		else{
			return append(path);
		}
	}
	public boolean append(File file) throws IOException{
		if(!file.exists())
			return false;

		List<String> refClasses = getRefClasses(file);
		if(refClasses==null || refClasses.isEmpty())
			return false;
		String fileName = file.getName();
		if(fileName.endsWith(".java")){
			fileName = fileName.substring(0, fileName.length()-5);
		}
		String ClassName=getPackageName(file) + "." + fileName;
		append(ClassName, refClasses);
		return true;
	}
	public boolean append(String className, String refClass){
		List<String> refClasses = map.get(className);
		if(refClasses==null){
			refClasses = new ArrayList<String>();
			map.put(className, refClasses);
		}
		refClasses.add(refClass);
		return true;
	}
	public boolean append(String className, List<String>refClasses){
		if(className.isEmpty())
			return false;
		List<String> rc = map.get(className);
		if(rc!=null){
			map.remove(className);
		}
		map.put(className, refClasses);
		return true;
	}

	protected static String getPackageName(File file) throws IOException{
		String suffix=LsjarUtil.getSuffix(file);
		if(!suffix.equals("java"))
			return "";
		FileReader reader = new FileReader(file);
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		while((line = br.readLine()) != null) {
			String []sentances = line.split(";");
			for(String st:sentances){
				//clean space
				st=LsjarUtil.cleanSpaces(st);
				if(st.startsWith("package ")){
					String pacageName = st.substring(8);
					br.close();
					reader.close();
					return LsjarUtil.getLastTag(pacageName);
				}
			 }
		 }
		 br.close();
		 reader.close();
		 return "";
	}
	protected static List<String> getRefClasses(File file) throws IOException{
		String suffix=LsjarUtil.getSuffix(file);
		if(!suffix.equals("java"))
			return null;
		FileReader reader = new FileReader(file);
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		List<String> refClasses = new ArrayList<String>();
		while((line = br.readLine()) != null) {
			String []sentances = line.split(";");
			for(String st:sentances){
				//去空格
				st=LsjarUtil.cleanSpaces(st);
				if(st.startsWith("import ")){
					String className = st.substring(7);
					className = LsjarUtil.cleanSpaces(className);
					
					String tag = LsjarUtil.getLastTag(className);
					if(className.startsWith("static ")){
						for(int i=tag.length()-1; i>=0; i--){
							if(tag.charAt(i)=='.'){
								if(i==0)
									tag="";
								else
									tag = tag.substring(0, i);
								break;
							}
						}
					}
					if(tag.isEmpty())
						continue;
					boolean findFlag = false;
					for(String rc:refClasses){
						if(rc == tag){
							findFlag = true;
							break;
						}
					}
					if(findFlag)
						continue;
					refClasses.add(tag);
					
				}	  
			 }
		 }
		 br.close();
		 reader.close();
		 LsjarUtil.sort(refClasses);
		 return refClasses;
	}
	public String className2PackageName(String className){
		if(components != null){
			for(String component:components){
				if(className.startsWith(component))
					return component;
			}
		}
		for(int i=className.length()-1; i>=0; i--){
			char ch=className.charAt(i);
			if(ch=='.'){
				if(i<=0)
					return "";
				return className.substring(0, i);
			}
		}
		return "";
	}
	
}
