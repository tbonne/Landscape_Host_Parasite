package LHP;

import java.util.ArrayList;

import tools.GroupUtils;
import tools.SimUtils;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Primate {

	//primate level variables
	Coordinate coordinate;
	ArrayList<Primate> groupMates;
	ArrayList<FoodSourceMem> memory,sleepSites;
	ArrayList<Cell> visualPatches;
	FoodSourceMem lastMem;
	Cell myPatch;
	String species;
	boolean leader;
	int groupId,lastGroup;
	private ArrayList<Primate> groupTotal;
	int id;
	double energy;
	boolean hungry, infected;
	int safeNeighSize;
	Gut myGut;
	double moveDist,homeRange;
	ArrayList<Cell> homeRangeList;

	//this method is threaded and controlled via model setup (runs every step at highest priority)
	public void getInputs(){

	}

	//Behavioural responses within each species of primate
	public void behaviouralResponse(){
		
	}
	
	//energy update (eat)
	public void energyUpdate(){
		
	}
	
	//this method is threaded and controlled via model setup (runs every step at 2nd lowest priority)
	public void updateMemory(){
		
	}


	//all primates will have these get and set methods
	/****************************
	 * 							*
	 * Get and set parameters	*
	 * 							*
	 ****************************/

	public ArrayList<FoodSourceMem> getSleepSites(){
		return sleepSites;
	}
	public void safeNeighSizeUp(int visiblePrimates){
		if (safeNeighSize+1<visiblePrimates)safeNeighSize=safeNeighSize+1;
	}
	public void safeNeighSizeDown(){
		if (safeNeighSize-1>=0)safeNeighSize=safeNeighSize-1;
	}
	public String getSpecies(){
		return species;
	}
	public int getGroupID(){
		return groupId;
	}
	public void setGroupID(int id){
		groupId = id;
	}
	public int getId(){
		return id;
	}
	public int getSafeNeighSize(){
		return safeNeighSize;
	}
	public void setSafeNeighSize(int d){
		safeNeighSize = d;
	}
	public double getEnergy(){
		return energy;
	}
	public void setEnergy(double d){
		energy = (int)d;
	}
	public void setCoord(Coordinate c){
		coordinate = c;
	}
	public Coordinate getCoord(){
		return coordinate;
	}
	public void setIsLeader(boolean t){
		leader = t;
	}
	public boolean getIsLeader(){
		return leader;
	}
	public ArrayList<FoodSourceMem> getMemory(){
		return memory;
	}
	public FoodSourceMem getLastRememberedSite(){
		return lastMem;
	}
	public void setLastRememberedSite(FoodSourceMem mem){
		lastMem = mem;
	}
	public Cell getMyPatch(){
		return myPatch;		
	}
	public void setMyPatch(Cell c){
		myPatch = c;
	}
	public ArrayList<Primate> getGroupMates(){
		return groupMates;
	}
	public ArrayList<Cell> getVisualPatches(){
		return visualPatches;
	}
	public double getEggsInGut(){
		return myGut.getEggs();
	}
	public ArrayList<Primate> getTotalGroup(){
		return groupTotal;
	}
	public boolean getInfected(){
		return infected;
	}
	public void setInfected(boolean i){
		infected = i;
	}
	public int getLastGroupId(){
		return lastGroup;
	}
	public void setLastGroupId(int last){
		lastGroup = last;
	}
	public void addMoveDist(double add){
		moveDist = moveDist + add;
	}
	public double getMoveDist(){
		return moveDist;
	}
	public ArrayList<Cell> getHomeRangeList(){
		return homeRangeList;
	}

}
