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

/**
 * @author Andrea Muccioli
 *
 */
public class AuthCheckInterceptor {
	
	@EJB
	private it.unibo.alice.tuprolog.ws.security.SecurityManager security;
	
	
	
	/**
	 * Interceptor method that check the authentication token before the execution
	 * of the business logic. Only methods with the @RequiresAuth annotation are
	 * affected by the interceptor.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	@AroundInvoke
	public Object checkAuth(InvocationContext ctx) throws Exception {
		Annotation[] ann = ctx.getMethod().getAnnotationsByType(RequiresAuth.class);
		if (ann.length > 0)
		{
			Annotation[][] a = ctx.getMethod().getParameterAnnotations();
			int index = -1;
			for(int i = 0; i < a.length; i++) {
				if (a[i].length > 0) {
		            for (Annotation anno : a[i])
		            {
		            	if (anno.annotationType().getSimpleName().equals(HeaderParam.class.getSimpleName()))
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
				if (!jwtClaims.getClaimValue("role").equals("configurator"))
					return Response.status(Status.UNAUTHORIZED)
							.entity(""+Status.UNAUTHORIZED.getStatusCode()+": user's role can't access this service").build();
			} catch (InvalidJwtException e) {
				return Response.status(Status.FORBIDDEN).entity(""+Status.FORBIDDEN.getStatusCode()+": Access Denied!").build();
			}
		}
		return ctx.proceed();
	}

}
