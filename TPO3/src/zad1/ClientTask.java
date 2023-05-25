/**
 *
 *  @author Woźnicki Piotr SO0139
 *
 */

package zad1;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ClientTask extends FutureTask<String> {
    private final Client associatedClient;
    private final List<String> listOfRequests;
    private final boolean displayResults;

    private ClientTask(Client client, List<String> requests, boolean shouldDisplayResults) {
        super(generateCallable(client, requests, shouldDisplayResults));
        this.associatedClient = client;
        this.listOfRequests = requests;
        this.displayResults = shouldDisplayResults;
    }

    private static Callable<String> generateCallable(Client client, List<String> requests, boolean shouldDisplayResults) {
        return () -> {
            StringBuilder activityLogBuilder = new StringBuilder();
            client.connect();
            client.send("login " + client.getId());
            for (String singleRequest : requests) {
                String receivedResponse = client.send(singleRequest);
                activityLogBuilder.append(singleRequest);
                if (shouldDisplayResults) {
                    System.out.println(receivedResponse);
                }
            }
            activityLogBuilder = new StringBuilder(client.send("bye and log transfer"));
            if(shouldDisplayResults) {
                return activityLogBuilder.toString();
            } else {
                return "";
            }
        };
    }

    public static ClientTask create(Client client, List<String> requests, boolean shouldDisplayResults) {
        return new ClientTask(client, requests, shouldDisplayResults);
    }
}
