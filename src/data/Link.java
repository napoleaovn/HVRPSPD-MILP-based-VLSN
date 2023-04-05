package data;

public class Link {

	public Node s;
	public Node t;
	public double distance;

	public Link(Node sNode, Node tNode, double distance) {
		this.s = sNode;
		this.t = tNode;
		this.distance = distance;
	}
	
}