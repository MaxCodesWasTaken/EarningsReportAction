package app.earningreportaction.controller;

import app.earningreportaction.model.EarningsDataModel;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.json.JSONObject;
import org.json.JSONArray;
import javafx.fxml.FXML;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.FXCollections;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class MainController {
    @FXML
    private TextField searchField;
    @FXML
    private TextArea resultArea;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> importanceComboBox;
    @FXML
    private ListView<String> stocksListView;

    private List<EarningsDataModel> allEarningsData;
    private static final String API_KEY = "pk_5eb2e76ca8544c9ab0b0115b4fbc1f75";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    private void initialize()  {
        // Initialize your data and set up the TableView
        LocalDate today = LocalDate.now();
        allEarningsData = fetchEarningsCalendar();
        importanceComboBox.getItems().addAll("All Stocks", "3 and above", "Market Movers");
        List<String> parsedEarningsData = parseEarnings(allEarningsData, today);
        datePicker.setValue(LocalDate.now());
        importanceComboBox.setValue("All Stocks");
        if(!allEarningsData.isEmpty()) {
            stocksListView.setItems(FXCollections.observableArrayList(parsedEarningsData));
        }
        stocksListView.setCellFactory(lv -> new ListCell<String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: lightblue;");
                    } else {
                        setStyle("-fx-background-color: #629dfc; -fx-text-fill: white;");
                    }
                }
            }
        });
    }
    @FXML
    private void searchStock() {
        String symbol = searchField.getText().trim();
        if (!symbol.isEmpty()) {
            getStockPrice(symbol);
        }
    }
    @FXML
    private void handleStockClick(MouseEvent event) {
        String selectedStock = stocksListView.getSelectionModel().getSelectedItem();
        if (selectedStock != null) {
            // Handle the click event, for example, display the stock details
            resultArea.setText("You selected: " + selectedStock);
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
        if (!jsonArray.isEmpty()) {
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
    public List<EarningsDataModel> fetchEarningsCalendar() {
        List<EarningsDataModel> earningsList = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.stocktwits.com/api/2/discover/earnings_calendar"))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());
            JSONObject earnings = jsonObject.getJSONObject("earnings");
            earnings.keys().forEachRemaining(date -> {
                JSONObject dateEarnings = earnings.getJSONObject(date);
                JSONArray stocks = dateEarnings.getJSONArray("stocks");
                for (int i = 0; i < stocks.length(); i++) {
                    JSONObject stock = stocks.getJSONObject(i);
                    String symbol = stock.getString("symbol");
                    String title = stock.getString("title");
                    String earningsDate = stock.getString("date");
                    String time = stock.getString("time");
                    Integer importance = stock.getInt("importance");
                    earningsList.add(new EarningsDataModel(symbol, title, earningsDate, time, importance));
                }
            });
        }
        catch (Exception e){
            System.out.println("Unable to get earnings calendar");
        }
        return earningsList;
    }
    private List<String> parseEarnings (List<EarningsDataModel> earnings, LocalDate datetime){
        List<String> formatted = new ArrayList<>();
        List<EarningsDataModel> filteredEarningsData = new ArrayList<>(earnings.stream()
                .filter(data -> !LocalDate.parse(data.getDate()).isBefore(datetime))
                .toList());
        Collections.sort(filteredEarningsData);
        for(EarningsDataModel stock: filteredEarningsData){
            formatted.add(stock.toString());
        }
        return formatted;
    }
    @FXML
    private void handleFilterAction() {
        String selectedImportance = importanceComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        Stream<EarningsDataModel> stream = allEarningsData.stream();

        if (selectedImportance != null && !selectedImportance.equals("All Stocks")) {
            if (selectedImportance.equals("3 and above")) {
                stream = stream.filter(data -> data.getImportance() >= 3);
            }
            else {
                stream = stream.filter(data -> data.getImportance().equals(5));
            }
        }
        if (selectedDate != null) {
            stream = stream.filter(data -> LocalDate.parse(data.getDate()).isEqual(selectedDate));
        }

        List<EarningsDataModel> filteredData = stream.sorted().collect(toList());
        updateListView(filteredData);
    }

    private void updateListView(List<EarningsDataModel> data) {
        List<String> displayData = data.stream()
                .map(EarningsDataModel::toString) // Assuming you've overridden toString() method in EarningsDataModel for display
                .collect(Collectors.toList());
        stocksListView.setItems(FXCollections.observableArrayList(displayData));
    }
}
