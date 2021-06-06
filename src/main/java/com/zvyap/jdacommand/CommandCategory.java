package com.zvyap.jdacommand;


public class CommandCategory {
	
	private final String name;
	
	public CommandCategory(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof String)) {
			return false;
		}
		return ((String)obj).equals(name);
	}
}
