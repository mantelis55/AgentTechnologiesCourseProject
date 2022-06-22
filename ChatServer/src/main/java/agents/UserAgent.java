package agents;

import java.io.IOException;
import java.util.ArrayList;
import javax.ejb.*;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.json.JSONException;
import org.json.JSONObject;

import rest.RestCalls;
import ws.WebSocketEP;

@Stateful
public class UserAgent implements Agent {
	
	AID agentID;
	String username;
	public ArrayList<String> messages = new ArrayList<String>();

	@Override
	public void handleMessage(ACLMessage message) {
		try {
			if(message.performative == Performative.CONSUME_MESSAGE) {
				JSONObject json = new JSONObject(message.content);
			    String messageText = json.get("message").toString();
				JSONObject jsonReply = new JSONObject();
				jsonReply.put("message", messageText);
				messages.add(messageText);
				String sessionID = AgentCenter.sessionIDS.get(agentID);
				if(!sessionID.equals(""))
					WebSocketEP.sessions.get(AgentCenter.sessionIDS.get(agentID)).getBasicRemote().sendText(jsonReply.toString());
				if(message.replyto != null) {
					Message reply;
					MessageProducer producer;
					try {
							producer = RestCalls.factory.getSession().createProducer(null);
							reply = RestCalls.factory.getSession().createMessage();
							reply.setIntProperty("responseStatus", 200);
							reply.setStringProperty("responseEntity", "Message recieved");
							producer.send((Destination)message.replyto, reply);
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void setAID(AID id) {
		agentID = id;
	}
	
	public AID getAID() {
		return agentID;
	}
	
	public void setUsername(String user) {
		username = user;
	}
	
	public String getUsername() {
		return username;
	}
	
	public ArrayList<String> getMessages(){
		return messages;
	}
}
