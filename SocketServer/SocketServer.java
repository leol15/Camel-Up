
import static spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.Filter;

import org.eclipse.jetty.websocket.api.Session;

import java.util.*;
import java.util.concurrent.*;

public class SocketServer {
	
	// this map is shared between sessions and threads, so it needs to be thread-safe (http://stackoverflow.com/a/2688817)
	static Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
	static int nextUserNumber = 1; //Used for creating the next username

	public static void main(String[] args) {
		Spark.port(45678);
		staticFileLocation("/"); //index.html is served at localhost:4567 (default port)
		webSocket("/chat", new ChatWebSocketHandler());
		// webSocket("/chat", ChatWebSocketHandler.class);
		// Spark.unmap("/chat");

		init();
	}

	
}

