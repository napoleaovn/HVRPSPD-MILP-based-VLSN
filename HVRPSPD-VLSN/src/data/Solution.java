package data;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class Solution {
	
	public double totalCost;
	public double objective;
	public double bound;
	public double gap;
	public String status;
	public double creationTime;
	public double solvingTime;
	public int[][] matrix;
	public int[] vehicles;
	public List<Route> routes;
			
	public Solution(int[][] matrix, int[] vehicles, List<Route> routes, double objective, double bound,
			double gap,	String status, double creationTime, double solvingTime) {
		this.matrix = matrix;
		this.vehicles = vehicles;
		this.routes = routes;
		this.totalCost = totalCost();
		this.objective = objective;
		this.bound = bound;
		this.gap = gap;
		this.status = status;
		this.creationTime = creationTime;
		this.solvingTime = solvingTime;
	}
	
	private double totalCost() {
		double cost = 0;
		for (int r = 0; r < routes.size(); r++) {
			Route route = routes.get(r);
			cost += route.totalCost;
		}
		return cost; 
	}
	
	public void exportSolution(String filename, double processTime, int operation, double rate) throws IOException {
		PrintStream printer = new PrintStream(filename);
		printer.printf("%-15s%15.2f\n", "Total cost:", totalCost);
		printer.printf("%-15s%15.2f\n", "Objetive:", objective);
		printer.printf("%-15s%15.2f\n", "Lower bound:", bound);
		printer.printf("%-15s%15.4f\n", "Gap:", gap);
		printer.printf("%-15s%15s\n", "Status:", status);
		printer.printf("%-15s%15d\n", "Operation:", operation);
		printer.printf("%-15s%15.2f\n", "Insertion rate:", rate);
		printer.printf("%-15s%15.2f\n", "Creation time:", creationTime);
		printer.printf("%-15s%15.2f\n", "Solving time:", solvingTime);
		printer.printf("%-15s%15.2f\n", "Process time:", processTime);
		printer.println("------------------------------");
		for (int r = 0; r < routes.size(); r++) {
			Route route = routes.get(r);
			printer.printf("%-15s%d\n", "Vehicle type:", route.vehicleType.id);
			printer.printf("%-15s", "Route: ");
			for (int n = 0; n < route.nodes.size(); n++) {
				Node node = route.nodes.get(n);
				printer.printf("%5d", node.id);
			}
			printer.println();
			printer.println("------------------------------");
		}
		printer.close();
	}
	
}
