package app.earningreportaction;

import javafx.application.Platform;
import org.json.JSONObject;
import org.json.JSONArray;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class MainController {
    @FXML
    private TextField searchField;
    @FXML
    private TextArea resultArea;

    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    private void searchStock() {
        String symbol = searchField.getText().trim();
        if (!symbol.isEmpty()) {
            getStockPrice(symbol);
        }
    }
    private void getStockPrice(String symbol) {
        String uri = "https://cloud.iexapis.com/stable/tops?token=" + API_KEY + "&symbols=" + symbol;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::processStockData)
                .join();
    }
    private void processStockData(String response) {
        // Parse the response using org.json or another JSON library
        JSONArray jsonArray = new JSONArray(response);
        if (jsonArray.isEmpty()) {
            JSONObject stockData = jsonArray.getJSONObject(0); // Assuming the first object is the one we need

            String symbol = stockData.getString("symbol");
            double bidPrice = stockData.getDouble("bidPrice");
            double askPrice = stockData.getDouble("askPrice");
            double lastSalePrice = stockData.getDouble("lastSalePrice");
            long volume = stockData.getLong("volume");

            // Update your UI with the extracted data
            // This must be run on the JavaFX application thread
            Platform.runLater(() -> {
                resultArea.setText("Symbol: " + symbol + "\nBid Price: " + bidPrice
                        + "\nAsk Price: " + askPrice + "\nLast Sale Price: "
                        + lastSalePrice + "\nVolume: " + volume);
            });
        }
    }
}