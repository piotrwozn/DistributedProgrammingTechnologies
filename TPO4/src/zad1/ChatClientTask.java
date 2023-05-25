/**
 *
 *  @author Woźnicki Piotr SO0139
 *
 */

package zad1;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<String> {
    private final ChatClient associatedClient;
    private final List<String> listOfRequests;
    private final int wait;
    private static final int MINIMUM_WAIT = 10;

    private ChatClientTask(ChatClient client, List<String> requests, int wait) {
        super(generateCallable(client, requests, wait));
        this.associatedClient = client;
        this.listOfRequests = requests;
        this.wait = wait;
    }

    private static Callable<String> generateCallable(ChatClient client, List<String> requests, int wait) {
        return () -> {
            StringBuilder activityLogBuilder = new StringBuilder();
            synchronized (client) {
                Thread.sleep(Math.max(wait, MINIMUM_WAIT)); // Ustawiamy minimalne opóźnienie
                client.login();
                for (String singleRequest : requests) {
                    client.send(singleRequest + "\n");
                    Thread.sleep(Math.max(wait, MINIMUM_WAIT)); // Ustawiamy minimalne opóźnienie
                    activityLogBuilder.append(singleRequest);
                }
                client.logout();
                Thread.sleep(Math.max(wait, MINIMUM_WAIT)); // Ustawiamy minimalne opóźnienie
            }
            return activityLogBuilder.toString();
        };
    }


    public static ChatClientTask create(ChatClient client, List<String> requests, int wait) {
        return new ChatClientTask(client, requests, wait);
    }

    public ChatClient getClient() {
        return associatedClient;
    }
}
