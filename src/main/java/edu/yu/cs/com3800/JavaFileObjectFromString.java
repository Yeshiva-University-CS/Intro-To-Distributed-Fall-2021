package edu.yu.cs.com3800;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * copied from https://docs.oracle.com/en/java/javase/11/docs/api/java.compiler/javax/tools/JavaCompiler.html
 * Used to facilitate compilation of a Java source code stored in a java.lang.String
 */
public class JavaFileObjectFromString extends SimpleJavaFileObject
{
    /**
     * The source code of this "file".
     */
    final String code;

    /**
     * Constructs a new JavaSourceFromString.
     * @param name the name of the compilation unit represented by this file object
     * @param code the source code for the compilation unit represented by this file object
     */
    JavaFileObjectFromString(String name, String code) {
        super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension),Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}