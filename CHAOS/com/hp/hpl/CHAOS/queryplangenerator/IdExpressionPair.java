package com.hp.hpl.CHAOS.queryplangenerator;

import com.hp.hpl.CHAOS.Expression.Expression;


public class IdExpressionPair {
	protected int column;
	public Expression value;
	public IdExpressionPair(int column, Expression value) {
		
		this.column = column;
		this.value = value;
	}
	public int getColumn() {
		return column;
	}
	public void setColumn(int column) {
		this.column = column;
	}
	public Expression getValue() {
		return value;
	}
	public void setValue(Expression value) {
		this.value = value;
	}

}
