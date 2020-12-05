
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;


import java.util.concurrent.*;
import java.util.*;

@WebSocket
public class ChatWebSocketHandler {

    private String sender, msg;

    private Map<Session, String> usernameMap = new ConcurrentHashMap<>();
    private int usernameId = 0;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String username = "User" + usernameId++;
        usernameMap.put(user, username);
        broadcastMessage(username + " joined the chat");
    	System.out.println("new user: " + user.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = usernameMap.get(user);
        usernameMap.remove(user);
        broadcastMessage(username + " left the chat");
    	System.out.println("user left " + reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        broadcastMessage(usernameMap.get(user) + ": " + message);
    	System.out.println("user says " + message);
    }

    private void broadcastMessage(String msg) {
        usernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
			try {
				session.getRemote().sendString(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
    }

}