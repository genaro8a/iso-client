package ec.fin.baustro.isoclient.utilidades;

import java.io.Serializable;
import java.time.LocalDateTime;

public class GeneratorState implements Serializable {
    private static final long serialVersionUID = 1L;
    private int count;
    private LocalDateTime lastDateTime;

    public GeneratorState(int count, LocalDateTime lastDateTime) {
        this.count = count;
        this.lastDateTime = lastDateTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public LocalDateTime getLastDateTime() {
        return lastDateTime;
    }

    public void setLastDateTime(LocalDateTime lastDateTime) {
        this.lastDateTime = lastDateTime;
    }
}



