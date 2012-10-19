package test;

import org.bson.types.ObjectId;

import com.orientechnologies.orient.core.id.ORID;

public class Address {
	public String street;
	public String city;
	public int zip;
	public ORID orid;
	public String contentid;
	public ObjectId mongoid;
}
