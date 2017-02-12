package jp.ac.ehime_u.cite.se.jmfc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.computer.aman.io.sourcecode.NotSupportedSourceFileExeption;
import org.computer.aman.metrics.comment.CommentCounter;
import org.computer.aman.metrics.comment.CommentCounterFactory;
import org.computer.aman.metrics.comment.CountResultForJava;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

public class MethodVisitor extends ASTVisitor
{
	public MethodVisitor(String targetFilePath,boolean onDebugMode)
	{
		this.targetFilePath=targetFilePath;
		this.onDebugMode=onDebugMode;
	}

	@Override
	public boolean visit(TypeDeclaration node)
	{
		if(node.isInterface()){
			return false;
		}
		if(isInnerClass(node)){
			printDebugMessage(node.getName()+"はインナークラスであるため，解析をスキップします．");
			return false;
		}
		this.variables.clear();
		FieldDeclaration[] fd=node.getFields();
		CompilationUnit unit=(CompilationUnit)node.getRoot();
		for(int i=0;i<fd.length;i++){
			@SuppressWarnings("unchecked")
			List<VariableDeclarationFragment> vdfList=fd[i].fragments();
			for(Iterator<VariableDeclarationFragment> iter=vdfList.iterator();iter.hasNext();){
				VariableDeclarationFragment vdfNode=iter.next();
				int begin=unit.getLineNumber(node.getStartPosition());
				int end=unit.getLineNumber(node.getStartPosition()+node.getLength()-1);
				this.variables.add(new VariableScope(vdfNode.getName().toString(),VariableScope.FIELD,begin,end));
				printDebugMessage("フィールドスコープの追加：\t"+vdfNode.getName().toString()+"["+begin+","+end+"]");
			}
		}
		System.err.println("******************************");
		System.err.println("解析中のクラス：\t"+node.getName().toString());
		System.err.println("******************************");
		return super.visit(node);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node)
	{
		printDebugMessage(node.toString()+"は匿名クラスであるため，解析をスキップします．");
		return false;
	}

	/**
	 * このTypeDeclarationが，インナークラスであるかどうかを判定します．
	 * @return
	 */
	private static boolean isInnerClass(ASTNode node)
	{
		ASTNode parent=node.getParent();
		if(parent==null){
			return false;
		}else if(parent instanceof TypeDeclaration){
			return true;
		}else{
			return isInnerClass(parent);
		}
	}

	@Override
	public boolean visit(MethodDeclaration node)
	{
		if(node.isConstructor()){
			return false;
		}
		this.invocedMethodNameList.clear();
		this.localVariableCount=0;
		this.variables.clearMethodVariables();
		this.branchStructureCount=0;
		this.loopStructureCount=0;
		this.returnStatementCount=0;
		this.throwStatementCount=0;
		this.tryStatementCount=0;
		this.castExpressionCount=0;
		this.instanceofExpressionCount=0;
		this.classInstanceCreationCount=0;
		this.fieldWriteCount=0;
		this.fieldReadCount=0;
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node)
	{
		this.invocedMethodNameList.add(node.getName().toString());
		return super.visit(node);
	}

	@Override
	public void endVisit(MethodDeclaration node)
	{
		if(!node.isConstructor()){
			printMethodStatus(node);
		}
		super.endVisit(node);
	}

