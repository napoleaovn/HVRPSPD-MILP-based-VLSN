package data;

public class Node {
	
	public int id;
	public double delivery;
	public double pickup;
	public double x;
	public double y;
	public boolean infeasible;
		
	public Node(int id, double delivery, double pickup, double x, double y) {
		this.id = id;
		this.delivery = delivery;
		this.pickup = pickup;
		this.x = x;
		this.y = y;
	}
}

