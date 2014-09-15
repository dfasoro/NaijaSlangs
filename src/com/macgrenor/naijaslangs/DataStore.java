package com.macgrenor.naijaslangs;

import java.util.Hashtable;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;

public class DataStore {
	private static long SLANGS_DATA_KEY = 0x6baa0d030491335dL; //com.macgrenor.naijaslangs.SLANGS_DATA
	private static long SLANGS_DATA_RUNTIME = 0x67f496746fa5db25L; //com.macgrenor.naijaslangs.SLANGS_DATA_RUNTIME
	
	
	private static PersistentObject slangPersist = null;
	private static synchronized PersistentObject getSlangPersistObject() {
		if (slangPersist == null) slangPersist = PersistentStore.getPersistentObject(SLANGS_DATA_KEY);
		return slangPersist;
	}
	protected static Hashtable loadSlangData() {
		Hashtable ret = (Hashtable) getSlangPersistObject().getContents();
		putSlangDataRuntime(ret);
		return ret;
	}
	protected static void putSlangData(Hashtable data) {
		getSlangPersistObject().setContents(data);
		putSlangDataRuntime(data);
		saveSlangData();
	}
	protected static void saveSlangData() {
		getSlangPersistObject().commit();
	}
	
	
	protected static Hashtable loadSlangDataRuntime() {
		return (Hashtable) RuntimeStore.getRuntimeStore().get(SLANGS_DATA_RUNTIME);
	}
	private static void putSlangDataRuntime(Hashtable data) {
		if (data == null) RuntimeStore.getRuntimeStore().remove(SLANGS_DATA_RUNTIME);
		else RuntimeStore.getRuntimeStore().replace(SLANGS_DATA_RUNTIME, data);
	}
}