	@Override
	public boolean visit(SimpleName node)
	{
		if(isFullName(node) && isVariablesUsing(node)){
			CompilationUnit unit=(CompilationUnit)node.getRoot();
			int point=unit.getLineNumber(node.getStartPosition());
			if(isAssigningToThisVariable(node)){
				if(!this.variables.isLocalVariable(node.toString(), unit.getLineNumber(node.getStartPosition()))){
					this.fieldWriteCount++;
					printDebugMessage("Field Write1:\t"+node.toString()+" ("+point+"行目)");
				}
			}else{
				if(!this.variables.isLocalVariable(node.toString(), unit.getLineNumber(node.getStartPosition()))){
					this.fieldReadCount++;
					printDebugMessage("Field Read1:\t"+node.toString()+" ("+point+"行目)");
				}
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node)
	{
		if(isFullName(node) && isVariablesUsing(node)){
			CompilationUnit unit=(CompilationUnit)node.getRoot();
			int point=unit.getLineNumber(node.getStartPosition());
			if(isAssigningToThisVariable(node)){
				if(!this.variables.isLocalVariable(node.toString(), unit.getLineNumber(node.getStartPosition()))){
					this.fieldWriteCount++;
					printDebugMessage("Field Write2:\t"+node.toString()+" ("+point+"行目)");
				}
			}else{
				if(!this.variables.isLocalVariable(node.toString(), unit.getLineNumber(node.getStartPosition()))){
					this.fieldReadCount++;
					printDebugMessage("Field Read2:\t"+node.toString()+" ("+point+"行目)");
				}
			}
		}
		return super.visit(node);
	}

	/**
	 * 名前要素を受け取って，それが名前全体を表しているか判定します．
	 * @param node
	 * @return
	 */
	private static boolean isFullName(ASTNode node)
	{
		ASTNode parent=node.getParent();
		if(parent instanceof Name){//名前要素の一部であればFalse
			return false;
		}
		return true;
	}

	/**
	 * この名前要素が代入される要素であるかどうかを判定する．（つまり，フィールドへの書き込み）
	 * @param node
	 * @return
	 */
	private static boolean isAssigningToThisVariable(ASTNode node)
	{
		ASTNode parent=node.getParent();
		if(parent instanceof Assignment){
			if(((Assignment) parent).getLeftHandSide()==node){//SimpleNameかQualifiedNameへの代入
				return true;
			}
			return false;
		}
		if(parent instanceof VariableDeclarationFragment){
			if(((VariableDeclarationFragment) parent).getInitializer()==null){//初期値の代入は行われない
				return false;
			}else{
				if(((VariableDeclarationFragment) parent).getName().toString().equals(node.toString())){//宣言され，代入される変数はAssignである
					return true;
				}
				return false;
			}
		}
		if(parent instanceof PrefixExpression){
			return true;
		}
		if(parent instanceof PostfixExpression){
			return true;
		}
		return false;
	}

	/**
	 * 名前要素がある変数もしくは定数を使用するためのものであるかを判定する．
	 * @return
	 */
	private static boolean isVariablesUsing(ASTNode node)
	{
		ASTNode parent=node.getParent();
		if(parent instanceof PackageDeclaration){//パッケージ宣言内の名前要素は変数ではない
			return false;
		}
		if(parent instanceof ImportDeclaration){//インポート文内の名前要素は変数ではない
			return false;
		}
		if(parent instanceof TypeDeclaration){//クラス・インターフェース名は変数ではない
			return false;
		}
		if(parent instanceof Annotation){//アノテーションの名前要素は変数ではない
			return false;
		}
		if(parent instanceof MethodDeclaration){//メソッド名・コンストラクタ名は変数ではない
			return false;
		}
		if(parent instanceof Type){//型は変数ではない（Stringなどのクラス名がこれにあたるため）．
			return false;
		}
		if(parent instanceof MethodInvocation){//メソッド呼び出しのうち，呼び出すメソッド自体は変数ではない
			if(((MethodInvocation) parent).getName()==node){//名前要素がメソッド名と一致する
				return false;
			}
			return true;
		}
		if(parent instanceof SuperMethodInvocation){
			if(((SuperMethodInvocation) parent).getName()==node){//名前要素がメソッド名と一致する
				return false;
			}
			return true;
		}
		if(parent instanceof FieldAccess){//フィールドアクセスは別で扱う．
			return false;
		}
		if(parent instanceof SuperFieldAccess){//スーパーフィールドアクセスは別で扱う．
			return false;
		}
		if(parent instanceof ArrayAccess){//配列についてはExpressionと一致すれば変数として使われている．
			if((((ArrayAccess)parent).getArray())==node){
				return true;
			}
			return false;
		}

		return true;
	}

	@Override
	public boolean visit(FieldAccess node)
	{
		if(isVariablesUsing(node)){
			int point=((CompilationUnit)node.getRoot()).getLineNumber(node.getStartPosition());
			if(isAssigningToThisVariable(node)){
				this.fieldWriteCount++;
				printDebugMessage("Field Write3:\t"+node.toString()+" ("+point+"行目)");
			}else{
				this.fieldReadCount++;
				printDebugMessage("Field Read3:\t"+node.toString()+" ("+point+"行目)");
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node)
	{
		if(isVariablesUsing(node)){
			int point=((CompilationUnit)node.getRoot()).getLineNumber(node.getStartPosition());
			if(isAssigningToThisVariable(node)){
				this.fieldWriteCount++;
				printDebugMessage("Field Write4:\t"+node.toString()+" ("+point+"行目)");
			}else{
				this.fieldReadCount++;
				printDebugMessage("Field Read4:\t"+node.toString()+" ("+point+"行目)");
			}
		}
		return super.visit(node);
	}

	/**
	 * メソッドの特徴を標準出力します．
	 * @param node
	 */
	private void printMethodStatus(MethodDeclaration node)
	{
		Metrics metrics=this.measureMetrics(node);
		System.out.println("- - - - - - - - - - - - - - - - - - - - ");
		System.out.println("FQMN:\t\t\t\t"+getFullyQualifiedMethodName(node));
		System.out.println("MN:\t\t\t\t\t"+node.getName());
		System.out.println("FW:\t\t\t\t\t"+getFirstWord(node));
		System.out.println("R-Type:\t\t\t\t"+getReturnTypeCategory(node)+" ("+node.getReturnType2()+")");
		System.out.println("Access:\t\t\t\t"+getAccessModifier(node));
		System.out.println("Static:\t\t\t\t"+hasStaticModifier(node));
		System.out.println("Params:\t\t\t\t"+countParameters(node)+" "+node.parameters().toString());
		System.out.println("throws:\t\t\t\t"+countThrowsExceptions(node)+" "+node.thrownExceptionTypes().toString());
		System.out.println("Invoke:\t\t\t\t"+this.invocedMethodNameList.size()+" "+this.invocedMethodNameList.toString());
		System.out.println("Invoke-SameName:\t"+this.countSameNameMethodInvoke(node));
		System.out.println("Invoke-SameFWord:\t"+this.countSameFirstWordMethodInvoke(node));
		System.out.println("Local Variables:\t"+this.localVariableCount);
		System.out.println("Available V:\t\t"+this.variables.toString());
		System.out.println("branch structure:\t"+this.branchStructureCount);
		System.out.println("loop structure\t\t"+this.loopStructureCount);
		System.out.println("return stmt:\t\t"+this.returnStatementCount);
		System.out.println("throw stmt:\t\t\t"+this.throwStatementCount);
		System.out.println("try stmt:\t\t\t"+this.tryStatementCount);
		System.out.println("cast exp:\t\t\t"+this.castExpressionCount);
		System.out.println("instanceof exp:\t\t"+this.instanceofExpressionCount);
		System.out.println("new stmt:\t\t\t"+this.classInstanceCreationCount);
		System.out.println("field Write:\t\t"+this.fieldWriteCount);
		System.out.println("field Read:\t\t\t"+this.fieldReadCount);
		System.out.println("Metrics Values:\t\t"+metrics.toString());
		System.out.println("- - - - - - - - - - - - - - - - - - - - ");
	}

	/**
	 * 各種メトリクス値を計測し，それらをまとめたMetricsオブジェクトのインスタンスを返します．
	 * @param node
	 * @return
	 * @throws IOException
	 * @throws NotSupportedSourceFileExeption
	 * @throws SecurityException
	 * @throws FileNotFoundException
	 */
	private Metrics measureMetrics(MethodDeclaration node)
	{
		try {
			Metrics mtx=new Metrics();
			CompilationUnit unit=(CompilationUnit)node.getRoot();
			int begin=unit.getLineNumber(node.getStartPosition());
			int end=unit.getLineNumber(node.getStartPosition()+node.getLength()-1);
			printDebugMessage(node.getName()+"["+begin+","+end+"]");
			AnalyzedTempFile tmpFile=new AnalyzedTempFile(this.targetFilePath,begin,end);
			mtx.setLOC(getLOC(tmpFile));
			mtx.setCC(getCyclomaticNumber(tmpFile));
			NestLevel nest=getNestLevel(tmpFile);
			mtx.setNest(nest.getNestMax(), nest.getNestAverage());
			Comments cms=getComments(tmpFile);
			mtx.setComments(
					cms.getCommentType1(),
					cms.getCommentType2(),
					cms.getCommentType3(),
					cms.getCommentType4(),
					cms.getCommentType5(),
					cms.getCommentType6(),
					cms.getCommentType7());
			return mtx;
		} catch (SecurityException | NotSupportedSourceFileExeption | IOException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 外部プロセスでLOCCounter.jarを実行し，一時ファイル内のメソッドLOCを返します．
	 * 一時ファイルはjavaファイルとして成立させるためにクラス宣言を含んでいますが，
	 * 当該箇所は無視した値が返ってくるため戻り値はメソッドLOCとしてそのまま使用することができます．
	 * @param tmpFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws SecurityException
	 * @throws NotSupportedSourceFileExeption
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static int getLOC(AnalyzedTempFile tmpFile)
			throws FileNotFoundException, SecurityException, NotSupportedSourceFileExeption, IOException, InterruptedException
	{
		ProcessBuilder pb=new ProcessBuilder("java","-jar","LOCCounter.jar",tmpFile.getPath());
		pb.directory(new File("item/"));
		Process p=pb.start();
		InputStream is=p.getInputStream();
		p.waitFor();
		Scanner scan=new Scanner(is);
		String[] ret=scan.nextLine().split(",");
		scan.close();
		try{
			return Integer.parseInt(ret[1])-TMP_FILE_LOC_OFFSET;
		}catch(NumberFormatException e){
			return -1;
		}
	}

	/**
	 * 外部プロセスでCyclomaticNumberCounterを実行し，一時ファイルのサイクロマティック数を返します．
	 * 何らかの事情によってサイクロマティック数が形状できなかった場合は-1が返されます．
	 * @param tmpFile
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static int getCyclomaticNumber(AnalyzedTempFile tmpFile) throws IOException, InterruptedException
	{
		ProcessBuilder pb=new ProcessBuilder("java","-jar","CyclomaticNumberCounter.jar",tmpFile.getPath());
		pb.directory(new File("item/"));
		Process p=pb.start();
		InputStream is=p.getInputStream();
		p.waitFor();
		Scanner scan=new Scanner(is);
		String[] ret=scan.nextLine().split(",");
		scan.close();
		try{
			return Integer.parseInt(ret[1]);
		}catch(NumberFormatException e){
			return -1;
		}
	}

	/**
	 * 外部プロセスでNestLevelMeterを実行し，一時ファイルのネストレベルを返します．
	 * ネストレベルは専用のクラスのインスタンスによって返されます．
	 * @param tmpFile
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static NestLevel getNestLevel(AnalyzedTempFile tmpFile)
			throws IOException, InterruptedException
	{
		ProcessBuilder pb=new ProcessBuilder("java","-jar","NestLevelMeter.jar","--all",tmpFile.getPath());
		pb.directory(new File("item/"));
		Process p=pb.start();
		InputStream is=p.getInputStream();
		p.waitFor();
		Scanner scan=new Scanner(is);
		NestLevel nl=new NestLevel(scan.nextLine());
		scan.close();
		return nl;
	}

	/**
	 * CommentCounter.jarを内部的に使用し，
	 * 指定したFileオブジェクトが指す一時ファイルを解析することでコメント行に関する情報を返します．
	 * @param tmpFile
	 * @return
	 * @throws IOException
	 * @throws NotSupportedSourceFileExeption
	 * @throws SecurityException
	 */
	private static Comments getComments(AnalyzedTempFile tmpFile)
			throws SecurityException, NotSupportedSourceFileExeption, IOException
	{
		CommentCounter cc=CommentCounterFactory.create(tmpFile.getPath());
		CountResultForJava res=(CountResultForJava)cc.measure(tmpFile.getMethodBegin(), tmpFile.getMethodEnd());
		Comments retObj=new Comments(
				res.getEolCommentCount(),
				res.getTraditionalCommentCount(),
				res.getJavadocCommentCount(),
				res.getEolCommentCountInHead(),
				res.getTraditionalCommentCountInHead(),
				res.getEolCommentOutCount(),
				res.getTraditionalCommentOutCount()
				);
		return retObj;
	}

	/**
	 * 完全修飾メソッド名を返します．
	 * @param node
	 * @return
	 */
	private static String getFullyQualifiedMethodName(MethodDeclaration node)
	{
		IMethodBinding methodBinding=node.resolveBinding();
		ITypeBinding typeBinding=methodBinding.getDeclaringClass();
		return typeBinding.getQualifiedName()+"#"+node.getName();
	}

	@Override
	public boolean visit(VariableDeclarationFragment node)
	{
		CompilationUnit unit=(CompilationUnit)node.getRoot();
		if(!isInFieldDeclaration(node)){//フィールド宣言ではない
			ASTNode declaredNode=node.getParent().getParent();
			int begin=unit.getLineNumber(node.getStartPosition());
			int end=unit.getLineNumber(declaredNode.getStartPosition()+declaredNode.getLength()-1);
			this.variables.add(new VariableScope(node.getName().toString(),VariableScope.LOCAL_VARIABLE,begin,end));
		}
		this.localVariableCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node)
	{
		CompilationUnit unit=(CompilationUnit)node.getRoot();
		ASTNode declaredNode=node.getParent();
		int begin=unit.getLineNumber(node.getStartPosition());
		int end=unit.getLineNumber(declaredNode.getStartPosition()+declaredNode.getLength()-1);
		this.variables.add(new VariableScope(node.getName().toString(),VariableScope.METHOD_ARGUMENT,begin,end));
		return super.visit(node);
	}

	private static String getFirstWord(MethodDeclaration node)
	{
		return getFirstWord(node.getName().toString());
	}

	private static String getFirstWord(String name)
	{
		String[] nameParts=name.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|[_-]|(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		return nameParts[0];
	}

	private static String getReturnTypeCategory(MethodDeclaration node)
	{
		if(node.getReturnType2().isPrimitiveType()){
			PrimitiveType type=(PrimitiveType)node.getReturnType2();
			if(type.getPrimitiveTypeCode()==PrimitiveType.VOID){
				return "void";
			}else if(type.getPrimitiveTypeCode()==PrimitiveType.BOOLEAN){
				return "boolean";
			}else{
				return "primitive";
			}
		}else{
			return "reference";
		}
	}

	private static String getAccessModifier(MethodDeclaration node)
	{
		int flag=node.getModifiers();
		if(Modifier.isPublic(flag)){
			return "public";
		}else if(Modifier.isPrivate(flag)){
			return "private";
		}else if(Modifier.isProtected(flag)){
			return "protected";
		}else{
			return "none";
		}
	}

	/**
	 * メソッドがstaticメソッドであるかどうかを判定する．staticメソッドであればtrue，そうでなければfalseを返す．
	 * @param node
	 * @return
	 */
	private static boolean hasStaticModifier(MethodDeclaration node)
	{
		if(Modifier.isStatic(node.getModifiers())){
			return true;
		}
		return false;
	}

	/**
	 * メソッドの引数の数を返す．
	 * @param node
	 * @return
	 */
	private static int countParameters(MethodDeclaration node)
	{
		return node.parameters().size();
	}

	/**
	 * メソッドのthrows句に記述された例外の数を返す．
	 * @param node
	 * @return
	 */
	private static int countThrowsExceptions(MethodDeclaration node)
	{
		return node.thrownExceptionTypes().size();
	}

	/**
	 * メソッド宣言内で，当該メソッドと同じ名前を持つメソッドが呼び出される回数を返す．
	 * @param node
	 * @return
	 */
	private int countSameNameMethodInvoke(MethodDeclaration node)
	{
		int cnt=0;
		String methodName=node.getName().toString();
		for(Iterator<String> iter=this.invocedMethodNameList.iterator();iter.hasNext();){
			String invokedMethodName=iter.next();
			if(methodName.equals(invokedMethodName)){
				cnt++;
			}
		}
		return cnt;
	}

	/**
	 * メソッド宣言内で，当該メソッドと同じ先頭単語を持つメソッドが呼び出される回数を返す．
	 * なお，ここでいう同一の先頭単語とは，活用形によって区別される．
	 * したがって，getsとgetは別の単語として扱われる．
	 * @param node
	 * @return
	 */
	private int countSameFirstWordMethodInvoke(MethodDeclaration node)
	{
		int cnt=0;
		String methodFirstWord=getFirstWord(node);
		for(Iterator<String> iter=this.invocedMethodNameList.iterator();iter.hasNext();){
			String invokedMethodName=iter.next();
			String invokedFirstWord=getFirstWord(invokedMethodName);
			if(methodFirstWord.equals(invokedFirstWord)){
				cnt++;
			}
		}
		return cnt;
	}



	@Override
	public boolean visit(CastExpression node)
	{
		this.castExpressionCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node)
	{
		this.classInstanceCreationCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node)
	{
		this.loopStructureCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node)
	{
		this.loopStructureCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node)
	{
		this.loopStructureCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node)
	{
		this.branchStructureCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node)
	{
		this.instanceofExpressionCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node)
	{
		this.returnStatementCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node)
	{
		this.branchStructureCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node)
	{
		this.throwStatementCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node)
	{
		this.tryStatementCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node)
	{
		this.loopStructureCount++;
		return super.visit(node);
	}

	/**
	 * このASTNodeがフィールド宣言内のものであるか判別します．
	 * @param node
	 * @return
	 */
	private static boolean isInFieldDeclaration(ASTNode node)
	{
		if(node==null){
			return false;
		}else if(node instanceof FieldDeclaration){
			return true;
		}else{
			return isInFieldDeclaration(node.getParent());
		}
	}

	/**
	 * デバッグ用の出力を行います．
	 * ただし，ビジタ作成時にデバッグモードがオンでない場合は出力されません．
	 * @param message
	 */
	private void printDebugMessage(String message)
	{
		if(!this.onDebugMode){
			return;
		}
		System.err.println(message);
	}

	private String targetFilePath;
	private boolean onDebugMode;
	private List<String> invocedMethodNameList=new LinkedList<String>();
	private int localVariableCount=0;
	private VariableScopeList variables=new VariableScopeList();
	private int branchStructureCount=0;
	private int loopStructureCount=0;
	private int returnStatementCount=0;
	private int throwStatementCount=0;
	private int tryStatementCount=0;
	private int castExpressionCount=0;
	private int instanceofExpressionCount=0;
	private int classInstanceCreationCount=0;
	private int fieldWriteCount=0;
	private int fieldReadCount=0;

	private static final int TMP_FILE_LOC_OFFSET=2;
}
