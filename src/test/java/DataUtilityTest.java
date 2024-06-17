import org.junit.Test;

import database.dao.NvdBulkOperationsDao;
import database.dao.NvdMetaDataDao;
import database.interfaces.HTTPMethod;
import database.nvdMirror.NVDMirror;
import org.json.JSONException;
import org.json.JSONObject;

import api.GHSARequest;
import api.GHSAResponse;
import api.GraphQlQueries;
import api.NVDRequest;
import api.NVDRequestFactory;
import api.NVDResponse;
import api.cveData.CveDetails;
import api.cveData.Vulnerability;
import api.ghsaData.CweNode;
import common.Utils;
import common.DataProperties;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DataUtilityTest {
    private static Properties prop = DataProperties.getProperties();

    @Test
    public void testDataStoreFullBuild() {
        NVDMirror mirror = new NVDMirror();
        mirror.getFullDataSet();
    }

    @Test
    public void testGhsaRequest() throws JSONException {
        String ghsaId = "GHSA-vh2m-22xx-q94f";

        // Define the query string
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("query", GraphQlQueries.GHSA_SECURITY_ADVISORY_QUERY);
        String query = jsonBody.toString();
        String formattedQuery = String.format(query, ghsaId);

        String githubToken = Utils.getAuthToken(prop.getProperty("github-token-path"));
        String authHeader = String.format("Bearer %s", githubToken);
        List<String> headers = Arrays.asList("Content-Type", "application/json", "Authorization", authHeader);

        GHSARequest ghsaRequest = new GHSARequest(HTTPMethod.POST, Utils.GHSA_URI, headers, formattedQuery);
        GHSAResponse ghsaResponse = ghsaRequest.executeRequest();

        assertEquals(200, ghsaResponse.getStatus());
        assertEquals("GHSA-vh2m-22xx-q94f", ghsaResponse.getSecurityAdvisory().getGhsaId());
        assertEquals("Sensitive query parameters logged by default in OpenTelemetry.Instrumentation http and AspNetCore",
                ghsaResponse.getSecurityAdvisory().getSummary());

        ArrayList<CweNode> nodes = ghsaResponse.getSecurityAdvisory().getCwes().getNodes();
        assertEquals("CWE-201", nodes.get(0).getCweId());
    }

    @Test
    public void testSimpleNVDRequest() {
        List<String> apiKeyHeader = Arrays.asList("apiKey", Utils.getAuthToken(prop.getProperty("nvd-api-key-path")));
        NVDRequestFactory requestFactory = new NVDRequestFactory();
        NVDRequest nvdRequest = requestFactory.createNVDRequest(HTTPMethod.GET, Utils.NVD_BASE_URI, apiKeyHeader, 0, 1);
        NVDResponse nvdResponse = nvdRequest.executeRequest();

        assertEquals(200, nvdResponse.getStatus());
        assertEquals("CVE-1999-0095", nvdResponse.getCveResponse().getVulnerabilities().get(0).getCve().getId());
    }

    @Test
    public void testDaoInsertMany() {
        NvdBulkOperationsDao nvdBulkOperationsDao = new NvdBulkOperationsDao();
        List<String> apiKey = Arrays.asList("apiKey", Utils.getAuthToken(prop.getProperty("nvd-api-key-path")));
        NVDRequestFactory nvdRequestFactory = new NVDRequestFactory();
        NVDResponse response;

        NVDRequest request = nvdRequestFactory.createNVDRequest(HTTPMethod.GET, Utils.NVD_BASE_URI, apiKey, 0, 2000);
        response = request.executeRequest();
        List<CveDetails> cves = new ArrayList<>();

        for (Vulnerability vulnerability : response.getCveResponse().getVulnerabilities()) {
            cves.add(vulnerability.getCve());
        }

        nvdBulkOperationsDao.insert(cves);
    }

    @Test
    public void testMetaDataInsert() {
        List<String> apiKey = Arrays.asList("apiKey", Utils.getAuthToken(prop.getProperty("nvd-api-key-path")));
        NVDRequestFactory nvdRequestFactory = new NVDRequestFactory();
        NVDResponse response;

        NVDRequest request = nvdRequestFactory.createNVDRequest(HTTPMethod.GET, Utils.NVD_BASE_URI, apiKey, 0, 1);
        response = request.executeRequest();

        NvdMetaDataDao nvdMetaDataDao = new NvdMetaDataDao();
        nvdMetaDataDao.insert(response.getCveResponse());
    }

    @Test
    public void testMetaDataReplace() {
        List<String> apiKey = Arrays.asList("apiKey", Utils.getAuthToken(prop.getProperty("nvd-api-key-path")));
        NVDRequestFactory nvdRequestFactory = new NVDRequestFactory();
        NVDResponse response;

        NVDRequest request = nvdRequestFactory.createNVDRequest(HTTPMethod.GET, Utils.NVD_BASE_URI, apiKey, 0, 1);
        response = request.executeRequest();

        NvdMetaDataDao nvdMetaDataDao = new NvdMetaDataDao();
        nvdMetaDataDao.replace(response.getCveResponse());
    }


}
