package com.hp.hpl.CHAOS.StreamData;

public class Str20SchemaElement extends SchemaElement {

	private static final long serialVersionUID = 1L;

	public Str20SchemaElement(String colName, int offset) {
		super(colName, Constant.STR20_T, offset, Constant.STR20_S);
	}

	public Str20SchemaElement(String colName, int dataType, int offset,
			int length) {
		super(colName, Constant.STR20_T, offset, Constant.STR20_S);
	}

	@Override
	public byte[] buildByteArray(String str) {
		return Utility.toByta(str);
	}

	@Override
	public char[] getStr20Value() {
		return Utility.toCharA(tuple, this.getTupleOffset());
	}

	@Override
	public double getValue() {
		return 0;
	}

}
