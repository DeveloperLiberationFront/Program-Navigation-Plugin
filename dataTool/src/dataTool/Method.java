package dataTool;

import java.util.List;

import org.eclipse.jdt.core.dom.SimpleName;

public class Method {
	SimpleName name;
	List<DataNode> args;
	public Method( SimpleName methodName ) {
		this.name = methodName;
	}
	
	public List<DataNode> getArgs() {
		return args;
	}
	public void setArgs( List<DataNode> args) {
		this.args = args;
	}
	
	public SimpleName getName() {
		return name;
	}
	@Override
	public String toString() {
		return name.resolveBinding().toString();
	}
	
}
