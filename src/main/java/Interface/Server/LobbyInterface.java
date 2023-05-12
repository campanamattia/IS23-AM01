package Interface.Server;

import Interface.Client.RemoteClient;
import Interface.Client.RemoteView;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LobbyInterface extends Remote {

    void getLobbyInfo(RemoteView remote) throws RemoteException;

    void setLobbySize(String playerID, String lobbyID, int lobbySize) throws RemoteException;

    void logIn(String playerID, String lobbyID, RemoteView remoteView) throws RemoteException;

    void ping(String playerID, String lobbyID) throws RemoteException;

    void getGameController(String lobbyID, RemoteClient remote) throws RemoteException;

    void logOut(String playerID, String lobbyID) throws RemoteException;
}