package it.unibo.alice.tuprolog.ws.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import it.unibo.alice.tuprolog.ws.security.Role;

/**
 * This annotation is used to specify on a method the required authorization
 * needed to execute it. It contains a field roleRequired (default Role.CONFIGURATOR),
 * to set the minimum role required. On its own the annotation has no effect, but
 * the RetentionPolicy is at RUNTIME and so other components can retrieve RequiresAuth at runtime and
 * use it to perform the actual verification of the credentials (see AuthCheckInterceptor).
 * 
 * @author Andrea Muccioli
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresAuth {
	public Role roleRequired() default Role.CONFIGURATOR;
}
