package alice.tuprolog.ios;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.foundation.NSStringEncoding;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationDelegateAdapter;
import org.robovm.apple.uikit.UIApplicationLaunchOptions;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIWindow;

import alice.tuprolog.Prolog;
import alice.tuprolog.ios.annotations.RoboVM_Required;
import alice.tuprolog.ios.annotations.iOS_Deploy;

@RoboVM_Required
@iOS_Deploy(forArch = "x86") //META-DATA FOR DEPLOYMENT
public class Main extends UIApplicationDelegateAdapter 
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	public static final double cornerRadius = 5.0; 
	public static final double borderWidth = 1.0;
	
    private UIWindow window;
    private IOSViewController rootViewController;
    private I_URL_Manager URL_Manager;
    
    private Prolog tuProlog;

    @Override
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) 
    {
        rootViewController = new IOSViewController();
        window = new UIWindow(UIScreen.getMainScreen().getBounds());
        window.setRootViewController(rootViewController);
        window.makeKeyAndVisible();
        
        if(URL_Manager == null)
        	URL_Manager = new URL_Manager(rootViewController);
        
        return true;
    }
    
    @Override
	public boolean handleOpenURL(UIApplication application, NSURL url)
	{
		String urlString = NSURL.decodeURLString(url.getAbsoluteString(), NSStringEncoding.UTF8);
		URL_Manager.handleRequest(urlString);
		
		return true;
	}

    public static void main(String[] args) 
    {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(args, null, Main.class);
        pool.close();
    }

	public Prolog getTuProlog() 
	{
		if(tuProlog == null)
			tuProlog = new Prolog();
		
		return tuProlog;
	}
}