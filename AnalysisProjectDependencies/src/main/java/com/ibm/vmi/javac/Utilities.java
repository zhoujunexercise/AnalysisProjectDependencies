package com.ibm.vmi.javac;

import java.util.Queue;



import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.util.*;


import java.util.zip.ZipFile;

import java.io.File;
import java.util.ArrayList;

import com.sun.tools.javac.tree.JCTree.JCClassDecl;




import java.util.Queue;


public class Utilities {
	public Utilities(){
		
	}
	Queue<Env<AttrContext>> envs;
	public int compile(String[] args){
		VmiMain javacmain = new VmiMain("javac");
		javacmain.enableOutputTree = true;
		//Context context = new Context();
        //JavacFileManager.preRegister(context); // can't create it until Log has been set up
        int result = javacmain.compile(args);
        VmiJavaCompiler compiler = javacmain.javaCompiler;
        envs=null;
        if(compiler!=null)
        	envs = compiler.envs;
		//ListBuffer<Env<AttrContext>> results = lb();
		//TreeMaker make = T
		
        return result;
	}
	public SymbolRecorder visitEutSymbol(String sourcefilename, SymbolFilter filter){
		if(envs==null)
			return null;
		if(sourcefilename==null)
			return null;
		
		SymbolRecorder recorder = null;
        for (Env<AttrContext> env: envs) {
        	if(env.tree instanceof JCClassDecl){
	        	recorder = new SymbolRecorder((JCClassDecl)env.tree, filter);
	        	SymbolVistor vistor = new SymbolVistor(recorder);
	        	vistor.analyzeTree(env.tree);
	        	break;
        	}
        }
        return recorder;
	}
	

    public static String[] Attach(String[] array1, String[] array2){
    	int size1 = 0;
    	int size2 = 0;
    	if(array1 != null)
    		size1 = array1.length;
    	if(array2 != null)
    		size2 = array2.length;
    	if(size1+size2 <= 0)
    		return null;
    	String[]result = new String[size1+size2];

    	int i=0;
    	for(i=0; i<size1;i++){
    		result[i]=array1[i];
    	}
    	for(i=0; i<size2;i++){
    		result[size1+i]=array2[i];
    	}
    	return result;
    }
    
    public static Object[] Attach(Object[] array1, Object[] array2){
    	int size1 = 0;
    	int size2 = 0;
    	if(array1 != null)
    		size1 = array1.length;
    	if(array2 != null)
    		size2 = array2.length;
    	if(size1+size2 <= 0)
    		return null;
    	Object[]result = new Object[size1+size2];

    	int i=0;
    	for(i=0; i<size1;i++){
    		result[i]=array1[i];
    	}
    	for(i=0; i<size2;i++){
    		result[size1+i]=array2[i];
    	}
    	return result;
    }
    
    public static String[] getAllJavaFile(String path)
    {
    	File dir = new File(path);
    	java.util.List<String>fileArray = new ArrayList<String>();
    	getAllJavaFile(fileArray, dir);
    	if(fileArray.size()<=0)
    		return null;
    	String []result = new String[fileArray.size()];
    	for(int i=0; i<fileArray.size(); i++){
    		result[i] = fileArray.get(i);
    	}
    	return result;
    }
    public static String[] getJavaFile(String path){
    	File dir = new File(path);
    	if(dir==null)
    		return null;
    	File[] files = dir.listFiles();
        if(files==null)
        	return null;
        java.util.List<String>fileArray = new ArrayList<String>();
        for(int x=0; x<files.length; x++)
        {
            if(!files[x].isDirectory()){
            	String filename = files[x].toString();
            	if(filename.endsWith(".java"))
            		fileArray.add(filename);
            }
        }
        String []result = new String[fileArray.size()];
    	for(int i=0; i<fileArray.size(); i++){
    		result[i] = fileArray.get(i);
    	}
    	return result;
    }
 
    protected static void getAllJavaFile(java.util.List<String>fileArray, File dir)
    {
    	if(dir==null)
    		return;
        File[] files = dir.listFiles();
        if(files==null)
        	return;
        for(int x=0; x<files.length; x++)
        {
            if(files[x].isDirectory())
            	getAllJavaFile(fileArray, files[x]);
            else{
            	String filename = files[x].toString();
            	if(filename.endsWith(".java"))
            		fileArray.add(filename);
            }
        }
    }

}
