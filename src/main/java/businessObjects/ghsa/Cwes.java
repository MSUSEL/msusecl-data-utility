package businessObjects.ghsa;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public final class Cwes {
    private ArrayList<CweNode> nodes;
}
