package task11;

import java.awt.image.RescaleOp;
import java.util.ArrayList;

public class Node {
	String CustomerType;
	String LifeStyle;
	double Vacation;
	double eCredit;
	double Salary;
	double Property;
	String Label;
//	double Distance;
	public Node() {		
	}
	
	public Node(Node node) {
		this.CustomerType = node.CustomerType;
		this.LifeStyle = node.LifeStyle;
		this.Vacation = node.Vacation;
		this.eCredit = node.eCredit;
		this.Salary = node.Salary;
		this.Property = node.Property;
		this.Label = node.Label;
	}
	
	
	public void print() {
		System.out.println(CustomerType + "," + LifeStyle + "," + Vacation + "," + eCredit + "," + Salary + "," + Property + "," + Label);
	}
}
