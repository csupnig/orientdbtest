package test;

import java.net.UnknownHostException;

import com.gentics.cr.CRConfigUtil;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.orientechnologies.orient.client.remote.OEngineRemote;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class DBProvider {
	
	private static String connectstring = "remote:localhost/test";
	
	private static String name = "admin";
	private static String password = "admin";
	
	private static Mongo m;
	
	private static String mongoDb = "test";
	
	public static void init(CRConfigUtil conf) throws UnknownHostException {
		connectstring = "remote:" + conf.get("orientHost") + "/" + conf.get("orientDatabase");
		name = (String) conf.get("orientUser");
		password = (String) conf.get("orientPassword");
		
		m = new Mongo( (String) conf.get("mongoHost") );
		mongoDb = (String) conf.get("mongoDatabase");
		m.setWriteConcern(WriteConcern.SAFE);
	}
	
	public static ODatabaseDocumentTx getDB() {
		Orient.instance().registerEngine(new OEngineRemote());
		return ODatabaseDocumentPool.global().acquire(connectstring, name, password);
		
	}
	
	public static DB getMongoDB() {
		return m.getDB(mongoDb);
	}
}
