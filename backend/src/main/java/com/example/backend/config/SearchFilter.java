package com.example.backend.config;

import java.util.List;

public class SearchFilter {
    private int id;
    private List<Object> selects;
    private String input;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Object> getSelects() {
        return selects;
    }

    public void setSelects(List<Object> selects) {
        this.selects = selects;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}
