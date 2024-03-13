package app.earningreportaction.controller;

import app.earningreportaction.model.EarningsDataModel;
import app.earningreportaction.model.StockOpenCloseModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.json.JSONArray;
import javafx.fxml.FXML;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.collections.FXCollections;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
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
    @FXML
    private Label stockTickerSymbol;
    @FXML
    private Label currentPriceLabel;
    @FXML
    private Label bidPriceLabel;
    @FXML
    private Label askPriceLabel;
    @FXML
    private Label volumeLabel;
    @FXML
    private Label openLabel;
    @FXML
    private Label closeLabel;
    @FXML
    private Label highLabel;
    @FXML
    private Label lowLabel;
    @FXML
    private Label windowTitle;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button maximizeButton;
    @FXML
    private Button closeButton;

    private final List<EarningsDataModel> allEarningsData = new ArrayList<EarningsDataModel>();

    private final HashMap<String, StockOpenCloseModel> checkedStocks = new HashMap<>();

    private static final String API_KEY = "pk_5eb2e76ca8544c9ab0b0115b4fbc1f75";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    private void initialize()  {
        // Initialize your data and set up the TableView
        //windowTitle.setText("Earnings Report Action Tool");
        LocalDate today = LocalDate.now();
        fetchEarningsCalendar();
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
                    setGraphic(null);
                    setStyle("");
                }
                else {
                    String[] lines = item.split("\n", -1);
                    TextFlow textFlow = new TextFlow();
                    if (lines.length > 0) {
                        // Make the first line bold
                        Text firstLine = new Text(lines[0] + "\n");
                        firstLine.setStyle("-fx-font-family: 'arial'; -fx-font-weight: bold; -fx-font-size: 16;");
                        textFlow.getChildren().add(firstLine);
                        // Add the rest of the lines as normal Text nodes
                        for (int i = 1; i < lines.length; i++) {
                            Text line = new Text(lines[i] + (i < lines.length - 1 ? "\n" : ""));
                            textFlow.getChildren().add(line);
                        }
                    }
                    setGraphic(textFlow);
                    // Apply alternating background color
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #7f4c9e;");
                    } else {
                        setStyle("-fx-background-color: #432c52; -fx-text-fill: white;");
                    }
                }
            }
        });
    }
    @FXML
    private void searchStock() {
        String symbol = searchField.getText().trim().toUpperCase();
        if (!symbol.isEmpty()) {
            getStock(symbol);
        }
    }
    @FXML
    private void handleStockClick(MouseEvent event) {
        String selectedStock = stocksListView.getSelectionModel().getSelectedItem();

        if (selectedStock != null) {
            // Handle the click event, for example, display the stock details
            getStock(selectedStock.split("\n")[0]);
        }
    }
    private void getStock(String symbol) {
        String uri = "https://api.iex.cloud/v1/data/core/iex_tops/"+symbol+"?token=" + API_KEY;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::processStockData)
                .join();

        StockOpenCloseModel sOCHL = checkedStocks.get(symbol);
        if (sOCHL == null){
            StockOpenCloseModel stock = fetchStockOCHL(symbol);
            checkedStocks.put(symbol, stock);
            openLabel.setText("Open: "+stock.getOpen());
            closeLabel.setText("Close: "+stock.getClose());
            highLabel.setText("High: "+stock.getHigh());
            lowLabel.setText("Low: "+stock.getLow());
        }
        else {
            openLabel.setText("Open: "+sOCHL.getOpen());
            closeLabel.setText("Close: "+sOCHL.getClose());
            highLabel.setText("High: "+sOCHL.getHigh());
            lowLabel.setText("Low: "+sOCHL.getLow());
        }
    }
    private void processStockData(String response) {
        if (response == null){
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(response);
            if (!jsonArray.isEmpty()) {
                JSONObject stockData = jsonArray.getJSONObject(0); // Assuming the first object is the one we need

                String symbol = stockData.optString("symbol");
                double bidPrice = stockData.optDouble("bidPrice");
                double askPrice = stockData.optDouble("askPrice");
                int bidSize = stockData.optIntegerObject("bidSize");
                int askSize = stockData.optIntegerObject("askSize");
                double lastSalePrice = stockData.optDouble("lastSalePrice");
                long volume = stockData.optLong("volume");

                // Convert timestamp to a human-readable format, if necessary
                long lastUpdated = stockData.optLong("lastUpdated");
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastUpdated));

                // Update your UI with the extracted data
                // This must be run on the JavaFX application thread
                Platform.runLater(() -> {
                    // Assuming 'stockTickerSymbol', 'stockGraph', and 'currentPrice' are fx:id in your FXML
                    stockTickerSymbol.setText(symbol); // Set the ticker symbol
                    currentPriceLabel.setText(String.format("$%.2f", lastSalePrice)); // Set the current price
                    bidPriceLabel.setText("Bid: " + String.format("$%.2f", bidPrice) + " x " + bidSize);
                    askPriceLabel.setText("Ask: " + String.format("$%.2f", askPrice) + " x " + askSize);
                    volumeLabel.setText("Volume: " + volume);
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to retrieve stock data");
        }
    }
    public void fetchEarningsCalendar() {
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
                    allEarningsData.add(new EarningsDataModel(symbol, title, earningsDate, time, importance));
                }
            });
        }
        catch (Exception e){
            System.out.println("Unable to get earnings calendar");
        }
    }
    public StockOpenCloseModel fetchStockOCHL(String stockTicker) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.polygon.io/v2/aggs/ticker/"+stockTicker+"/prev?adjusted=true&apiKey=kXovlxMMEqF0izpuvLEUUBWlc9KspgpA"))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());

            // Get the JSON array "results"
            JSONArray resultsArray = jsonObject.getJSONArray("results");
            // Check if the array is not empty and get the first object
            if (!resultsArray.isEmpty()) {
                JSONObject stockInfo = resultsArray.getJSONObject(0);

                String symbol = stockInfo.getString("T");
                Double open = stockInfo.getDouble("o");
                Double close = stockInfo.getDouble("c");
                Double high = stockInfo.getDouble("h");
                Double low = stockInfo.getDouble("l");
                // Assuming StockOpenCloseModel constructor is defined as
                // StockOpenCloseModel(String symbol, Double open, Double close, Double high, Double low)
                return new StockOpenCloseModel(symbol, open, close, high, low);
            }
        }
        catch (Exception e) {
            System.out.println("Unable to get stockOCHL data for "+stockTicker);
        }
        return null;
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
    public void minimizeWindow(ActionEvent event) {
        ((Stage)((Button)event.getSource()).getScene().getWindow()).setIconified(true);
    }

    public void maximizeWindow(ActionEvent event) {
        Stage stage = ((Stage)((Button)event.getSource()).getScene().getWindow());
        stage.setFullScreen(!stage.isFullScreen());
    }

    public void closeWindow(ActionEvent event) {
        ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
    }
}
