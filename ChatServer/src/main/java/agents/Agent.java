package agents;

import java.util.ArrayList;
import javax.ejb.Local;

@Local
public interface Agent{
	public void handleMessage(ACLMessage message);
	public void setAID(AID id);
	public void setUsername(String user);
	public AID getAID();
	public String getUsername();
	public ArrayList<String> getMessages();
}
