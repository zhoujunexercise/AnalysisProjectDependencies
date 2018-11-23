package com.ibm.vmi.javac;

import com.sun.tools.javac.tree.JCTree;
import java.util.HashMap;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type.MethodType;
import javax.tools.JavaFileObject;
import com.sun.tools.javac.file.BaseFileObject;
import com.sun.tools.javac.file.ZipFileIndexArchive.ZipFileIndexFileObject;

import java.util.List;
public class SymbolFilter {
	protected SymbolFilter(){
		
	}
	static SymbolFilter defaultfilter = new SymbolFilter();
	static HashMap<String, SymbolFilter>projectfilter=new HashMap<String, SymbolFilter>();
	public static SymbolFilter getInstance(String projectid){
		if(projectid==null || projectid.length()==0)
			return defaultfilter;
		SymbolFilter filter = projectfilter.get(projectid);
		if(filter==null){
			filter = new SymbolFilter();
			projectfilter.put(projectid, filter);
		}
		return filter;
	}
	public static SymbolFilter findInstance(String projectid){
		if(projectid==null || projectid.length()==0)
			return defaultfilter;
		return projectfilter.get(projectid);
	}
	public static SymbolFilter removeInstance(String projectid){
		if(projectid==null || projectid.length()==0)
			return null;
		return projectfilter.remove(projectid);
	}
	HashMap<String, String>importfilter=new HashMap<String, String>();
	public boolean IsValidSymbol(TypeSymbol tsym){
		if(tsym==null)
			return false;
		if(importfilter==null)
			return false;
		if(tsym instanceof Symbol.ClassSymbol){
			JavaFileObject classfile = ((Symbol.ClassSymbol)tsym).classfile;
			if(classfile instanceof ZipFileIndexFileObject){
				ZipFileIndexFileObject zipfileobj = (ZipFileIndexFileObject)classfile;
				 String zipfile = zipfileobj.getName();
				 int length = zipfile.length();
				 for(int i=0; i<length; i++){
					 if(zipfile.charAt(i) == '('){
						 zipfile = zipfile.substring(0,i);
						 break;
					 }
				 }
				 String imp = importfilter.get(zipfile);
				 if(imp!=null)
					 return true;
			}
			
		}
		return false;
	}
	public boolean IsValidMethodSymbol(MethodSymbol sym){
		if(sym==null)
			return false;
		Type type = sym.type;
		if(type instanceof MethodType){
			MethodType methodtype = (MethodType)type;
			List<Type> typelist = methodtype.getParameterTypes();
			if(typelist==null)
				return false;
			for(Type t:typelist){
				if(IsValidSymbol(t.tsym))
					return true;
			}
		}
		return false;
	}
	public boolean IsValidSymbol(JCTree tree){
		Type type=null;
		if(tree instanceof JCMethodInvocation){
			JCExpression meth =  ((JCMethodInvocation) tree).meth;
			
			if(meth instanceof JCFieldAccess){
				JCFieldAccess field = (JCFieldAccess)meth;
				if(field.sym instanceof MethodSymbol){
					if(IsValidMethodSymbol((MethodSymbol)field.sym))
						return true;
				}
				JCExpression selected = field.selected;
				if(selected instanceof JCIdent){
					JCIdent ident = (JCIdent)selected;
					type = ident.type;
				}
			}
			if(type==null)
				type = ((JCMethodInvocation)tree).type;
		}
		else if(tree instanceof JCNewClass){
			type = ((JCNewClass)tree).type;
		}
		if(type==null)
			return false;

		return IsValidSymbol(type.tsym);
/*		if(tsym==null)
			return false;
		if(importfilter==null)
			return false;
		if(tsym instanceof Symbol.ClassSymbol){
			JavaFileObject classfile = ((Symbol.ClassSymbol)tsym).classfile;
			if(classfile instanceof ZipFileIndexFileObject){
				ZipFileIndexFileObject zipfileobj = (ZipFileIndexFileObject)classfile;
				 String zipfile = zipfileobj.getName();
				 int length = zipfile.length();
				 for(int i=0; i<length; i++){
					 if(zipfile.charAt(i) == '('){
						 zipfile = zipfile.substring(0,i);
						 break;
					 }
				 }
				 String imp = importfilter.get(zipfile);
				 if(imp!=null)
					 return true;
			}
			
		}
		return false;*/
	}
	public boolean IsValidJarFile(String jarfile){
		String imp = importfilter.get(jarfile);
		 if(imp!=null)
			 return true;
		 return false;
	}
	public boolean AppendJarFile(String jarfile){
		if(IsValidJarFile(jarfile))
			return false;
		importfilter.put(jarfile, jarfile);
		return true;
	}
	public boolean AppendJarFile(String []jarfiles){
		int count=0;
		for(String jarfile:jarfiles){
			if(AppendJarFile(jarfile))
				count++;
		}
		return count > 0;
	}
	public static String getSourceSymbolFile(JCTree tree){
		Type type=null;
		String path = null;
		if(tree instanceof JCMethodInvocation){
			type = ((JCMethodInvocation)tree).type;
		}
		else if(tree instanceof JCNewClass){
			type = ((JCNewClass)tree).type;
		}
		else if(tree instanceof JCClassDecl){
			type = ((JCClassDecl)tree).type;
		}
		if(type==null)
			return null;
		TypeSymbol tsym = type.tsym;
		if(tsym==null)
			return null;
		if(tsym instanceof Symbol.ClassSymbol){
			JavaFileObject sourceFile = ((Symbol.ClassSymbol)tsym).sourcefile;
			if(sourceFile==null)
				return null;
			if(sourceFile instanceof BaseFileObject){
				BaseFileObject fileobj = (BaseFileObject)sourceFile;
				 path = fileobj.getName();
				 int length = path.length();
				 for(int i=0; i<length; i++){
					 if(path.charAt(i) == '('){
						 path = path.substring(0,i);
						 break;
					 }
				 }

			}
			
		}
		return path;
	}
	
}
