package orientdbtest;

import java.util.Collection;

import com.orientechnologies.orient.core.id.ORID;

public class Company {
	public Address address;
	public String name;
	public Collection<String> permissions;
	public ORID orid;
	public String contentid;
}
