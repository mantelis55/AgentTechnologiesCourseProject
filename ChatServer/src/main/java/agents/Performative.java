package agents;

public enum Performative {
	LOGIN,
	REGISTER,
	LOGGED_IN,
	USER_MESSAGES,
	REGISTERED_USERS,
	LOG_OUT,
	MESSAGE,
	CONSUME_MESSAGE;
	
	public static Performative asPerformative(String str) {
	    for (Performative p : Performative.values()) {
	        if (p.name().equalsIgnoreCase(str))
	            return p;
	    }
	    return null;
	}
}
