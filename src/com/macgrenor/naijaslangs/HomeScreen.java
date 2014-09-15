package com.macgrenor.naijaslangs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

/**
 * A class extending the MainScreen class, which provides default standard
 * behavior for BlackBerry GUI applications.
 */
public final class HomeScreen extends MainScreen
{
    /**
     * Creates a new HomeScreen object
     */
    public HomeScreen()
    {        
        // Set the displayed title of the screen       
        setTitle("Naija Slangs");
        
        String message = "Find meanings to complex Naija word codifications. We know how we do it: LWKMD, LWTMTLS, ISSORAI, ISSOKAY. \n" + 
        		"Whenever a Naija Slang is found anywhere on your phone, it is highlighted. Click on it and Click on See Meaning. Voila.\n" +
        		"You can also contribute to the growing database. Contact the Developer on twitter: @dfasoro http://macgrenor.com " + 
        		"\nSend me a word and it's meaning by mentioning me on twitter and I will add it asap.";
        
        ActiveAutoTextEditField fld = new ActiveAutoTextEditField(null, getMessage(message), TextField.DEFAULT_MAXCHARS, 
        		TextField.READONLY | TextField.USE_ALL_WIDTH | TextField.USE_ALL_HEIGHT | TextField.FOCUSABLE);
        
        add(fld);
        
        try {
			InputStream in = getClass().getResourceAsStream("/img/icon_128.png");
			Bitmap bitmap = Bitmap.createBitmapFromBytes(IOUtilities.streamToBytes(in, 10*1024), 0, -1, 1);
			in.close();
			getMainManager().setBackground(BackgroundFactory.createBitmapBackground(bitmap, 
					Background.POSITION_X_CENTER, Background.POSITION_Y_CENTER, Background.REPEAT_SCALE_TO_FIT));
			bitmap = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
    }
    
    private String getMessage(String defaultMessage) {
    	Hashtable Slangs = DataStore.loadSlangDataRuntime();
    	if (Slangs == null) return defaultMessage;
		
    	String data = (String)Slangs.get(NaijaSlangs.INFO_KEY);
		if (data != null) return data;
		else return defaultMessage;
    }
}
