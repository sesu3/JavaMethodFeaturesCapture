package jp.ac.ehime_u.cite.se.jmfc;

public class Metrics
{
	/**
	 * LOC値を返します．
	 * @return
	 */
	public int getLOC()
	{
		return this.loc;
	}

	/**
	 * サイクロマティック数を返します．
	 * @return
	 */
	public int getCC()
	{
		return this.cc;
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

	/**
	 * タイプ１のコメントを返します．
	 * @return
	 */
	public int getCommentType1()
	{
		return this.ct1;
	}

	/**
	 * タイプ2のコメントを返します．
	 * @return
	 */
	public int getCommentType2()
	{
		return this.ct2;
	}

	/**
	 * タイプ3のコメントを返します．
	 * @return
	 */
	public int getCommentType3()
	{
		return this.ct3;
	}

	/**
	 * タイプ4のコメントを返します．
	 * @return
	 */
	public int getCommentType4()
	{
		return this.ct4;
	}

	/**
	 * タイプ5のコメントを返します．
	 * @return
	 */
	public int getCommentType5()
	{
		return this.ct5;
	}

	/**
	 * タイプ6のコメントを返します．
	 * @return
	 */
	public int getCommentType6()
	{
		return this.ct6;
	}

	/**
	 * タイプ7のコメントを返します．
	 * @return
	 */
	public int getCommentType7()
	{
		return this.ct7;
	}

	/**
	 * LOC値をMetricsオブジェクトにセットします．
	 * @param loc
	 */
	public void setLOC(int loc)
	{
		this.loc=loc;
	}

	/**
	 * サイクロマティック数をMetricsオブジェクトにセットします．
	 * @param cc
	 */
	public void setCC(int cc)
	{
		this.cc=cc;
	}

	/**
	 * ネストの深さをMetricsオブジェクトにセットします．
	 * @param max
	 * @param average
	 */
	public void setNest(int max, double average)
	{
		this.nestM=max;
		this.nestA=average;
	}

	/**
	 * コメントをMetricsオブジェクトにセットします．
	 * @param ct1
	 * @param ct2
	 * @param ct3
	 * @param ct4
	 * @param ct5
	 * @param ct6
	 * @param ct7
	 */
	public void setComments(int ct1,int ct2,int ct3,int ct4,int ct5,int ct6,int ct7)
	{
		this.ct1=ct1;
		this.ct2=ct2;
		this.ct3=ct3;
		this.ct4=ct4;
		this.ct5=ct5;
		this.ct6=ct6;
		this.ct7=ct7;
	}


	@Override
	public String toString()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("["+this.loc+", ");
		sb.append(this.cc+", ");
		sb.append(this.nestM+", ");
		sb.append(this.nestA+", ");
		sb.append(ct1+", "+ct2+", "+ct3+", "+ct4+", "+ct5+", "+ct6+", "+ct7+"]");
		return sb.toString();
	}


	private int loc;
	private int cc;
	private int nestM;
	private double nestA;
	private int ct1,ct2,ct3,ct4,ct5,ct6,ct7;

}
