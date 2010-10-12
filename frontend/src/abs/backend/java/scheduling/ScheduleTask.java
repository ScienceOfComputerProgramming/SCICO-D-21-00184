package abs.backend.java.scheduling;

import abs.backend.java.lib.runtime.COG;
import abs.backend.java.lib.runtime.Task;

public class ScheduleTask extends ScheduleAction {
    public ScheduleTask(COG cog) {
        super(cog);
    }
 
    
    @Override
    public String toString() {
        return "Schedule task in "+getCOG();
    }
    
    @Override
    public String shortString() {
        return "C"+getCOG().getID();
    }
}
