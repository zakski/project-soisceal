package com.szadowsz.gospel.core.event;

import com.szadowsz.gospel.core.event.io.ReadEvent;

import java.util.EventListener;

public interface ReadListener extends EventListener{

	public void readCalled(ReadEvent event);
}
