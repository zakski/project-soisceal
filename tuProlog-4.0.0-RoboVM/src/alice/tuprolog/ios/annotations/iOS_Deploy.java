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
public @interface iOS_Deploy {
	
	String forArch();
	
	/*
	 * 
	 * #simulator for iPhone 5/5C/4S
		forArch=x86

	   #simulator for iPhone 5S/SE/6/6Plus/6S/6SPlus/7/7Plus
        forArch=x86_64

       #deploy to iPhone 5/5C/4S
        forArch=thumbv7

       #deploy to iPhone 5S/SE/6/6Plus/6S/6SPlus/7/7Plus
I       forArch=arm64
	 * 
	 */

}
