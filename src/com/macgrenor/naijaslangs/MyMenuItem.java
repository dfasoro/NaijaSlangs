package com.macgrenor.naijaslangs;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

public class MyMenuItem extends ApplicationMenuItem {
	public MyMenuItem() {
		super(10);
	}

	public Object run(Object context) {
		String slang = context.toString();
		String meaning = NaijaSlangs.getMeaning(slang);
		String message = slang.toUpperCase() + ": " + (meaning == null ? "Not Found" : meaning);
		
		UiApplication.getUiApplication().pushGlobalScreen(new Dialog(Dialog.D_OK, message, 0, null, Dialog.GLOBAL_STATUS), 100, UiApplication.GLOBAL_MODAL);
		UiApplication.getUiApplication().requestBackground();
		
		return null;
	}

	public String toString() {
		return "See Meaning";
	}
}
