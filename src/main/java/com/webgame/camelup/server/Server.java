package com.webgame.camelup.server;
import com.webgame.camelup.game.*;
import com.webgame.camelup.*;

import static spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.Filter;
// websocket
import org.eclipse.jetty.websocket.api.Session;

import java.util.HashMap;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import com.google.gson.Gson;
import com.google.gson.*;


public class Server {

	// static for static methods
	static Gson gson;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: exe rootdir");
			return;
		}
		String rootPath = args[0];

		// use assigned port 
		port(getHerokuAssignedPort());

		// static folder todo
		Spark.staticFiles.location("/public");

		webSocket("/socket", GameSocket.class);
		// socket timeout
		webSocketIdleTimeoutMillis(1000 * 300);
		
		///////////////////////
		// CORSFILTER stuff  //
		///////////////////////

		final HashMap<String, String> corsHeaders = new HashMap<>();
		corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
		corsHeaders.put("Access-Control-Allow-Origin", "*");
		corsHeaders.put("Access-Control-Allow-Headers",
				"Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
		corsHeaders.put("Access-Control-Allow-Credentials", "true");
		Filter filter = new Filter() {
			@Override
			public void handle(Request request, Response response) {
				corsHeaders.forEach(response::header);
				// System.out.print("____IP: ");
				// System.out.print(request.ip());
				// System.out.print(" -> ");
			}
		};
		Spark.afterAfter(filter); // Applies this filter even if there's a halt() or exception.
		////////////////////////
		// End of CORSFILTER  //
		////////////////////////

		/////////////////
		// initialize  //
		/////////////////

		Set<String> gameRooms = new HashSet<>();
		gson = new Gson();

		// home handled by public folder

		// return a new game link to use
		get(Const.CREATE_GAME_ROUTE, (req, res) -> {
			String roomLink = genRandomString(5);
			while (gameRooms.contains(roomLink)) {
				roomLink = genRandomString(5);
			}
			// create game
			gameRooms.add(roomLink);
			res.redirect(Const.GAME_ROUTE + "/" + roomLink);
			return "";
		});

		// serve the main game page
		get(Const.GAME_ROUTE + "/*", (req, res) -> {
			if (req.splat().length == 0)
				return "";
			// connect request to specific room
			// or return something like room does not exist
			String roomLink = req.splat()[0];
			res.type("text/html"); 
			if (!gameRooms.contains(roomLink)) {
				// 404
				res.status(404);
				res.body(readFileToString(rootPath + "/client/404.html"));
			} else {
				// fetch file
				res.body(readFileToString(rootPath + "/client/game.html"));
				res.status(200);
			}
			return "";
		});

		System.out.println("Server ignited");

	}


	//////////////
	// helpers
	public static String genRandomString(int len) {
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			sb.append((char) ('a' + r.nextInt(26)));
		}
		return sb.toString();
	}


	public static String readFileToString(String pathStr) {
	    Path path = Paths.get(pathStr);
	    StringBuilder sb = new StringBuilder();
	    try {
		    BufferedReader reader = Files.newBufferedReader(path);
		    while (reader.ready()) {
		    	sb.append(reader.readLine() + "\n");
		    }
	    } catch (IOException e) {
	    	System.err.println(e);
	    }
	    return sb.toString();
	}

	static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return Const.PORT; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}

