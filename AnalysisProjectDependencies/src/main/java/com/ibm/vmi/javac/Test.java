package com.ibm.vmi.javac;

import javax.tools.StandardLocation;





public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String javaargs[] = {"","",""};
		javaargs[0] = "-classpath";
		javaargs[1] = "D:\\program\\oracle\\Berkeley DB 12cR1 6.0.20\\bin\\db.jar;D:\\program\\oracle\\Berkeley DB 12cR1 6.0.20\\bin\\dbexamples.jar;";
		javaargs[2] = "E:\\Projects\\runtime-EclipseApplication\\test\\src\\com\\ibm\\vmi\\test\\Main.java";
		//com.sun.tools.javac.main.Main main = new Main("javac");
		//main.compile(javaargs);
		System.out.println("abc");
		ProjectCompiler comp = ProjectCompiler.getInstance("test");
		comp.compile(javaargs);
		SymbolFilter filter = SymbolFilter.getInstance("test");
		comp.visitEutSymbol(javaargs[2], filter);
	}

}
