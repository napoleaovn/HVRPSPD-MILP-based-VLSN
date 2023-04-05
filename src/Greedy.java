import data.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Greedy {
	
	Solution solution;

	public Greedy(Data data, double nearestNeighborProbability, int timeLimit) {
		double startTime = System.currentTimeMillis();
		Random randomGenerator = new Random();
		double currentTime = 0.0;
		while (currentTime <= timeLimit) {
			int[][] solutionMatrix = new int[data.nNodes][data.nNodes];
			int[] solutionVehicles = new int[data.nTypes];
			List<Route> routes = new ArrayList<Route>();
			List<Node> freeNodes = new ArrayList<Node>(Arrays.asList(data.nodes));
			freeNodes.remove(0);
			Collections.shuffle(freeNodes);
			double totalCost = 0;
			while (!freeNodes.isEmpty()) {
				int t = randomGenerator.nextInt(data.nTypes);
				solutionVehicles[t]++;
				Type type = data.types[t];
				double capacity = type.capacity;
				double fixedCost = type.fixedCost;
				double initialLoad = 0;
				totalCost += fixedCost; 
				List<Node> nodes = new ArrayList<Node>();
				nodes.add(data.nodes[0]);	
				Node end = data.nodes[0];
				boolean first = true;
				boolean feasibleNodes = true;
				for (int n = 0; n < freeNodes.size(); n++) {
					Node next = freeNodes.get(n);
					next.infeasible = false;
				}
				while (feasibleNodes) {
					Node trial = null;
					int trialIndex = -1;
					if (!first && randomGenerator.nextDouble() < nearestNeighborProbability) {
						double minDistance = Double.MAX_VALUE;
						for (int n = 0; n < freeNodes.size(); n++) {
							Node next = freeNodes.get(n);
							if (! next.infeasible) {
								if (data.distances[end.id][next.id] < minDistance) {
									minDistance = data.distances[end.id][next.id];
									trial = next;
									trialIndex = n;
								}
							}
						}
					} else {
						for (int n = 0; n < freeNodes.size(); n++) {
							Node next = freeNodes.get(n);
							if (! next.infeasible) {
								trial = next;
								trialIndex = n;
								first = false;
								break;
							}
						}
					}
					double loadTrial = initialLoad + trial.delivery;
					if (loadTrial > capacity) {
						trial.infeasible = true;
					} else {
						boolean feasibleRoute = true;
						for (int m = 1; m < nodes.size(); m++) {
							Node next = nodes.get(m);
							loadTrial += next.pickup - next.delivery;
							if (loadTrial > capacity) {
								trial.infeasible = true;
								feasibleRoute = false;
								break;
							}
						}
						if (feasibleRoute) {
							loadTrial += trial.pickup - trial.delivery;
							if (loadTrial > capacity) {
								trial.infeasible = true;
							} else {
								solutionMatrix[end.id][trial.id] = 1;
								totalCost += data.distances[end.id][trial.id] * type.variableCost;
								nodes.add(freeNodes.remove(trialIndex));
								end = trial;
								initialLoad += trial.delivery;
							}
						} else {
							trial.infeasible = true;
						}
					}
					feasibleNodes = false;
					for (int n = 0; n < freeNodes.size(); n++) {
						Node next = freeNodes.get(n);
						if (! next.infeasible) {
							feasibleNodes = true;
							break;
						}
					}
				}
				solutionMatrix[nodes.get(nodes.size()-1).id][nodes.get(0).id] = 1;
				totalCost += data.distances[nodes.get(nodes.size()-1).id][nodes.get(0).id] * type.variableCost;
				nodes.add(nodes.get(0));
				routes.add(new Route(type, nodes, data.distances));
			}
			
			if ((solution == null) || totalCost < solution.totalCost) {
				solution = new Solution(solutionMatrix, solutionVehicles, routes, totalCost, totalCost, 0.0, "Optimal", 0.0, 0.0);
			}

			currentTime = (System.currentTimeMillis() - startTime) / 1000;
		}
		
		solution.solvingTime = currentTime;
		
	}
	
}