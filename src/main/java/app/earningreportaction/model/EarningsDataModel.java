package app.earningreportaction.model;

import java.util.Objects;

/**
 * @param time Add other fields as needed
 */
public record EarningsDataModel(String symbol, String title, String date, String time, Integer importance) implements Comparable<EarningsDataModel> {
    // Constructor, getters, and setters
    public EarningsDataModel {
        Objects.requireNonNull(symbol);
        Objects.requireNonNull(title);
        Objects.requireNonNull(date);
        Objects.requireNonNull(time);
        Objects.requireNonNull(importance);
    }
    public String getSymbol() {
        return symbol;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public Integer getImportance() {
        return importance;
    }

    public String toString() {
        return symbol + '\n' + title + '\n' + date + ' ' + time + "\nImportance: " + importance;
    }

    public int compareTo(EarningsDataModel other){ // priority {date, time, importance, symbol}
        int dateComparison = this.getDate().compareTo(other.getDate());
        if (dateComparison != 0) {
            return dateComparison;
        }
        int importanceComparison = this.getImportance().compareTo(other.getImportance());
        if (importanceComparison != 0) {
            return -importanceComparison;
        }
        return this.getSymbol().compareTo(other.getSymbol());
    }
}
