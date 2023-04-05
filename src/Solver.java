import data.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import ilog.concert.*;

public class Solver {

	public static void main(String[] args) {

		Locale.setDefault(Locale.US);
		
		int inputFormat = 1;
		String[] filenames = {
		"instance101", "instance102", "instance103", "instance104", "instance105", "instance106","instance107",
		"instance108", "instance109", "instance110", "instance111", "instance112", "instance113", "instance114",
		"instance201", "instance202", "instance203", "instance204", "instance205", "instance206", "instance207",
		"instance208", "instance209", "instance210", "instance211", "instance212", "instance213", "instance214"};

		int[] subprobTimeLimits = {5, 10, 20, 30, 60};
		int numberOfExecutions = 10;
		Random randomGenerator = new Random();
		int timeLimit = 3600;

		for (String filename: filenames) {
			for (int exec = 1; exec <= numberOfExecutions; exec++) {
				for (int solverTimeLimit : subprobTimeLimits) {
					try {
						String fileDirectory = "./solution/" + filename + String.format("/limit-%02d", solverTimeLimit) + String.format("/exec-%02d", exec);
						new File(fileDirectory).mkdirs();			
						Data data = new Data(filename, inputFormat);
						double startTime = System.currentTimeMillis();
						double processTime;
						double timeToBest = 0;
						int iterToBest = 0;
						int s = 0;
						Solution bestSolution = new Greedy(data, 1.00, 60).solution;
						processTime = (System.currentTimeMillis() - startTime) / 1000;
						bestSolution.exportSolution(fileDirectory + "/" + filename + String.format("-%06d", s) + ".sol", processTime, -1, 0.00);
						int n, r, t, r1, r2, n1, n2;
						Node node1, node2;
						Route route, route1, route2;
						double[] insertionRate = {0.05, 0.05, 0.05, 0.05};
						int[] operationCounter = new int[4];
						int[] improveCounter = new int[4];
						double[] improveRate = new double[4];
						double[] operationProb = new double[4];
						int operation;
						
						do {
							int[] nextVehicles = bestSolution.vehicles.clone();
							for (t = 0; t < data.nTypes; t++) {
								nextVehicles[t]++;
							}

							int[][] nextMatrix = Arrays.stream(bestSolution.matrix).map(int[]::clone).toArray(int[][]::new);								
							
							double sumOfRates = 0;
							for (int i = 0; i < operationCounter.length; i++) {
								improveRate[i] = (improveCounter[i] + 1.0) / (operationCounter[i] + 1.0);
								sumOfRates += improveRate[i];
							}
							for (int i = 0; i < operationCounter.length; i++) {
								operationProb[i] = improveRate[i] / sumOfRates;
							}
							for (int i = 1; i < operationCounter.length; i++) {
								operationProb[i] = operationProb[i] + operationProb[i-1];
							}
							if (bestSolution.routes.size() == 1) {
								operation = 0;
							} else {
								double roulete = randomGenerator.nextDouble();
								if (roulete < operationProb[0]) {
									operation = 0;
								} else if (roulete < operationProb[1]) {
									operation = 1;
								} else if (roulete < operationProb[2]) {
									operation = 2;
								} else {
									operation = 3;
								}
							}
							switch (operation) {
							case 0:
								//ROUTE IMPROVEMENT
								//for (r = 0; r < currentSolution.routes.size(); r++) {
									//route = currentSolution.routes.get(r);
									r = randomGenerator.nextInt(bestSolution.routes.size());
									route = bestSolution.routes.get(r);
									// clique within route
									for (n1 = 0; n1 < route.nodes.size(); n1++) { 
										node1 = route.nodes.get(n1);
										for (n2 = n1+1; n2 < route.nodes.size()-1; n2++) {
											node2 = route.nodes.get(n2);
											if (randomGenerator.nextDouble() < insertionRate[operation]) {
												nextMatrix[node1.id][node2.id] = 1;
											}
											if (randomGenerator.nextDouble() < insertionRate[operation]) {
												nextMatrix[node2.id][node1.id] = 1;
											}
										}
									}
								//}
								break;
							case 1:
								//ROUTES COMMUTATION
								List<Node> commuters = new ArrayList<Node>();
								List<Node> previous = new ArrayList<Node>();
								List<Node> next = new ArrayList<Node>();
								// determination of the minimum size of the routes
								int minSize = bestSolution.routes.get(0).nodes.size(); 
								for (r = 1; r < bestSolution.routes.size(); r++) {
									route = bestSolution.routes.get(r);
									minSize = Math.min(minSize, route.nodes.size());
								}
								// index n is between 1 and the index value of the last customer of the smallest route
								n = randomGenerator.nextInt(minSize-2)+1;  
								// determination of previous/commuters/next nodes 
								for (r = 0; r < bestSolution.routes.size(); r++) {
									route = bestSolution.routes.get(r);
									commuters.add(route.nodes.get(n));
									if (n+1 < route.nodes.size()-1) { //valid customer
										commuters.add(route.nodes.get(n+1));
									}
									previous.add(route.nodes.get(n-1));
									if (n+2 < route.nodes.size()) { //valid node
										next.add(route.nodes.get(n+2));
									} else {
										next.add(route.nodes.get(n+1)); // node 0
									}
								}
								
								// clique within commuters
								for (n1 = 0; n1 < commuters.size(); n1++) {
									node1 = commuters.get(n1);
									for (n2 = n1+1; n2 < commuters.size(); n2++) {
										node2 = commuters.get(n2);
										if (randomGenerator.nextDouble() < insertionRate[operation]) {
											nextMatrix[node1.id][node2.id] = 1;
										}
										if (randomGenerator.nextDouble() < insertionRate[operation]) {
											nextMatrix[node2.id][node1.id] = 1;
										}
									}
								}
								// links from previous to commuters
								for (n1 = 0; n1 < previous.size(); n1++) {
									node1 = previous.get(n1);
									for (n2 = 0; n2 < commuters.size(); n2++) {
										node2 = commuters.get(n2);
										if (randomGenerator.nextDouble() < insertionRate[operation]) {
											nextMatrix[node1.id][node2.id] = 1;
										}
									}
								}
								// links from commuters to next
								for (n1 = 0; n1 < commuters.size(); n1++) {
									node1 = commuters.get(n1);
									for (n2 = 0; n2 < next.size(); n2++) {
										node2 = next.get(n2);
										if (randomGenerator.nextDouble() < insertionRate[operation]) {
											nextMatrix[node1.id][node2.id] = 1;
										}
									}
								}
								break;
							case 2:
								//ROUTES AGGLUTINATION
								r1 = randomGenerator.nextInt(bestSolution.routes.size());
								r2 = randomGenerator.nextInt(bestSolution.routes.size());
								while (r1 == r2) {
									r2 = randomGenerator.nextInt(bestSolution.routes.size());
								}
								route1 = bestSolution.routes.get(r1);
								route2 = bestSolution.routes.get(r2);
								// clique within route1
								for (n1 = 0; n1 < route1.nodes.size(); n1++) {
									node1 = route1.nodes.get(n1);
									for (n2 = n1+1; n2 < route1.nodes.size()-1; n2++) {
										node2 = route1.nodes.get(n2);
										if (randomGenerator.nextDouble() < insertionRate[operation]) {
											nextMatrix[node1.id][node2.id] = 1;
										}
										if (randomGenerator.nextDouble() < insertionRate[operation]) {
											nextMatrix[node2.id][node1.id] = 1;
										}
									}
								}
								// clique within route2
								for (n1 = 0; n1 < route2.nodes.size(); n1++) {
									node1 = route2.nodes.get(n1);
									for (n2 = n1+1; n2 < route2.nodes.size()-1; n2++) {
										node2 = route2.nodes.get(n2);
										if (randomGenerator.nextDouble() < insertionRate[operation]) {
											nextMatrix[node1.id][node2.id] = 1;
										}
										if (randomGenerator.nextDouble() < insertionRate[operation]) {
											nextMatrix[node2.id][node1.id] = 1;
										}
									}
								}
								// cross links between route1 and route2
								for (n1 = 1; n1 < route1.nodes.size()-1; n1++) {
									node1 = route1.nodes.get(n1);
									for (n2 = 1; n2 < route2.nodes.size()-1; n2++) {
										node2 = route2.nodes.get(n2);
										if (randomGenerator.nextDouble() < insertionRate[operation]) {
											nextMatrix[node1.id][node2.id] = 1;
										}
										if (randomGenerator.nextDouble() < insertionRate[operation]) {
											nextMatrix[node2.id][node1.id] = 1;
										}
									}
								}
								break;
							case 3:
								//CUSTOMERS CLIQUE
								List<Node> clique = new ArrayList<Node>(Arrays.asList(data.nodes));
								clique.remove(0);
								while (clique.size() > data.nNodes * insertionRate[operation]) {
									clique.remove(randomGenerator.nextInt(clique.size()));
								}
								// clique within customers
								for (n1 = 0; n1 < clique.size(); n1++) {
									node1 = clique.get(n1);
									for (n2 = n1+1; n2 < clique.size(); n2++) {
										node2 = clique.get(n2);
										nextMatrix[node1.id][node2.id] = 1;
										nextMatrix[node2.id][node1.id] = 1;
									}
								}
								break;
							}
							
							Solution nextSolution = null;
							Model nextModel = new Model(data, nextMatrix, nextVehicles, bestSolution, solverTimeLimit);
							if (nextModel.solveModel()) {
								nextSolution = nextModel.solution;
							}
							nextModel.finalizeModel();

							processTime = (System.currentTimeMillis() - startTime) / 1000;

							operationCounter[operation]++;

							if (nextSolution != null) {
								nextSolution.exportSolution(fileDirectory + "/" + filename + String.format("-%06d", ++s) + ".sol", processTime, operation, insertionRate[operation]);
								if (nextSolution.totalCost < bestSolution.totalCost - 0.01) {
									bestSolution = nextSolution;
									improveCounter[operation]++;
									timeToBest = (System.currentTimeMillis() - startTime) / 1000;
									iterToBest = s;
								}
								if (nextSolution.gap < 0.01) {
									if (insertionRate[operation] <= 0.01) {
										insertionRate[operation] = 0.05;
									} else {
										insertionRate[operation] = Math.min(insertionRate[operation] + 0.05, 1.00);
									}
								} else {
									if (nextSolution.gap > 0.05) {
										insertionRate[operation] = Math.max(insertionRate[operation] - 0.05, 0.01);
									}
								}
							} else {
								bestSolution.exportSolution(fileDirectory + "/" + filename + String.format("-%06d", ++s) + ".sol", processTime, operation, insertionRate[operation]);
								System.err.println("no solution at iteration " + s);
								insertionRate[operation] = Math.max(insertionRate[operation] - 0.05, 0.01);
							}
							
						} while (processTime <= timeLimit && s - iterToBest < 100);
						
						PrintStream printer = new PrintStream(fileDirectory + "/"+ filename + ".cnt");
						printer.printf("%-30s%8d%8d%8.2f\n", "ROUTE IMPROVEMENT", operationCounter[0], improveCounter[0], insertionRate[0]);
						printer.printf("%-30s%8d%8d%8.2f\n", "ROUTES COMMUTATION", operationCounter[1], improveCounter[1], insertionRate[1]);
						printer.printf("%-30s%8d%8d%8.2f\n", "ROUTES AGGLUTINATION", operationCounter[2], improveCounter[2], insertionRate[2]);
						printer.printf("%-30s%8d%8d%8.2f\n", "CUSTOMERS CLIQUE", operationCounter[3], improveCounter[3], insertionRate[3]);
						printer.printf("%-30s%8.2f\n", "BEST COST", bestSolution.totalCost);
						printer.printf("%-30s%8.2f\n", "PROCESS TIME", processTime);
						printer.printf("%-30s%8.2f\n", "TIME TO BEST", timeToBest);
						printer.printf("%-30s%8d\n", "ITERATIONS", s);
						printer.printf("%-30s%8d\n", "ITERATIONS TO BEST", iterToBest);
						printer.close();
					}
					catch (IOException exc1) {
						System.err.println(exc1);
					}
					catch (IloException exc2) {
						System.err.println(exc2);
					}
					catch (OutOfMemoryError exc3) {
						System.err.println(exc3);
					}
				}
			}
		}
	}
}
