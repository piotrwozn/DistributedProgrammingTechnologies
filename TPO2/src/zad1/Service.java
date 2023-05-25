/**
 *
 * @author Woźnicki Piotr SO0139
 *
 */
package zad1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Service {
    private final String country;
    private final ObjectMapper objectMapper;
    private String currency;
    private String cityName;

    public Service(String country) {
        this.country = country;
        this.objectMapper = new ObjectMapper();
    }

    public String getWeather(String city) {
        cityName = city;
        String apiKey = "c59ef6657a4f0286f0c08c00becde61f";
        String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;
        return makeHttpRequest(urlString);
    }

    public Double getRateFor(String currencyCode) {
        currency = currencyCode;
        String urlString = "https://api.exchangerate.host/latest?base=" + country + "&symbols=" + currencyCode;
        String response = makeHttpRequest(urlString);
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(response);
        } catch (JsonProcessingException ignored) {
        }
        assert rootNode != null;
        JsonNode ratesNode = rootNode.get("rates");
        return ratesNode.get(currencyCode).asDouble();
    }

    public Double getNBPRate() {
        if (currency == null) {
            try {
                throw new Exception("Currency code is not set");
            } catch (Exception ignored) {
            }
        }

        String[] urls = {
                "https://www.nbp.pl/kursy/xml/lasta.xml",
                "https://www.nbp.pl/kursy/xml/lastb.xml"
        };
        Double rate = null;

        for (String url : urls) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
            Document document = null;
            try {
                document = builder.parse(new URL(url).openStream());
            } catch (SAXException | IOException ignored) {
            }

            assert document != null;
            NodeList currencyList = document.getElementsByTagName("kod_waluty");
            NodeList rateList = document.getElementsByTagName("kurs_sredni");

            for (int i = 0; i < currencyList.getLength(); i++) {
                Node currencyNode = currencyList.item(i);
                Node rateNode = rateList.item(i);

                if (currencyNode.getTextContent().equals(currency)) {
                    rate = Double.parseDouble(rateNode.getTextContent().replace(",", "."));
                    break;
                }
            }

            if (rate != null) {
                break;
            }
        }

        if (rate == null) {
            try {
                throw new Exception("Exchange rate not found for currency: " + currency);
            } catch (Exception ignored) {
            }
        }

        return rate;
    }


    private String makeHttpRequest(String urlString) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }
            }
        } catch (IOException ignored) {
        }
        return response.toString();
    }

    public String getCurrency() {
        return currency;
    }

    public String getCityName() {
        return cityName;
    }
}