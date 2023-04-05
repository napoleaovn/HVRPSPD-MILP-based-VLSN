import data.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import ilog.concert.*;
import ilog.cplex.*;

public class Model {
	
	private Data data;
	private IloCplex cplex;
	private IloIntVar[][] x;
	private IloNumVar[][] y;
	private IloNumVar[][] z;
	private double startTime;
	private double modelCreationTime;
	private double modelSolvingTime;
	Solution solution;
	List<Link> links;
	int[][] matrix;
	int[] vehicles;

	public Model(Data data, int[][] matrix, int[] vehicles, Solution solution, double timeLimit) throws IloException, FileNotFoundException {
		
		startTime = System.currentTimeMillis();

		this.data = data;
		this.matrix = matrix;
		this.vehicles = vehicles;
		
		//cria links
		links = new ArrayList<Link>();
		for (int i = 0; i < data.nNodes; i++) {
			for (int j = 0; j < data.nNodes; j++) {
				if (i != j && matrix[i][j] >= 1) {
					Link link = new Link(data.nodes[i], data.nodes[j], data.distances[i][j]);
					links.add(link);
				}
			}
		}

		//total de veiculos
		int qty = 0; int k;
		for (int t = 0; t < data.nTypes; t++) {
			qty += vehicles[t];
		}
		
		cplex = new IloCplex();
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 0);
		//cplex.setParam(IloCplex.IntParam.MIPEmphasis, 1);
		cplex.setParam(IloCplex.DoubleParam.TiLim, timeLimit);

		x = new IloIntVar[links.size()][qty];
		y = new IloNumVar[links.size()][qty];
		z = new IloNumVar[links.size()][qty];
		for (int l = 0; l < links.size(); l++) {				
			Link link = links.get(l);
			k = 0;
			for (int t = 0; t < data.nTypes; t++) {				
				for (int v = 0; v < vehicles[t]; v++) {
					String varName = "(" + link.s.id + "," + link.t.id + "," + k + "," + t + ")";
					x[l][k] = cplex.boolVar("x" + varName);
					y[l][k] = cplex.numVar(0, Double.MAX_VALUE, "y" + varName);
					z[l][k] = cplex.numVar(0, Double.MAX_VALUE, "z" + varName);
					k++;
				}
			}
		}		
						
		IloLinearNumExpr expression = cplex.linearNumExpr();
		String exprName = "expression01";
		for (int l = 0; l < links.size(); l++) {
			Link link = links.get(l);
			k = 0;
			for (int t = 0; t < data.nTypes; t++) {				
				for (int v = 0; v < vehicles[t]; v++) {
					if (link.s.id == 0) {
						expression.addTerm(data.types[t].fixedCost, x[l][k]);
					}
					expression.addTerm(data.types[t].variableCost * link.distance, x[l][k]);
					k++;
				}
			}
		}
		cplex.addMinimize(expression, exprName);

		for (int j = 1; j < data.nNodes; j++) {
			expression = cplex.linearNumExpr();
			exprName = "expression02(" + j + ")";
			for (int l = 0; l < links.size(); l++) {
				Link link = links.get(l);
				if (link.t.id == j) {
					k = 0;
					for (int t = 0; t < data.nTypes; t++) {				
						for (int v = 0; v < vehicles[t]; v++) {
							expression.addTerm(1, x[l][k]);
							k++;
						}
					}
				}
			}
			cplex.addEq(expression, 1, exprName);
		}

		for (int p = 0; p < data.nNodes; p++) {
			k = 0;
			for (int t = 0; t < data.nTypes; t++) {				
				for (int v = 0; v < vehicles[t]; v++) {
					expression = cplex.linearNumExpr();
					exprName = "expression03(" + p + "," + k + ")";
					for (int l = 0; l < links.size(); l++) {
						Link link = links.get(l);
						if (link.t.id == p) {
							expression.addTerm(1, x[l][k]);
						}
						if (link.s.id == p) {
							expression.addTerm(-1, x[l][k]);
						}
					}
					cplex.addEq(expression, 0, exprName);
					k++;
				}
			}
		}

		k = 0;
		for (int t = 0; t < data.nTypes; t++) {				
			for (int v = 0; v < vehicles[t]; v++) {
				expression = cplex.linearNumExpr();
				exprName = "expression04(" + k + ")";
				for (int l = 0; l < links.size(); l++) {
					Link link = links.get(l);
					if (link.s.id == 0) {
						expression.addTerm(1, x[l][k]);
					}
				}
				cplex.addLe(expression, 1, exprName);
				k++;
			}
		}

