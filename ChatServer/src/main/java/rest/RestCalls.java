package rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import ws.WebSocketEP;
import javax.ejb.EJB;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.json.*;
import agents.Agent;
import agents.AgentCenter;
import agents.JMSTopicPublisher;
import agents.ACLMessage;

@Path("/")
public class RestCalls {
	public static Connection connections;
	@EJB
	static public JMSTopicPublisher factory;
	
	@GET
	@Path("/messages/")
	@Produces("application/json")
	public Response getMessages(){
    	JSONObject jsonPerformatives = new JSONObject();
    	jsonPerformatives.put("ChatAgent", AgentCenter.getChatAgentPerformatives().toString());
    	jsonPerformatives.put("UserAgent", AgentCenter.getUserAgentPerformatives().toString());
		return Response
		        .status(201)
		        .entity(jsonPerformatives.toString())
		        .build();
	}
	
	@GET
	@Path("/agents/classes")
	@Produces("application/json")
	public Response getTypes(){
    	JSONObject jsonTypes = new JSONObject();
    	jsonTypes.put("Types", AgentCenter.getAgentTypes().toString());
		return Response
		        .status(201)
		        .entity(jsonTypes.toString())
		        .build();
	}
	
	@GET
	@Path("/agents/running")
	@Produces("application/json")
	public Response getRunning(){
    	JSONObject jsonAgents = new JSONObject();
    	jsonAgents.put("Agents", AgentCenter.getAllAgents().toString());
		return Response
		        .status(201)
		        .entity(jsonAgents.toString())
		        .build();
	}
	
	@POST
	@Path("/messages/")
	@Produces("application/json")
	public Response messages(String jsonString){
		Message sendingMessage = null;
		String responseEntity = "";
		Message reply = null;
		int responseStatus = 400;
		try {
			Session session = getQueue();
			sendingMessage = session.createMessage();
			Queue tmpQueue = session.createTemporaryQueue();
			MessageConsumer consumer = session.createConsumer(tmpQueue);
			sendingMessage.setJMSReplyTo(tmpQueue);
			AgentCenter.Employ(jsonString, sendingMessage);
			reply = consumer.receive(3000);
			if(reply != null) {
				responseEntity = reply.getStringProperty("responseEntity");
				responseStatus = reply.getIntProperty("responseStatus");
			}
		} catch (JMSException e) {
			e.printStackTrace();
		} 
		return Response
		        .status(responseStatus)
		        .entity(responseEntity)
		        .build();
	}
	
	@PUT
	@Path("/agents/running/{type}/{name}")
	@Produces("application/json")
	public Response runNewAgent(@PathParam("type") String type, @PathParam("name") String name){
		 return AgentCenter.createAgent(name, type);

	}
	
	@DELETE
	@Path("/agents/running/{aid}")
	@Produces("application/json")
	public Response runNewAgent(@PathParam("aid") String aid){
		 return AgentCenter.removeAgentWithAID(aid);

	}
	
	public Session getQueue() {
		InitialContext ctx;
		Session session = null;
		try {
			ctx = new InitialContext();
			ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("java:jboss/exported/jms/RemoteConnectionFactory");
			Connection connection = connectionFactory.createConnection("guest", "guest.guest.1");
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			connection.start();
		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}
		return session;
	}
}