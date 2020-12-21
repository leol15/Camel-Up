
package com.webgame.camelup.server;

import com.webgame.camelup.game.*;
import com.webgame.camelup.*;


import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import com.google.gson.Gson;


import java.util.concurrent.*;
import java.util.*;
import java.util.function.Function;

@WebSocket
public class GameSocket {

    interface ServerMethod {
        void run(Set<Session> sessions, CamelUp game);
    }

    private class ClientInfo {
        String gameID;      // room link
        String playerID;    // player id
        String method;      // communication method
        Object value;       // actual value said
    }

    private static Gson GSON = new Gson();

    // room id is the key
    private Map<String, Set<Session>> playersMap = new ConcurrentHashMap<>();
    private Map<String, CamelUp> gamesMap = new ConcurrentHashMap<>();

    // todo: manage user id
    private Map<Session, String> sessionToRoom = new ConcurrentHashMap<>();
    private Map<Session, String> sessionToPlayerID = new ConcurrentHashMap<>();

    // methods wrapped together
    private Map<String, ServerMethod> SERVER_METHOD_MAP;

    private static boolean INITIALIZED = false; 
    // set up the methods
    private void init() {
        SERVER_METHOD_MAP = new HashMap<>();
        // create observation routes
        SERVER_METHOD_MAP.put(Const.INFO_PLAYERS, (sessions, game) -> {
            broadcastState(sessions, Const.INFO_PLAYERS, game.getPlayers());
        });
        SERVER_METHOD_MAP.put(Const.INFO_DICE, (sessions, game) -> {
            broadcastState(sessions, Const.INFO_DICE, game.getDice());
        });
        SERVER_METHOD_MAP.put(Const.INFO_CAMELS, (sessions, game) -> {
            broadcastState(sessions, Const.INFO_CAMELS, game.getCamels());
        });
        SERVER_METHOD_MAP.put(Const.INFO_BET, (sessions, game) -> {
            broadcastState(sessions, Const.INFO_BET, game.getBet());
        });
        SERVER_METHOD_MAP.put(Const.INFO_PLAYER_SCORE, (sessions, game) -> {
            broadcastState(sessions, Const.INFO_PLAYER_SCORE, game.getPlayerScore());
        });
        SERVER_METHOD_MAP.put(Const.INFO_TRAP, (sessions, game) -> {
            broadcastState(sessions, Const.INFO_TRAP, game.getTrap());
        });
        SERVER_METHOD_MAP.put(Const.INFO_PLAYER_TURN, (sessions, game) -> {
            broadcastState(sessions, Const.INFO_PLAYER_TURN, game.getPlayerTurn());
        });
    }


    
    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        if (!INITIALIZED) {
            init();
            INITIALIZED = true;
        }
        System.out.println("Connected");
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        // todo remove player
        String roomID = sessionToRoom.get(user);
        String playerID = sessionToPlayerID.get(user);
        if (roomID != null && playerID != null) {
            Set<Session> sessions = playersMap.get(roomID);
            // remove from sessions
            if (sessions != null) sessions.remove(user);
            // todo: remove from game

            // remove room
            if (sessions.isEmpty()) {
                playersMap.remove(roomID);
                gamesMap.remove(roomID);
                System.out.println("removing room: " + roomID);
            }
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        // parse info
        ClientInfo client = GSON.fromJson(message, ClientInfo.class);
        String playerID = client.playerID;
        Set<Session> allSessions = playersMap.get(client.gameID);
        CamelUp game = gamesMap.get(client.gameID);

        // check validity
        if (!client.method.equals(Const.INFO_ID) &&
            (allSessions == null || game == null)) {
            System.err.println("Game id " + client.gameID + " does not exists");
            return;
        }
        // process actions
        switch (client.method) {
            case Const.INFO_ID:
                playerID = (String) client.value;
                // check game exists
                if (sessionToRoom.containsKey(user)) {
                    game = gamesMap.get(sessionToRoom.get(user));
                }
                // new player
                if (game == null) {
                    // new game
                    game = new CamelUp();
                    allSessions = new HashSet<>();
                    gamesMap.put(client.gameID, game);
                    playersMap.put(client.gameID, allSessions);
                } else {
                    // random id?
                    while (game.containsPlayer(playerID))
                        playerID += genRandomString(1);
                }
                game.addPlayer(playerID);
                allSessions.add(user);
                // add to session mappings
                sessionToPlayerID.put(user, playerID);
                sessionToRoom.put(user, client.gameID);
                // send id to new user
                sendString(user, 
                    GSON.toJson(createMessageMapping(Const.INFO_ID, playerID)));
                // announce a new player came, done in the end
                // also send everything to the new user
                Set<Session> tmp = new HashSet<>();
                tmp.add(user);
                serverApplyAll(tmp, game);
                break;
            case Const.ACTION_ROLL:
                game.rollDie(playerID);
                serverApplyAll(allSessions, game, 
                    Const.INFO_DICE, Const.INFO_CAMELS);
                break;
            case Const.ACTION_MAKE_BET:
                game.placeBets(playerID, (String) client.value);
                serverApplyAll(allSessions, game, Const.INFO_BET);
                break;
            case Const.ACTION_WINNER_BET:
                game.placeWinnerGlobalBet(playerID, (String) client.value);
                // todo just give the global bet back?
                break;
            case Const.ACTION_LOSER_BET:
                game.placeLoserGlobalBet(playerID, (String) client.value);
                // todo just give the global bet back?
                break;
            case Const.ACTION_PLACE_TRAP:
                String[] tile_boost = (String[]) client.value;
                if (tile_boost.length != 2) return;
                game.placeTrap(playerID,
                    Integer.parseInt(tile_boost[0]),
                    tile_boost[1].equals("boost"));
                serverApplyAll(allSessions, game, Const.INFO_TRAP);
                break;
            case Const.ACTION_RESET:
                // update all
                serverApplyAll(allSessions, game);
                break;
            default:
                System.err.println("Unknown method: " + client.method);
        }
        // also broadcast whose turn it is
        serverApplyAll(allSessions, game,
            Const.INFO_PLAYERS, Const.INFO_PLAYER_TURN);
    }

    // convinent helpers
    
    /*
        apply methods to all sessions
        if "method" is empty, apply all methods avaliable
        from SERVER_METHOD_MAP
    */
    private void serverApplyAll(Set<Session> sessions, CamelUp game,
        String... methods) {
        if (methods.length == 0) {
            for (String m : SERVER_METHOD_MAP.keySet())
                SERVER_METHOD_MAP.get(m).run(sessions, game);
        } else {
            for (String m : methods) {
                if (!SERVER_METHOD_MAP.containsKey(m)) continue;
                SERVER_METHOD_MAP.get(m).run(sessions, game);
            }
        }
    }

    public String genRandomString(int len) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((char) ('a' + r.nextInt(26)));
        }
        return sb.toString();
    }

    private Object createMessageMapping(String method, Object value) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("method", method);
        obj.put("value", value);
        return obj;
    }

    private void sendString(Session sess, String str) {
        try {
            sess.getRemote().sendString(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastState(
        Set<Session> playerSessions, String method, Object value) {
        String msg = GSON.toJson(createMessageMapping(method, value));
        playerSessions.stream().filter(Session::isOpen)
            .forEach(sess -> sendString(sess, msg));
    }

}

