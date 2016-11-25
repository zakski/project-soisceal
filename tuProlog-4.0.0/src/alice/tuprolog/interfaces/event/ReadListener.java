package alice.tuprolog.interfaces.event;

import java.util.EventListener;

import alice.tuprolog.event.ReadEvent;

public interface ReadListener extends EventListener{

	public void readCalled(ReadEvent event);
}
