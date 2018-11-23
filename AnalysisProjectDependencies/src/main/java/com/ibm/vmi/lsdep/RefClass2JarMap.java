package com.ibm.vmi.lsdep;

import java.util.Enumeration;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class RefClass2JarMap {
	private HashMap<String, String> map=new HashMap<String, String>();
	public void clean(){
		map.clear();
	}
	public RefClass2JarMap(){
		
	}
	public HashMap<String, String> getRefClass2JarMap(){
		return map;
	}
	public String Find(String className){
		return map.get(className);
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
    public boolean append(File file) throws IOException{ //jar file
        String suffix = LsjarUtil.getSuffix(file);
        String path = file.getAbsolutePath();
        if(!suffix.equals("jar"))
            return false;
        JarFile jarFile=new JarFile(file);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()){
            JarEntry ent = entries.nextElement();
            String name = ent.getName();
            if(name.endsWith(".class")){
                name = name.substring(0,name.length()-6).replace('/', '.');
                map.put(name, path);
                System.out.println(name +":"+path);
            }
        }
        return true;
    }
}
