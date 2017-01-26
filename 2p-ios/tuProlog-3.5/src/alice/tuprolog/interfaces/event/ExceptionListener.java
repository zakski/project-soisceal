/*Castagna 06/2011 >*/
package alice.tuprolog.interfaces.event;

import java.util.EventListener;

import alice.tuprolog.event.ExceptionEvent;

public interface ExceptionListener extends EventListener {
    public abstract void onException(ExceptionEvent e);
}
/**/