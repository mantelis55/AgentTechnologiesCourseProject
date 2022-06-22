package agents;

public class AID {
	public String name;
	public String host;
	public AgentType type;
	
	public AID(String name, String host, AgentType type) {
		this.name = name;
		this.host = host;
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		return (name + ":" + host + ":" + type.toString()).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		AID aid = (AID)obj;
		if(aid == null) return false;
		if(aid.name.equals(name) && aid.host.equals(host) && aid.type.equals(type)) {
			return true;
		}
		else return false;
	}
	
	@Override
	public String toString() {
		return name + ":" + host + ":" + type.toString();
	}
}
