package com.ibm.vmi.javac;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.annotation.processing.Processor;

import com.sun.tools.javac.file.CacheFSInfo;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.processing.AnnotationProcessingError;
import static com.sun.tools.javac.main.OptionName.*;
import com.sun.tools.javac.main.CommandLine;
import com.sun.tools.javac.main.Main;



public class VmiMain extends Main{

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
    String ownName;
    PrintWriter out;
    boolean enableOutputTree=false;
    VmiJavaCompiler javaCompiler=null;
    boolean apiMode=false;
    private JavaFileManager fileManager;
    private Options options = null;
    public VmiMain(String name) {
        this(name, new PrintWriter(System.err, true));
    }

    /**
     * Construct a compiler instance.
     */
    public VmiMain(String name, PrintWriter out) {
        super(name, out);
        this.ownName = name;
        this.out = out;
    }
    /** Result codes.
     */
    static final int
        EXIT_OK = 0,        // Compilation completed with no errors.
        EXIT_ERROR = 1,     // Completed but reported errors.
        EXIT_CMDERR = 2,    // Bad command-line arguments
        EXIT_SYSERR = 3,    // System error or resource exhaustion.
        EXIT_ABNORMAL = 4;  // Compiler terminated abnormally
    /** Programmatic interface for main function.
     * @param args    The command line parameters.
     */
    public int compile(String[] args) {
        Context context = new Context();
        JavacFileManager.preRegister(context); // can't create it until Log has been set up
        int result = compile(args, context);
        if (fileManager instanceof JavacFileManager) {
            // A fresh context was created above, so jfm must be a JavacFileManager
            ((JavacFileManager)fileManager).close();
        }
        return result;
    }

    public int compile(String[] args, Context context) {
        return compile(args, context, List.<JavaFileObject>nil(), null);
    }

    /** Programmatic interface for main function.
     * @param args    The command line parameters.
     */
    public int compile(String[] args,
                       Context context,
                       List<JavaFileObject> fileObjects,
                       Iterable<? extends Processor> processors)
    {
        if (options == null){
            options = Options.instance(context); // creates a new one
            setOptions(options);
        }

        filenames = new ListBuffer<File>();
        classnames = new ListBuffer<String>();
        VmiJavaCompiler comp = null;
        
        /*
         * TODO: Logic below about what is an acceptable command line
         * should be updated to take annotation processing semantics
         * into account.
         */
        try {
            if (args.length == 0 && fileObjects.isEmpty()) {
                super.compile(args, context, fileObjects, processors);
                return EXIT_CMDERR;
            }

            List<File> files;
            try {
                files = processArgs(CommandLine.parse(args));
                if (files == null) {
                    // null signals an error in options, abort
                    return EXIT_CMDERR;
                } else if (files.isEmpty() && fileObjects.isEmpty() && classnames.isEmpty()) {
                    // it is allowed to compile nothing if just asking for help or version info
                    if (options.isSet(HELP)
                        || options.isSet(X)
                        || options.isSet(VERSION)
                        || options.isSet(FULLVERSION))
                        return EXIT_OK;
                    if (options.isSet(PROCESSOR) ||
                            options.isSet(PROCESSORPATH) ||
                            options.isSet(PROC, "only") ||
                            options.isSet(XPRINT)) {
                        error("err.no.source.files.classes");
                    } else {
                        error("err.no.source.files");
                    }
                    return EXIT_CMDERR;
                }
            } catch (java.io.FileNotFoundException e) {
                Log.printLines(out, ownName + ": " +
                               getLocalizedString("err.file.not.found",
                                                  e.getMessage()));
                return EXIT_SYSERR;
            }

            boolean forceStdOut = options.isSet("stdout");
            if (forceStdOut) {
                out.flush();
                out = new PrintWriter(System.out, true);
            }

            context.put(Log.outKey, out);

            // allow System property in following line as a Mustang legacy
            boolean batchMode = (options.isUnset("nonBatchMode")
                        && System.getProperty("nonBatchMode") == null);
            if (batchMode)
                CacheFSInfo.preRegister(context);

            fileManager = context.get(JavaFileManager.class);

            comp = VmiJavaCompiler.instance(context);
            if (comp == null) return EXIT_SYSERR;
            javaCompiler = comp;
            if(javaCompiler!=null){
                javaCompiler.saveTree = enableOutputTree;
            }

            Log log = Log.instance(context);

            if (!files.isEmpty()) {
                // add filenames to fileObjects
                comp = VmiJavaCompiler.instance(context);
                List<JavaFileObject> otherFiles = List.nil();
                JavacFileManager dfm = (JavacFileManager)fileManager;
                for (JavaFileObject fo : dfm.getJavaFileObjectsFromFiles(files))
                    otherFiles = otherFiles.prepend(fo);
                for (JavaFileObject fo : otherFiles)
                    fileObjects = fileObjects.prepend(fo);
            }
            comp.compile(fileObjects,
                         classnames.toList(),
                         processors);

            if (log.expectDiagKeys != null) {
                if (log.expectDiagKeys.isEmpty()) {
                    Log.printLines(log.noticeWriter, "all expected diagnostics found");
                    return EXIT_OK;
                } else {
                    Log.printLines(log.noticeWriter, "expected diagnostic keys not found: " + log.expectDiagKeys);
                    return EXIT_ERROR;
                }
            }

            if (comp.errorCount() != 0)
                return EXIT_ERROR;
        } catch (IOException ex) {
            ioMessage(ex);
            return EXIT_SYSERR;
        } catch (OutOfMemoryError ex) {
            resourceMessage(ex);
            return EXIT_SYSERR;
        } catch (StackOverflowError ex) {
            resourceMessage(ex);
            return EXIT_SYSERR;
        } catch (FatalError ex) {
            feMessage(ex);
            return EXIT_SYSERR;
        } catch (AnnotationProcessingError ex) {
            if (apiMode)
                throw new RuntimeException(ex.getCause());
            apMessage(ex);
            return EXIT_SYSERR;
        } catch (ClientCodeException ex) {
            // as specified by javax.tools.JavaCompiler#getTask
            // and javax.tools.JavaCompiler.CompilationTask#call
            throw new RuntimeException(ex.getCause());
        } catch (PropagatedException ex) {
            throw ex.getCause();
        } catch (Throwable ex) {
            // Nasty.  If we've already reported an error, compensate
            // for buggy compiler error recovery by swallowing thrown
            // exceptions.
            if (comp == null || comp.errorCount() == 0 ||
                options == null || options.isSet("dev"))
                bugMessage(ex);
            return EXIT_ABNORMAL;
        } finally {
            if (comp != null) {
                try {
                    comp.close();
                } catch (ClientCodeException ex) {
                    throw new RuntimeException(ex.getCause());
                }
            }
            filenames = null;
            options = null;
        }
        return EXIT_OK;
    }
    
