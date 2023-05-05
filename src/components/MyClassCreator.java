package components;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

public class MyClassCreator {
    public static void main(String[] args) throws Exception {

    	start("Hazem","nodes.IndividualNode","Meow","System.out.print(1);",String.class,"meoww");
    }
    public static void start(String className, String superClassName, String methodName, String methodCode,Class<?> constructorParam,Object constructorArg) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException{
    	

    	String classCode = "public class " + className + " extends " + superClassName +"{"
    			+ " public " +className+"("+ constructorParam.getCanonicalName()+" " +constructorArg+")"+"{"
    					+ "super("+constructorArg+");}"
    	+ "  public void " + methodName + "() { " + methodCode + "} }";
    	Class<?> myClass = MyClassCreator.createClass(className, classCode, className, superClassName);
    	Object a1 = myClass.newInstance();
    	Method m1 = null;
		try {
			m1 = a1.getClass().getMethod(methodName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	m1.invoke(a1);
    }
 
    public static Class<?> createClass(String className, String classCode, String fileName, String superClassName) throws IOException, ClassNotFoundException {
        String filePath = fileName + ".java";
        try (PrintWriter out = new PrintWriter(filePath)) {
            out.println(classCode);
        }
        System.out.println("Class " + className + " successfully created as " + filePath);
        String[] options = new String[] { "-d", "bin" };
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File("bin")));
        JavaFileObject sourceFile = new SourceFile(className, classCode);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceFile);
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics, Arrays.asList(options), null, compilationUnits);
        if (!task.call()) {
            throw new RuntimeException("Compilation failed: " + diagnostics.getDiagnostics());
        }
        ClassLoader classLoader = fileManager.getClassLoader(StandardLocation.CLASS_OUTPUT);
        Class<?> compiledClass = Class.forName(className, true, classLoader);
        fileManager.close();
        return compiledClass;
    }
    public static Class<?> createClass4(String className, String superClassName, List<String> fieldTypes, List<String> fieldNames, List<String> constructorParamTypes, List<String> constructorParamNames) throws IOException, ClassNotFoundException {
        StringBuilder classCodeBuilder = new StringBuilder();
        classCodeBuilder.append("public class " + className);
        if (superClassName != null) {
            classCodeBuilder.append(" extends " + superClassName);
        }
        classCodeBuilder.append(" {\n");
        for (int i = 0; i < fieldTypes.size(); i++) {
            String fieldType = fieldTypes.get(i);
            String fieldName = fieldNames.get(i);
            classCodeBuilder.append("\tprivate " + fieldType + " " + fieldName + ";\n");
        }
        classCodeBuilder.append("\n");
        classCodeBuilder.append("\tpublic " + className + "(");
        for (int i = 0; i < constructorParamTypes.size(); i++) {
            String paramType = constructorParamTypes.get(i);
            String paramName = constructorParamNames.get(i);
            classCodeBuilder.append(paramType + " " + paramName);
            if (i < constructorParamTypes.size() - 1) {
                classCodeBuilder.append(", ");
            }
        }
        classCodeBuilder.append(") {\n");
        for (int i = 0; i < constructorParamTypes.size(); i++) {
            String paramName = constructorParamNames.get(i);
            classCodeBuilder.append("\t\tthis." + paramName + " = " + paramName + ";\n");
        }
        classCodeBuilder.append("\t}\n");
        classCodeBuilder.append("}\n");
        String classCode = classCodeBuilder.toString();
        String fileName = className + ".java";
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(classCode);
        }
        System.out.println("Class " + className + " successfully created as " + fileName);
        String[] options = new String[] { "-d", "bin" };
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File("bin")));
        JavaFileObject sourceFile = new SourceFile(className, classCode);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceFile);
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics, Arrays.asList(options), null, compilationUnits);
        if (!task.call()) {
            throw new RuntimeException("Compilation failed: " + diagnostics.getDiagnostics());
        }
        URL[] urls = { new File("bin/").toURI().toURL() };
        URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        Class<?> compiledClass = Class.forName(className, true, classLoader);
        fileManager.close();
        return compiledClass;
    }
    public static Object createInstance(Class<?> myClass) throws Exception {
        return myClass.getDeclaredConstructor().newInstance();
    }
   
    
    static class Compiler {
        public byte[] compile(String className, String code) throws Exception {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File("bin")));
            JavaFileObject sourceFile = new SourceFile(className, code);
            Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceFile);
            CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
            if (!task.call()) {
                throw new RuntimeException("Compilation failed: " + diagnostics.getDiagnostics());
            }
            ClassLoader classLoader = fileManager.getClassLoader(StandardLocation.CLASS_OUTPUT);
            Class<?> compiledClass = Class.forName(className, true, classLoader);
            fileManager.close();
            return Files.readAllBytes(Paths.get(compiledClass.getProtectionDomain().getCodeSource().getLocation().toURI()).resolve(className.replace('.', '/') + ".class"));
        }
    }



    static class ByteClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

    static class SourceFile extends SimpleJavaFileObject {
        private final String code;
        public SourceFile(String name, String code) {
            super(URI.create("string:///" + name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    static class ByteArrayJavaClass extends SimpleJavaFileObject {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        public ByteArrayJavaClass(String className) {
            super(URI.create("bytes:///" + className), Kind.CLASS);
        }

        public byte[] getBytes() {
            return outputStream.toByteArray();
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return outputStream;
        }
    }

}
