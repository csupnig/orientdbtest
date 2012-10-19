package test;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoTest {

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
		
	}
	
	@Test
	public void testCrap() {
		DB mongoDB = DBProvider.getMongoDB();
		DBCollection coll = mongoDB.getCollection("Address");
		
		BasicDBObject doc = new BasicDBObject();
        doc.put("type", "Address");
        
		
		doc.put( "street", "asdf");
		doc.put( "city", "asdfasdfasdf" );
		doc.put( "zip", "2234" );
		coll.insert(doc);
		doc.get("id");
	}
	
	
}
