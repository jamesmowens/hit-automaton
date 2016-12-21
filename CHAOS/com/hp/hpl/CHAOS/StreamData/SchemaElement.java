/**
 * 
 */
package com.hp.hpl.CHAOS.StreamData;

import java.io.Serializable;

/**
 * @author wangson
 * 
 */
public abstract class SchemaElement implements Serializable, Cloneable {
	private String colName;
	private int dataType;
	private int offset;// without default columns
	private int length;
	private int tupleOffset;// with default columns
	transient protected byte[] tuple;

	public abstract byte[] buildByteArray(String str);

	public abstract double getValue();

	public abstract char[] getStr20Value();

	public byte[] extract() {
		return Utility.extractTuple(tuple, this.tupleOffset, this.length);
	}

	public byte[] extract(byte[] tuple) {
		return Utility.extractTuple(tuple, this.tupleOffset, this.length);
	}

	public SchemaElement(String colName, int dataType, int offset, int length) {
		super();
		this.tuple = null;
		this.colName = colName;
		this.dataType = dataType;
		this.offset = offset;
		this.length = length;
		this.tupleOffset = this.offset + Constant.TUPLE_HEAD_S;
	}

	public SchemaElement(SchemaElement srcSchemaElement, int newOffset) {
		super();
		this.colName = srcSchemaElement.getColName();
		this.dataType = srcSchemaElement.getDataType();
		this.offset = newOffset;
		this.length = srcSchemaElement.getLength();
		this.tupleOffset = this.offset + Constant.TUPLE_HEAD_S;
	}

	public String getColName() {
		return colName;
	}

	public int getDataType() {
		return dataType;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
//		return this.colName + " " + Integer.toString(this.dataType) + " "
//		+ Integer.toString(offset) + " " + Integer.toString(length)
//		+ "\t" + Integer.toString(tupleOffset);
		return this.colName;
	}

	public int getTupleOffset() {
		return tupleOffset;
	}

	public byte[] getTuple() {
		return tuple;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public void setOffset(int offset) {
		this.offset = offset;
		this.tupleOffset = this.offset + Constant.TUPLE_HEAD_S;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setTuple(byte[] tuple) {
		this.tuple = tuple;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
