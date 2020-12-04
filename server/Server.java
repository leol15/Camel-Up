
import static spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.Filter;

import java.net.URLEncoder;
import java.net.URLDecoder;

import java.util.HashMap;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import com.google.gson.Gson;
import com.google.gson.*;
// all response are JSON
public class Server {

	static final int PORT = 45678;
	static final String GAME_ROUTE = "/game";  // /game/[gamecode] => landing page
	static final String GAME_COMM_ROUTE = "/gamespeaks";  // internal request route
	static final String CREATE_GAME_ROUTE = "/create";
	static final String HOME_ROUTE = "/";
	static final String DEV_ROUTE = "/dev";
	static final String PLAYER_NAME_KEY = "uname";
	static final String COLOR_KEY = "color";
	static final String TILE_KEY = "tile";
	static final String SCALAR_KEY = "scalar";
	static final String SCALAR_VALUE = "boost"; // boost == +1, otherwise it is -1

	
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
				res.body(readFileToString(ROOT_PATH + "/client/landing.html"));
				return "";
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				return "bad";
			}
		});


		// return a new game link to use
		get(CREATE_GAME_ROUTE, (req, res) -> {
			String roomLink = genRandomString(5);
			while (gamePool.containsKey(roomLink)) {
				roomLink = genRandomString(5);
			}
			// create game
			gamePool.put(roomLink, new CamelUp());
			res.redirect(GAME_ROUTE + "/" + roomLink);
			return "";
		});

		// return a new gameID, for dev
		post(CREATE_GAME_ROUTE, (req, res) -> {
			System.out.println("post");
			String roomLink = genRandomString(5);
			while (gamePool.containsKey(roomLink)) {
				roomLink = genRandomString(5);
			}
			// create game
			gamePool.put(roomLink, new CamelUp());
			res.status(200);
			return roomLink;
		});

		// serve the main game page
		get(GAME_ROUTE + "/*", (req, res) -> {
			if (req.splat().length == 0)
				return "";
			// connect request to specific room
			// or return something like room does not exist
			String roomLink = req.splat()[0];
			CamelUp game = gamePool.get(roomLink);
			res.type("text/html"); 
			if (game == null) {
				// 404
				res.status(404);
				res.body(readFileToString(ROOT_PATH + "/client/404.html"));
			} else {
				// fetch file
				res.body(readFileToString(ROOT_PATH + "/client/game.html"));
				res.status(200);
			}
			return "";
		});

		get(GAME_COMM_ROUTE + "/*", (req, res) -> {
			if (req.splat().length == 0)
				return "";
			// connect request to specific room
			String roomLink = req.splat()[0];
			if (!gamePool.containsKey(roomLink))
				return "Room does not exist";
			CamelUp game = gamePool.get(roomLink);
			if (game == null)
				return "Room does not exist";
			
			// resolve player name first
			String playerName = resolvePlayerName(req, res, game);
			System.out.println("player name: " + playerName);
			if (playerName == null)
				return "authenticate failed, please create a name to play";
			// handle request
			handleGameRequest(req, res, game, playerName);
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



	public static void handleGameRequest(Request req, Response res, CamelUp game, String player) {
		String action = req.queryParams("action");
		action = action == null ? "" : action;
		String color = "";
		// autheticate player
		switch (action) {
			case PLAYER_NAME_KEY:
				// dealt with already!!
				break;
			case "camels":
				res.body(gson.toJson(game.getCamels()));
				res.type("application/json");
				break;
			case "players":
				res.body(gson.toJson(game.getPlayers()));
				res.type("application/json");
				break;
			case "roll":
				game.rollDie(player);
				// and return the dice
			case "dice":
				res.body(gson.toJson(game.getDice()));
				res.type("application/json");
				break;
			case "makeBet":
				color = req.queryParams(COLOR_KEY);
				game.placeBets(player, color);
				// pass to "bet"
			case "bet":
				res.body(gson.toJson(game.getBet()));
				res.type("application/json");
				break;
			case "makeWinnerGlobalBet":
				color = req.queryParams(COLOR_KEY);
				game.placeWinnerGlobalBet(player, color);
				// pass to return globalBet
			case "globalBet":
				res.body(gson.toJson(game.getGlobalBet(player)));
				res.type("application/json");
				break;
			case "makeLoserGlobalBet":
				color = req.queryParams(COLOR_KEY);
				game.placeLoserGlobalBet(player, color);
				res.body(gson.toJson(game.getGlobalBet(player)));
				res.type("application/json");
				break;
			case "placeTrap":
				int tile = Integer.parseInt(req.queryParams(TILE_KEY));
				String scalar = req.queryParams(SCALAR_KEY);
				boolean traped = game.placeTrap(player, tile, scalar.equals("boost"));
				if (!traped)
					System.out.println("failed place trap " + tile + " (" + scalar + ")");
				// pass on to "trap"
			case "trap":
				res.body(gson.toJson(game.getTrap()));
				res.type("application/json");
				break;
			case "reset":
				game.reset();
				break;
			case "timestamp":
				res.body(gson.toJson(game.getTimeStamp()));
				res.type("application/json");
				break;
			default:
				res.body(game.toString());
		}
	}

	// return a existing player name
	// return null for players that cannot be authorized
	public static String resolvePlayerName(Request req, Response res, CamelUp game) {
		String action = req.queryParams("action");
		if (action == null)
			return null;
		String oldName = req.cookie(PLAYER_NAME_KEY);
		if (oldName != null) {
			oldName = URLDecoder.decode(oldName);
		}
		if (!action.equals(PLAYER_NAME_KEY)) {
			// name must exist
			if (game.containsPlayer(oldName)) {
				// valid name
				return oldName;
			} else {
				return null;
			}
		}
		// is request valid?
		String newName = req.queryParams(PLAYER_NAME_KEY);
		if (newName == null)
			return null;

		// change name || new player
		if (game.containsPlayer(oldName)) {
			// change name
			if (oldName.equals(newName))
				return oldName;
			while (game.containsPlayer(newName)) {
				newName += genRandomString(1);
			}
			System.out.println("changing name");
			game.changePlayer(oldName, newName);
			res.cookie(PLAYER_NAME_KEY, URLEncoder.encode(newName));
			res.body(newName);
			return newName;
		} else {
			// new player, make sure name does no collide		
			while (game.containsPlayer(newName)) {
				newName += genRandomString(1);
			}
			// add!
			if (game.addPlayer(newName)) {
				res.cookie(PLAYER_NAME_KEY, URLEncoder.encode(newName));
				res.body(newName);
				return newName;
			} else {
				System.out.println("failed to add player " + newName);
				res.removeCookie(PLAYER_NAME_KEY);
				return null;
			}
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
}

