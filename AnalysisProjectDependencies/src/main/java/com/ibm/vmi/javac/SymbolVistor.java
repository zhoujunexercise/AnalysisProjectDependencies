package com.ibm.vmi.javac;


import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;


public class SymbolVistor extends TreeScanner{
	SymbolVistor(SymbolRecorder recorder){
		this.recorder = recorder;
	}
	SymbolRecorder recorder = null;
	public void analyzeTree(JCTree tree) {
		if(!(tree instanceof JCClassDecl))
			return;
        try {
            scan(tree);
        } finally {
            // note that recursive invocations of this method fail hard
        	if(tree!=null){
        		recorder.DebugPrint();
        	}
        }
    }
	public void visitNewClass(JCNewClass tree) {
		recorder.Add(tree);
        super.visitNewClass(tree);
    }
	public void visitApply(JCMethodInvocation tree) {
		recorder.Add(tree);
        super.visitApply(tree);
    }
	public void visitMethodDef(JCMethodDecl tree) {
		recorder.MethodStart(tree);
		super.visitMethodDef(tree);
		recorder.MethodEnd();
    }
	public void visitVarDef(JCVariableDecl tree) {
		recorder.Add(tree);
		super.visitVarDef(tree);
    }
}
