package handlers;

import api.ghsaData.CweNode;
import api.ghsaData.Cwes;
import api.ghsaData.SecurityAdvisory;
import database.interfaces.IJsonMarshaller;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class SecurityAdvisoryMarshaller implements IJsonMarshaller<SecurityAdvisory> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAdvisoryMarshaller.class);

    @Override
    public SecurityAdvisory unmarshallJson(String json) {
        SecurityAdvisory securityAdvisory = new SecurityAdvisory();
        Cwes cwes = new Cwes();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject jsonResponse = jsonObject.optJSONObject("data").optJSONObject("securityAdvisory");
            if (jsonResponse != null) {
                securityAdvisory.setGhsaId(jsonResponse.optString("ghsaId"));
                securityAdvisory.setSummary(jsonResponse.optString("summary"));
                cwes.setNodes(getNodesFromJson(jsonResponse));
                securityAdvisory.setCwes(cwes);
            } else {
                LOGGER.info("GHSA response was null");
            }
        } catch (JSONException e) {
            LOGGER.error("Malformed Json", e);
            throw new RuntimeException(e);
        }

        return securityAdvisory;
    }

    @Override
    public String marshallJson(SecurityAdvisory securityAdvisory) {
        return "";
    }

    private ArrayList<CweNode> getNodesFromJson(JSONObject response) {
        ArrayList<CweNode> nodes = new ArrayList<>();
        try {
            JSONArray jsonNodes = response.optJSONObject("cwes").optJSONArray("nodes");
            for(int i = 0; i < jsonNodes.length(); i++) {
                CweNode cweNode = new CweNode();
                cweNode.setCweId(jsonNodes.optJSONObject(i).getString("cweId"));
                nodes.add(cweNode);
            }
        } catch (JSONException e) {
            LOGGER.error("Malformed Json", e);
            throw new RuntimeException(e);
        }
        return nodes;
    }
}
