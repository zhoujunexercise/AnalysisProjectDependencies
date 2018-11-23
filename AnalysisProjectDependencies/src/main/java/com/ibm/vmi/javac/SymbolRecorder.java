package com.ibm.vmi.javac;


import java.util.List;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.util.Name;

import java.util.List;
import java.util.ArrayList;
public class SymbolRecorder {
	public SymbolRecorder(JCTree.JCClassDecl classDecl,SymbolFilter filter){
		this.classDecl = classDecl;
		methodRecorders = new ArrayList<SymbolMethodRecorder>();
		//classSymbolRecorder = new SymbolMethodRecorder(null);
		variables = new ArrayList<JCVariableDecl>();
		newclasses = new ArrayList<JCNewClass>();
		invocations = new ArrayList<JCMethodInvocation>();
		constructors = new ArrayList<JCMethodInvocation>();
		flatname = classDecl.type.tsym.flatName().toString();
		this.filter = filter;
	}
	//class construction
	protected List<JCMethodInvocation> invocations=null;
	protected List<JCMethodInvocation> constructors = null;
	protected List<JCVariableDecl> variables;//only list public variables
	protected List<JCNewClass> newclasses=null;
	
	public List<JCMethodInvocation> getMethodInvocations(){
		return invocations;
	}
	public List<JCMethodInvocation> getMethodConstructors(){
		return constructors;
	}
	public List<JCVariableDecl> getVariables(){
		return variables;
	}
	public List<JCNewClass> getNewClasses(){
		return newclasses;
	}
	
	public class SymbolMethodRecorder{
		protected JCMethodDecl method=null;
		protected List<JCMethodInvocation> invocations=null;
		protected List<JCNewClass> newclasses=null;
		SymbolMethodRecorder(JCTree.JCMethodDecl method){
			this.method = method;
			invocations = new ArrayList<JCMethodInvocation>();
			newclasses = new ArrayList<JCNewClass>();
		}
		public JCMethodDecl getMethodDecl(){
			return method;
		}
		public List<JCMethodInvocation> getInvocation(){
			return invocations;
		}
		public List<JCNewClass> getNewClass(){
			return newclasses;
		}
		public void Add(JCMethodInvocation tree){
			if(tree==null || filter==null)
				return;
			if(hasMethodInvocation(tree))
				return;
			invocations.add(tree);
		}
		public void Add(JCNewClass tree){
			if(tree==null || filter==null)
				return;
			if(hasNewClass(tree))
				return;
			newclasses.add(tree);
		}
		public boolean hasNewClass(JCNewClass tree){
			for(JCNewClass savetree : newclasses){
				if(isSameType(tree, savetree))
					return true;
			}
			return false;
		}
		public boolean hasMethodInvocation(JCMethodInvocation tree){
			for(JCMethodInvocation savetree : invocations){
				if(isSameType(tree, savetree))
					return true;
			}
			return false;
		}
		public void DebugPrint(){
			if(method!=null){
				System.out.println(method.sym.toString());
			}
			for(JCMethodInvocation invocation:invocations){
				if(invocation==null)
					continue;
				System.out.println(invocation.toString());
			}
			for(JCNewClass newclass:newclasses){
				if(newclass==null)
					continue;
				System.out.println(newclass.toString());
			}
		}
		public boolean hasClassChildren(){
			int size = 0;
			if(newclasses!=null){
				size+=newclasses.size();
			}
			if(invocations!=null){
				size+=invocations.size();
			}
			return size>0;
		}
		public Object[] getAllClassChildren(){
			int newclasslen = 0;
			int methodlen = 0;
			if(newclasses!=null){
				newclasslen = newclasses.size();
			}
			if(invocations!=null){
				methodlen = invocations.size();
			}
			int totallen = newclasslen+methodlen;
			if(totallen<=0)
				return null;
			Object []trees = new Object [totallen];
			int len = 0;
			if(newclasses!=null){
				for(JCTree tree:newclasses){
					trees[len++] = tree;
				}
			}
			if(invocations!=null){
				for(JCTree tree:invocations){
					trees[len++] = tree;
				}
			}
			return trees;
		}
		public boolean hasMethodDef(){
			int totallen = 0;
			int methodlen = 0;
			List<JCTypeParameter> typarams=null;
			if(method!=null){
				typarams = method.typarams;
				if(typarams!=null && typarams.size()<=0)
					typarams = null;
				if(typarams!=null)
					totallen+=typarams.size();
			}
			if(newclasses!=null){
				totallen += newclasses.size();
			}
			if(invocations!=null){
				totallen += invocations.size();
			}
			return totallen>0;
		}
		public Object[] getMethodDef(){
			int totallen = 0;
			int methodlen = 0;
			List<JCTypeParameter> typarams=null;
			if(method!=null){
				typarams = method.typarams;
				if(typarams!=null && typarams.size()<=0)
					typarams = null;
				if(typarams!=null)
					totallen+=typarams.size();
			}
			if(newclasses!=null){
				totallen += newclasses.size();
			}
			if(invocations!=null){
				totallen += invocations.size();
			}
			if(totallen<=0)
				return null;
			Object []trees = new Object [totallen];
			int len = 0;
			if(typarams!=null){
				for(Object obj:typarams){
					trees[len++] = obj;
				}
			}
			if(newclasses!=null){
				for(JCTree tree:newclasses){
					trees[len++] = tree;
				}
			}
			if(invocations!=null){
				for(JCTree tree:invocations){
					trees[len++] = tree;
				}
			}
			return trees;
		}
	}
	List<SymbolMethodRecorder> methodRecorders = null;
	SymbolMethodRecorder curMethodRecorder = null;
	
