
import static spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.Filter;

import java.util.HashMap;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.*;
// all response are JSON
public class Server {

	static final int PORT = 45678;

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

        CamelUp game = new CamelUp();
        


        Gson gson = new Gson();
        // default
        //Spark.get("/", )
        get("/", (req, res) -> {
            try {

                // String name = req.queryParams("name");
                // String message = req.queryParams("message");
    //            Map<String, String[]> mp = req.queryMap().toMap();
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                return "bad";
            }
            return "";
        });

        get("/chat", (req, res) -> {
//            res.body(gson.toJson("ok"));
//            res.type("test/json");
            // Update chat
//            updateChat(history, DATE.getTime());
            // Return chat history
            // return gson.toJson(history);
            return "";
        	
        });

        get("/clear", (req, res) -> {
//            res.body(gson.toJson("ok"));
//            res.type("test/json");
            // Update chat
            // Return chat history
            return "";
        });

        get("/page", (req, res) -> {
            res.body(gson.toJson("ok"));
            res.type("test/json");
            return "";
        });
	}
}
