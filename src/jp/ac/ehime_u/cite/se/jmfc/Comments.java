package jp.ac.ehime_u.cite.se.jmfc;

/**
 * 七種類のコメントをまとめて管理するオブジェクト．
 * @author sho
 *
 */
public class Comments
{
	public Comments(int ct1,int ct2,int ct3,int ct4,int ct5,int ct6,int ct7)
	{
		this.ct1=ct1;
		this.ct2=ct2;
		this.ct3=ct3;
		this.ct4=ct4;
		this.ct5=ct5;
		this.ct6=ct6;
		this.ct7=ct7;
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

	private int ct1,ct2,ct3,ct4,ct5,ct6,ct7;
}
