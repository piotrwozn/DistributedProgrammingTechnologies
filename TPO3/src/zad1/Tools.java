/**
 *
 *  @author Woźnicki Piotr SO0139
 *
 */

package zad1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class Tools {
    public static Options createOptionsFromYaml(String fileName) {
        Yaml yaml = new Yaml();
        Map<String, Object> configData;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            configData = yaml.loadAs(bufferedReader, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read options from YAML file: " + fileName, e);
        }

        String host = (String) configData.get("host");
        int port = (int) configData.get("port");
        boolean concurMode = (boolean) configData.get("concurMode");
        boolean showSendRes = (boolean) configData.get("showSendRes");
        Map<String, List<String>> clientMappings = (HashMap<String, List<String>>) configData.get("clientsMap");

        return new Options(host, port, concurMode, showSendRes, clientMappings);
    }
}
