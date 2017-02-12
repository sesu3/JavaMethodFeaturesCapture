package jp.ac.ehime_u.cite.se.jmfc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.mozilla.universalchardet.UniversalDetector;

public class IOTools
{
	/**
	 * パスで指定したテキストファイルの文字コードを返す
	 * @param targetFilePath
	 * @return
	 */
	public static String getEncoding(String targetFilePath)
	{
		try {
			UniversalDetector detector=new UniversalDetector(null);
			FileInputStream fis;
			fis = new FileInputStream(targetFilePath);
			int nread;
			byte[] buf=new byte[4096];
			while((nread=fis.read(buf))>0 && !detector.isDone()){
				detector.handleData(buf, 0, nread);
			}
			detector.dataEnd();
			fis.close();
			return detector.getDetectedCharset();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * パスで指定されたファイルの中身をString型で返します．
	 * @param targetFilePath
	 * @return
	 * @throws IOException
	 */
	public static String readFileAsString(String targetFilePath) throws IOException
	{
		String encode=IOTools.getEncoding(targetFilePath);
		if(encode==null){
			encode=DEFAULT_ENCODING;
		}
		String text=FileUtils.readFileToString(new File(targetFilePath),encode);
		if(text.charAt(0)==UTF8_MK){
			text=text.substring(1);
		}
		return text;
	}

	private static final String DEFAULT_ENCODING="MS932";
	private static final int UTF8_MK=65279;
}
