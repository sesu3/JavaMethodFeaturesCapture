package jp.ac.ehime_u.cite.se.jmfc;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		printHeader();

		boolean onDebugMode=false;
		String targetFilePath=null;
		for(int i=0;i<args.length;i++){
			if(args[i].startsWith("-")){
				if(args[i].equals("-d")){
					onDebugMode=true;
				}else{
					printError("Invalid option: "+args[i]);
				}
			}else if(targetFilePath==null){
				targetFilePath=args[i];
			}else{
				printError("Specify ONE souce file !");
			}
		}

		if(targetFilePath==null){
			printError("No Java source file is specified!" +"\n"+"Specify the path of Java source file to be analyzed.");
		}

		if(onDebugMode){
			System.err.println("[debug mode]");
		}

		System.err.println("Reading "+targetFilePath+ "...");

		ASTParser parser=createASTParser(targetFilePath);
		CompilationUnit unit=(CompilationUnit) parser.createAST(new NullProgressMonitor());
		unit.recordModifications();
		unit.accept(new MethodVisitor(targetFilePath,onDebugMode));//解析の開始
	}

	/**
	 * 分析対象のファイルパスから，抽象構文木（AST）を生成する
	 */
	private static ASTParser createASTParser(String targetFilePath) throws IOException
	{
		final Map<String,String> options=JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);

		String source=IOTools.readFileAsString(targetFilePath);

		ASTParser parser=ASTParser.newParser(AST.JLS8);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setCompilerOptions(options);
		String[] properties=System.getProperty("java.class.path", ".").split(File.pathSeparator);
		parser.setEnvironment(properties, new String[]{"."}, null, true);
		parser.setUnitName(targetFilePath);
		parser.setSource(source.toCharArray());
		return parser;
	}

	/**
	 * 実行時にプログラム名とバージョンを表示する．
	 */
	private static void printHeader()
	{
		System.err.println("This is JavaCodeAnalizer ver."+VERSION+".");
		System.err.println(COPYRIGHT);
		System.err.println();
	}

	/**
	 * エラーメッセージを出力し，プログラムを終了させる．
	 * @param aMessage
	 */
	private static void printError(final String aMessage)
	{
		System.err.println("*** ERROR ***");
		System.err.println(aMessage);
		System.exit(1);
	}

	private static final String VERSION="0.0";
	private static final String COPYRIGHT="(C) 2017 s_suzuki <s_suzuki@se.cite.ehime-u.ac.jp>";
}
