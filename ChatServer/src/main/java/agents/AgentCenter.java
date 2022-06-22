package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import rest.RestCalls;
import ws.WebSocketEP;

@Stateful
@LocalBean
public class AgentCenter {
	
	public static Map<AID, Agent>  agents = new HashMap<AID, Agent>();	
	public static Map<AID, String> sessionIDS = new HashMap<AID,String>();
	public static ArrayList<ACLMessage> messages = new ArrayList<ACLMessage>();
	public static ArrayList<Agent> chatAgents = new ArrayList<Agent>();
	public static ArrayList<AgentType> types = new ArrayList<AgentType>();
	
	static public Response CreateUserAgent(String sessionID, String name) {
		InitialContext ctx;
		int responseStatus = 400;
		String responseEntity = Status.BAD_REQUEST.toString();
		try {
			if(!isNameTaken(name, "UserAgent")) {
				ctx = new InitialContext();
				Agent agent = (Agent)ctx.lookup("java:global/ChatServer/UserAgent!agents.Agent");
				AID aid = new AID(name, "local", types.get(0));
				agent.setAID(aid);
				agent.setUsername(aid.toString());
				sessionIDS.put(aid, sessionID);
				agents.put(aid,agent);
				System.out.println("Created UserAgent, UserAgent list size - " + agents.size());
				responseStatus = 201;
				responseEntity = "UserAgent created";
				WebSocketEP.SendUserListUpdate();
				WebSocketEP.SendAgentListUpdate();
			}else {
				responseStatus = 400;
				responseEntity = "There is already an UserAgent with this name";
			}

		} catch (NamingException e) {
			e.printStackTrace();
			return Response
					.status(responseStatus)
				    .entity(responseEntity)
				    .build();
		}
		return Response
				.status(responseStatus)
			    .entity(responseEntity)
			    .build();
	}
	
	static public Response CreateChatAgent(String name) {
		InitialContext ctx;
		int responseStatus = 400;
		String responseEntity = Status.BAD_REQUEST.toString();
		try {
			if(!isNameTaken(name, "ChatAgent")) {
				ctx = new InitialContext();
				Agent agent = (Agent)ctx.lookup("java:global/ChatServer/ChatAgent!agents.Agent");
				AID aid = new AID(name, "local", types.get(1));
				agent.setAID(aid);
				chatAgents.add(agent);
				System.out.println("Created ChatAgent, ChatAgent list size - " + chatAgents.size());
				responseStatus = 201;
				responseEntity = "ChatAgent created";
				WebSocketEP.SendUserListUpdate();
				WebSocketEP.SendAgentListUpdate();
			}else {
				responseStatus = 400;
				responseEntity = "There is already an ChatAgent with this name";
			}

		} catch (NamingException e) {
			e.printStackTrace();
			return Response
					.status(responseStatus)
				    .entity(responseEntity)
				    .build();
		}
		return Response
				.status(responseStatus)
			    .entity(responseEntity)
			    .build();
	}
	
	static public void Employ(String jsonString, Message message) {
		Random random = new Random();
		ACLMessage ACLMessage = new ACLMessage(jsonString);
		if(getUserAgentPerformatives().contains(ACLMessage.performative)) {
			MessageProducer producer = RestCalls.factory.getProducer();
	    	try {
				message.setObjectProperty("ACLMessage", ACLMessage.toString());
				messages.add(ACLMessage);
				WebSocketEP.SendACLMessagesListUpdate();
				producer.send(message);
			} catch (JMSException e) {
				e.printStackTrace();
			}	
		}
		else {
			AID[] receivers = new AID[1];
			int rndNumber = random.nextInt(AgentCenter.chatAgents.size());
			receivers[0] = chatAgents.get(rndNumber).getAID();
			ACLMessage.receivers = receivers;
			MessageProducer producer = RestCalls.factory.getProducer();
	    	try {
				message.setObjectProperty("ACLMessage", ACLMessage.toString());
				messages.add(ACLMessage);
				WebSocketEP.SendACLMessagesListUpdate();
				producer.send(message);
			} catch (JMSException e) {
				e.printStackTrace();
			}	
		}

	}
	
	static public boolean isNameTaken(String name, String type) {
		if(type.equals("UserAgent"))
			for(Agent a : agents.values()) {
				if(a.getAID().name.equals(name)) return true;
			}
		else if(type.equals("ChatAgent"))
			for(Agent a : chatAgents) {
				if(a.getAID().name.equals(name)) return true;
			}
		return false;
	}
	
	static public void RemoveUserAgent(String sessionID) {
		for(Agent a : agents.values()) {
			if(a.getAID().equals(getAIDBySessionID(sessionID))) {
				agents.remove(a.getAID());
			}		
		}
		System.out.println("Deleted UserAgent, UserAgent list size - " + agents.size());
	}
	
