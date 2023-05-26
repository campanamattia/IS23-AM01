package Server.Network.Lobby;

import Interface.Client.RemoteClient;
import Interface.Server.LobbyInterface;
import Interface.Client.RemoteView;
import Server.Controller.GameController;
import Server.Network.Client.ClientHandler;
import Server.Network.Client.SocketHandler;
import Server.ServerApp;
import Utils.MockObjects.MockFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;


/**
 * The Lobby class represents the lobby system in the game server.
 * It manages player login, logout, lobby sizes, game initialization, and provides lobby information.
 */
public class Lobby extends UnicastRemoteObject implements LobbyInterface {
    /**
     * A mapping of lobby IDs to a mapping of player IDs to their respective client handlers.
     */
    private final HashMap<String, HashMap<String, ClientHandler>> lobby;
    /**
     * A mapping of games to their corresponding game controllers.
     */
    private final HashMap<Game, GameController> games;
    /**
     * A mapping of player-hashcode to their heartbeat timer.
     */
    private final HashMap<Integer, PingTimer> heartbeat;
    /**
     * A mapping of lobby IDs to their respective lobby sizes.
     */
    private final HashMap<String, Integer> lobbySize;

    private final ExecutorService executorService;

    /**
     * Constructs a new instance of the Lobby class.
     * Initializes the lobby, heartbeat, lobby size, and games.
     *
     * @throws RemoteException if a communication error occurs during the remote method call
     */
    public Lobby() throws RemoteException {
        super();
        this.executorService = Executors.newCachedThreadPool();
        this.heartbeat = new HashMap<>();
        this.lobby = new HashMap<>();
        this.lobbySize = new HashMap<>();
        this.games = new HashMap<>();
    }

    /**
     * Sends the lobby information to the specified remote view.
     *
     * @param remote the remote view to which the lobby information will be sent
     * @throws RemoteException if a communication error occurs during the remote method call
     */
    @Override
    public void getLobbyInfo(RemoteView remote) throws RemoteException {
        remote.askPlayerInfo(getLobbyInfo());
    }

    /**
     * Retrieves the lobby information as a list of maps containing lobby and game details.
     *
     * @return a list of maps representing the lobby information. Each map contains lobby ID and player count, and game name and active player count.
     */
    private List<Map<String, String>> getLobbyInfo() {
        List<Map<String, String>> lobbyInfo = new ArrayList<>();
        Map<String, String> lobbies = new HashMap<>();
        Map<String, String> games = new HashMap<>();
        lobbyInfo.add(lobbies);
        lobbyInfo.add(games);

        // Retrieve lobby information
        for (String lobbyID : this.lobby.keySet()) {
            lobbies.put(lobbyID, this.lobby.get(lobbyID).size() + "/" + this.lobbySize.get(lobbyID));
        }

        // Retrieve game information
        for (Game game : this.games.keySet()) {
            games.put(game.name(), activePlayers(game) + "/" + game.players().size());
        }

        return lobbyInfo;
    }


    /**
     * Calculates the number of active players in the specified game.
     *
     * @param game the game for which to count the active players
     * @return the number of active players in the game
     */
    private int activePlayers(Game game) {
        int activePlayers = 0;
        for (Boolean status : game.players().values()) {
            activePlayers += status ? 1 : 0;
        }
        return activePlayers;
    }


    /**
     * Sets the lobby size for the specified lobby.
     *
     * @param playerID  the ID of the player requesting the lobby size change
     * @param lobbyID   the ID of the lobby for which to set the size
     * @param lobbySize the desired lobby size to set
     * @throws RemoteException if a communication error occurs during the remote method call
     */
    @Override
    public synchronized void setLobbySize(String playerID, String lobbyID, int lobbySize) throws RemoteException {
        if (this.lobbySize.get(lobbyID) == null) {
            if (sizeValid(lobbySize)) {
                this.lobbySize.put(lobbyID, lobbySize);
            } else {
                this.executorService.submit(() -> {
                    try {
                        this.lobby.get(lobbyID).get(playerID).remoteView().outcomeException(new RuntimeException("Lobby size must be between 2 and 4"));
                        this.lobby.get(lobbyID).get(playerID).remoteView().askLobbySize();
                    } catch (RemoteException e) {
                        ServerApp.logger.log(Level.SEVERE, e.getMessage());
                    }
                });
            }
        } else {
            this.executorService.submit(() -> {
                try {
                    this.lobby.get(lobbyID).get(playerID).remoteView().outcomeException(new RuntimeException("LobbySize has already been set"));
                } catch (RemoteException e) {
                    ServerApp.logger.log(Level.SEVERE, e.getMessage());
                }
            });
        }
    }

