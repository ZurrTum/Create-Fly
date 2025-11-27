package com.zurrtum.create.compat.computercraft.events;

import com.zurrtum.create.content.trains.signal.SignalBlockEntity;

public class SignalStateChangeEvent implements ComputerEvent {

	public SignalBlockEntity.SignalState state;

	public SignalStateChangeEvent(SignalBlockEntity.SignalState state) {
		this.state = state;
	}

}
