/*Castagna 06/2011 >*/
package alice.tuprolog.event;

import java.util.EventListener;

import alice.tuprolog.core.event.logging.ExceptionEvent;

public interface ExceptionListener extends EventListener {
    public abstract void onException(ExceptionEvent e);
}
/**/