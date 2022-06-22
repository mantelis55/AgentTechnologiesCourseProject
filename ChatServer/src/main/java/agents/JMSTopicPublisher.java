package agents;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Singleton
@LocalBean
public class JMSTopicPublisher{
	
	public Connection connection;
	MessageProducer defaultProducer;
	Session session;
	
	@PostConstruct
	public void postConstruction() {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			Topic topic = (Topic) ctx.lookup("java:jboss/exported/jms/topic/publicTopic");
			ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("java:jboss/exported/jms/RemoteConnectionFactory");
			connection = connectionFactory.createConnection("guest", "guest.guest.1");
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			connection.start();
			defaultProducer = session.createProducer(topic);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public MessageProducer getProducer() {
		return defaultProducer;
	}

	public Session getSession() {
		return session;
	}
	
	@PreDestroy
	public void preDestroy() {
		try {
			connection.close();
		} catch (JMSException ex) {
			System.out.println("Exception while closing the JMS connection." + ex);
		}
	}
}