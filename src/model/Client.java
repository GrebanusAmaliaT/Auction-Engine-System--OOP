package src.model;

public class Client {
    private int id;
    private String name;
    private double budget;

    public Client(int id, String name, double budget) {
        this.id = id;
        this.name = name;
        this.budget = budget;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getBudget() { return budget; }

    public void setBudget(double budget) { this.budget = budget; }
}