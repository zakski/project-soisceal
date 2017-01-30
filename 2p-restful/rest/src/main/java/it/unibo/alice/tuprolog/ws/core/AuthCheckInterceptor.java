package it.unibo.alice.tuprolog.ws.core;

import java.lang.annotation.Annotation;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;

import it.unibo.alice.tuprolog.ws.security.Role;

/**
 * This component is an interceptor class needed to verify user credentials
 * in a transparent way in other components. Its functionalities are tightly tied to
 * the use of the annotation @RequiresAuth .
 * 
 * @author Andrea Muccioli
 *
 */
public class AuthCheckInterceptor {
	
	@EJB
	private it.unibo.alice.tuprolog.ws.security.SecurityManager security;
	
	
	
	/**
	 * Interceptor method that checks the authentication token before the execution
	 * of the business logic. It verifies if the user has a Role of equal or higher level than the one
	 * specified in the @RequiresAuth annotation. Only methods with the @RequiresAuth annotation are
	 * affected by this interceptor.
	 * 
	 * @param ctx
	 * @return returns control to the intercepted method if the verification had success, returns a
	 * FORBIDDEN (403) or UNAUTHORIZED (401) Response otherwise. 
	 * @throws Exception
	 */
	@AroundInvoke
	public Object checkAuth(InvocationContext ctx) throws Exception {
		RequiresAuth[] ann = ctx.getMethod().getAnnotationsByType(RequiresAuth.class);
		if (ann.length > 0)
		{
			Annotation[][] a = ctx.getMethod().getParameterAnnotations();
			int index = -1;
			//for each parameter
			for(int i = 0; i < a.length; i++) {
				if (a[i].length > 0) {
					//for each annotation of the current parameter.
		            for (Annotation anno : a[i])
		            {
		            	if (anno instanceof HeaderParam)
		            		if (ctx.getMethod().getParameters()[i].getAnnotation(HeaderParam.class).value().equals("token"))
		            			index = i;
		            }
				}
			}
			
			String token = (String)ctx.getParameters()[index];
			if (token == null) {
				return Response.status(Status.FORBIDDEN).entity(""+Status.FORBIDDEN.getStatusCode()+": Access Denied!").build();
			}
			JwtClaims jwtClaims = null;
			try {
				jwtClaims = security.decryptAndVerifyToken(token);
				
				Role required = ann[0].roleRequired();
				Role userRole = Role.valueOf(jwtClaims.getClaimValue("role").toString().toUpperCase());
				if (required.compareTo(userRole) > 0)
					return Response.status(Status.UNAUTHORIZED)
							.entity(""+Status.UNAUTHORIZED.getStatusCode()+": user's role can't access this service").build();
			} catch (InvalidJwtException e) {
				return Response.status(Status.FORBIDDEN).entity(""+Status.FORBIDDEN.getStatusCode()+": Access Denied!").build();
			}
		}
		return ctx.proceed();
	}

}
