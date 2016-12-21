package com.hp.hpl.CHAOS.StreamData;

public class IntSchemaElement extends SchemaElement {

	private static final long serialVersionUID = 1L;

	public IntSchemaElement(String colName, int offset) {
		super(colName, Constant.INT_T, offset, Constant.INT_S);
	}

	public IntSchemaElement(String colName, int dataType, int offset, int length) {
		super(colName, Constant.INT_T, offset, Constant.INT_S);
	}

	@Override
	public byte[] buildByteArray(String str) {
		return Utility.toByta(Integer.parseInt(str));
	}

	@Override
	public char[] getStr20Value() {
		return null;
	}

	@Override
	public double getValue() {
		return Utility.toInt(tuple, this.getTupleOffset());
	}

}
