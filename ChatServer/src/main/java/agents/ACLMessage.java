package agents;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.*;
import com.google.gson.Gson;

public class ACLMessage implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	public Performative performative;
	public AID sender;
	public AID[] receivers;
	public String content;
	public Object replyto;
	
	public ACLMessage(String jsonString) {
		JSONObject json = new JSONObject(jsonString);
		performative = null;
		sender = null;
		receivers = null;
		if(!json.get("performative").toString().equals(""))
			this.performative = Performative.asPerformative(json.get("performative").toString());

		if(json.has("content")) {
			content = json.get("content").toString();
			JSONObject jsonContent = new JSONObject(content);
			if(jsonContent.has("receivers")) {
				if(!jsonContent.get("receivers").equals("")) {	
					JSONArray receivers = (JSONArray) jsonContent.get("receivers");
					ArrayList<AID> AIDs = new ArrayList<AID>();
					for(Object o : receivers) {
						String s = (String)o;
						String[] values = s.split(":");
						if(values.length == 3) {
							AgentType type = null;
							if(values[2].equals("UserAgent")) type = AgentCenter.types.get(0);
							else AgentCenter.types.get(1);
							AID aid = new AID(values[0], values[1], type);
							AIDs.add(aid);
						}

					}
					this.receivers = AIDs.toArray(new AID[AIDs.size()]);
				}
			}
		}
		else content = "";
	}
	
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
	
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
