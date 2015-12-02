package LHP;

import java.awt.geom.Point2D;

import com.vividsolutions.jts.geom.Coordinate;

//node associated with one grid cell
public class HabitatNode {
	
	int id; 
	Cell myCell;
	double betweenScore=0;
	int unreachable=0;
	
	public HabitatNode(int id, Cell c) {
		this.id = id;
		myCell=c;
	}
	
	public String toString() { 
		return "V"+id; 
	} 
	
	public int getID(){
		return id;
	}
	
	public Cell getMyCell(){
		return myCell;
	}
	
	public void setBTW(double b){
		betweenScore = b;
	}
	public double getBTW(){
		return betweenScore;
	}
	public void setUnreachable(int i){
		unreachable = i;
	}
	public int getUnreachable(){
		return unreachable;
	}
	

}
