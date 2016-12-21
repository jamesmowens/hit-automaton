package com.hp.hpl.CHAOS.StreamData;

public class DoubleSchemaElement extends SchemaElement {

	private static final long serialVersionUID = 1L;

	public DoubleSchemaElement(String colName, int offset) {
		super(colName, Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
	}

	public DoubleSchemaElement(String colName, int dataType, int offset,
			int length) {
		super(colName, Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
	}

	@Override
	public byte[] buildByteArray(String str) {
		return Utility.toByta(Double.parseDouble(str));
	}

	@Override
	public char[] getStr20Value() {
		return null;
	}

	@Override
	public double getValue() {
		return Utility.toDouble(tuple, this.getTupleOffset());
	}

}
