package LHP;

import java.util.ArrayList;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

//this represents a memory held by an individual primate
public class FoodSourceMem {

	Coordinate coord;
	double maxFood;
	double regrowRate;
	double foodRemembered;
	Cell resource;
	ArrayList<FoodSourceMem> nearbySites;
	int age;
	boolean forget=false, sleepSite=false;
	
	
	public FoodSourceMem(Cell res){
		//parameters
		regrowRate = Parameter.regrowthRate;
		resource = res;
		coord = res.getCoord();
		maxFood=res.getMaxResourceLevel();
		foodRemembered = res.getResourceLevel();
		nearbySites = new ArrayList<FoodSourceMem>();
		res.adjustRemembered(1);
		addToNearBySites(this);
	}
	
	public FoodSourceMem(Cell res, ArrayList<FoodSourceMem> mem){
		//parameters
		regrowRate = Parameter.regrowthRate;
		resource = res;
		coord = res.getCoord();
		maxFood=res.getMaxResourceLevel();
		foodRemembered = res.getResourceLevel();
		nearbySites = new ArrayList<FoodSourceMem>();
		setNearbySites(mem);
		res.adjustRemembered(1);
		addToNearBySites(this);
	}
	
	public boolean step(){
		//food remembered will grow back at the same rate as the "regrowRate"
		if (foodRemembered + regrowRate< maxFood){
			foodRemembered = foodRemembered + regrowRate;
		}else{
			foodRemembered = maxFood;
		}

		return forget;
	}
	
	public void setNearbySites(ArrayList<FoodSourceMem> memory){
		
		try {
		//buffer to see if any other sites are nearby
		for (FoodSourceMem fm: memory){
			if(fm.getCoord().distance(this.getCoord())<Parameter.topoMemDist)nearbySites.add(fm);
		}
		
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	/************************************** get and set methods *******************************************/
	
	public Cell getMyCell(){
		return resource;
	}
	public void setSleepSite(boolean b){
		sleepSite=b;
		resource.setSleepSite(b);
	}
	public boolean getSleepSite(){
		return sleepSite;
	}
	public void setFoodRemembered(double f){
		foodRemembered=f;
	}
	public double getFoodRemembered(){
		return foodRemembered;
	}
	public Coordinate getCoord(){
		return coord;
	}
	public void setCoord(Coordinate c){
		coord = c;
	}
	public void setMaxFood(double m){
		maxFood = m;
	}
	public Cell getResource(){
		return resource;
	}
	public ArrayList<FoodSourceMem> getNearBySites(){
		return nearbySites;
	}
	public void addToNearBySites(FoodSourceMem mem){
		nearbySites.add(mem);
	}
	public double getFoodRememberedMax(){
		return maxFood;
	}
}
