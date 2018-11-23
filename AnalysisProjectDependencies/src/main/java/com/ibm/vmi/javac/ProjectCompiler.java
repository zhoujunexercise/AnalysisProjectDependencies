package com.ibm.vmi.javac;

import java.util.Queue;

//import org.eclipse.core.resources.IProject;

import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import java.util.HashMap;

public class ProjectCompiler {
	private String projectId="";
	String[] compilerargs=null;
	String classpath = "";
	String [] javafiles = null;
	private Queue<Env<AttrContext>> envs=null;
	protected static HashMap<String, ProjectCompiler>projectcompilers=new HashMap<String, ProjectCompiler>();
	protected ProjectCompiler(String projectId){
		this.projectId = projectId;
	}
	public static ProjectCompiler getInstance(String projectId){
		if(projectId==null || projectId.isEmpty())
			return null;

		ProjectCompiler compiler = projectcompilers.get(projectId);
		if(compiler==null){
			compiler = new ProjectCompiler(projectId);
			projectcompilers.put(projectId, compiler);
		}
		return compiler;
	}
	public static ProjectCompiler findInstance(String projectid){
		if(projectid==null || projectid.length()==0)
			return null;
		return projectcompilers.get(projectid);
	}
	public static ProjectCompiler removeInstance(String projectid){
		if(projectid==null || projectid.length()==0)
			return null;
		return projectcompilers.remove(projectid);
	}
	public int compile(String[] args){
		VmiMain javacmain = new VmiMain("javac");
		javacmain.enableOutputTree = true;

        int result = javacmain.compile(args);
        VmiJavaCompiler compiler = javacmain.javaCompiler;
        envs=null;
        if(compiler!=null)
        	envs = compiler.envs;
		
        return result;
	}
	public void setCompilerArgs(String []compilerargs){
		this.compilerargs = compilerargs;
		Refresh();
	}
	public void setClassPath(String classpath){
		this.classpath = classpath;
	}
	public void setJavaFiles(String []javafiles){
		this.javafiles = javafiles;
	}
	boolean Refresh(){//refresh project trees
		if(projectId==null || projectId.isEmpty())
			return false;

		
		String [] classpatharray = {"-classpath",classpath};
		
		String [] allargs = Utilities.Attach(compilerargs, null);
		allargs = Utilities.Attach(allargs, classpatharray);
		allargs = Utilities.Attach(allargs, javafiles);
		
		compile(allargs);
		/*
		Main javacmain = new com.sun.tools.javac.main.Main("javac");
		javacmain.enableOutputTree = true;
		//javacmain.compile(allargs);
		JavaCompiler compiler = javacmain.javaCompiler;
        envs=null;*/
        if(envs==null)
        	return false;
        //envs = compiler.envs;
		return true;
	}
	public SymbolRecorder visitEutSymbol(String sourceId, SymbolFilter filter){
		if(envs==null){
			Refresh();
			if(envs==null)
				return null;
		}
		if(sourceId==null)
			return null;
		
		SymbolRecorder recorder = null;
		sourceId=sourceId.replace('\\', '/');
        for (Env<AttrContext> env: envs) {
        	if(env.tree instanceof JCClassDecl){
        		String sourcename = SymbolFilter.getSourceSymbolFile(env.tree);
        		if(sourcename==null)
        			continue;
        		sourcename=sourcename.replace('\\', '/');
        		if(sourcename.compareTo(sourceId)!=0)
        			continue;
	        	recorder = new SymbolRecorder((JCClassDecl)env.tree, filter);
	        	SymbolVistor vistor = new SymbolVistor(recorder);
	        	vistor.analyzeTree(env.tree);
	        	break;
        	}
        }
        return recorder;
	}
}
