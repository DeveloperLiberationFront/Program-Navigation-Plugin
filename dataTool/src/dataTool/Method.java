package dataTool;

import java.util.List;

import org.eclipse.jdt.core.dom.SimpleName;

public class Method {
	SimpleName name;
	List<SimpleName> args;
	public Method( SimpleName methodName ) {
		this.name = methodName;
	}
	
	public List<SimpleName> getArgs() {
		return args;
	}
	public void setArgs( List<SimpleName> args) {
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
