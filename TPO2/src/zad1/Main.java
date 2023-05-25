/**
 *
 * @author Woźnicki Piotr SO0139
 *
 */

package zad1;


import javafx.application.Application;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;

public class Main extends Application {
    Service s = new Service("Italy");
    String weatherJson = s.getWeather("Rome");
    Double rate1 = s.getRateFor("USD");
    Double rate2 = s.getNBPRate();

    public static void main(String[] args) {
        launch(args);
    }

    public static void showWikiPageForCity(String city) {
        WikiPage.showWikiPage(city);
    }

    @Override
    public void start(Stage stage) {
        JFrame jFrame = new JFrame("Pogoda i Waluta informacja");
        jFrame.setSize(800, 600);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel jPanel = new JPanel(new GridLayout(3, 2));
        JLabel nbp = new JLabel("Przewalutowanie wedlug NBP na PLN:");
        JTextField nbpField = new JTextField(rate2.toString());
        nbpField.setEditable(false);
        JLabel rateLabel = new JLabel("Przewalutowanie " + s.getCurrency() + " na PLN:");
        JTextField rateField = new JTextField(rate1.toString());
        rateField.setEditable(false);
        JLabel weatherLabel = new JLabel("Pogoda w " + s.getCityName() + ":");
        JTextArea weatherArea = new JTextArea(weatherJson);
        weatherArea.setEditable(false);
        jPanel.add(weatherLabel);
        jPanel.add(weatherArea);
        jPanel.add(rateLabel);
        jPanel.add(rateField);
        jPanel.add(nbp);
        jPanel.add(nbpField);
        jFrame.add(jPanel);
        jFrame.setVisible(true);
        showWikiPageForCity(s.getCityName());
    }
}