	//SymbolMethodRecorder classSymbolRecorder = null;
	JCClassDecl classDecl=null;
	SymbolFilter filter = null;
	String flatname="";
	int methodref = 0;
	/*
	public SymbolMethodRecorder getClassSymbolRecorder(){
		return classSymbolRecorder;
	}*/
	
	public List<SymbolMethodRecorder> getMethodSymbolRecorders(){
		return methodRecorders;
	}
	public JCClassDecl getRoot(){
		return classDecl;
	}
	protected boolean isCurConstructionMethod(){
		if(curMethodRecorder==null)
			return false;
		JCMethodDecl methoddecl = curMethodRecorder.getMethodDecl();
		if(methoddecl==null)
			return false;
		Name name = methoddecl.name;
		if(name.equals(name.table.names.init)){
			String owner = methoddecl.type.tsym.owner.flatName().toString();
			if(flatname.compareTo(owner)==0)
				return true;
		}
		return false;
	}
	public void MethodStart(JCMethodDecl method){
		methodref++;
		if(methodref>1){
			assert(curMethodRecorder!=null);
			return;
		}
		curMethodRecorder = new SymbolMethodRecorder(method);
	}
	
	public void MethodEnd(){
		if(methodref==0){
			assert(curMethodRecorder==null);
			return;
		}
		methodref--;
		if(methodref==0){
			assert(curMethodRecorder!=null);
			if(curMethodRecorder==null)
				return;
			methodRecorders.add(curMethodRecorder);
			return;
		}
		curMethodRecorder = null; 
	}
	/*
	public void Add(JCTree tree){
		if(tree==null || filter==null)
			return;
		if(!filter.IsValidSymbol(tree))
			return;
		SymbolMethodRecorder classormethodRecorder = curMethodRecorder;
		if(classormethodRecorder==null)
			classormethodRecorder=classSymbolRecorder;
		if(tree instanceof JCMethodInvocation){
			classormethodRecorder.invocations.add((JCMethodInvocation)tree);
		}
		else if(tree instanceof JCNewClass){
			classormethodRecorder.newclasses.add((JCNewClass)tree);
		}
	}*/
	public void Add(JCMethodInvocation tree){
		if(tree==null || filter==null)
			return;
		if(!filter.IsValidSymbol(tree))
			return;
		if(curMethodRecorder!=null){
			if(isCurConstructionMethod()){
				constructors.add(tree);
			}
			curMethodRecorder.Add(tree);
			return;
		}
		if(hasMethodInvocation(tree))
			return;
		invocations.add(tree);
		
	}
	public void Add(JCNewClass tree){
		if(tree==null || filter==null)
			return;
		if(!filter.IsValidSymbol(tree))
			return;
		if(curMethodRecorder!=null){
			curMethodRecorder.Add(tree);
			return;
		}
		if(hasNewClass(tree))
			return;
		newclasses.add(tree);
	}
	public boolean hasNewClass(JCNewClass tree){
		for(JCNewClass savetree : newclasses){
			if(isSameType(tree, savetree))
				return true;
		}
		return false;
	}
	public boolean hasMethodInvocation(JCMethodInvocation tree){
		for(JCMethodInvocation savetree : invocations){
			if(isSameType(tree, savetree))
				return true;
		}
		return false;
	}
	public static boolean isSameType(JCTree tree1, JCTree tree2){
		if(tree1.type==tree2.type)
			return true;
		return false;
	}
	public void Add(JCVariableDecl tree){
		//only show class variable, not variable in method
		if(curMethodRecorder!=null)
			return;
		//check ower
		String owner = tree.sym.owner.flatName().toString();
		if(flatname.compareTo(owner)!=0)
			return;
		//check accessable
		if((tree.type.tsym.flags_field & Flags.AccessFlags) != Flags.PUBLIC)
			return;
		variables.add(tree);
		
	}
	public void DebugPrint(){
		//if(classSymbolRecorder!=null)
		//	classSymbolRecorder.DebugPrint();
		if(methodRecorders!=null){
			for(SymbolMethodRecorder recorder:methodRecorders){
				if(recorder==null)
					continue;
				recorder.DebugPrint();
			}
		}
	}
	public boolean hasChildren(){
		/*if(classSymbolRecorder!=null){
			if(classSymbolRecorder.hasClassChildren())
				return true;
		}*/
		if(invocations!=null && invocations.size()>0)
			return true;
		if(constructors!=null && constructors.size()>0)
			return true;
		if(newclasses!=null && newclasses.size()>0)
			return true;
		if(variables!=null && variables.size()>0)
			return true;
		if(methodRecorders!=null && methodRecorders.size()>0)
			return true;
		return false;
	}
	/*
	public Object[] getAllChildren(){
		//class Symbol
		int objlen = 0;
		Object []results=null;
		//if(classSymbolRecorder!=null){
		//	results = classSymbolRecorder.getAllClassChildren();
		//	if(results!=null)
		//		objlen = results.length;
		//}
		if(methodRecorders==null || methodRecorders.size()<=0)
			return results;
		int size = methodRecorders.size();
		
		Object []objRecorders = new Object[size];
		objlen = 0;
		for(Object obj:methodRecorders){
			objRecorders[objlen++] = obj;
		}
		return Utilities.Attach(results, objRecorders);
	}*/
}
