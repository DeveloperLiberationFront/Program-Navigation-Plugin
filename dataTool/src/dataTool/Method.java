package dataTool;

import java.util.List;

import org.eclipse.jdt.core.dom.SimpleName;

public class Method {
	SimpleName name;
	List<String> args;
	String signature;
	public Method( SimpleName methodName, List<String> args ) {
		this.name = methodName;
		this.args = args;
		
		signature = methodName.toString() + "[";
		for( String s: args ) {
			signature = signature + s + "]";
		}
	}
	
	public List<String> getArgs() {
		return args;
	}
	
	public SimpleName getName() {
		return name;
	}

	public String getSignature() {
		return signature;
	}
	
}
