/*Castagna 06/2011 >*/
package com.szadowsz.gospel.core.event;

import com.szadowsz.gospel.core.event.logging.ExceptionEvent;

import java.util.EventListener;

public interface ExceptionListener extends EventListener {
    public abstract void onException(ExceptionEvent e);
}
/**/