package jp.ac.ehime_u.cite.se.jmfc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class AnalyzedTempFile
{
	public AnalyzedTempFile(String path,int begin,int end) throws IOException
	{
		this.orgPath=path;
		this.content=File.createTempFile("forAnalysis", ".java",new File(System.getProperty("user.dir")));
		this.content.deleteOnExit();

		Scanner scan=new Scanner(IOTools.readFileAsString(path));
		PrintWriter tmpFileWriter=new PrintWriter(new BufferedWriter(new FileWriter(this.content)));
		tmpFileWriter.println("abstract class TMP{");
		int lineCnt=1;
		while(scan.hasNextLine()){
			String line=scan.nextLine();
			if(begin<=lineCnt&&lineCnt<=end){
				tmpFileWriter.println(line);
			}
			lineCnt++;
		}
		tmpFileWriter.println("}");
		tmpFileWriter.flush();
		scan.close();
		tmpFileWriter.close();

		this.methodBegin=2;
		this.methodEnd=end-begin+2;
		this.lineLength=end-begin+1;
	}

	/**
	 * 一時ファイル内のメソッド開始行番号を返します．
	 * @return
	 */
	public int getMethodBegin()
	{
		return this.methodBegin;
	}

	/**
	 * 一時ファイル内のメソッド終了行番号を返します．
	 * @return
	 */
	public int getMethodEnd()
	{
		return this.methodEnd;
	}

	/**
	 * コピー元のファイルパスを返します．
	 * @return
	 */
	public String getOrgPath()
	{
		return this.orgPath;
	}

	/**
	 * この一時ファイルのパスを返します．
	 */
	public String getPath()
	{
		return this.content.getPath();
	}

	/**
	 * この一時ファイルのFileオブジェクトのインスタンスを返します．
	 * @return
	 */
	public File getContent()
	{
		return this.content;
	}

	/**
	 * この一時ファイルの行数を返します．
	 * @return
	 */
	public int getLineLength()
	{
		return this.lineLength;
	}

	private int methodBegin;
	private int methodEnd;
	private String orgPath;
	private File content;
	private int lineLength;
}
