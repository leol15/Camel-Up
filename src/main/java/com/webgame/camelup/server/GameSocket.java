
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

    public static 

    interface ServerMethod {
        void run(Set<Session> sessions, CamelUp game);
    }

    private class ClientInfo {
        String gameID;
        String playerID;
        String method;
        Object value;
    }

    private static Gson GSON = new Gson();

    private Map<String, Set<Session>> playersMap = new ConcurrentHashMap<>();
    private Map<String, CamelUp> gamesMap = new ConcurrentHashMap<>();
    private Map<Session, String> sessionToRoom = new ConcurrentHashMap<>();
    private Map<Session, String> sessionToPlayerID = new ConcurrentHashMap<>();

    // methods wrapped together
    private Map<String, ServerMethod> SERVER_METHOD_MAP;

    private static boolean INITIALIZED = false; 
    // set up the methods
    private void init() {
        SERVER_METHOD_MAP = new HashMap<>();
        // observations
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
        
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        System.err.println("Getting msg: " + message);
        // parse info
        ClientInfo client = GSON.fromJson(message, ClientInfo.class);
        String playerID = client.playerID;
        Set<Session> playerSessions = playersMap.get(client.gameID);
        CamelUp game = gamesMap.get(client.gameID);

        // check validity
        if (!client.method.equals(Const.INFO_ID) && (playerSessions == null || game == null)) {
            System.err.println("Game id " + client.gameID + " does not exists");
            return;
        }
        // process actions
        System.err.println("method: " + client.method);
        switch (client.method) {
            case Const.INFO_ID:
                String name = (String) client.value;
                // new player
                if (game == null) {
                    // new game
                    game = new CamelUp();
                    playerSessions = new HashSet<>();
                    gamesMap.put(client.gameID, game);
                    playersMap.put(client.gameID, playerSessions);
                } else {
                    // random id?
                    while (game.containsPlayer(name))
                        name += genRandomString(1);
                }
                game.addPlayer(name);
                playerSessions.add(user);
                // add to session mappings
                sessionToPlayerID.put(user, name);
                sessionToRoom.put(user, client.gameID);
                sendString(user, GSON.toJson(createMessageMapping(Const.INFO_ID, name)));
                SERVER_METHOD_MAP.get(Const.INFO_PLAYERS).run(playerSessions, game);
                // also send everything to user
                Set<Session> tmp = new HashSet<>();
                tmp.add(user);
                for (String s : SERVER_METHOD_MAP.keySet()) {
                    SERVER_METHOD_MAP.get(s).run(tmp, game);
                }
                break;
            case Const.ACTION_ROLL:
                System.err.println("rolling " + playerID);
                game.rollDie(playerID);
                SERVER_METHOD_MAP.get(Const.INFO_DICE).run(playerSessions, game);
                SERVER_METHOD_MAP.get(Const.INFO_CAMELS).run(playerSessions, game);
                break;
            case Const.ACTION_MAKE_BET:
                game.placeBets(playerID, (String) client.value);
                SERVER_METHOD_MAP.get(Const.INFO_BET).run(playerSessions, game);
                break;
            case Const.ACTION_WINNER_BET:
                game.placeWinnerGlobalBet(playerID, (String) client.value);
                // todo just give the global bet back
                break;
            case Const.ACTION_LOSER_BET:
                game.placeLoserGlobalBet(playerID, (String) client.value);
                // todo just give the global bet back
                break;
            case Const.ACTION_PLACE_TRAP:
                String[] tile_boost = (String[]) client.value;
                if (tile_boost.length != 2) return;
                int tile = Integer.parseInt(tile_boost[0]);
                game.placeTrap(playerID, tile, tile_boost[1].equals("boost"));
                SERVER_METHOD_MAP.get(Const.INFO_TRAP);
                break;
            case Const.ACTION_RESET:
                // update all
                for (String s : SERVER_METHOD_MAP.keySet())
                    SERVER_METHOD_MAP.get(s).run(playerSessions, game);
                break;
            default:
                System.err.println("Unknown method: " + client.method);
        }
    }

    // server responses


    // convinent helpers

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

    private void broadcastMessage(Set<Session> playerSessions, Object obj) {
        String msg = GSON.toJson(obj);
        playerSessions.stream().filter(Session::isOpen)
            .forEach(session -> {
    			try {
    				session.getRemote().sendString(msg);
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
            });
    }

    private void broadcastState(
        Set<Session> playerSessions, String method, Object value) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("method", method);
        obj.put("value", value);
        String msg = GSON.toJson(obj);
        System.err.println("sending " + msg);
        playerSessions.stream().filter(Session::isOpen)
            .forEach(sess -> sendString(sess, msg));
    }

}