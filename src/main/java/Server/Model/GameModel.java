package Server.Model;

import Exception.BoardException;
import Exception.ChatException;
import Exception.Player.InvalidInputException;
import Exception.PlayerException;
import Exception.Player.PlayerNotFoundException;
import Interface.Scout;
import Server.Model.LivingRoom.CommonGoal.CommonGoal;
import Server.Model.LivingRoom.CommonGoal.CommonGoalFactory;
import Server.Model.LivingRoom.Bag;
import Server.Model.LivingRoom.Board;
import Utils.ChatMessage;
import Utils.ChatRoom;
import Server.Model.Player.PersonalGoal;
import Server.Model.Player.Player;
import Utils.Coordinates;
import Utils.MockObjects.MockFactory;
import Utils.Tile;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Represents the game model, contain information about the game state, players, board, and chat.
 */
public class GameModel {
    /**
     * the unique identifier of the game
     */
    private final String lobbyID;
    /**
     * the ID of the first player who starts the game
     */
    private final String firstPlayer;
    /**
     * the current player of the game
     */
    private Player currentPlayer;
    /**
     * list of all players in the game
     */
    private final List<Player> players;

    private final Talent talent;
    /**
     * the list of the two common goals for the game
     */
    private final List<CommonGoal> commonGoals;
    /**
     * the bag that contains the tiles used in the game
     */
    private final Bag bag;
    /**
     * the board of the game
     */
    private final Board board;
    /**
     * the chat room for the players to communicate with each other
     */
    private final ChatRoom chatRoom;


    /**
     * Creates a new instance of GameModel class using the specified unique identifier and the list of players.
     * The method initializes all the class fields and generates the game objects (board, players, common goals)
     * based on the JSON configuration files. It also sets the initial state of the game to STARTING and
     * the current player to the first player in the list.
     *
     * @param lobbyID the unique identifier of the game
     * @param players the list of names of players to be added to the game
     * @throws FileNotFoundException if the configuration files are not found
     */
    public GameModel(String lobbyID, List<String> players) throws IOException {
        this.lobbyID = lobbyID;
        this.firstPlayer = players.get(0);

        this.bag = new Bag();
        this.chatRoom = new ChatRoom();
        this.talent = new Talent();

        this.players = new ArrayList<>();
        this.commonGoals = new ArrayList<>();

        //creating board
        JsonObject board_json = decoBoard(players.size());
        this.board = new Board(board_json, this.bag);

        //creating Players
        JsonArray array = decoPersonal();
        Random random = new Random();
        for (String tmp : players) {
            PersonalGoal pGoal = new PersonalGoal(array.remove(random.nextInt(array.size())).getAsJsonObject());
            this.players.add(new Player(tmp, pGoal));
        }
        this.currentPlayer = this.players.get(0);

        //creating 2 commonGoal
        generateCommonGoal(players.size());
    }

