package com.moneymap.expensetracker.transaction;

public class Transaction {
    private final int id;
    private final String type;
    private final String category;
    private final double amount;
    private final String date;

    public Transaction(int id, String type, String category, double amount, String date) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}