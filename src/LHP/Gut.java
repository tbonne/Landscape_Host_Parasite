package LHP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;


import repast.simphony.engine.environment.RunEnvironment;
import Jama.Matrix;

//this represents a gut of an individual primate
public class Gut {
	
	//Gut variables
	Primate myPrimate;
	int size;
	int eggs;
	int lastDefication;
	int intensityCount=0;
	double eggs_hatching=0;
	
	//infection groups
	ArrayList<NGroup_Developing_Larvae> ingested;
	ArrayList<NGroup_Larvae> larvae;
	
	public Gut(Primate p){
		myPrimate = p;
		eggs=0;
		lastDefication=0;
		ingested = 	new ArrayList<NGroup_Developing_Larvae>();
		larvae = 	new ArrayList<NGroup_Larvae>();
	}
	
	
	public void step(){
		
		//step: developing larvae
		Iterator<NGroup_Developing_Larvae> ig = ingested.iterator();
		while (ig.hasNext()){
		if(ig.next().step())ig.remove();
		}
		
		//step: larvae
		int larvaeSize = larvae.size();
		Iterator<NGroup_Larvae> lg = larvae.iterator();
		while(lg.hasNext()){
			if(lg.next().step())lg.remove();
		}
		
		//reproduction
		int eggs_made=0;
		if(this.getLarvaeNumbs()>0){
			eggs_made = 1;
			this.addEggs(eggs_made); 
		} 
		
		//monitor if primate is infected
		this.myPrimate.setInfected(this.getIsInfected());
	}
	
	/****************************** GUT Related Methods ******************************************************************/
	
	public void deficate(Cell cell){
		if(eggs>0){
			lastDefication=1;
				NGroup_Eggs_Non_Infectious group = new NGroup_Eggs_Non_Infectious();
				group.addEggs(1);
				eggs = 0;
				cell.addNewNonInfectiousGroup(group);
		} else {
			lastDefication=0;	
		}
	}
	
	public void ingestion(int eggs){
		if(ingested.size()==0 && larvae.size()==0){
			intensityCount=1;
			ingested.add(new NGroup_Developing_Larvae(eggs,this));
		} else {
			intensityCount++;
		}
	}
	
	/************************************* Get and Set methods *************************************************************/
	
	public boolean getIsInfected(){
		boolean infected=false;
		if(ingested.size() > 0 || larvae.size() > 0 )infected=true; //removed || eggs > 0
		return infected;
	}
	
	public ArrayList<NGroup_Larvae> getLarvae(){
		return larvae;
	}
	public ArrayList<NGroup_Developing_Larvae> getDLarvae(){
		return ingested;
	}
	
	public long getLarvaeNumbs(){
		long count=0;
		for (NGroup_Larvae l:this.getLarvae()){
			count = count + (long)l.larvae;
		}
		return count;
	}
	
	public long getDevelopmentLarvaeNumbs(){
		long count=0;
		for (NGroup_Developing_Larvae l: this.getDLarvae()){
			count = (long)l.eggs;
		}
		return count;
	}
	
	public int getEggs(){
		return eggs;
	}
	public void addEggs(int e){
		eggs=e;
		}
	
	public int getLastDeficationEggs(){
		return lastDefication;
	}
	public int getIntensityCount(){
		return intensityCount; 
	}
	
}
