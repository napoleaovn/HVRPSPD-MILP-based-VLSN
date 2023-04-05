package data;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Data {
	
	public String filename;
	public int nTypes;
	public int nNodes;
	public Type[] types;
	public Node[] nodes;
	public double[][] distances;
			
	public Data(String filename, int inputFormat) throws IOException{
		
		this.filename = filename;

		Scanner scanner = new Scanner(new FileReader("./data/" + filename + ".dat"));
		
		switch (inputFormat) {
		case 1:
			nTypes = scanner.nextInt();
			types = new Type[nTypes];
			for (int t = 0; t < nTypes; t++) {
				int id = scanner.nextInt();
				double capacity = scanner.nextDouble();
				double variableCost = scanner.nextDouble();
				double fixedCost = scanner.nextDouble();
				types[t] = new Type(id, capacity, variableCost, fixedCost);
			}

			nNodes = scanner.nextInt(); 
			nodes = new Node[nNodes];
			for (int n = 0; n < nNodes; n++) {
				int id = scanner.nextInt();
				double delivery = scanner.nextDouble();
				double pickup = scanner.nextDouble();
				double x = scanner.nextDouble();
				double y = scanner.nextDouble();
				nodes[n] = new Node(id, delivery, pickup, x, y);
			}

			distances = new double[nNodes][nNodes];
			for (int i = 0; i < nNodes; i++) {
				Node s = nodes[i];
				for (int j = 0; j < nNodes; j++) {
					Node t = nodes[j];
					if (i != j) {
						distances[i][j] = Math.sqrt(Math.pow(s.x - t.x, 2) + Math.pow(s.y - t.y, 2)); 
						BigDecimal bigDecimal = new BigDecimal(distances[i][j]);
					    bigDecimal = bigDecimal.setScale(6, RoundingMode.HALF_UP);
					    distances[i][j] = bigDecimal.doubleValue();	
					}
				}
			}
			break;
		case 2:
			double capacity = scanner.nextDouble();
			nTypes = 1;
			types = new Type[nTypes];
			types[0] = new Type(1, capacity, 1.0, 0.0);

			nNodes = scanner.nextInt() + 1; 
			
			distances = new double[nNodes][nNodes];
			for (int i = 0; i < nNodes; i++) {
				for (int j = 0; j < nNodes; j++) {
					distances[i][j] = scanner.nextDouble();
				}
			}
			
			nodes = new Node[nNodes];
			nodes[0] = new Node(0, 0.0, 0.0, 0.0, 0.0);
			for (int n = 1; n < nNodes; n++) {
				double delivery = scanner.nextDouble();
				double pickup = scanner.nextDouble();
				nodes[n] = new Node(n, delivery, pickup, 0.0, 0.0);
			}
			break;
		}
		
		scanner.close();
			
	}
	
}
