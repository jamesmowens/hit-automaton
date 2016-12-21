package com.hp.hpl.CHAOS.Queue;

import com.hp.hpl.CHAOS.State.HashState.TupleKey;

public class SingleReaderTupleKeyQueueArrayImp extends
		SingleReaderQueueArrayImpGeneric<TupleKey> {

	public SingleReaderTupleKeyQueueArrayImp() {
		super();
	}

	@Override
	public TupleKey[] addNewTypeVariableArray() {
		return new TupleKey[this.MAX];
	}

}
