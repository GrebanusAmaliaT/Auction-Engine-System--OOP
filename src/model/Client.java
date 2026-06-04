package src.model;

public class Client {
    private int id;
    private String name;
    private double budget;
    private boolean isNpc;

    public Client(int id, String name, double budget) {
        this.id = id;
        this.name = name;
        this.budget = budget;
    }

    public Client(int id, String name, double budget, boolean isNpc) {
        this.id = id;
        this.name = name;
        this.budget = budget;
        this.isNpc = isNpc;
    }

    public Client(String name, double budget, boolean isNpc) {
        this.name = name;
        this.budget = budget;
        this.isNpc = isNpc;
    }

    public boolean isNpc() {
        return isNpc;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public void setNpc(boolean isNpc) {
        this.isNpc = isNpc;
    }
}