    public void setAPIMode(boolean apiMode) {
        this.apiMode = apiMode;
    }
    
    /** Report a usage error.
     */
    void error(String key, Object... args) {
        if (apiMode) {
            String msg = getLocalizedString(key, args);
            throw new PropagatedException(new IllegalStateException(msg));
        }
        warning(key, args);
        Log.printLines(out, getLocalizedString("msg.usage", ownName));
    }

    /** Report a warning.
     */
    void warning(String key, Object... args) {
        Log.printLines(out, ownName + ": "
                       + getLocalizedString(key, args));
    }
    
    /** Print a message reporting an internal error.
     */
    void bugMessage(Throwable ex) {
        Log.printLines(out, getLocalizedString("msg.bug",
                                               VmiJavaCompiler.version()));
        ex.printStackTrace(out);
    }

    /** Print a message reporting a fatal error.
     */
    void feMessage(Throwable ex) {
        Log.printLines(out, ex.getMessage());
        if (ex.getCause() != null && options.isSet("dev")) {
            ex.getCause().printStackTrace(out);
        }
    }

    /** Print a message reporting an input/output error.
     */
    void ioMessage(Throwable ex) {
        Log.printLines(out, getLocalizedString("msg.io"));
        ex.printStackTrace(out);
    }

    /** Print a message reporting an out-of-resources error.
     */
    void resourceMessage(Throwable ex) {
        Log.printLines(out, getLocalizedString("msg.resource"));
//      System.out.println("(name buffer len = " + Name.names.length + " " + Name.nc);//DEBUG
        ex.printStackTrace(out);
    }

    /** Print a message reporting an uncaught exception from an
     * annotation processor.
     */
    void apMessage(AnnotationProcessingError ex) {
        Log.printLines(out,
                       getLocalizedString("msg.proc.annotation.uncaught.exception"));
        ex.getCause().printStackTrace(out);
    }


}
