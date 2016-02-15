package dataTool;

import java.util.List;

import org.eclipse.jdt.core.dom.SimpleName;

public class Method {
	SimpleName name;
	List<DataNode> args;
	List<String> argTypes;
	String signature;
	public Method( SimpleName methodName, List<String> argTypes, List<DataNode> args ) {
		this.name = methodName;
		this.args = args;
		this.argTypes = argTypes;
		
		signature = methodName.toString() + "[";
		for( String s: argTypes ) {
			//Need to get type of param
			signature = signature + s + "]";
		}
	}
	
	public List<String> getArgTypes() {
		return argTypes;
	}
	
	public List<DataNode> getArgs() {
		return args;
	}
	
	public SimpleName getName() {
		return name;
	}

	public String getSignature() {
		return signature;
	}

	public void setArgs(List<DataNode> args) {
		this.args = args;
	}
	
}