    /**
     * Checks if the specified lobby size is valid.
     *
     * @param lobbySize the lobby size to validate
     * @return true if the lobby size is between 2 and 4 (inclusive), false otherwise
     */
    private boolean sizeValid(int lobbySize) {
        return lobbySize >= 2 && lobbySize <= 4;
    }

    /**
     * Handles the login process for a player.
     *
     * @param playerID the ID of the player logging in
     * @param lobbyID  the ID of the lobby in which the player is logging in
     * @param client   the remote view of the player
     * @param network  the remote client representing the player's network connection
     * @throws RemoteException if a communication error occurs during the remote method call
     */
    public synchronized void login(String playerID, String lobbyID, RemoteView client, RemoteClient network) throws RemoteException {
        Game game = findGame(lobbyID);
        if (game != null) { //if the game exists
            if (game.players().containsKey(playerID)) {
                if (!game.players().get(playerID)) { //if the player is not playing
                    if (rejoin(playerID, lobbyID, client)) {
                        this.executorService.submit(() -> {
                            try {
                                client.allGame(MockFactory.getMock(this.games.get(game).getGameModel()));
                            } catch (RemoteException e) {
                                ServerApp.logger.log(Level.SEVERE, e.getMessage());
                            }
                        });
                        game.setStatus(playerID, true);
                        startTimer(playerID, lobbyID, network);
                    }
                } else { //if the player is playing
                    this.executorService.submit(() -> {
                        try {
                            client.outcomeException(new RuntimeException("Player is already playing"));
                            client.askPlayerInfo(getLobbyInfo());
                        } catch (RemoteException e) {
                            ServerApp.logger.log(Level.SEVERE, e.getMessage());
                        }
                    });
                }
            }
        } else if (this.lobby.get(lobbyID) != null) { //if the lobby exists
            if (this.lobby.get(lobbyID).containsKey(playerID)) { //if the playerID is already taken
                this.executorService.submit(() -> {
                    try {
                        client.outcomeException(new RuntimeException("PlayerID already taken"));
                        client.askPlayerInfo(getLobbyInfo());
                    } catch (RemoteException e) {
                        ServerApp.logger.log(Level.SEVERE, e.getMessage());
                    }
                });
            } else { //if the playerID is not taken
                this.lobby.get(lobbyID).put(playerID, new ClientHandler(playerID, lobbyID, client));
                startTimer(playerID, lobbyID, network);
                initGame(lobbyID);
            }
        } else { //if the lobby does not exist
            this.lobby.put(lobbyID, new HashMap<>());
            this.lobby.get(lobbyID).put(playerID, new ClientHandler(playerID, lobbyID, client));
            startTimer(playerID, lobbyID, network);
            if (!firstPlayer(lobbyID, client)) initGame(lobbyID);
        }
    }

    /**
     * Starts a ping timer for the specified player in the given lobby.
     *
     * @param playerID the ID of the player
     * @param lobbyID  the ID of the lobby
     * @param client   the remote client representing the player's network connection
     */
    private void startTimer(String playerID, String lobbyID, RemoteClient client) {
        int hash = Objects.hash(playerID, lobbyID);
        this.heartbeat.put(hash, new PingTimer(playerID, lobbyID, client));
        this.heartbeat.get(hash).start();
    }

    /**
     * Receives a ping message from the specified player in the given lobby, indicating that the player is still active.
     *
     * @param playerID the ID of the player
     * @param lobbyID  the ID of the lobby
     * @throws RemoteException if a communication error occurs during the remote method call
     */
    @Override
    public synchronized void ping(String playerID, String lobbyID) throws RemoteException {
        int hash = Objects.hash(playerID, lobbyID);
        this.executorService.submit(() -> this.heartbeat.get(hash).receivedPing());
    }

