package businessObjects.cve;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter 
@Setter
public final class Cve {
    private String id;
    private String sourceIdentifier;
    private String published;
    private String lastModified;
    private String vulnStatus;
    private ArrayList<Description> descriptions;
    private Metrics metrics;
    private ArrayList<Weakness> weaknesses;
    private ArrayList<Configuration> configurations;
    private ArrayList<Reference> references;
}