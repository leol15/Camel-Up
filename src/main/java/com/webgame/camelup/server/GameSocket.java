
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

    private String sender, msg;

    private Map<String, Set<Session>> playersMap = new ConcurrentHashMap<>();
    private Map<String, CamelUp> gamesMap = new ConcurrentHashMap<>();
    private static Gson GSON = new Gson();

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
        // parse info
        System.out.println("message: " + message);
        ClientInfo client = GSON.fromJson(message, ClientInfo.class);
        Set<Session> playerSessions = playersMap.get(client.gameID);
        CamelUp game = gamesMap.get(client.gameID);
        if (!client.method.equals("id") && (playerSessions == null || game == null)) {
            System.err.println("Game id " + client.gameID + " does not exists");
            return;
        }
        // process actions
        switch (client.method) {
            case "id":
                String name = (String) client.value;
                // random id?
                if (game == null) {
                    // new game
                    game = new CamelUp();
                    gamesMap.put(client.gameID, game);
                    playersMap.put(client.gameID, new HashSet<>());
                    playersMap.get(client.gameID).add(user);
                } else {
                    while (game.containsPlayer(name))
                        name += genRandomString(1);
                }
                game.addPlayer(name);
                sendString(user, GSON.toJson(createMessageMapping("id", name)));
                break;
            case "roll":
                game.rollDie(client.playerID);
                SERVER_METHOD_MAP.get(Const.INFO_DICE).run(playerSessions, game);
                SERVER_METHOD_MAP.get(Const.INFO_CAMELS).run(playerSessions, game);
                break;
            case "makeBet":
                game.placeBets(client.playerID, (String) client.value);
                SERVER_METHOD_MAP.get(Const.INFO_BET).run(playerSessions, game);
                break;
            case "makeWinnerGlobalBet":
                break;
            case "makeLoserGlobalBet":
                break;
            case "placeTrap":
                break;
            case "reset":
                break;
            default:

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
        playerSessions.stream().filter(Session::isOpen)
            .forEach(sess -> {
                try {
                    sess.getRemote().sendString(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

}