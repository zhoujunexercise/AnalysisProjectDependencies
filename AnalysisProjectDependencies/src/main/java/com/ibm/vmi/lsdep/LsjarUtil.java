package com.ibm.vmi.lsdep;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LsjarUtil {
	public static String getSuffix(File file){
		if(file==null)
			return "";
		if(!file.isFile())
			return "";
		String name=file.getName();
		int index = name.lastIndexOf(".");
        if (index < 0) return "";
        return name.substring(index + 1);
	}
	public static String getClassName(File file) throws IOException{
		String suffix=getSuffix(file);
		if(!suffix.equals("java"))
			return "";
		FileReader reader = new FileReader("D://lbhdev//feisanWeb//src//265_url.txt");
		  BufferedReader br = new BufferedReader(reader);
		  String line = null;
		  while((line = br.readLine()) != null) {
			  if(line.startsWith("package ")){
				  String className = line.substring(8);
				  className = className.replaceAll(" ", "");
				  className = className.replaceAll(";", "");
				  className = className.replaceAll("\t", "");
				  br.close();
				  return "";
			  }
		  }
		 br.close();
		 reader.close();
		 return "";
	}
	public static String cleanSpaces(String st){
		int start=0;
		int i=0;
		 
		for(i=0; i<st.length(); i++){
			char ch=st.charAt(i);
			if(ch==' ' || ch=='\t'){
				start++;
			}
			else
				break;
		}
		int end = st.length();
		for(i=end-1; i>start; i--){
			char ch=st.charAt(i);
			if(ch==' ' || ch=='\t'){
				end=i;
			}
			else
				break;
		}
		if(start >= end)
			return "";
		if(start>0 || end<st.length()){
			st = st.substring(start, end);
		}
		return st;
	}
	public static String getLastTag(String st){
		String tag = cleanSpaces(st);
		for(int i=tag.length()-1; i>=0; i--){
			char ch=st.charAt(i);
			if(ch==' ' || ch=='\t'){
				if(i==tag.length()-1)
					return "";
				return tag.substring(i+1);
			}
		}
		return tag;
	}
	public static void sort(List <String> list){
		class StringComparator implements Comparator<String>{
            @Override
            public int compare(String s1, String s2){
                 return s1.compareTo(s2);
            }
            
        }
		StringComparator comparator = new StringComparator();
		Collections.sort(list,comparator);
	}
	public static boolean append(List<String> list, String value){
		if(list==null)
			return false;
		for(String v:list){
			if(v.compareTo(value)==0)
				return false;
		}
		list.add(value);
		return true;
	}
	
	public static List<String> copyList(List<String> list){
		if(list==null)
			return null;
		List<String> newList = new ArrayList<String>();
		for(String s:list){
			newList.add(s);
		}
		return newList;
	}
	public static List<String> append(List<String> dst, List<String> src){
		if(src==null)
			return dst;
		if(dst==null)
			return copyList(src);
		for(String s:src){
			append(dst,s);
		}
		return dst;
	}
	public static String getShortPath(String packageName){
		for(int i=packageName.length()-1; i>=0; i--){
			char ch = packageName.charAt(i);
			if(ch == '/' || ch == '\\'){
				if(i==packageName.length()-1)
					return "";
				return packageName.substring(i+1, packageName.length());
			}
		}
		return packageName;
	}
}
