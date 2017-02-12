package jp.ac.ehime_u.cite.se.jmfc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class VariableScopeList
{
	/**
	 * 現在このリストが管理している変数情報の数を返します．
	 * @return リストが保持している変数の数
	 */
	public int size()
	{
		return this.list.size();
	}

	/**
	 * リストに変数のスコープ情報を追加します．
	 * @param vs
	 */
	public void add(VariableScope vs)
	{
		list.add(vs);
	}

	/**
	 * リストのイテレータを返します．
	 * @return
	 */
	public Iterator<VariableScope> iterator()
	{
		return this.list.iterator();
	}

	/**
	 * リストから現在管理している全ての変数のスコープ情報を削除します．
	 */
	public void clear()
	{
		this.list.clear();
	}

	/**
	 * リストからメソッドをスコープとする要素を全て削除します．
	 */
	public void clearMethodVariables()
	{
		for(Iterator<VariableScope> iter=this.list.iterator();iter.hasNext();){
			VariableScope vs=iter.next();
			if(vs.getType()==VariableScope.METHOD_ARGUMENT||vs.getType()==VariableScope.LOCAL_VARIABLE){
				iter.remove();
			}
		}
	}

	/**
	 * このリストが管理している変数情報を出力します．
	 */
	public void printVariables()
	{
		for(Iterator<VariableScope> iter=this.list.iterator();iter.hasNext();){
			VariableScope vs=iter.next();
			System.out.printf("%s,%d,%d,%d%n", vs.getName(),vs.getType(),vs.getBegin(),vs.getEnd());
		}
	}

	/**
	 * ある変数がローカル変数か否かを論理値で返します
	 * 名前の文字列が同値であり，かつ出現する行番号をスコープとして持つローカル変数が存在する場合 true を返します．
	 * @param variableName 判別したい変数の名前
	 * @param positionLine 変数が出現する行番号
	 * @return 指定した変数情報が，管理しているローカル変数情報に含まれていれば true，そうでなければfalseを返します．
	 */
	public boolean isLocalVariable(String variableName,int positionLine)
	{
		int pIndex=variableName.indexOf(".");
		pIndex=pIndex==-1?variableName.length():pIndex;
		String targetName=variableName.substring(0,pIndex);
		//System.err.println("isLocalVariable "+targetName);
		for(Iterator<VariableScope> iter=this.list.iterator();iter.hasNext();){
			VariableScope vs=iter.next();
			if(vs.getType()!=VariableScope.FIELD &&
					targetName.equals(vs.getName()) &&
					(vs.getBegin()<=positionLine&&positionLine<=vs.getEnd())){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString()
	{
		return this.list.toString();
	}

	private List<VariableScope> list=new LinkedList<VariableScope>();
}
