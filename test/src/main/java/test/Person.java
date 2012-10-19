package test;

import java.util.Collection;

import org.bson.types.ObjectId;

import com.orientechnologies.orient.core.id.ORID;

public class Person {
	public String name;
	public String surename;
	public Collection<String> permissions;
	public Company company;
	public ORID orid;
	public String contentid;
	public ObjectId mongoid;
	
}
