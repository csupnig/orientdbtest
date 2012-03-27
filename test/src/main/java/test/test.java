package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.datasource.WriteableDatasource;
import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.Expression;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
import com.gentics.api.lib.resolving.Changeable;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.cr.CRConfigUtil;
import com.orientechnologies.orient.client.remote.OEngineRemote;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class test {

	//private static int anz = 2 * 1000 * 1000;
	private static int anz = 10000;
	private static int anz2 = 100;
	
	private static final String[] perms = new String[]{"admin","soc","mar","emp","sal","cust","public","test"};
	
	private static final String[] names = new String[]{"Hans", "Luke", "Harri", "Peter", "Kopf", "Kässbauer", "Trobel", "Kunst"};
	
	private static CRConfigUtil conf;
	
	private static HashMap<String, String> props = new HashMap<String, String>();
	
	private static long insertTimeOrient = 0;
	private static long insertTimeCR = 0;
	
	private static long deleteTimeOrient = 0;
	private static long deleteTimeCR = 0;
	
	private static long selectTypeTimeOrient = 0;
	private static long selectTypeTimeCR = 0;
	
	private static long selectPermTimeOrient = 0;
	private static long selectPermTimeCR = 0;
	
	private static long selectPermComplexTimeOrient = 0;
	private static long selectPermComplexTimeCR = 0;
	
	private static long allOrient = 0;
	private static long allCR = 0;
	
	private static void init() {
		conf = new CRConfigUtil();
		conf.set("ds-handle.type", "jdbc");
		conf.set("ds-handle.driverClass", "com.mysql.jdbc.Driver");
		conf.set("ds-handle.url", "jdbc:mysql://localhost:3306/pcr?user=root" ); 
		/*conf.set("ds.cache", "true");
		conf.set("ds.cache.foreignlinkattributes", "true");
		conf.set("ds.cache.syncchecking", "true");*/
		props.put("type", "jdbc");
		props.put("driverClass", "com.mysql.jdbc.Driver");
		props.put("url", "jdbc:mysql://localhost:3306/pcr?user=root" );
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		allCR += deleteTimeCR += deleteAllCR();
		allOrient += deleteTimeOrient += deleteAllOrient();
		System.out.println("DELETE CR took: " + deleteTimeCR + "ms, Orient took: "+deleteTimeOrient+"ms");
		int cc = 0;
		for (int i = 0; i <= anz; i++) {
			createPerson(i);
			cc++;
			if(cc == 100000) {
				System.out.println("INSERTED: "+i);
				cc = 0;
			}
		}
		allCR +=insertTimeCR;
		allOrient +=insertTimeOrient;
		System.out.println("INSERT ("+anz+" elements) CR took: " + insertTimeCR + "ms, Orient took: "+insertTimeOrient+"ms");
		allCR +=selectTypeTimeCR += browseByTypeCR();
		allOrient +=selectTypeTimeOrient += browseByTypeOrient();
		System.out.println("BROWSE TYPE ("+anz+" elements) CR took: " + selectTypeTimeCR + "ms, Orient took: "+selectTypeTimeOrient+"ms");
		
		allCR+=selectPermTimeCR += browseByPermCR();
		allOrient+=selectPermTimeOrient += browseByPermOrient();
		System.out.println("BROWSE PERM CR took: " + selectPermTimeCR + "ms, Orient took: "+selectPermTimeOrient+"ms");
		
		insertTimeCR = 0;
		insertTimeOrient = 0;
		for (int i = 0; i <= anz2; i++) {
			createPerson(i);
		}
		allCR +=insertTimeCR;
		allOrient +=insertTimeOrient;
		System.out.println("INSERT ("+anz2+" elements) CR took: " + insertTimeCR + "ms, Orient took: "+insertTimeOrient+"ms");
		
		selectPermComplexTimeCR += browseByPermComplexCR();
		selectPermComplexTimeOrient += browseByPermComplexOrient();
		System.out.println("BROWSE PERM COMPLEX CR took: " + selectPermComplexTimeCR + "ms, Orient took: "+selectPermComplexTimeOrient+"ms");
		
		
		System.out.println("ALL CR took:" + (allCR) + "ms, Orient took: "+allOrient+"ms");
		PortalConnectorFactory.destroy();
	}
	
	private static void createPerson(int i) {
		Random r = new Random();
		int nameId = r.nextInt(7);
		int sNameId = r.nextInt(7);
		
		int permCount = r.nextInt(6);
		ArrayList<String> permissions = new ArrayList<String>();
		for (int j=0; j<=permCount; j++) {
			int permId = r.nextInt(7);
			permissions.add(perms[permId]);
		}
		insertTimeCR += insertCRPerson(names[nameId]+i, names[sNameId]+i, permissions);
		insertTimeOrient += insertOrientPerson(names[nameId]+i, names[sNameId]+i, permissions);
	}
	
	private static long insertOrientPerson(String name, String surename, Collection<String> permissions) {
		long start = System.currentTimeMillis();
		
		ODatabaseDocumentTx db = DBProvider.getDB();
		//ODatabaseDocumentTx db = new ODatabaseDocumentTx("remote:127.0.0.1:test").open("admin", "admin");
		// CREATE A NEW DOCUMENT AND FILL IT
		ODocument doc = new ODocument(db, "Person");
		doc.field("permissions", permissions);
		doc.field( "name", name );
		doc.field( "surename", surename );
		//doc.field( "city", new ODocument(db, "City").field("name","Rome").field("country", "Italy") );
		   
		// SAVE THE DOCUMENT
		doc.save();

		db.close();
		return (System.currentTimeMillis() - start);
	}
	
	private static long deleteAllOrient() {
		long start = System.currentTimeMillis();
		
		ODatabaseDocumentTx db = DBProvider.getDB();
		for(ODocument d : db.browseClass("Person")) {
			d.delete();
		}
		db.close();
		return (System.currentTimeMillis() - start);
	}
	
	private static long browseByTypeOrient() {
		long start = System.currentTimeMillis();
		

		ODatabaseDocumentTx db = DBProvider.getDB();
		for(ODocument d : db.browseClass("Person")) {
			String name = d.field("name");
		}
		db.close();
		return (System.currentTimeMillis() - start);
	}
	
	private static long browseByPermOrient() {
		long start = System.currentTimeMillis();
		

		ODatabaseDocumentTx db = DBProvider.getDB();
		
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Person where permissions in ?");
		List<ODocument> result = db.command(query).execute(Arrays.asList(new String[]{"soc", "sal"}));
		
		
		int counter = 0;
		for(ODocument d : result) {
			String name = d.field("name");
			counter ++;
		}
		System.out.println("Orient fetched " + counter + " elements.");
		db.close();
		return (System.currentTimeMillis() - start);
	}
	
	private static long browseByPermComplexOrient() {
		long start = System.currentTimeMillis();
		

		ODatabaseDocumentTx db = DBProvider.getDB();
		
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Person where permissions in ? AND name like ?");
		List<ODocument> result = db.command(query).execute(Arrays.asList(new String[]{"soc", "sal"}), "%Kopf%");
		
		int counter = 0;
		for(ODocument d : result) {
			String name = d.field("name");
			counter ++;
		}
		System.out.println("Orient fetched " + counter + " elements.");
		db.close();
		return (System.currentTimeMillis() - start);
	}
	
	private static long insertCRPerson(String name, String surename, Collection<String> permissions) {
		long start = System.currentTimeMillis();
		//WriteableDatasource ds = (WriteableDatasource) conf.getDatasource();
		WriteableDatasource ds = (WriteableDatasource) PortalConnectorFactory.createWriteableDatasource(props);
		HashMap<String, Object> obj = new HashMap<String, Object>();
		obj.put("obj_type", 12000);
		obj.put("name", name);
		obj.put("surename", surename);
		obj.put("permissions", permissions);
		try {
			Changeable c = ds.create(obj);
			ds.insert(Collections.singleton(c));
		} catch (DatasourceException e) {
			e.printStackTrace();
		}
		return (System.currentTimeMillis() - start);
	}
	
	private static long deleteAllCR() {
		long start = System.currentTimeMillis();
		Datasource d = conf.getDatasource();
		WriteableDatasource ds = (WriteableDatasource) PortalConnectorFactory.createWriteableDatasource(props);
		
		try {
		
			Expression expression = PortalConnectorFactory.createExpression("object.obj_type == 12000");
			DatasourceFilter filter = ds.createDatasourceFilter(expression);
			ds.delete(filter);
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExpressionParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (System.currentTimeMillis() - start);
	}
	
	private static long browseByTypeCR() {
		long start = System.currentTimeMillis();
		WriteableDatasource ds = (WriteableDatasource) PortalConnectorFactory.createWriteableDatasource(props);
		
		try {
		
			Expression expression = PortalConnectorFactory.createExpression("object.obj_type == 12000");
			DatasourceFilter filter = ds.createDatasourceFilter(expression);
			Collection<Resolvable> col = (Collection<Resolvable>)ds.getResult(filter, new String[]{"name", "surename", "permissions"});
			for(Resolvable r : col) {
				String name = (String) r.get("name");
			}
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExpressionParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (System.currentTimeMillis() - start);
	}
	
	private static long browseByPermCR() {
		long start = System.currentTimeMillis();
		WriteableDatasource ds = (WriteableDatasource) PortalConnectorFactory.createWriteableDatasource(props);
		
		try {
		
			Expression expression = PortalConnectorFactory.createExpression("object.permissions CONTAINSONEOF ['soc', 'sal']");
			DatasourceFilter filter = ds.createDatasourceFilter(expression);
			Collection<Resolvable> col = (Collection<Resolvable>)ds.getResult(filter, new String[]{"name", "surename", "permissions"});
			int counter = 0;
			for(Resolvable d : col) {
				String name = (String) d.get("name");
				counter ++;
			}
			System.out.println("CR fetched " + counter + " elements.");
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExpressionParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (System.currentTimeMillis() - start);
	}
	
	private static long browseByPermComplexCR() {
		long start = System.currentTimeMillis();
		WriteableDatasource ds = (WriteableDatasource) PortalConnectorFactory.createWriteableDatasource(props);
		
		try {
		
			Expression expression = PortalConnectorFactory.createExpression("object.permissions CONTAINSONEOF ['soc', 'sal'] AND object.name LIKE '%Kopf%'");
			DatasourceFilter filter = ds.createDatasourceFilter(expression);
			//Collection<Resolvable> col = (Collection<Resolvable>)ds.getResult(filter, new String[]{"name", "surename", "permissions"});
			Collection<Resolvable> col = (Collection<Resolvable>)ds.getResult(filter, new String[]{});
			int counter = 0;
			for(Resolvable d : col) {
				String name = (String) d.get("name");
				counter ++;
			}
			System.out.println("CR fetched " + counter + " elements.");
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExpressionParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (System.currentTimeMillis() - start);
	}

}