	static public String[] getAllUsers() {
		String[] usernames = new String[agents.size()];
		int i = 0;
		for(Agent a : agents.values()) {
			usernames[i] = a.getUsername() + "\n";
			i++;
		}
		return usernames;
	}
	
	static public Agent getAgentByUsername(String username) {
		for(Agent a : agents.values()) {
			if(a.getUsername().equals(username)){
				return a;
			}
		}
		return null;
	}
	
	static public AID getAIDBySessionID(String sessionID) {
		for(Entry<AID, String> entry: sessionIDS.entrySet()) {
			if(entry.getValue().equals(sessionID))
				return entry.getKey();
		}
		return null;
	}
	
	static public Response removeAgentWithAID(String aid) {
		int responseStatus = 400;
		String responseEntity = Status.BAD_REQUEST.toString();
		String[] arrOfStr = aid.split(":", 3);
		if(arrOfStr.length == 3) {
			if(arrOfStr[2].equals("UserAgent")) {
				AID AID = new AID(arrOfStr[0],arrOfStr[1], types.get(0));
				Agent a = agents.get(AID);
				if(a != null) {
					String sessionID = AgentCenter.sessionIDS.get(a.getAID());
					if(!sessionID.equals(""))
						WebSocketEP.SendLogout(sessionID);
					agents.remove(a.getAID());
					System.out.println("Deleted UserAgent, UserAgent list size - " + agents.size());
					responseEntity = "UserAgent deleted successfully";
					responseStatus = 203;
					WebSocketEP.SendAgentListUpdate();
					WebSocketEP.SendUserListUpdate();
					
				}
				else {
					responseEntity = "UserAgent does not exist";
				}
			}
			else if(arrOfStr[2].equals("ChatAgent")) {
				Agent a = null;
				AID AID = new AID(arrOfStr[0],arrOfStr[1], types.get(1));
				for(Agent temp : chatAgents) {
					if(temp.getAID().equals(AID)) a = temp;
				}

				if(a != null) {
					if(chatAgents.size() == 1)
						responseEntity = "Can't delete the last ChatAgent";
					else {
						chatAgents.remove(a);
						System.out.println("Deleted ChatAgent, ChatAgent list size - " + chatAgents.size());
						responseEntity = "ChatAgent deleted successfully";
						responseStatus = 203;
						WebSocketEP.SendAgentListUpdate();
					}
				}
				else {
					responseEntity = "ChatAgent does not exist";
				}
			}
			else responseEntity = "Invalid Agent type. Agent types: 'ChatAgent', 'UserAgent'";

		}
		else {
			responseEntity = "Invalid format of aid. Valid: 'name:host:type'";
		}
		
		return Response
				.status(responseStatus)
			    .entity(responseEntity)
			    .build();
	}
	
	static public ArrayList<Performative> getChatAgentPerformatives() {
		ArrayList<Performative> performatives = new ArrayList<Performative>();
		performatives.add(Performative.LOG_OUT);
		performatives.add(Performative.LOGGED_IN);
		performatives.add(Performative.LOGIN);
		performatives.add(Performative.MESSAGE);
		performatives.add(Performative.REGISTER);
		performatives.add(Performative.REGISTERED_USERS);
		performatives.add(Performative.USER_MESSAGES);
		return performatives;
	}
	
	static public ArrayList<Performative> getUserAgentPerformatives() {
		ArrayList<Performative> performatives = new ArrayList<Performative>();
		performatives.add(Performative.CONSUME_MESSAGE);
		return performatives;
	}
	
	static public ArrayList<AgentType> getAgentTypes() {
		return types;
	}
	
	static public Response createAgent(String name, String type) {
		if(type.equals("ChatAgent")) {
			return CreateChatAgent(name);
		}
		else if (type.equals("UserAgent")) {
			return CreateUserAgent("", name);
		}
		return Response
				.status(400)
			    .entity("Invalid Agent type. Agent types: 'ChatAgent', 'UserAgent'")
			    .build();
	}
	
	static public ArrayList<String> getAllAgents(){
		ArrayList<String> allAgents = new ArrayList<String>();
		for(Agent a : agents.values()) {
			allAgents.add(a.getAID().toString() + "\n");
		}
		for(Agent a : chatAgents) {
			allAgents.add(a.getAID().toString() + "\n");
		}
		return allAgents;
	}
	static public String[] getAllACLMessages() {
		ArrayList<String> list = new ArrayList<String>();
		for(ACLMessage m : messages) {
			list.add(m.toString() + "\n\n");
		}
		return list.toArray(new String[list.size()]);
	}
	
}