package Client.View.Cli;

import Client.Controller.Controller;
import Client.Network.ClientRMI;
import Client.Network.ClientSocket;
import Client.Network.Network;
import Client.View.View;
import Utils.ChatMessage;
import Utils.Cell;
import Utils.MockObjects.MockModel;
import Utils.Rank;
import Utils.Tile;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

public class Cli extends View {
    Controller clientController;
    MockModel mockModel;
    Network network;

    public Cli() {
        clientController = new Controller(this);
        mockModel = new MockModel();
    }

    public void start() throws IOException {
        int port;
        String address;
        showTitle();
        network = askConnection();
        address = askServerAddress();
        port = askServerPort();
        network.init(address, port);
    }

    // TODO: 16/05/23
    @Override
    public void askLobbySize() throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please insert the numbers of players (insert a number between 2 and 4)");
        String input = scanner.nextLine();

        int playerNumber = Integer.parseInt(input);
        while (playerNumber != 2 && playerNumber != 3 && playerNumber != 4) {
            System.out.println(CliColor.RED + "ERROR: you type something wrong, match can only start with 2, 3 or 4 players" + CliColor.RESET);
            input = scanner.nextLine();
            playerNumber = Integer.parseInt(input);
        }

        System.out.println("You are going to create a new Game, wait for the others players");

