
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;


@WebSocket
public class ChatWebSocketHandler {

    private String sender, msg;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String username = "User" + SocketServer.nextUserNumber++;
        SocketServer.userUsernameMap.put(user, username);
        SocketServer.broadcastMessage(sender = "Server", msg = (username + " joined the chat"));
    	System.out.println("new user ");
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = SocketServer.userUsernameMap.get(user);
        SocketServer.userUsernameMap.remove(user);
        SocketServer.broadcastMessage(sender = "Server", msg = (username + " left the chat"));
    	System.out.println("user left ");
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        SocketServer.broadcastMessage(sender = SocketServer.userUsernameMap.get(user), msg = message);
    	System.out.println("user says " + message);
    }

}