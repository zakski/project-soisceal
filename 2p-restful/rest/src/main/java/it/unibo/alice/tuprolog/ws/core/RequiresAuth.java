package it.unibo.alice.tuprolog.ws.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import it.unibo.alice.tuprolog.ws.security.Role;

/**
 * @author Andrea Muccioli
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresAuth {
	public Role roleRequired() default Role.CONFIGURATOR;
}
