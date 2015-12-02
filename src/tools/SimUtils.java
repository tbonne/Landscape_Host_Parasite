package tools;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import LHP.ModelSetup;
import LHP.Primate;
import LHP.Cell;
import LHP.Parameter;

import repast.simphony.space.gis.Geography;

public final strictfp class SimUtils {

	/* Method list:
	 * 		(1)	getCellAtThisCoord()
	 * 		(2) copyIterable()    				- converts Iterable to arrayList: primates
	 * 		(3) copyIterable()					- converts Iterable to arrayList: cells
	 * 		(4) getObjectsWithin()
	 */
	
	//(1)
	public static Cell getCellAtThisCoord(Coordinate coord){

		double dist = 0, minDist = 9999;
		Cell closestR = null;

		//find all resources near by
		Iterable<Cell> primateFood = SimUtils.getObjectsWithin(coord, 2.0*Parameter.cellSize, Cell.class);

		//which one is the closest one to me
		for (Cell c:primateFood){
			dist = coord.distance(c.getCoord());
			if (dist<minDist){
				closestR=c;
				minDist = dist;
			}
		}
		//if i've found no resources
		if (closestR==null){
			//System.out.println("for some strange reason i'm not in a resource!! and can't see any around me!!");
		} else if  (minDist>(Math.pow(2, 1/2))*Parameter.cellSize) {
			//System.out.println("not in a resource, but i see some nearby");
		}

		return closestR;
	}
	public static Cell getCellAtThisCoord(Coordinate coord,boolean testing){

		double dist = 0, minDist = 9999;
		Cell closestR = null;

		//find all resources near by
		Iterable<Cell> primateFood = SimUtils.getObjectsWithin(coord, 2.0*Parameter.cellSize, Cell.class);

		//which one is the closest one to me
		for (Cell c:primateFood){
			dist = coord.distance(c.getCoord());
			if (dist<minDist){
				closestR=c;
				minDist = dist;
			}
		}
		//if i've found no resources
		if (closestR==null){
			//System.out.println("for some strange reason i'm not in a resource!! and can't see any around me!! From right after red colobus move");
		} else if  (minDist>(Math.pow(2, 1/2))*Parameter.cellSize) {
			//System.out.println("not in a resource, but i see some nearby");
		}

		return closestR;
	}
	
	public static Cell getCellAtThisCoord(Cell lastCell,Primate p){

		double dist = 0, minDist = 9999;
		Cell closestR = null;

		//find all resources near by
		Iterable<Cell> primateFood = lastCell.getVisibleNeigh();

		//which one is the closest one to me
		for (Cell c:primateFood){
			dist = p.getCoord().distance(c.getCoord());
			if (dist<minDist){
				closestR=c;
				minDist = dist;
			}
		}
		//if i've found no resources
		if (closestR==null){
			//System.out.println("for some strange reason i'm not in a resource!! and can't see any around me!! From right after red colobus move");
		} else if  (minDist>(Math.pow(2, 1/2))*Parameter.cellSize) {
			//System.out.println("not in a resource, but i see some nearby");
		}

		return closestR;
	}
	
	//(2)
	public static <T> ArrayList<Primate> copyIterable(Iterable<T> iter) {
	    ArrayList<Primate> copy = new ArrayList<Primate>();
	    while (iter.iterator().hasNext())
	        copy.add((Primate) iter.iterator().next());
	    return copy;
	}
	
	//(3)
	public static <T> ArrayList<Cell> copyIterable(Iterable<T> iter, boolean e) {
	    ArrayList<Cell> copy = new ArrayList<Cell>();
	    while (iter.iterator().hasNext())
	        copy.add((Cell) iter.iterator().next());
	    return copy;
	}

	//(4)
	public static  <T> Iterable<T> getObjectsWithin(Coordinate coord,double radius, Class clazz){
		Envelope envelope = new Envelope();
		envelope.init(coord);
		envelope.expandBy(radius);
		Iterable<T> objectsInArea = ModelSetup.getGeog().getObjectsWithin(envelope,clazz);
		envelope.setToNull();
		return objectsInArea;
	}
	
	//can;t get this to work (strange error with the same primate within groupmates arraylist)
	public static ArrayList<Primate> getPrimatesWithin(Coordinate coord,double radius){

		ArrayList<Primate> pInArea = new ArrayList<Primate>();
		
		for(Primate p: ModelSetup.primatesAll){
			double dist = p.getCoord().distance(coord);
			if(dist<radius && dist > 0){
				pInArea.add(p);
			}
		}

		return pInArea;
	}
}
