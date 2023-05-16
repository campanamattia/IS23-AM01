package Messages.Client;

import Enumeration.OperationType;
import Messages.ClientMessage;
import Server.Controller.GameController;
import Server.Network.Client.SocketHandler;

import java.util.List;

public class InsertTilesMessage extends ClientMessage {
    List<Integer> sorted;
    int column;

    public InsertTilesMessage() {
        super();
        this.sorted = null;
        this.column = 0;
    }

    public InsertTilesMessage(OperationType operationType, String playerID, List<Integer> sorted, int column) {
        this.operationType = operationType;
        this.playerID = playerID;
        this.sorted = sorted;
        this.column = column;
    }

    public List<Integer> getSorted() {
        return sorted;
    }

    public int getColumn() {
        return column;
    }

    public void execute(SocketHandler socketHandler) {
        GameController gameController=  socketHandler.getGameController();
        gameController.insertTiles(this.operationType, this.playerID, this.sorted, this.column);
    }
}
