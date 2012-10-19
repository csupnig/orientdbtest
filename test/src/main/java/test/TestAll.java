package test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class TestAll {

	private  int addressesAnz = 100;
	private  int companiesAnz = 1000;
	private  int personsAnz = 1000;
	
	private  final String[] perms = new String[]{"admin","soc","mar","emp","sal","cust","public","test"};
	
	private  final String[] names = new String[]{"Hans", "Luke", "Harri", "Peter", "Kopf", "K�ssbauer", "Trobel", "Kunst"};
	
	private  final String[] cities = new String[]{"Vienna", "Rome", "Amsterdam", "New York", "Berlin", "San Francisco", "Wels", "Prag"};
	
	private  final String[] streets = new String[]{"blahstreet", "blehstreet", "klostreet", "pizzastreet", "hungerstreet"};
	
	private  List<Address> addresses = new ArrayList<Address>();
	
	private  List<Company> companies = new ArrayList<Company>();
	
	private  List<Person> persons = new ArrayList<Person>();
	
	private  CRConfigUtil conf;
	
	
	
	@Before
	public void init() throws UnknownHostException {
		conf = new CRConfigUtil();
		conf.set("ds-handle.type", "jdbc");
		conf.set("ds-handle.driverClass", "com.mysql.jdbc.Driver");
		conf.set("ds-handle.url", "jdbc:mysql://localhost:3306/pcr?user=root" );
		
		conf.set("orientHost", "localhost");
		conf.set("orientDatabase", "test");
		conf.set("orientUser", "admin");
		conf.set("orientPassword", "admin");
		
		conf.set("mongoHost", "localhost");
		conf.set("mongoDatabase", "test");
		DBProvider.init(conf);
		createData();
	}
	
	@After
	public  void tearDown() {
		PortalConnectorFactory.destroy();
	}
	
	public void doIt() throws Exception {
		init();
		
		insertCR();
		insertOrient();
		insertMongo();
		
		browseByTypeCR();
		browseByTypeOrient();
		browseByTypeMongo();
		
		browseByPermDirectCR();
		browseByPermDirectOrient();
		browseByPermDirectMongo();
		
		browseByPermInDirectCR();
		browseByPermInDirectOrient();
		browseByPermInDirectMongo();
		
		System.out.println("Test Done. Starting Cleanup.");
		deleteFromCR();
		deleteFromOrient();
		deleteFromMongo();
		
		PortalConnectorFactory.destroy();
		System.out.println("Cleanup Done.");
		System.exit(0);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		TestAll a = new TestAll();
		a.doIt();
	}
	
	private void createData() {
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
	
	@Test
	public  void insertMongo() throws Exception{
		
		long startInsertO = System.currentTimeMillis();
		for (Address a : addresses) {
			insertMongoAddress(a);
		}
		System.out.println("Finished addresses.");
		for (Company c : companies) {
			insertMongoCompany(c);
		}
		System.out.println("Finished companies.");
		int i = 0;
		for (Person p : persons) {
			insertMongoPerson(p);
			if (i++ % 10000 == 0) {
				System.out.print(".");
			}
		}
		System.out.println("Finished persons.");
		long endInsertO = System.currentTimeMillis();
		
		System.out.println("Insert Mongo took: " + (endInsertO - startInsertO) + "ms");
	}
	
	private void insertMongoAddress(Address address) {
		
		DB mongoDB = DBProvider.getMongoDB();
		DBCollection coll = mongoDB.getCollection("Address");
		
		BasicDBObject doc = new BasicDBObject();
        doc.put("type", "Address");
        
		
		doc.put( "street", address.street);
		doc.put( "city", address.city );
		doc.put( "zip", address.zip );
		coll.insert(doc);
		address.mongoid = (ObjectId) doc.get("_id");
	}
	
	private  void insertMongoCompany(Company company) {

		DB mongoDB = DBProvider.getMongoDB();
		DBCollection coll = mongoDB.getCollection("Company");
		DBObject address = coll.findOne(new BasicDBObject("_id", company.address.mongoid));
		
		BasicDBObject doc = new BasicDBObject();
        doc.put("type", "Company");
		
		doc.put( "name", company.name);
		doc.put( "address", address );
		doc.put( "permissions", company.permissions);
		coll.insert(doc);
		company.mongoid =  (ObjectId) doc.get("_id");
	}
	
	private  void insertMongoPerson(Person person) {
		DB mongoDB = DBProvider.getMongoDB();
		DBCollection coll = mongoDB.getCollection("Person");
		DBObject company = coll.findOne(new BasicDBObject("_id", person.company.mongoid));
		
		BasicDBObject doc = new BasicDBObject();
        doc.put("type", "Person");
		
		doc.put( "name", person.name);
		doc.put( "surename", person.surename);
		doc.put( "company", company );
		doc.put( "permissions", person.permissions);
		coll.insert(doc);
		person.mongoid = (ObjectId) doc.get("_id");
		
	}
	
	
	@Test
	public  void insertCR() throws Exception{
		
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
	
	@Test
	public  void insertOrient() throws Exception{
		
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
	
	
	private  Address createAddress() {
		Random r = new Random();
		Address a = new Address();
		a.street = streets[r.nextInt(streets.length)];
		a.zip = r.nextInt(10000);
		a.city = cities[r.nextInt(cities.length)];
		return a;
	}
	
	private  Company createCompany() {
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
	
	private  Person createPerson() {
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
	
	
	private  void insertOrientAddress(Address address) {
		
		ODatabaseDocumentTx db = DBProvider.getDB();
		
		ODocument doc = new ODocument("Address");
		
		doc.field( "street", address.street);
		doc.field( "city", address.city );
		doc.field( "zip", address.zip );
		doc.save();
		address.orid = doc.getIdentity();
		db.close();
	}
	
	
	private  void insertOrientCompany(Company company) {
		ODatabaseDocumentTx db = DBProvider.getDB();
		
		ODocument doc = new ODocument("Company");
		
		doc.field( "name", company.name);
		doc.field( "address", new ODocument(company.address.orid) );
		doc.field( "permissions", company.permissions);
		doc.save();
		company.orid = doc.getIdentity();
		db.close();
	}
	
	
	private  void insertOrientPerson(Person person) {
		ODatabaseDocumentTx db = DBProvider.getDB();
		
		ODocument doc = new ODocument("Person");
		
		doc.field( "name", person.name);
		doc.field( "surename", person.surename);
		doc.field( "company", new ODocument(person.company.orid) );
		doc.field( "permissions", person.permissions);
		doc.save();
		person.orid = doc.getIdentity();
		db.close();
	}
	
	
	private  void insertCRAddress(Address address) throws DatasourceException {
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
	
	
	private  void insertCRCompany(Company company) throws DatasourceException {
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

	private  void insertCRPerson(Person person) throws DatasourceException {
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
	
	@Test
	public  void browseByTypeOrient() {
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
	
	@Test
	public  void browseByTypeMongo() {
		long start = System.currentTimeMillis();
		DB mongoDB = DBProvider.getMongoDB();
		DBCollection coll = mongoDB.getCollection("Person");

		DBCursor cursor = coll.find();
		
		
		long i = 0;
		while(cursor.hasNext()) {
			DBObject d = cursor.next();
			String name = (String) d.get("name");
			String surename = (String) d.get("surename");
			Collection<String> p= (Collection<String>) d.get("permissions");
			Object c = d.get("company");
			i++;
		}
		System.out.println("MONGO BROWSED " + i+ " elements");
		cursor.close();
		long endInsertO = System.currentTimeMillis();
		System.out.println("Browse MONGO took: " + (endInsertO - start) + "ms");
	}
	
	@Test
	public  void browseByTypeCR() {
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
	
	@Test
	public  void browseByPermDirectOrient() {
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
	
	@Test
	public  void browseByPermDirectMongo() {
		long start = System.currentTimeMillis();
		DB mongoDB = DBProvider.getMongoDB();
		DBCollection coll = mongoDB.getCollection("Person");

		BasicDBObject query = new BasicDBObject();
		query.put("permissions", new BasicDBObject("$in", new String[]{"soc", "sal"}));
		
		DBCursor cursor = coll.find(query);
		
		
		long i = 0;
		while(cursor.hasNext()) {
			DBObject d = cursor.next();
			String name = (String) d.get("name");
			String surename = (String) d.get("surename");
			Collection<String> p= (Collection<String>) d.get("permissions");
			Object c = d.get("company");
			i++;
		}
		System.out.println("MONGO BROWSED " + i+ " elements");
		cursor.close();
		long endInsertO = System.currentTimeMillis();
		System.out.println("Browse PERM DIrect Mongo took: " + (endInsertO - start) + "ms");
	}
	
	@Test
	public  void browseByPermInDirectOrient() {
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
	
	@Test
	public  void browseByPermInDirectMongo() {
		long start = System.currentTimeMillis();
		DB mongoDB = DBProvider.getMongoDB();
		DBCollection coll = mongoDB.getCollection("Person");

		BasicDBObject query = new BasicDBObject();
		query.put("company.permissions", new BasicDBObject("$in", new String[]{"soc", "sal"}));
		
		DBCursor cursor = coll.find(query);
		
		
		long i = 0;
		while(cursor.hasNext()) {
			DBObject d = cursor.next();
			String name = (String) d.get("name");
			String surename = (String) d.get("surename");
			Collection<String> p= (Collection<String>) d.get("permissions");
			Object c = d.get("company");
			i++;
		}
		System.out.println("MONGO BROWSED " + i+ " elements");
		cursor.close();
		long endInsertO = System.currentTimeMillis();
		System.out.println("Browse PERM InDirect Mongo took: " + (endInsertO - start) + "ms");
	}
	
	@Test
	public  void browseByPermDirectCR() {
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
	
	@Test
	public  void browseByPermInDirectCR() {
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
	
	@Test
	public  void deleteFromCR() {
		WriteableDatasource ds = (WriteableDatasource)conf.getDatasource();
		
		try {
		
			Expression expression = PortalConnectorFactory.createExpression("object.obj_type CONTAINSONEOF [12000, 13000, 14000] ");
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
	}
	
	@Test
	public  void deleteFromMongo() {
		DB mongoDB = DBProvider.getMongoDB();
		DBCollection coll = mongoDB.getCollection("Person");
		coll.drop();
		coll = mongoDB.getCollection("Address");
		coll.drop();
		coll = mongoDB.getCollection("Company");
		coll.drop();
	}
	
	@Test
	public  void deleteFromOrient() {
		ODatabaseDocumentTx db = DBProvider.getDB();
		
		int recordsUpdated = db.command(
				  new OCommandSQL("delete from Person")).execute();
		recordsUpdated = db.command(
				  new OCommandSQL("delete from Address")).execute();
		recordsUpdated = db.command(
				  new OCommandSQL("delete from Company")).execute();
		
		
		db.close();
	}
}
