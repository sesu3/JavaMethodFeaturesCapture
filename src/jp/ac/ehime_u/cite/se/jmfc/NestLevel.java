package jp.ac.ehime_u.cite.se.jmfc;

/**
 * 二種類のネストレベルを格納するためのクラス
 * @author sho
 *
 */
public class NestLevel
{
	/**
	 * NestLevelMeterの出力からオブジェクトを生成します．
	 * 何らかの事情により整数あるいは実数変換エラーが発生した場合は，-1または-1.0がそれぞれ設定されます．
	 * @param outputOfNestLevelMeter
	 */
	public NestLevel(String outputOfNestLevelMeter)
	{
		String[] ret=outputOfNestLevelMeter.split(",");
		try{
			this.nestM=Integer.parseInt(ret[2]);
		}catch(NumberFormatException e){
			this.nestM=-1;
		}
		try{
			this.nestA=Double.parseDouble(ret[1]);
		}catch(NumberFormatException e){
			this.nestA=-1.0;
		}
	}

	/**
	 * 最大ネスト数を返します．
	 * @return
	 */
	public int getNestMax()
	{
		return this.nestM;
	}

	/**
	 * 平均ネスト数を返します．
	 * @return
	 */
	public double getNestAverage()
	{
		return this.nestA;
	}
	private int nestM;
	private double nestA;
}
