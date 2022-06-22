package rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import agents.AgentCenter;
import agents.AgentType;

@ApplicationPath("/")
public class RestEasy extends Application {
	public RestEasy() {
		AgentType userAgent = new AgentType("UserAgent");
		AgentType chatAgent = new AgentType("ChatAgent");
		AgentCenter.types.add(userAgent);
		AgentCenter.types.add(chatAgent);
		AgentCenter.CreateChatAgent("MAIN");
	}
}