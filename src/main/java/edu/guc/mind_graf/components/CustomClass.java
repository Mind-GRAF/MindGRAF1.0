package edu.guc.mind_graf.components;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.StandardLocation;

import edu.guc.mind_graf.cables.DownCableSet;

public class CustomClass {
	private String className;
	private String superClass;
	private Class<?> newClass = null;

	public CustomClass(String className, String superClass) {
		this.className = className;
		this.superClass = superClass;
	}

	public CustomClass(String className) {
		this.className = className;
		this.superClass = "";
	}

	public Class<?> getNewClass() {
		return newClass;
	}

	public void setNewClass(Class<?> newClass) {
		this.newClass = newClass;
	}

	public static void main(String[] args) throws Exception {
		CustomClass c = new CustomClass("Fears", "nodes.IndividualNode");
		Class<?>[] params = { String.class, Boolean.class };
		String[] arguments = { "name", "isVariable" };
		Class<?>[] params2 = { DownCableSet.class };
		String[] arguments2 = { "downCableSet" };
		CustomConstructor constructor = new CustomConstructor("Fears", params,
				arguments);
		CustomConstructor constructor2 = new CustomConstructor("Fears",
				params2, arguments2);
		ArrayList<CustomConstructor> constructors = new ArrayList<>();
		constructors.add(constructor);
		constructors.add(constructor2);

		CustomMethod method = new CustomMethod("method1",
				"System.out.println(1);");
		CustomMethod method2 = new CustomMethod("method2",
				"System.out.println(3);");
		ArrayList<CustomMethod> methods = new ArrayList<>();
		methods.add(method);
		methods.add(method2);

		Class<?> Hazem = c.createClass(methods, constructors);
		Object hazoum = c.createInstance(Hazem, "n1", false);
		c.executeMethod("method1", hazoum);
		c.executeMethod("method2", hazoum);

	}

	public String constructorToString(ArrayList<CustomConstructor> constructors) {
		String result = "";
		for (CustomConstructor constructor : constructors) {
			String Params = "";
			String Args = "";
			for (int i = 0; i < constructor.getConstructorArgs().length; i++) {
				Params += constructor.getConstructorParams()[i].getSimpleName()
						+ " "
						+ constructor.getConstructorArgs()[i]
						+ (i == constructor.getConstructorArgs().length - 1 ? " "
								: ",");
			}
			int k = 0;
			for (Object object : constructor.getConstructorArgs()) {
				Args += object
						+ (k == constructor.getConstructorArgs().length - 1 ? " "
								: ",");
				k++;
			}

			result += "\n" + " public " + constructor.getName() + "(" + Params
					+ ")" + "{" + "\n" + "super(" + Args + ");" + "\n" + "}";
		}
		return result;
	}

	public String MethodToString(ArrayList<CustomMethod> methods) {
		String result = " ";
		String Params = "";
		if (methods != null) {

			for (CustomMethod method : methods) {
				for (int i = 0; i < method.getMethodArgs().size(); i++) {
					Params += method.getMethodParams().get(i).getSimpleName()
							+ " "
							+ method.getMethodArgs().get(i)
							+ (i == method.getMethodArgs().size() - 1 ? " "
									: ",");
				}
				result += "  public " + method.getReturnType().getSimpleName()
						+ " " + method.getMethodName() + "(" + Params + ")"
						+ "{ " + "\n" + method.getMethodCode() + "\n" + "}";

			}
		}
		return result;
	}

	public String AttributesToString(Object... attributes) {
		String result = "";
		for (Object object : attributes) {
			result += "Object " + object;
		}
		return result;
	}

	public String AttributeInitilizeToString(Object... attributes) {
		String result = "";
		for (Object object : attributes) {
			result += "this." + object + " = " + object;
		}
		return result;
	}

	public Class<?> createClass(ArrayList<CustomMethod> methods, ArrayList<CustomConstructor> constructors)
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		// Set to store unique classes
		Set<Class<?>> uniqueClasses = new HashSet<>();

		// Iterate over constructors and add parameter classes to uniqueClasses
		for (CustomConstructor constructor : constructors) {
			Class<?>[] constructorParams = constructor.getConstructorParams();
			if (constructorParams != null) {
				Collections.addAll(uniqueClasses, constructorParams);
			}
		}

		// Generate import statements for the unique classes
		List<String> importStatements = new ArrayList<>();
		Set<String> packageNames = new HashSet<>();

		// Extract package names from uniqueClasses
		for (Class<?> Class : uniqueClasses) {
			String packageName = Class.getPackage().getName();
			packageNames.add(packageName);
		}

		// Create import statements
		for (String packageName : packageNames) {
			importStatements.add("import " + packageName + ".*;");
		}

		// Combine import statements into a single string
		StringBuilder imports = new StringBuilder();
		for (String importStatement : importStatements) {
			imports.append(importStatement).append("\n");
		}

		// Determine if a superclass is specified
		String extend = superClass.isEmpty() ? "" : " extends " + superClass;

		// Generate the class code
		StringBuilder classCode = new StringBuilder();
		classCode.append(imports)
				.append("public class ").append(className).append(extend).append(" {\n")
				.append(constructorToString(constructors)).append("\n") // Convert constructors to string representation
				.append(MethodToString(methods)).append("\n") // Convert methods to string representation
				.append("}");

