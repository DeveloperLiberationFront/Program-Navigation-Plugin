package dataTool.ui;

import java.util.ArrayList;

public class DataLink {
	private String name;
	private String location;
	
	final public static String INVALID = "Invalid";
	final public static String INVALID_DESC = "Function is not in scope project.";
	
	public DataLink(String name, String location) {
		this.name = name;
		this.location = location;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getText() {
		if(!name.equals(INVALID)) {
			return "<a>"+name+"</a>";
		}
		return name;
	}
}
