package test;

import java.util.Collection;

import org.bson.types.ObjectId;

import com.orientechnologies.orient.core.id.ORID;

public class Company {
	public Address address;
	public String name;
	public Collection<String> permissions;
	public ORID orid;
	public String contentid;
	public ObjectId mongoid;
}