		// Create the class using the generated code
		Class<?> myClass = buildClass(className, classCode.toString(), className, superClass);
		newClass = myClass;
		return myClass;
	}

	public Object createInstance(Class<?> myClass, Object... constructorArgs)
			throws Exception {
		// Retrieve the constructors of the class
		Constructor<?>[] constructors = myClass.getConstructors();

		// Find the appropriate constructor with matching parameter types
		Constructor<?> constructor = null;
		Class<?>[] constructorParams2 = new Class<?>[constructorArgs.length];
		for (int i = 0; i < constructorArgs.length; i++) {
			constructorParams2[i] = constructorArgs[i].getClass();
		}
		for (Constructor<?> c : constructors) {
			Class<?>[] parameterTypes = c.getParameterTypes();

			if (parameterTypes.length == constructorParams2.length) {
				boolean match = true;
				for (int i = 0; i < parameterTypes.length; i++) {
					if (!parameterTypes[i].equals(constructorParams2[i])) {
						match = false;
						break;
					}
				}
				if (match) {
					constructor = c;
					break;
				}
			}
		}

		if (constructor != null) {
			Object instance = constructor.newInstance(constructorArgs);

			return instance;
		} else {
			throw new NoSuchMethodException("Constructor not found");
		}
	}

	public void executeMethod(String methodName, Object instance)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Class<?> myClass = instance.getClass();
		Method method = myClass.getMethod(methodName);
		method.invoke(instance);

	}

	public Class<?> buildClass(String className, String classCode, String fileName, String superClassName)
			throws IOException, ClassNotFoundException {
		// Specify the file path where the class will be written
		String filePath = fileName + ".java";

		// Write the class code to the file
		try (PrintWriter out = new PrintWriter(filePath)) {
			out.println(classCode);
		}

		// Print a success message with the file path
		System.out.println("Class " + className + " successfully created as " + filePath);

		// Set up compilation options and tools
		String[] options = new String[] { "-d", "bin" };
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

		// Set the output location for compiled class files
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File("bin")));

		// Create a JavaFileObject representing the source file
		JavaFileObject sourceFile = new SourceFile(className, classCode);
		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceFile);

		// Compile the source file
		CompilationTask task = compiler.getTask(null, fileManager, diagnostics, Arrays.asList(options), null,
				compilationUnits);

		// Check if compilation failed and throw an exception if it did
		if (!task.call()) {
			throw new RuntimeException("Compilation failed: " + diagnostics.getDiagnostics());
		}

		// Obtain a class loader and load the compiled class
		ClassLoader classLoader = fileManager.getClassLoader(StandardLocation.CLASS_OUTPUT);
		Class<?> compiledClass = Class.forName(className, true, classLoader);

		// Close the file manager
		fileManager.close();

		// Return the compiled class
		return compiledClass;
	}

	public Object createInstance(Class<?> myClass)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		return myClass.getDeclaredConstructor().newInstance();
	}

	static class Compiler {
		public byte[] compile(String className, String code) throws Exception {
			// Get the system Java compiler
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

			// Create a diagnostic collector to collect compilation diagnostics
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

			// Get the standard file manager from the compiler
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

			// Set the output location to the "bin" directory
			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File("bin")));

			// Create a Java source file object with the given class name and code
			JavaFileObject sourceFile = new SourceFile(className, code);

			// Create a compilation task with the file manager, diagnostics, and source file
			Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceFile);
			CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

			// Call the compilation task and check if it was successful
			if (!task.call()) {
				// Compilation failed, throw a runtime exception with the diagnostics
				// information
				throw new RuntimeException("Compilation failed: " + diagnostics.getDiagnostics());
			}

			// Get the class loader from the file manager
			ClassLoader classLoader = fileManager.getClassLoader(StandardLocation.CLASS_OUTPUT);

			// Load the compiled class using the class loader
			Class<?> compiledClass = Class.forName(className, true, classLoader);

			// Close the file manager
			fileManager.close();

			// Read the compiled class bytes from the file system and return as a byte array
			return Files.readAllBytes(Paths.get(
					compiledClass.getProtectionDomain().getCodeSource().getLocation().toURI())
					.resolve(className.replace('.', '/') + ".class"));
		}
	}

	// static class ByteClassLoader extends ClassLoader {
	// public Class<?> defineClass(String name, byte[] b) {
	// return defineClass(name, b, 0, b.length);
	// }
	// }

	static class SourceFile extends SimpleJavaFileObject {
		private final String code;

		public SourceFile(String name, String code) {
			super(URI.create("string:///" + name.replaceAll("\\.", "/")
					+ Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}
	}
	//
	// static class ByteArrayJavaClass extends SimpleJavaFileObject {
	// private final ByteArrayOutputStream outputStream = new
	// ByteArrayOutputStream();
	//
	// public ByteArrayJavaClass(String className) {
	// super(URI.create("bytes:///" + className), Kind.CLASS);
	// }
	//
	// public byte[] getBytes() {
	// return outputStream.toByteArray();
	// }
	//
	// @Override
	// public OutputStream openOutputStream() throws IOException {
	// return outputStream;
	// }
	//
	// }

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getSuperClass() {
		return superClass;
	}

	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}

}
