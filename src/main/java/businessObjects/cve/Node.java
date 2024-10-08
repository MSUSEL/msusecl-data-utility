package businessObjects.cve;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public final class Node {
    private String operator;
    private String negate;
    private ArrayList<CpeMatch> cpeMatch;
}