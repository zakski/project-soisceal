/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package alice.tuprolog.event;

import java.util.*;

import alice.tuprolog.Engine;

/**
 *  This class defines an internal event
 *  generated by the prolog core
 *
 */

public class SpyEvent extends EventObject
{
	private static final long serialVersionUID = 1L;
	
    /**
	 * event description
	 */
	
    private String msg;
    
    /**
	 * @uml.property  name="env"
	 * @uml.associationEnd  
	 */
    
    private Engine env;

    public SpyEvent(Object source, String msg_) 
    {
        super(source);
        env = null;
        msg = msg_;
    }

    public SpyEvent(Object source, Engine env_, String msg_)
    {
        super(source);
        env = env_;
        msg = msg_;
    }

    public String getMsg()
    {
        return msg;
    }

    public Engine getSnapshot() 
    {
        return env;
    }

    public String toString()
    {
        return msg + ((env == null) ? "" : env.toString());
    }
}