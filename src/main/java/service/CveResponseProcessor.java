package service;

import api.cveData.*;
import common.Utils;

import java.util.ArrayList;

public class CveResponseProcessor {

    public String[] extractCwes(Cve cve) {
        ArrayList<Weakness> cweList = cve.getWeaknesses();

        int size = cweList.size();
        String[] cwes = new String[size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < cweList.get(i).getDescription().size(); j++) {
                cwes[i] = cweList.get(i).getDescription().get(j).getValue();
            }
        }

        return cwes;
    }

    public Cve extractSingleCve(CVEResponse cveResponse) {
        return cveResponse.getVulnerabilities().get(0).getCve();
    }

    public int extractTotalResults(CVEResponse cveResponse) {
        return cveResponse.getTotalResults();
    }

    public ArrayList<Vulnerability> extractVulnerabilities(CVEResponse cveResponse) {
        return cveResponse.getVulnerabilities();
    }

    public NvdMirrorMetaData formatNvdMetaData(CVEResponse response) {
        NvdMirrorMetaData metaData = new NvdMirrorMetaData();
        metaData.setId(Utils.MONGO_NVD_METADATA_ID);
        metaData.setTotalResults(Integer.toString(response.getTotalResults()));
        metaData.setFormat(response.getFormat());
        metaData.setVersion(response.getVersion());
        metaData.setTimestamp(response.getTimestamp());

        return metaData;
    }
}
