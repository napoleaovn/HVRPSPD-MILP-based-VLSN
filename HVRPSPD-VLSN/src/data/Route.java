package data;

import java.util.List;

public class Route {
	
	public Type vehicleType;
	public List<Node> nodes;
	public double totalCost;
	public double variableCost;
	public double fixedCost;
		
	public Route(Type vehicleType, List<Node> nodes, double[][] distances) {
		this.vehicleType = vehicleType;
		this.nodes = nodes;
		this.fixedCost = vehicleType.fixedCost;
		this.variableCost = variableCost(distances);
		this.totalCost = this.fixedCost + this.variableCost;
	}
	
	private double variableCost(double[][] distances) {
		double cost = 0;
		for (int i = 0; i < nodes.size() - 1; i++) {
			cost += distances[nodes.get(i).id][nodes.get(i+1).id] * vehicleType.variableCost;
		}
		return cost; 
	}
	
}

