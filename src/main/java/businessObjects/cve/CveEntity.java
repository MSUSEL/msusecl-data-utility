package businessObjects.cve;

import businessObjects.baseClasses.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * This represents the root object for NVD response data.
 * This set of classes comprises the complete set of POJOs
 * necessary to deserialize the NVD API's json response.
 */
@Getter
@Setter
public final class CveEntity extends BaseEntity {
    private int resultsPerPage;
    private int startIndex;
    private int totalResults;
    private String format;
    private String version;
    private String timestamp;
    private ArrayList<Vulnerability> vulnerabilities;
}
