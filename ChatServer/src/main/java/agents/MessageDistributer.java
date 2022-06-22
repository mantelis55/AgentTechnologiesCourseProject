package agents;

import javax.ejb.ActivationConfigProperty;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.ejb.MessageDriven;
import com.google.gson.Gson;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/topic/publicTopic") })
public class MessageDistributer implements MessageListener {
	
	public void onMessage(Message message) {
		try {
			Gson gson = new Gson();
			ACLMessage aclmessage = gson.fromJson(message.getStringProperty("ACLMessage"), ACLMessage.class);
			aclmessage.replyto = message.getJMSReplyTo();
			if(AgentCenter.getUserAgentPerformatives().contains(aclmessage.performative)) {
				for(Agent a : AgentCenter.agents.values()) {
					for(int i = 0; i < aclmessage.receivers.length; i++) {
						if(a.getAID().equals(aclmessage.receivers[i]))
							a.handleMessage(aclmessage);
					}
				}
			}
			else {
				for(Agent a : AgentCenter.chatAgents) {
					for(int i = 0; i < aclmessage.receivers.length; i++) {
						if(a.getAID().equals(aclmessage.receivers[i]))
							a.handleMessage(aclmessage);
					}
				}
			}


		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}