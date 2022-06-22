package agents;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.ejb.Stateful;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.websocket.Session;
import org.json.JSONObject;

import rest.RestCalls;
import ws.WebSocketEP;

@Stateful
public class ChatAgent implements Agent {

	AID agentID;
	String username;
	public ArrayList<String> messages = new ArrayList<String>();
	public Connection connection;
	
	@Override
	public void handleMessage(ACLMessage message) {
		String responseEntity = "";
		int responseStatus = 400;
		if(message.performative == Performative.LOGIN) {
			if(connection == null) {
				Connect();
			}
			JSONObject json = new JSONObject(message.content);
		    String username = json.get("username").toString();
		    String password = json.get("password").toString();
		    Statement statement;
		    ResultSet result;
		    int count = 0;
			try {
				statement = connection.createStatement();
				result = statement.executeQuery("SELECT COUNT(*) AS recordCount FROM users WHERE username = '" + username +"' AND password = '" + password + "'");
				if(result.next())
					count = result.getInt("recordCount");
			} catch (SQLException e) {
				responseStatus = 400;
				responseEntity = e.toString();
			}
	
			if(count == 1) {
				if(AgentCenter.getAgentByUsername(username) == null) {
					responseStatus = 200;
					responseEntity = "Logged in";
				}
				else {
					responseStatus = 400;
					responseEntity = "User already logged in using this account";
				}
			}
			else {
				responseStatus = 401;
				responseEntity = "Incorrect credentials";
			}
		}
		if(message.performative == Performative.REGISTER) {
			if(connection == null) {
				Connect();
			}
			JSONObject json = new JSONObject(message.content);
		    String username = json.get("username").toString();
		    String password = json.get("password").toString();
		    Statement statement;
			try {
				statement = connection.createStatement();
				statement.executeUpdate("INSERT INTO users (username, password) VALUES " + " ('" + username + "','"+ password + "')");
			} catch (SQLException e) {
				responseStatus = 400;
				responseEntity = e.toString();
			}
			responseStatus = 201;
			responseEntity = "User created";
		}
		if(message.performative == Performative.LOGGED_IN) {
	    	JSONObject jsonUsers = new JSONObject();
			jsonUsers.put("message", AgentCenter.getAllUsers());
			responseStatus = 201;
			responseEntity = jsonUsers.toString();
		}
		if(message.performative == Performative.USER_MESSAGES) {
	    	JSONObject json = new JSONObject(message.content);
	    	JSONObject jsonUsers = new JSONObject();
	    	Agent agent = AgentCenter.getAgentByUsername(json.get("username").toString());
	    	if(agent != null) {
	    		jsonUsers.put("messages", agent.getMessages());
				responseStatus = 200;
				responseEntity = jsonUsers.toString();
	    	}
	    	else {
				responseStatus = 400;
				responseEntity = "User is not logged in";
	    	}
	
		}
		if(message.performative == Performative.REGISTERED_USERS) {
			if(connection == null) {
				Connect();
			}
		    Statement statement;
		    ResultSet result;
			try {
				statement = connection.createStatement();
				result = statement.executeQuery("SELECT username FROM users");
				ArrayList<String> usernames = new ArrayList<String>();
				while (result.next()) { 
					usernames.add(result.getString(1));
				}
				responseStatus = 200;
				responseEntity = usernames.toString();
			} catch (SQLException e) {
				responseStatus = 400;
				responseEntity = e.toString();
			}
		}
		if(message.performative == Performative.LOG_OUT) {
			JSONObject json = new JSONObject(message.content);
		    String sessionID = json.get("sessionID").toString();
	    	Agent agent = AgentCenter.getAgentByUsername(json.get("username").toString());
	    	String senderID = "";
	    	if(agent != null)
	    		senderID = AgentCenter.sessionIDS.get(agent.getAID());
	    	if(senderID.equals(sessionID) && !sessionID.equals("")) {
	    		try {
					WebSocketEP.sessions.get(sessionID).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		responseStatus = 200;
				responseEntity = "User logged out";
	    	}
	    	else {
	    		responseStatus = 400;
				responseEntity = "Unauthenticated action";
	    	}
		}
		if(message.performative == Performative.MESSAGE) {
			JSONObject json = new JSONObject(message.content);
			ACLMessage newMessage = null;
			try {
				newMessage = (ACLMessage) message.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			newMessage.replyto = null;
			newMessage.performative = Performative.CONSUME_MESSAGE;
			Agent sender = null;
		    if(!json.has("receiver")) {
				if(json.has("sender")) {
					sender = AgentCenter.getAgentByUsername(json.get("sender").toString());
				}
				for(Agent a : AgentCenter.agents.values()) {
					if(sender != null) {
						if(!sender.getAID().equals(a.getAID()))
							a.handleMessage(newMessage);
					}
					else a.handleMessage(newMessage);
				}
	    		responseStatus = 200;
				responseEntity = "Message sent to ALL";
		    }else {
			    String reciever = json.get("receiver").toString();
		    	Agent recieverAgent = AgentCenter.getAgentByUsername(reciever);
		    	if(recieverAgent != null) {
		    		recieverAgent.handleMessage(newMessage);
		    		responseStatus = 200;
		    		responseEntity = "Message sent to " + reciever;
		    	}
		    	else {
		    		responseStatus = 400;
		    		responseEntity = "User - " + reciever + " not found";
		    	}
		    }
		}
		Message reply;
		MessageProducer producer;
		try {
				producer = RestCalls.factory.getSession().createProducer(null);
				reply = RestCalls.factory.getSession().createMessage();
				reply.setIntProperty("responseStatus", responseStatus);
				reply.setStringProperty("responseEntity", responseEntity);
				producer.send((Destination)message.replyto, reply);
		} catch (JMSException e) {
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
	
	public void Connect() {
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat_base", "root", "");
			return;
		} catch (SQLException e) {
			return;
		}
	}
}
