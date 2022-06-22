package agents;

public class AgentType {
	public String name;
	
	public AgentType(String name) {
		this.name = name;
	}
	@Override
	public boolean equals(Object obj) {
		AgentType type = (AgentType)obj;
		if( type == null) return false;
		if( type.name.equals(name)) {
			return true;
		}
		else return false;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
