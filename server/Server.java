
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

	public static void main(String[] args) {
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
				System.out.print("___Client IP: ");
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
		Gson gson = new Gson();




		// return a new game link to use
		get("/startgame", (req, res) -> {
			Random r = new Random();
			while (true) {
				String roomLink = "";
				for (int i = 0; i < 5; i++) {
					roomLink += (char) ('a' + r.nextInt(26));
				}
				if (gamePool.containsKey(roomLink))
					continue;
				// create game
				gamePool.put(roomLink, new CamelUp());
				res.redirect(GAME_ROUTE + "/" + roomLink);
				return "";
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
			// return game info, todo
			return game.toString();
		});

		// default
		// should we serve the web as well?
		get("/", (req, res) -> {
			try {
				// read a local html file
				// and send it back
				// String name = req.queryParams("name");
				// Map<String, String[]> mp = req.queryMap().toMap();
				res.status(200);          // set status code to 401
				res.type("text/html"); 
				res.body();               // get response content
				
				return "";
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				return "bad";
			}
		});

	}

	public String readFileToString(String pathStr) throws IOException {
	    Path path = Paths.get(pathStr);
	    BufferedReader reader = Files.newBufferedReader(path);
	    StringBuilder sb = new StringBuilder();
	    // while (reader.hasNextLine()) {
	    	sb.append(reader.readLine());
	    // }
	    return sb.toString();
	}
}