		for (int j = 1; j < data.nNodes; j++) {
			k = 0;
			for (int t = 0; t < data.nTypes; t++) {				
				for (int v = 0; v < vehicles[t]; v++) {
					expression = cplex.linearNumExpr();
					exprName = "expression05(" + j + "," + k + ")";
					for (int l = 0; l < links.size(); l++) {
						Link link = links.get(l);
						if (link.s.id == 0 && link.t.id == j) {
							expression.addTerm(1, y[l][k]);
						}
					}
					cplex.addEq(expression, 0, exprName);
					k++;
				}
			}
		}

		for (int i = 1; i < data.nNodes; i++) {
			k = 0;
			for (int t = 0; t < data.nTypes; t++) {				
				for (int v = 0; v < vehicles[t]; v++) {
					expression = cplex.linearNumExpr();
					exprName = "expression06(" + i + "," + k + ")";
					for (int l = 0; l < links.size(); l++) {
						Link link = links.get(l);
						if (link.t.id == 0 && link.s.id == i) {
							expression.addTerm(1, z[l][k]);
						}
					}
					cplex.addEq(expression, 0, exprName);
					k++;
				}
			}
		}

		for (int p = 1; p < data.nNodes; p++) {
			Node node = data.nodes[p];
			expression = cplex.linearNumExpr();
			exprName = "expression07(" + p + ")";
			for (int l = 0; l < links.size(); l++) {
				Link link = links.get(l);
				if (link.s.id == p) {
					k = 0;
					for (int t = 0; t < data.nTypes; t++) {				
						for (int v = 0; v < vehicles[t]; v++) {
							expression.addTerm(1, y[l][k]);
							k++;
						}
					}
				}
				if (link.t.id == p) {
					k = 0;
					for (int t = 0; t < data.nTypes; t++) {				
						for (int v = 0; v < vehicles[t]; v++) {
							expression.addTerm(-1, y[l][k]);
							k++;
						}
					}
				}
			}
			cplex.addEq(expression, node.pickup, exprName);
		}

		for (int p = 1; p < data.nNodes; p++) {
			Node node = data.nodes[p];
			expression = cplex.linearNumExpr();
			exprName = "expression08(" + p + ")";
			for (int l = 0; l < links.size(); l++) {
				Link link = links.get(l);
				if (link.t.id == p) {
					k = 0;
					for (int t = 0; t < data.nTypes; t++) {				
						for (int v = 0; v < vehicles[t]; v++) {
							expression.addTerm(1, z[l][k]);
							k++;
						}
					}
				}
				if (link.s.id == p) {
					k = 0;
					for (int t = 0; t < data.nTypes; t++) {				
						for (int v = 0; v < vehicles[t]; v++) {
							expression.addTerm(-1, z[l][k]);
							k++;
						}
					}
				}
			}
			cplex.addEq(expression, node.delivery, exprName);
		}
		
		double totalPickup = 0;
		double totalDelivery = 0;
		for (int i = 1; i < data.nNodes; i++) {
			Node node = data.nodes[i];
			totalPickup += node.pickup;
			totalDelivery += node.delivery;
		}

		expression = cplex.linearNumExpr();
		exprName = "expression09";
		for (int l = 0; l < links.size(); l++) {
			Link link = links.get(l);
			if (link.t.id == 0) {
				k = 0;
				for (int t = 0; t < data.nTypes; t++) {				
					for (int v = 0; v < vehicles[t]; v++) {
						expression.addTerm(1, y[l][k]);
						k++;
					}
				}
			}
		}
		cplex.addEq(expression, totalPickup, exprName);

		expression = cplex.linearNumExpr();
		exprName = "expression10";
		for (int l = 0; l < links.size(); l++) {
			Link link = links.get(l);
			if (link.s.id == 0) {
				k = 0;
				for (int t = 0; t < data.nTypes; t++) {				
					for (int v = 0; v < vehicles[t]; v++) {
						expression.addTerm(1, z[l][k]);
						k++;
					}
				}
			}
		}
		cplex.addEq(expression, totalDelivery, exprName);

