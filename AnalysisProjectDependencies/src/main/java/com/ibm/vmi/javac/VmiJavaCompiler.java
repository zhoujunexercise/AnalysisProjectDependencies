package com.ibm.vmi.javac;

import java.util.Queue;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.Lint.LintCategory;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.processing.*;
import static com.sun.tools.javac.main.OptionName.*;
import com.sun.tools.javac.main.JavaCompiler;

public class VmiJavaCompiler extends JavaCompiler{

    private boolean hasBeenUsed = false;
    private long start_msec = 0;
    protected VmiJavaCompiler delegateCompiler;
    private JavacProcessingEnvironment procEnvImpl = null;
    public boolean saveTree = false;
    public Queue<Env<AttrContext>> envs=null;
    public VmiJavaCompiler(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    /** Get the JavaCompiler instance for this context. */
    public static VmiJavaCompiler instance(Context context) {
        VmiJavaCompiler instance = (VmiJavaCompiler)context.get(compilerKey);
        if (instance == null)
            instance = new VmiJavaCompiler(context);
        return instance;
    }
    
    public void compile(List<JavaFileObject> sourceFileObject)
            throws Throwable {
            compile(sourceFileObject, List.<String>nil(), null);
        }

        /**
         * Main method: compile a list of files, return all compiled classes
         *
         * @param sourceFileObjects file objects to be compiled
         * @param classnames class names to process for annotations
         * @param processors user provided annotation processors to bypass
         * discovery, {@code null} means that no processors were provided
         */
        public void compile(List<JavaFileObject> sourceFileObjects,
                            List<String> classnames,
                            Iterable<? extends Processor> processors)
        {
            if (processors != null && processors.iterator().hasNext())
                explicitAnnotationProcessingRequested = true;
            // as a JavaCompiler can only be used once, throw an exception if
            // it has been used before.
            if (hasBeenUsed)
                throw new AssertionError("attempt to reuse JavaCompiler");
            hasBeenUsed = true;

            // forcibly set the equivalent of -Xlint:-options, so that no further
            // warnings about command line options are generated from this point on
            options.put(XLINT_CUSTOM + "-" + LintCategory.OPTIONS.option, "true");
            options.remove(XLINT_CUSTOM + LintCategory.OPTIONS.option);

            start_msec = now();

            try {
                initProcessAnnotations(processors);

                // These method calls must be chained to avoid memory leaks
                delegateCompiler =(VmiJavaCompiler)
                    processAnnotations(
                        enterTrees(stopIfError(CompileState.PARSE, parseFiles(sourceFileObjects))),
                        classnames);

                delegateCompiler.compile2();
                delegateCompiler.close();
                elapsed_msec = delegateCompiler.elapsed_msec;
            } catch (Abort ex) {
                if (devVerbose)
                    ex.printStackTrace(System.err);
            } finally {
                if (procEnvImpl != null)
                    procEnvImpl.close();
            }
        }
        


        /**
         * The phases following annotation processing: attribution,
         * desugar, and finally code generation.
         */
        private void compile2() {
        try {
            if(true){
                envs=flow(attribute(todo));
            }
            /*
            switch (compilePolicy) {
            case ATTR_ONLY:
                attribute(todo);
                break;

            case CHECK_ONLY:
                flow(attribute(todo));
                break;

            case SIMPLE:
                generate(desugar(flow(attribute(todo))));
                break;

            case BY_FILE: {
                    Queue<Queue<Env<AttrContext>>> q = todo.groupByFile();
                    while (!q.isEmpty() && !shouldStop(CompileState.ATTR)) {
                        generate(desugar(flow(attribute(q.remove()))));
                    }
                }
                break;

            case BY_TODO:
                while (!todo.isEmpty())
                    generate(desugar(flow(attribute(todo.remove()))));
                break;

            default:
                Assert.error("unknown compile policy");
            }
            */
        } catch (Abort ex) {
            if (devVerbose)
                ex.printStackTrace(System.err);
        }
        

        if (verbose) {
            elapsed_msec = elapsed(start_msec);
            log.printVerbose("total", Long.toString(elapsed_msec));
        }

        reportDeferredDiagnostics();

        if (!log.hasDiagnosticListener()) {
            printCount("error", errorCount());
            printCount("warn", warningCount());
        }
    }
    private static long now() {
        return System.currentTimeMillis();
    }
    private static long elapsed(long then) {
        return now() - then;
    }
    
}
