package edu.yu.cs.com3800;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Since this class has only one instance variable (target dir) and it is never changed after the constructor, this class's code is threadsafe.
 */
public class JavaRunner {
    /**
     * Location for compiler to write class file to.
     */
    private Path targetDir;
    private static final String PREFIX = "JavaCompileAndRun-";

    /**
     * will create a temp dir as the target dir
     *
     * @throws IOException
     */
    public JavaRunner() throws IOException {
        //adding nano time to reduce risk of two threads or JVMs running on the same machine using the same
        //dir, compiling two Java classes with the same name, and one overriding the other
        this.targetDir = Files.createTempDirectory(PREFIX + System.nanoTime());
    }

    /**
     * @param targetDir dir in which class files will be compiled into
     * @throws IllegalArgumentException if the path does not point to an existing directory
     * @throws IOException if the needed subdirectory couldn't be created
     */
    public JavaRunner(Path targetDir) throws IOException {
        if (!targetDir.toFile().isDirectory()) {
            throw new IllegalArgumentException(targetDir.toString() + " is not a directory");
        }
        //adding subdirectory with nano time in name to reduce risk of two threads or JVMs running on the same machine
        // using the same dir, compiling two Java classes with the same name, and one .class file overriding the other
        Path subDir = targetDir.resolve(PREFIX + System.nanoTime());
        Files.createDirectories(subDir);
        this.targetDir = subDir;
    }

    /**
     * Reads the source code from the InputStream, compiles it, and runs it. The code must not depend on any classes
     * other than those in the standard JRE. The Java class whose source code is submitted must have a no-args constructor and a no-args method called
     * "run" that returns a String. The run method will be called, and its return value returned.
     *
     * @param in
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public String compileAndRun(InputStream in) throws IllegalArgumentException, IOException, ReflectiveOperationException {
        //check validity of inputstream
        if (in == null || in.available() == 0) {
            throw new IllegalArgumentException("input stream in null or empty");
        }
        byte[] bytes = in.readAllBytes();
        //compile it
        ClassName name = compileFromString(new String(bytes));
        //run it
        return runClass(name);
    }

    /**
     * See https://www.programcreek.com/java-api-examples/?api=javax.tools.JavaCompiler
     * @param src
     * @return ClassName of the compiled code
     * @throws IllegalArgumentException
     */
    private ClassName compileFromString(String src) throws IllegalArgumentException {
        src = src.trim();
        ClassName name = getSourceClassName(src);

        JavaFileObjectFromString srcStr = new JavaFileObjectFromString(name.canonicalName, src);
        DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<JavaFileObject>();
        //compile it. Throw exception if compilation fails
        if (!compile(this.targetDir, srcStr, dc)) {
            String errors = "";
            for (Diagnostic<? extends JavaFileObject> diagnostic : dc.getDiagnostics()) {
                errors += String.format("Error on line %d, column %d in %s%n", diagnostic.getLineNumber(), diagnostic.getColumnNumber(), diagnostic.getSource().toUri());
            }
            throw new IllegalArgumentException("Code did not compile:\n" + errors);
        }
        return name;
    }

    /**
     *
     */
    private boolean compile(Path destDir, JavaFileObjectFromString srcStr, DiagnosticCollector dc) {
        //get compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(dc, null, null);
        //prepare files
        ArrayList<JavaFileObject> files = new ArrayList<>(1);
        files.add(srcStr);
        //add destination dir option
        List<String> options = new ArrayList<>();
        if (destDir != null) {
            options.add("-d");
            options.add(destDir.toString());
        }
        //compile it
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, dc, options, null, files);
        return task.call();
    }

    /**
     * get package, class, and canonical names out of the java source
     *
     * @param src
     */
    private ClassName getSourceClassName(String src) {
        String pkgName = null;
        String className = "";
        //get package className, if any
        if (src.startsWith("package")) {
            pkgName = src.substring(7, src.indexOf(";")).trim();
        }
        //get class className
        //link to regex: https://regex101.com/r/vFwJHg/2/
        Pattern classNamePattern = Pattern.compile("[\\s\\S]*public\\s+class\\s+([a-zA-Z_$][a-zA-Z\\d_$]*)[\\s\\S]*");
        Matcher m = classNamePattern.matcher(src);
        if (!m.matches()) {
            throw new IllegalArgumentException("No class name found in code");
        }
        className = m.group(1);
        String canonicalName = pkgName == null ? className : pkgName + "." + className;
        return new ClassName(pkgName, className, canonicalName);
    }

    /**
     * Use our custom class loader to load the class we have compiled. Execute it using reflection.
     */
    private String runClass(ClassName name) throws ReflectiveOperationException {
        try {
            CompiledClassLoader loader = new CompiledClassLoader(name);
            Class clazz = loader.findClass(name.canonicalName);
            Object obj = clazz.getDeclaredConstructor().newInstance();
            Method run = obj.getClass().getDeclaredMethod("run");
            if (run.getReturnType() != String.class) {
                throw new IllegalArgumentException("The return type of the class was " + run.getReturnType() + ", not java.lang.String");
            }
            return (String) run.invoke(obj);
        }
        catch (ReflectiveOperationException e) {
            throw new ReflectiveOperationException("Could not create and run instance of class", e);
        }
    }

    /**
     * holds package, class, and canonical names of a java class
     */
    class ClassName {
        String packageName;
        String className;
        String canonicalName;

        ClassName(String packageName, String className, String canonicalName) {
            this.packageName = packageName;
            this.canonicalName = canonicalName;
            this.className = className;
        }
    }

    /**
     * loads the class file that has been created by the compilation step in JavaCompileAndRun.compileAndRun
     * see https://www.baeldung.com/java-classloaders
     */
    class CompiledClassLoader extends ClassLoader {
        private ClassName name;

        public CompiledClassLoader(ClassName name) {
            super();
            this.name = name;

        }

        @Override
        public Class findClass(String name) throws ClassNotFoundException {
            if (!name.equals(this.name.canonicalName)) {
                throw new ClassNotFoundException();
            }
            byte[] b = loadClassFromFile();
            return defineClass(name, b, 0, b.length);
        }

        /**
         * reads bytes of className's class file from the targetDir
         *
         * @return
         */
        private byte[] loadClassFromFile() {
            String subpath = this.name.canonicalName.replace('.', File.separatorChar) + ".class";
            Path filePath = FileSystems.getDefault().getPath(JavaRunner.this.targetDir.toString(), subpath);
            try {
                byte[] bytes = Files.readAllBytes(filePath);
                return bytes;
            }
            catch (Exception e) {
                System.err.println("Could not load class from file " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
}