package businessObjects.cve;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public final class Weakness {
    private String source;
    private String type;
    private ArrayList<WeaknessDescription> description;
}