		for (int l = 0; l < links.size(); l++) {
			Link link = links.get(l);
			k = 0;
			for (int t = 0; t < data.nTypes; t++) {				
				for (int v = 0; v < vehicles[t]; v++) {
					expression = cplex.linearNumExpr();
					exprName = "expression11(" + link.s.id + "," + link.t.id + "," + k + ")";
					expression.addTerm(1, y[l][k]);
					expression.addTerm(1, z[l][k]);
					expression.addTerm(- data.types[t].capacity, x[l][k]);
					cplex.addLe(expression, 0, exprName);
					k++;
				}
			}
		}

		//MIP start
		if (solution != null) {
			IloIntVar[] startVar = new IloIntVar[links.size() * qty];
			double[] startVal = new double[links.size() * qty];
			for (int l = 0; l < links.size(); l++) {				
				k = 0;
				for (int t = 0; t < data.nTypes; t++) {				
					for (int v = 0; v < vehicles[t]; v++) {
						int idx = l * qty + k; 
						startVar[idx] = x[l][k];
						k++;
					}
				}
			}
			int[] car = new int[data.nTypes];
			car[0] = 0;
			for (int t = 1; t < data.nTypes; t++) {
				car[t] = car[t-1] + vehicles[t-1];
			}
			for (int r = 0; r < solution.routes.size(); r++) {
				Route route = solution.routes.get(r);
				k = car[route.vehicleType.id-1];
				car[route.vehicleType.id-1]++;
				for (int n = 0; n < route.nodes.size(); n++) {
					if (n != route.nodes.size()-1) {
						Node node = route.nodes.get(n);
						Node next = route.nodes.get(n+1);
						for (int l = 0; l < links.size(); l++) {				
							Link link = links.get(l);
							if (link.s.id == node.id && link.t.id == next.id) {
								int idx = l * qty + k; 
								startVal[idx] = 1.0;
							}
						}
					} else {
						Node node = route.nodes.get(n);
						for (int l = 0; l < links.size(); l++) {				
							Link link = links.get(l);
							if (link.s.id == node.id && link.t.id == 0) {
								int idx = l * qty + k; 
								startVal[idx] = 1.0;
							}
						}
					}
				}
			}
			cplex.addMIPStart(startVar, startVal);
			startVar = null;
			startVal = null;
		}
		
		modelCreationTime = (System.currentTimeMillis() - startTime) / 1000;
	}
	
	public boolean solveModel() throws IloException {
		startTime = System.currentTimeMillis();
		if (cplex.solve()) {
			modelSolvingTime = (System.currentTimeMillis() - startTime) / 1000;
			buildSolution();
			return true;
		}
		modelSolvingTime = (System.currentTimeMillis() - startTime) / 1000;
		return false;
	}
	
	public void buildSolution() throws IloException {
		int[][] solutionMatrix = new int[data.nNodes][data.nNodes];
		int[] solutionVehicles = new int[data.nTypes];
		List<Route> routes = new ArrayList<Route>();
		int k = 0;
		for (int t = 0; t < data.nTypes; t++) {
			int countType = 0;
			for (int v = 0; v < vehicles[t]; v++) {
				for (int l = 0; l < links.size(); l++) {
					Link link = links.get(l);
					if (link.s.id == 0 && cplex.getValue(x[l][k]) > 0.9999) {
						solutionMatrix[link.s.id][link.t.id] = 1;
						List<Node> nodes = new ArrayList<Node>();
						nodes.add(link.s);
						int destination = link.t.id;
						while (destination != 0) {
							for (int m = 0; m < links.size(); m++) {
								Link next = links.get(m);
								if (next.s.id == destination && cplex.getValue(x[m][k]) > 0.9999) {
									solutionMatrix[next.s.id][next.t.id] = 1;
									nodes.add(next.s);
									destination = next.t.id;
									break;
								}
							}
						}
						nodes.add(nodes.get(0));
						routes.add(new Route(data.types[t], nodes, data.distances));
						solutionMatrix[nodes.get(nodes.size()-1).id][nodes.get(0).id] = 1;
						countType++;;
						break;
					}
				}
				k++;
			}
			solutionVehicles[t] = countType;
		}
		solution = new Solution(solutionMatrix, solutionVehicles, routes, cplex.getObjValue(), cplex.getBestObjValue(),
				Math.abs(cplex.getObjValue() - cplex.getBestObjValue()) / ((1E-10) + Math.abs(cplex.getObjValue())),
				cplex.getStatus().toString(), modelCreationTime, modelSolvingTime);
	}
	
	public void exportModel() throws IloException {
		cplex.exportModel("./model/" + data.filename + ".lp");
	}
	
	public void finalizeModel() throws IloException {
		cplex.end();
	}
		
}
