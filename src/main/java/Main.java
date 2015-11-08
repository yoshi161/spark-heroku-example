import org.eclipse.jetty.websocket.api.*;
import org.json.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;
import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Main {

    static List<Session> currentUsers = new ArrayList<>();
    static Map<Session, String> usernameMap = new HashMap<>();
    static int nextUserNumber = 1; //Assign to username for next connecting user

    public static void main(String[] args) {
        port(getHerokuAssignedPort());
        staticFileLocation("public"); //index.html will be served at localhost:4567 (default port)
        webSocket("/chat", ChatWebSocketHandler.class);
        init();
    }

    //Sends a message from one user to all users, along with a list of current users
    public static void broadcastMessageFromUser(Session user, String message) {
        currentUsers.stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("userMessage", createHtmlMessageFromUser(user, message))
                    .put("userlist", currentUsers.stream().map(usernameMap::get).collect(Collectors.toList()))
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //Builds a HTML element with username, message and timestamp,
    private static String createHtmlMessageFromUser(Session user, String message) {
        return article().with(
                b(usernameMap.get(user) + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567;
    }

}
