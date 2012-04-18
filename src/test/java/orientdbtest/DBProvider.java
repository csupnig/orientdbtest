package orientdbtest;

import com.orientechnologies.orient.client.remote.OEngineRemote;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.command.OCommandExecutor;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.sql.OSQLEngine;
import com.orientechnologies.orient.core.sql.functions.OSQLFunctionAbstract;

public class DBProvider {
	
	public static ODatabaseDocumentTx getDB() {
		Orient.instance().registerEngine(new OEngineRemote());
		
		ODatabaseDocumentTx db = ODatabaseDocumentPool.global().acquire("remote:qa-sandbox-2.office/mil", "admin", "admin");
		
		return db;
	}
}