    /**
     * Retrieves the game controller for the specified lobby and provides it to the remote client.
     *
     * @param lobbyID the ID of the lobby
     * @param remote  the remote client to receive the game controller
     * @throws RemoteException if a communication error occurs during the remote method call
     */
    @Override
    public void getGameController(String lobbyID, RemoteClient remote) throws RemoteException {
        Game game = findGame(lobbyID);
        if (game != null) {
            this.executorService.submit(()-> {
                try {
                    remote.setGameController(this.games.get(game));
                } catch (RemoteException e) {
                    ServerApp.logger.log(Level.SEVERE, e.getMessage());
                }
            });
        } else {
            ServerApp.logger.severe("Game not found");
        }
    }

    /**
     * Finds a game with the specified lobby ID.
     *
     * @param lobbyID the ID of the lobby
     * @return the Game object corresponding to the lobby ID, or null if no matching game is found
     */
    private Game findGame(String lobbyID) {
        return this.games.keySet().stream().filter(game -> game.name().equals(lobbyID)).findFirst().orElse(null);
    }

    /**
     * Logs out the specified player from the lobby.
     *
     * @param playerID the ID of the player
     * @param lobbyID  the ID of the lobby
     * @throws RemoteException if a communication error occurs during the remote method call
     */
    @Override
    public synchronized void logOut(String playerID, String lobbyID) throws RemoteException {
        Game game = findGame(lobbyID);
        ClientHandler clientHandler;
        if (game != null) {
            game.setStatus(playerID, false);
            clientHandler = this.games.get(game).logOut(playerID);
            if ((long) game.players().values().size() <= 1) {
                this.games.remove(game);
            }
        } else {
            clientHandler = this.lobby.get(lobbyID).remove(playerID);
            if (this.lobby.get(lobbyID).isEmpty()) {
                this.lobby.remove(lobbyID);
                this.lobbySize.remove(lobbyID);
            }
        }
        if (clientHandler.remoteView() instanceof SocketHandler) ((SocketHandler) clientHandler.remoteView()).logOut();
        deleteTimer(playerID, lobbyID);
    }

    /**
     * Deletes the ping timer for the specified player in the given lobby.
     *
     * @param playerID the ID of the player
     * @param lobbyID  the ID of the lobby
     */
    private void deleteTimer(String playerID, String lobbyID) {
        int hash = Objects.hash(playerID, lobbyID);
        this.heartbeat.get(hash).interrupt();
        this.heartbeat.remove(hash);
    }

    /**
     * Checks if the lobby has no players and prompts the client to provide the lobby size.
     *
     * @param lobbyID the ID of the lobby
     * @param client  the remote view of the client
     * @return true if the lobby size needs to be set, false otherwise
     * @throws RemoteException if a communication error occurs during the remote method call
     */
    private boolean firstPlayer(String lobbyID, RemoteView client) throws RemoteException {
        if (this.lobbySize.get(lobbyID) == null) {
            this.executorService.submit(()-> {
                try {
                    client.askLobbySize();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Initializes a new game for the specified lobby if the lobby is full.
     *
     * @param lobbyID the ID of the lobby
     */
    private void initGame(String lobbyID) {
        if (this.lobby.get(lobbyID).size() == this.lobbySize.get(lobbyID)) {
            HashMap<String, Boolean> players = new HashMap<>();
            for (String playerID : this.lobby.get(lobbyID).keySet())
                players.put(playerID, true);
            this.games.put(new Game(lobbyID, players), new GameController(lobbyID, this.lobby.get(lobbyID)));
            for (ClientHandler client : this.lobby.get(lobbyID).values()) {
                this.executorService.submit(()-> {
                    try {
                        client.remoteView().allGame(MockFactory.getMock(games.get(findGame(lobbyID)).getGameModel()));
                    } catch (RemoteException e) {
                        ServerApp.logger.severe("Error sending game to player");
                    }
                });
            }
            this.lobby.remove(lobbyID);
            this.lobbySize.remove(lobbyID);
        }
    }

    /**
     * Attempts to rejoin a disconnected player to the game.
     *
     * @param playerID   The ID of the player who wants to rejoin.
     * @param lobbyID    The ID of the lobby where the player wants to rejoin.
     * @param remoteView The remote view of the player.
     * @return {@code true} if the player successfully rejoins the game, {@code false} otherwise.
     */
    private boolean rejoin(String playerID, String lobbyID, RemoteView remoteView) {
        GameController gameController = this.games.get(findGame(lobbyID));
        try {
            gameController.rejoin(playerID, new ClientHandler(playerID, lobbyID, remoteView));
            return true;
        } catch (RemoteException e) {
            ServerApp.logger.severe("Error reloading player");
            return false;
        }
    }
}