        scanner.close();
        network.setLobbySize(mockModel.getLocalPlayer(), mockModel.getLobbyID(), playerNumber);
    }

    public String askServerAddress() {
        final String DEFAULT_ADDRESS = "127.0.0.1";
        String ip = DEFAULT_ADDRESS;
        boolean validInput = false;
        boolean firstTry = true;

        Scanner scanner = new Scanner(System.in);

        String address;
        do {
            if (!firstTry)
                System.out.println(CliColor.RED + "ERROR: Invalid address! (remember the syntax xxx.xxx.xxx.xxx)" + CliColor.RESET + " Try again.");
            else
                System.out.println("Please enter the server address");

            System.out.println("Insert 'd' for the default value (" + DEFAULT_ADDRESS + "): ");
            address = scanner.nextLine();

            if (address.equalsIgnoreCase("d") || address.equalsIgnoreCase("localhost") || address.equals(DEFAULT_ADDRESS)) {
                validInput = true;
            } else if (clientController.validateIP(address)) {
                ip = address;
                validInput = true;
            } else {
                firstTry = false;
            }
        } while (!validInput);

        scanner.close();

        return ip;

    }

    public int askServerPort() {
        final int DEFAULT_PORT = 2807;
        final int MIN_PORT = 1024;
        final int MAX_PORT = 65535;
        int port = DEFAULT_PORT;
        boolean validInput = false;
        boolean notAnInt = false;
        boolean wrongPort = false;

        Scanner scanner = new Scanner(System.in);

        String input;
        while (!validInput) {
            if (notAnInt) {
                notAnInt = false;
                System.out.println(CliColor.RED + "ERROR: Please insert only numbers or \"d\"." + CliColor.RESET + " Try again.");
            }
            if (wrongPort) {
                wrongPort = false;
                System.out.println(CliColor.RED + "ERROR: MIN PORT = " + MIN_PORT + ", MAX PORT = " + MAX_PORT + "." + CliColor.RESET + " Try again.");
            }

            System.out.println("Select a valid port between [" + MIN_PORT + ", " + MAX_PORT + "]");
            System.out.println("Insert 'd' for the default value (" + DEFAULT_PORT + "): ");

            input = scanner.nextLine();

            if (input.equalsIgnoreCase("d")) {
                validInput = true;
            } else {
                try {
                    port = Integer.parseInt(input);
                    if (MIN_PORT <= port && port <= MAX_PORT) {
                        validInput = true;
                    } else {
                        wrongPort = true;
                    }
                } catch (NumberFormatException e) {
                    notAnInt = true;
                }
            }
        }

        scanner.close();
        return port;
    }

    // TODO: 16/05/23 we print the names of both the lobbies and the games and ask you to write the name you want to play with and in which lobby/game
    //if we don't find matches, a new lobby will be instantiated
    @Override
    public void askPlayerInfo(List<Map<String, String>> lobbyInfo) throws RemoteException {

    }


    @Override
    public void showBoard() {
        Cell[][] board = mockModel.getMockBoard().getBoard();
        System.out.println(" \t   0   " + "   1   " + "   2   " + "   3   " + "   4   " + "   5   " + "   6   " + "   7   " + "   8   ");
        for (int i = 0; i < board.length; i++) {
            System.out.print(i + "\t");
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j].getStatus()) {
                    String colorString = board[i][j].getTile().getColor().toString();
                    System.out.print(CliColor.BBLACK + "|" + colorString + i + " , " + j + CliColor.BBLACK + "|" + CliColor.RESET);
                } else {
                    System.out.print(CliColor.BBLACK + "|     |" + CliColor.RESET); //print empty black space
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    @Override
    public void showChat() {
        Stack<ChatMessage> chat = mockModel.getChat();
        for (int i = 0; i < chat.size(); i++) {
            System.out.println(chat.pop().from() + ": " + chat.pop().message());
        }
    }

    @Override
    public void showStatus() {
        if (mockModel.getCurrentPlayer().equals(mockModel.getLocalPlayer())) {
            System.out.print("It's your turn. ");
            System.out.println(mockModel.getTurnPhase());
        } else {
            System.out.println("It's NOT your turn. Wait for other players. For help type 'help'");
        }
    }


    public void showTitle() {
        System.out.print("" + CliColor.CLEAR_ALL + CliColor.BOLDYELLOW);
        System.out.println("""
                 ✹ ｡  .  ･ . ∴ * ███╗   ███╗██╗   ██╗    ██████╗██╗  ██╗███████╗██╗     ███████╗██╗███████╗. 　･ ∴　　｡ 　
                ｡    ✦    *      ████╗ ████║╚██╗ ██╔╝   ██╔════╝██║  ██║██╔════╝██║     ██╔════╝██║██╔════╝ ∴⋆  ˚  *   .
                  ∴   *  ｡ .  ✹  ██╔████╔██║ ╚████╔╝    ╚█████╗ ███████║█████╗  ██║     █████╗  ██║█████╗   ｡ ·　 ✦   *
                 .   ･  *   ｡  ∴ ██║╚██╔╝██║  ╚██╔╝      ╚═══██╗██╔══██║██╔══╝  ██║     ██╔══╝  ██║██╔══╝　   ✹  ｡   ·  ✧
                  ･  .   ✦     * ██║ ╚═╝ ██║   ██║      ██████╔╝██║  ██║███████╗███████╗██║     ██║███████╗ ✦ ∴ 　･ ｡· ∴
                  ✹   ｡ ∴.  ･   .╚═╝     ╚═╝   ╚═╝      ╚═════╝ ╚═╝  ╚═╝╚══════╝╚══════╝╚═╝     ╚═╝╚══════╝ ･　 *　　✹　 ˚""" + CliColor.RESET);

        System.out.println("Complete rules are available here: " + CliColor.BOLDPINK + "https://www.craniocreations.it/prodotto/my-shelfie\n" + CliColor.RESET);
    }


    public void showTile(List<Tile> tiles) {
        System.out.print("\t");
        for (int i = 0; i < tiles.size(); i++) {
            System.out.print(tiles.get(i).getColor().toString() + "|" + (i + 1) + "|");
            System.out.print(CliColor.RESET + "   ");
        }
        System.out.println();
    }

    // TODO: 16/05/23 farlo bello
    @Override
    public void endGame(List<Rank> classification) {

    }

    @Override
    public void crashedPlayer(String crashedPlayer) throws RemoteException {
        System.out.println(crashedPlayer + "is crashed but the game continue!!");
    }

    @Override
    public void reloadPlayer(String reloadPlayer) throws RemoteException {
        System.out.println(reloadPlayer + "reconnected in the game");
    }


    public void showShelves() {
        int numColumn = 5;
        int numRow = 6;
        int numPlayer = mockModel.getMockPlayers().size();

        for (int i = 0; i < numRow; i++) {
            System.out.print(" \t");
            for (int k = 0; k < numPlayer; k++) {
                for (int j = 0; j < numColumn; j++) {
                    Tile[][] shelf = mockModel.getMockPlayers().get(k).getShelf();
                    if (shelf[i][j] != null) {
                        String colorString = shelf[i][j].getColor().toString();
                        System.out.print(CliColor.BBLACK + "|" + colorString + i + "," + j + CliColor.BBLACK + "|" + CliColor.RESET);
                    } else {
                        System.out.print(CliColor.BBLACK + "|   |" + CliColor.RESET);
                    }
                }
                System.out.print("\t\t");
            }
            System.out.println();
        }
        System.out.print("    ");

        for (int k = 0; k < numPlayer; k++) {
            System.out.print(mockModel.getMockPlayers().get(k).getPlayerID() + ": points");
            for (int i = 0; i < 32 - mockModel.getMockPlayers().get(k).getPlayerID().length() - 8; i++) {
                System.out.print(" ");
            }
        }
        System.out.println("\n");
    }

    @Override
    public void showGame() {

    }


    @Override
    public void newTurn(String playerID) throws RemoteException {
        clearCLI();
        mockModel.setCurrentPlayer(playerID);
        showBoard();
        showShelves();
        showHelp();
    }


    @Override
    public void outcomeSelectTiles(List<Tile> tiles) throws RemoteException {
        System.out.print("\t");
        for (int i = 0; i < tiles.size(); i++) {
            System.out.print(tiles.get(i).getColor().toString() + "|" + (i + 1) + "|");
            System.out.print(CliColor.RESET + "   ");
        }
    }

    @Override
    public void outcomeInsertTiles(boolean success) throws RemoteException {
        System.out.println("Tile inserted correctly");
    }

    @Override
    public void outcomeException(Exception e) throws RemoteException {
        System.out.println(CliColor.RED + e.toString());
    }

    @Override
    public void outcomeLogin(String localPlayer, String lobbyID) throws RemoteException {
        System.out.println("You login into the server");
        mockModel.setLocalPlayer(localPlayer);
        mockModel.setLobbyID(lobbyID);

    }


    public Network askConnection() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select connection Mode (insert SOCKET or RMI): ");
        String input = scanner.nextLine();

        while (!input.equalsIgnoreCase("SOCKET") && !input.equalsIgnoreCase("RMI")) {
            System.out.println(CliColor.RED + "ERROR: you type something wrong, please enter SOCKET or RMI" + CliColor.RESET);
            input = scanner.nextLine();
        }

        System.out.println("Well done you create a" + input.toLowerCase() + "connection");

        if (input.equalsIgnoreCase("SOCKET")) {
            return new ClientSocket(this);
        }
        if (input.equalsIgnoreCase("RMI")) {
            return new ClientRMI(this);
        }
        return null;
    }


    @Override
    public void allGame(MockModel mockModel) throws RemoteException {
        this.mockModel = mockModel;
        newTurn(mockModel.getCurrentPlayer());

        Thread inputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            String userInput;

            while (true) {
                if (scanner.hasNextLine()) {
                    userInput = scanner.nextLine();
                    try {
                        clientController.doAction(userInput);
                    } catch (RuntimeException e) {
                        System.out.println(CliColor.RED + e.getMessage() + CliColor.RESET);
                    }
                } else {
                    System.out.println("Don't enter without a body");
                }
            }
        });
        inputThread.start();
    }


    public void clearCLI() {
        System.out.print(CliColor.CLEAR_ALL);
        System.out.flush();
    }

}