    /**
     * This method reads a JSON file containing the information for a board and returns the JSON object
     * corresponding to the board for the specified number of players.
     *
     * @return the JSON object representing the board for the specified number of players
     * @throws FileNotFoundException if the specified file is not found
     */
    private JsonObject decoBoard(int players) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader;
        reader = new JsonReader(new InputStreamReader(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("settings/board.json"))));
        JsonObject json = gson.fromJson(reader, JsonObject.class);
        return json.getAsJsonObject(Integer.toString(players));
    }

    /**
     * Reads a JSON file containing an array of PersonalGoal objects and returns a JsonArray
     * representing the array.
     *
     * @return a JsonArray containing the PersonalGoal objects read from the file
     * @throws FileNotFoundException if the file at the given filepath cannot be found
     */
    private JsonArray decoPersonal() throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader;
        reader = new JsonReader(new InputStreamReader(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("settings/personalGoal.json"))));
        return gson.fromJson(reader, JsonArray.class);
    }

    /**
     * This method generates two common goals for the game by reading a json file from the specified filepath.
     * The method gets the common goals and the corresponding scoring tokens from the json file, and selects a random common goal to assign to each of the two goals.
     * The selected common goals are then added to the game's list of common goals.
     *
     * @throws FileNotFoundException if the specified filepath is not found
     */
    private void generateCommonGoal(int players) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader;
        reader = new JsonReader(new InputStreamReader(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("settings/commonGoal.json"))));
        JsonObject json = gson.fromJson(reader, JsonObject.class);
        JsonArray array = json.get("commonGoal").getAsJsonArray();
        json = json.get("scoringToken").getAsJsonObject();
        List<Integer> scoringToken = getAsList(json.get(Integer.toString(players)).getAsJsonArray());
        Random random = new Random();
        for(int i =0; i<2; i++)
            this.commonGoals.add(CommonGoalFactory.getCommonGoal(scoringToken, array.remove(random.nextInt(array.size())).getAsJsonObject()));
    }

    /**
     * Returns a list of integers (scoringTokens) given a JsonArray.
     * The integers are added to the list in reverse order because they are saved in ascending
     * order but are assigned in descending order .
     *
     * @param array the JsonArray to be converted to a list of integers.
     * @return a list of integers in reverse order from the JsonArray.
     */
    private List<Integer> getAsList(JsonArray array) {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= array.size(); i++)
            list.add(array.get(array.size() - i).getAsInt());
        return list;
    }

    /**
     * This method allows to retrieve the tiles at the given coordinates on the board.
     * It first validates that the move is legal by calling the convalidateMove method of the board object.
     * If the move is valid, it returns the tiles at the given coordinates using the getTiles method of the board object.
     *
     * @param coordinates a list of Coordinates objects representing the tiles to retrieve
     * @return a list of Tile objects representing the tiles at the given coordinates
     * @throws BoardException if the move is not valid according to the rules of the game
     */
    public List<Tile> selectTiles(List<Coordinates> coordinates) throws BoardException {
        this.board.convalidateMove(coordinates);
        List<Tile> tiles = this.board.getTiles(coordinates);
        this.board.checkRefill(this.bag);
        talent.onEvent(MockFactory.getMock(this.board));
        return tiles;
    }

    /**
     * This method is used to insert tiles on the player's personal shelf.
     *
     * @param sort   a list of integers representing the order in which the tiles should be inserted
     * @param tiles  a list of tiles that the player wants to insert
     * @param column an integer representing the column of the personal shelf where the tiles should be inserted
     * @throws PlayerException       if the player doesn't have enough space in their personal shelf
     * @throws InvalidInputException if the player didn't insert the correct parameters
     */
    public void insertTiles(List<Integer> sort, List<Tile> tiles, int column) throws PlayerException {
        if (sort.size() != tiles.size()) throw new InvalidInputException();
        for (int i = 1; i <= sort.size(); i++)
            if (!sort.contains(i)) throw new InvalidInputException();
        for (Integer integer : sort)
            tiles.add(tiles.get(integer - 1));
        tiles.subList(0, sort.size()).clear();
        this.currentPlayer.insert(column , tiles);
        talent.onEvent(MockFactory.getMock(this.currentPlayer));
    }

    /**
     * Writes a message in the chat room.
     *
     * @param from    the player who sent the message
     * @param message the body of the message
     * @param to      the player to whom the message is addressed NULL if it's broadcast
     */
    public void writeChat(String from, String message, String to) throws ChatException {
        if (message.equals("")) throw new ChatException();
        ChatMessage chatMessage = new ChatMessage(from, message, to);
        this.chatRoom.addMessage(chatMessage);
        talent.onEvent(chatMessage);
    }

    /**
     * Returns the player with the specified ID.
     *
     * @param id the ID of the player
     * @return the player with the specified ID
     * @throws PlayerNotFoundException if the player with the specified ID is not found
     */
    public Player getPlayer(String id) throws PlayerNotFoundException {
        for (Player tmp : this.players)
            if (tmp.equals(id)) return tmp;
        throw new PlayerNotFoundException(id);
    }

    /**
     * Sets the current player.
     *
     * @param currentPlayer the current player
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Returns the current player.
     *
     * @return the current player
     */
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    /**
     * Returns the list of players.
     *
     * @return the list of players
     */
    public List<Player> getPlayers() {
        return this.players;
    }

    /**
     * Returns the list of common goals.
     *
     * @return the list of common goals
     */
    public List<CommonGoal> getCommonGoals() {
        return this.commonGoals;
    }

    /**
     * Returns the bag of tile.
     *
     * @return the bag of tile
     */
    public Bag getBag() {
        return this.bag;
    }

    /**
     * Returns the board.
     *
     * @return the board
     */
    public Board getBoard() {
        return this.board;
    }

    /**
     * Returns the chat room.
     *
     * @return the chat room
     */
    public ChatRoom getChatRoom() {
        return this.chatRoom;
    }

    public void completeTurn(List<Tile> tiles) {
        for(int i = 0; i < 5; i++)
            try{
                this.currentPlayer.insert(i, tiles);
                break;
            } catch (PlayerException ignored) {
            }
        this.talent.onEvent(MockFactory.getMock(this.currentPlayer));
    }

    public String getLobbyID() {
        return this.lobbyID;
    }

    public void addScout(Scout scout) {
        this.talent.addScout(scout);
    }

    public Talent getScouts() {
        return this.talent;
    }
}