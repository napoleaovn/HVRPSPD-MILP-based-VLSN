package data;

public class Type {
	
	public int id;
	public double capacity;
	public double variableCost;
	public double fixedCost;
		
	public Type(int id, double capacity, double variableCost, double fixedCost) {
		this.id = id;
		this.capacity = capacity;
		this.variableCost = variableCost;
		this.fixedCost = fixedCost;
	}
	
}