package ru.mastkey.randomfactsbot.model;

public class FactResponse {
    private String fact;

    public FactResponse(String fact) {
        this.fact = fact;
    }

    public FactResponse() {
    }

    public String getFact() {
        return fact;
    }

public void setFact(String fact) {
        this.fact = fact;
    }
}
