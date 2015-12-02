package LHP;

public class HabitatAssociation {
	
	 public int id;
	 public int node1,node2;
	 
	 public HabitatAssociation(int to,int from) {
	 node2=from;
	 node1=to;
	 } 
	 public String toString() { 
	 return "E"+id;
	 }
}
