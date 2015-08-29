package alice.tuprolog.event;

import java.util.EventListener;

import alice.tuprolog.core.event.io.ReadEvent;

public interface ReadListener extends EventListener{

	public void readCalled(ReadEvent event);
}
