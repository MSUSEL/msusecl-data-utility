package businessObjects.cve;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public final class Metrics {
    private ArrayList<CvssMetricV2> cvssMetricV2;
}