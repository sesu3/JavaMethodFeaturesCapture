package jp.ac.ehime_u.cite.se.jmfc;

public class VariableScope
{
	/**
	 * メソッドの仮引数
	 */
	public static final int METHOD_ARGUMENT=0;
	/**
	 * ローカル変数
	 */
	public static final int LOCAL_VARIABLE=1;
	/**
	 * フィールド
	 */
	public static final int FIELD=2;

	public VariableScope(String name, int type, int begin, int end)
	{
		this.name=name;
		this.type=type;
		this.begin=begin;
		this.end=end;
	}

	/**
	 * この変数の名前を返します．
	 * @return
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * この変数のタイプを返します．
	 * タイプの整数はScopeクラスのパブリック定数に対応しています．
	 * @return
	 */
	public int getType()
	{
		return this.type;
	}

	/**
	 * この変数のスコープの開始地点の行数を返します．
	 * @return
	 */
	public int getBegin()
	{
		return this.begin;
	}

	/**
	 * この変数のスコープの囚虜う地点の行数を返します．
	 * @return
	 */
	public int getEnd()
	{
		return this.end;
	}

	@Override
	public String toString()
	{
		return this.name+"("+this.begin+","+this.end+")";
	}

	private String name;
	private int type;
	private int begin;
	private int end;
}
