package Server.Model.Talent;

import Interface.Scout;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import static Server.ServerApp.executorService;
import static Server.ServerApp.logger;

public abstract class Talent <O>{
    protected List<Scout> scouts;

    public Talent(){
        this.scouts = new ArrayList<>();
    }

    public void addScout(Scout scout){
        this.scouts.add(scout);
    }

    public void removeScout(Scout scout){
        this.scouts.remove(scout);
    }

    public void notifyScouts(O objects){
        for(Scout scout : this.scouts) {
            executorService.execute(()-> {
                try {
                    scout.update(objects);
                } catch (RemoteException e) {
                    logger.severe(e.getMessage());
                }
            });
        }
    }

    public List<Scout> getScouts() {
        return scouts;
    }
}
