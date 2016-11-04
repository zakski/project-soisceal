package alice.tuprolog.ios.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Alberto Sita
 * 
 */

@Target(value = { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RoboVM_Required {
	
	//marker
	
}
