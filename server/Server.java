
import static spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.Filter;

import java.util.HashMap;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import com.google.gson.Gson;
import com.google.gson.*;
// all response are JSON
public class Server {

	static final int PORT = 45678;
	static final String GAME_ROUTE = "/game";
	static final String CREATE_GAME_ROUTE = "/create";
	static final String HOME_ROUTE = "/";
	static final String DEV_ROUTE = "/dev";
	static final String PLAYER_NAME_KEY = "uname";

	
	static Gson gson;


	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: exe rootdir");
			return;
		}
		String ROOT_PATH = args[0];
		port(PORT);
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
				System.out.print("____IP: ");
				System.out.print(request.ip());
				System.out.print(" -> ");
				System.out.println(request.url());
			}
		};
		Spark.afterAfter(filter); // Applies this filter even if there's a halt() or exception.
		////////////////////////
		// End of CORSFILTER  //
		////////////////////////

		/////////////////
		// initialize  //
		/////////////////

		HashMap<String, CamelUp> gamePool = new HashMap<>();
		gson = new Gson();

		// default
		// should we serve the web as well?
		get(HOME_ROUTE, (req, res) -> {
			try {
				// read a local html file
				// and send it back
				// Map<String, String[]> mp = req.queryMap().toMap();
				res.status(200);          
				res.type("text/html"); 
				res.body(readFileToString(ROOT_PATH + "/server/Server.java"));
				return "";
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				return "bad";
			}
		});


		// return a new game link to use
		get(CREATE_GAME_ROUTE, (req, res) -> {
			while (true) {
				String roomLink = genRandomString(5);
				if (gamePool.containsKey(roomLink))
					continue;
				// create game
				gamePool.put(roomLink, new CamelUp());
				// res.redirect(GAME_ROUTE + "/" + roomLink);
				return roomLink;
			}

		});

		get(GAME_ROUTE + "/*", (req, res) -> {
			if (req.splat().length == 0)
				return "";
			// connect request to specific room
			String roomLink = req.splat()[0];
			if (!gamePool.containsKey(roomLink))
				return "";
			CamelUp game = gamePool.get(roomLink);
			if (game == null)
				return "";
			// or return something like room does not exist
			
			// handle request
			handleGameRequest(req, res, game);
			return "";
		});



		get(DEV_ROUTE, (req, res) -> {
			try {
				// read a local html file
				res.status(200);          
				res.type("text/html"); 
				res.body(readFileToString(ROOT_PATH + "/dev/debug.html"));
				return "";
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				return "bad";
			}
		});

	}

	public static String readFileToString(String pathStr) {
	    Path path = Paths.get(pathStr);
	    System.out.println(path.toAbsolutePath());
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


	public static void handleGameRequest(Request req, Response res, CamelUp game) {
		String action = req.queryParams("action");
		action = action == null ? "" : action;
		switch (action) {
			case PLAYER_NAME_KEY:
				String uname = req.queryParams(PLAYER_NAME_KEY);
				uname = uname == null ? "" : uname;
				String oldName = req.cookie(PLAYER_NAME_KEY);
				System.out.println("old name is " + uname);
				if (oldName != null && oldName.equals(uname) && game.containsPlayer(uname)) {
					// same name, do nothing
					break;
				}
				while (game.containsPlayer(uname)) {
					uname += genRandomString(1);
				}
				// might be changing name??
				if (oldName != null && game.containsPlayer(oldName)) {
					game.changePlayer(oldName, uname); //todo
				} else {
					game.addPlayer(uname);
				}
				// new player
				res.cookie(PLAYER_NAME_KEY, uname);
				res.body(uname);
				break;
			case "dice":
				res.body(gson.toJson(game.getDice()));
				break;
			case "roll":
				game.rollDie();
				res.body(game.toString());
				break;
			default:
				res.body(game.toString());
		}
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
}

