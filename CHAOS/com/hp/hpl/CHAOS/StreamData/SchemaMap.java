package com.hp.hpl.CHAOS.StreamData;

import java.io.Serializable;
import java.util.Hashtable;

public class SchemaMap implements Serializable{
	// Each operator for now will have only one output stream.
	// May fill to multiple queues, but same content.
	// Each operator will have an instance of SchemaMap
	// The key of the hashtable is the child SchemaElement
	// The value of the hashtable is the parent SchemaElement
	// If one operator have multiple output queue, the queues will share the
	// same SchemaElement[]
	// If one child have multiple parent operators, each parent operator will
	// have a different SchemaMap.

	private static final long serialVersionUID = 1L;

	private Hashtable<SchemaElement, SchemaElement> sMap;

	public SchemaMap() {
		super();
		sMap = new Hashtable<SchemaElement, SchemaElement>();
	}

	public SchemaMap(Hashtable<SchemaElement, SchemaElement> map) {
		super();
		sMap = map;
	}

	public void addEntry(SchemaElement childSchemaElement,
			SchemaElement parentSchemaElement) {
		sMap.put(childSchemaElement, parentSchemaElement);
	}

	public int[] getOffsetLength(SchemaElement childSchemaElement) {
		if (sMap.containsKey(childSchemaElement))
			return new int[] { sMap.get(childSchemaElement).getTupleOffset(),
					sMap.get(childSchemaElement).getLength() };
		else
			return new int[] { 0, 0 };
	}

}
