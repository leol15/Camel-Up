
package com.webgame.camelup.server;


import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;


import java.util.concurrent.*;
import java.util.*;

@WebSocket
public class SyncSocketHandler {

    private String sender, msg;

    private Map<String, Set<Session>> rooms = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
    	if (!rooms.containsKey(message)) {
    		rooms.put(message, new HashSet<Session>());
    	}
    	Set<Session> room = rooms.get(message);
    	if (!room.contains(user)) {
    		room.add(user);
    	}
        broadcastMessage(message);
    }

    private void broadcastMessage(String msg) {
    	if (!rooms.containsKey(msg))
    		return;
        rooms.get(msg).stream().filter(Session::isOpen).forEach(session -> {
			try {
				session.getRemote().sendString(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
    }

}