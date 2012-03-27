package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;

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
import com.gentics.lib.db.DB;
import com.gentics.lib.log.NodeLogger;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class NorbertTest {

	private static int addressesAnz = 100;
	private static int companiesAnz = 10000;
	private static int personsAnz = 1000000;
	
	private static final String[] perms = new String[]{"admin","soc","mar","emp","sal","cust","public","test"};
	
	private static final String[] names = new String[]{"Hans", "Luke", "Harri", "Peter", "Kopf", "Kässbauer", "Trobel", "Kunst"};
	
	private static final String[] cities = new String[]{"Vienna", "Rome", "Amsterdam", "New York", "Berlin", "San Francisco", "Wels", "Prag"};
	
	private static final String[] streets = new String[]{"blahstreet", "blehstreet", "klostreet", "pizzastreet", "hungerstreet"};
	
	private static List<Address> addresses = new ArrayList<Address>();
	
	private static List<Company> companies = new ArrayList<Company>();
	
	private static List<Person> persons = new ArrayList<Person>();
	
	private static CRConfigUtil conf;
	
	private static HashMap<String, String> props = new HashMap<String, String>();
	
	
	private static void init() {
		conf = new CRConfigUtil();
		conf.set("ds-handle.type", "jdbc");
		conf.set("ds-handle.driverClass", "com.mysql.jdbc.Driver");
		conf.set("ds-handle.url", "jdbc:mysql://qa-sandbox-2.office:3306/cr?user=node" ); 
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
	public static void main(String[] args) throws Exception{
		init();
		createData();
		insertCR();
		insertOrient();
		
		browseByTypeCR();
		browseByTypeOrient();
		
		browseByPermDirectCR();
		browseByPermDirectOrient();
		
		browseByPermInDirectCR();
		browseByPermInDirectOrient();
		
		PortalConnectorFactory.destroy();
		
		System.exit(0);
	}
	
	private static void createData() {
		for (int i=0; i<addressesAnz;i++) {
			addresses.add(createAddress());
		}
		
		for (int i=0; i<companiesAnz;i++) {
			companies.add(createCompany());
		}
		
		for (int i=0; i<personsAnz;i++) {
			persons.add(createPerson());
		}
		System.out.println("Create data finished.");
	}
	
	private static void insertCR() throws Exception{
		
		long startInsertO = System.currentTimeMillis();
		for (Address a : addresses) {
			insertCRAddress(a);
		}
		System.out.println("Finished addresses.");
		for (Company c : companies) {
			insertCRCompany(c);
		}
		System.out.println("Finished companies.");
		int i = 0;
		for (Person p : persons) {
			insertCRPerson(p);
			if (i++ % 10000 == 0) {
				System.out.print(".");
			}
		}
		System.out.println("Finished persons.");
		long endInsertO = System.currentTimeMillis();
		
		System.out.println("Insert CR took: " + (endInsertO - startInsertO) + "ms");
	}
	
	private static void insertOrient() throws Exception{
		
		long startInsertO = System.currentTimeMillis();
		for (Address a : addresses) {
			insertOrientAddress(a);
		}
		System.out.println("Finished addresses.");
		for (Company c : companies) {
			insertOrientCompany(c);
		}
		System.out.println("Finished companies.");
		int i = 0;
		for (Person p : persons) {
			insertOrientPerson(p);
			if (i++ % 10000 == 0) {
				System.out.print(".");
			}
		}
		System.out.println("Finished persons.");
		long endInsertO = System.currentTimeMillis();
		
		System.out.println("Insert Orient took: " + (endInsertO - startInsertO) + "ms");
	}
	
	
	private static Address createAddress() {
		Random r = new Random();
		Address a = new Address();
		a.street = streets[r.nextInt(streets.length)];
		a.zip = r.nextInt(10000);
		a.city = cities[r.nextInt(cities.length)];
		return a;
	}
	
	private static Company createCompany() {
		Random r = new Random();
		Company c = new Company();
		c.name = names[r.nextInt(names.length)];
		int permCount = r.nextInt(6);
		ArrayList<String> permissions = new ArrayList<String>();
		for (int j=0; j<=permCount; j++) {
			int permId = r.nextInt(7);
			permissions.add(perms[permId]);
		}
		c.permissions = permissions;
		c.address = addresses.get(r.nextInt(addresses.size()));
		return c;
	}
	
	private static Person createPerson() {
		Random r = new Random();
		Person p = new Person();
		
		p.name = names[r.nextInt(names.length)];
		p.surename = names[r.nextInt(names.length)];
		int permCount = r.nextInt(6);
		ArrayList<String> permissions = new ArrayList<String>();
		for (int j=0; j<=permCount; j++) {
			int permId = r.nextInt(7);
			permissions.add(perms[permId]);
		}
		p.permissions = permissions;
		p.company = companies.get(r.nextInt(companies.size()));
		return p;
	}
	
	private static void insertOrientAddress(Address address) {
		
		ODatabaseDocumentTx db = DBProvider.getDB();
		
		ODocument doc = new ODocument(db, "Address");
		
		doc.field( "street", address.street);
		doc.field( "city", address.city );
		doc.field( "zip", address.zip );
		doc.save();
		address.orid = doc.getIdentity();
		db.close();
	}
	
	private static void insertOrientCompany(Company company) {
		ODatabaseDocumentTx db = DBProvider.getDB();
		
		ODocument doc = new ODocument(db, "Company");
		
		doc.field( "name", company.name);
		doc.field( "address", new ODocument(company.address.orid) );
		doc.field( "permissions", company.permissions);
		doc.save();
		company.orid = doc.getIdentity();
		db.close();
	}
	
	private static void insertOrientPerson(Person person) {
		ODatabaseDocumentTx db = DBProvider.getDB();
		
		ODocument doc = new ODocument(db, "Person");
		
		doc.field( "name", person.name);
		doc.field( "surename", person.surename);
		doc.field( "company", new ODocument(person.company.orid) );
		doc.field( "permissions", person.permissions);
		doc.save();
		person.orid = doc.getIdentity();
		db.close();
	}
	
	
	private static void insertCRAddress(Address address) throws DatasourceException {
		WriteableDatasource ds = (WriteableDatasource)conf.getDatasource();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("obj_type", "14000");
		data.put("street", address.street);
		data.put("city", address.city);
		data.put("zip", address.zip);
		Changeable object = ds.create(data);
		ds.store(Collections.singleton(object));
		address.contentid = object.get("contentid").toString();
	}
	
	private static void insertCRCompany(Company company) throws DatasourceException {
		WriteableDatasource ds = (WriteableDatasource)conf.getDatasource();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("obj_type", "13000");
		data.put("name", company.name);
		data.put("permissions", company.permissions);
		data.put("address", company.address.contentid);
		Changeable object = ds.create(data);
		ds.store(Collections.singleton(object));
		company.contentid = object.get("contentid").toString();
	}

	private static void insertCRPerson(Person person) throws DatasourceException {
		WriteableDatasource ds = (WriteableDatasource)conf.getDatasource();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("obj_type", "12000");
		data.put("name", person.name);
		data.put("surename", person.surename);
		data.put("permissions", person.permissions);
		data.put("company", person.company.contentid);
		Changeable object = ds.create(data);
		ds.store(Collections.singleton(object));
		person.contentid = object.get("contentid").toString();
	}
	
	private static void browseByTypeOrient() {
		long start = System.currentTimeMillis();
		

		ODatabaseDocumentTx db = DBProvider.getDB();
		long i = 0;
		for(ODocument d : db.browseClass("Person")) {
			String name = d.field("name");
			String surename = d.field("surename");
			Collection<String> p= d.field("permissions");
			Object c = d.field("company");
			i++;
		}
		System.out.println("ORIENT BROWSED " + i+ " elements");
		db.close();
		long endInsertO = System.currentTimeMillis();
		System.out.println("Browse Orient took: " + (endInsertO - start) + "ms");
	}
	
	private static void browseByTypeCR() {
		long start = System.currentTimeMillis();
		WriteableDatasource ds = (WriteableDatasource)conf.getDatasource();
		
		try {
		
			Expression expression = PortalConnectorFactory.createExpression("object.obj_type == 12000");
			DatasourceFilter filter = ds.createDatasourceFilter(expression);
			Collection<Resolvable> col = (Collection<Resolvable>)ds.getResult(filter, new String[]{"name", "surename", "permissions", "company"});
			for(Resolvable r : col) {
				String name = (String) r.get("name");
				
			}
			System.out.println("CR BROWSED " + col.size()+ " elements");
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExpressionParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long endInsertO = System.currentTimeMillis();
		System.out.println("Browse CR took: " + (endInsertO - start) + "ms");
	}
	
	private static void browseByPermDirectOrient() {
		long start = System.currentTimeMillis();
		

		ODatabaseDocumentTx db = DBProvider.getDB();
		
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Person where permissions in ?");
		List<ODocument> result = db.command(query).execute(Arrays.asList(new String[]{"soc", "sal"}));
		
		
		int counter = 0;
		for(ODocument d : result) {
			String name = d.field("name");
			String surename = d.field("surename");
			Collection<String> p= d.field("permissions");
			Object c = d.field("company");
			counter ++;
		}
		System.out.println("Orient fetched " + counter + " elements.");
		db.close();
		long endInsertO = System.currentTimeMillis();
		System.out.println("Browse PERM Direct Orient took: " + (endInsertO - start) + "ms");
	}
	
	private static void browseByPermInDirectOrient() {
		long start = System.currentTimeMillis();
		

		ODatabaseDocumentTx db = DBProvider.getDB();
		
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Person where company.permissions in ?");
		List<ODocument> result = db.command(query).execute(Arrays.asList(new String[]{"soc", "sal"}));
		
		
		int counter = 0;
		for(ODocument d : result) {
			String name = d.field("name");
			String surename = d.field("surename");
			Collection<String> p= d.field("permissions");
			Object c = d.field("company");
			counter ++;
		}
		System.out.println("Orient fetched " + counter + " elements.");
		db.close();
		long endInsertO = System.currentTimeMillis();
		System.out.println("Browse PERM InDirect Orient took: " + (endInsertO - start) + "ms");
	}
	
	private static void browseByPermDirectCR() {
		long start = System.currentTimeMillis();
		WriteableDatasource ds = (WriteableDatasource)conf.getDatasource();
		
		try {
		
			Expression expression = PortalConnectorFactory.createExpression("object.obj_type == 12000 AND object.permissions CONTAINSONEOF ['soc', 'sal']");
			DatasourceFilter filter = ds.createDatasourceFilter(expression);
			Collection<Resolvable> col = (Collection<Resolvable>)ds.getResult(filter, new String[]{"name", "surename", "permissions", "company"});
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
		long endInsertO = System.currentTimeMillis();
		System.out.println("Browse PERM Direct CR took: " + (endInsertO - start) + "ms");
	}
	
	private static void browseByPermInDirectCR() {
		long start = System.currentTimeMillis();
		WriteableDatasource ds = (WriteableDatasource)conf.getDatasource();
		
		try {
		
			Expression expression = PortalConnectorFactory.createExpression("object.obj_type == 12000 AND object.company.permissions CONTAINSONEOF ['soc', 'sal']");
			DatasourceFilter filter = ds.createDatasourceFilter(expression);
			Collection<Resolvable> col = (Collection<Resolvable>)ds.getResult(filter, new String[]{"name", "surename", "permissions", "company"});
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
		long endInsertO = System.currentTimeMillis();
		System.out.println("Browse PERM InDirect CR took: " + (endInsertO - start) + "ms");
	}
}
