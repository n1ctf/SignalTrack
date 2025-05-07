package radio;

import java.util.EventObject;

public class SinadEvent extends EventObject {

    private static final long serialVersionUID = 6192294883918185372L;

    private final double dbSinad;

    public SinadEvent(Object source, double dbSinad) {
        super(source);
        this.dbSinad = dbSinad;
    }

    public double getdbSinad() {
        return dbSinad;
    }

}
