package com.macgrenor.naijaslangs;

import net.rim.device.api.ui.UiApplication;

public class NaijaSlangsUI extends UiApplication {
	public NaijaSlangsUI() {
		 pushScreen(new HomeScreen());
	}

	public boolean requestClose() {
		requestBackground();
		return false;
	}
	
}
