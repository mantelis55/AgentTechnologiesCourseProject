package ws;

import agents.*;
import java.io.IOException;
import java.util.*;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import org.json.JSONObject;

@Singleton
@ServerEndpoint("/ws")
@LocalBean
public class WebSocketEP {
	public static Map<String, Session> sessions = new HashMap<String, Session>();

	@OnOpen
	public void OnOpen(Session session) {
		if (!sessions.containsValue(session)) {
			sessions.put(session.getId(), session);
			System.out.println("Connected, Session list size - " + sessions.size());
			AgentCenter.CreateUserAgent(session.getId(), "UserAgent" + session.getId());
			SendTypeListUpdate();
		}
	}

	@OnMessage
	public void OnMessage(String message, Session session) {
		if (session.isOpen()) {
			Session s = sessions.get(session.getId());
			if (s != null) {
				try {
					AID aid = AgentCenter.getAIDBySessionID(session.getId());
					AgentCenter.agents.get(aid).setUsername(message);
					JSONObject json = new JSONObject();
					json.put("purpose", "SESSIONID");
					json.put("message", s.getId());
					s.getBasicRemote().sendText(json.toString());
					SendUserListUpdate();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@OnClose
	public void OnClose(Session session) {
		sessions.remove(session.getId());
		System.out.println("Disconnected, Session list size - " + sessions.size());
		AgentCenter.RemoveUserAgent(session.getId());
		SendUserListUpdate();
	}

	@OnError
	public void OnError(Session session, Throwable error) {
		sessions.remove(session.getId());
		AgentCenter.RemoveUserAgent(session.getId());
		SendUserListUpdate();
		error.printStackTrace();
	}

	static public void SendUserListUpdate() {
		JSONObject jsonUsers = new JSONObject();
		jsonUsers.put("purpose", "USERLIST");
		for (Session a : sessions.values()) {
			try {
				a.getBasicRemote().sendText(jsonUsers.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static public void SendAgentListUpdate() {
		JSONObject jsonAgents = new JSONObject();
		jsonAgents.put("purpose", "AGENTLIST");
		ArrayList<String> agents = AgentCenter.getAllAgents();
		jsonAgents.put("agents", agents.toArray(new String[agents.size()]));
		for (Session a : sessions.values()) {
			try {
				a.getBasicRemote().sendText(jsonAgents.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static public void SendTypeListUpdate() {
		JSONObject jsonTypes = new JSONObject();
		jsonTypes.put("purpose", "TYPELIST");
		ArrayList<String> list = new ArrayList<String>();
		for(AgentType a : AgentCenter.types) {
			list.add(a.toString() + " ");
		}
		jsonTypes.put("types", list.toArray(new String[list.size()]));
		for (Session a : sessions.values()) {
			try {
				a.getBasicRemote().sendText(jsonTypes.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static public void SendACLMessagesListUpdate() {
		JSONObject jsonACL = new JSONObject();
		jsonACL.put("purpose", "ACLLIST");
		jsonACL.put("acl", AgentCenter.getAllACLMessages());
		for (Session a : sessions.values()) {
			try {
				a.getBasicRemote().sendText(jsonACL.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static public void SendLogout(String sessionID) {
		JSONObject jsonLogout = new JSONObject();
		jsonLogout.put("purpose", "LOGOUT");
		try {
			sessions.get(sessionID).getBasicRemote().sendText(jsonLogout.toString());
			sessions.remove(sessionID);
			System.out.println("Disconnected, Session list size - " + sessions.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}