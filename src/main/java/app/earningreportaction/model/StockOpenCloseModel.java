package app.earningreportaction.model;

import java.util.Objects;

public record StockOpenCloseModel(String symbol, Double open, Double close, Double high,  Double low) {
    // Constructor, getters, and setters
    public StockOpenCloseModel {
        Objects.requireNonNull(symbol);
        Objects.requireNonNull(open);
        Objects.requireNonNull(close);
        Objects.requireNonNull(high);
        Objects.requireNonNull(low);
    }
    public String getSymbol() {
        return symbol;
    }

    public Double getClose() {
        return close;
    }

    public Double getHigh() {
        return high;
    }

    public Double getLow() {
        return low;
    }

    public Double getOpen() {
        return open;
    }

    public int compareTo(StockOpenCloseModel other){
        return this.getSymbol().compareTo(other.getSymbol());
    }
}
