package tools;

import java.util.ArrayList;

import repast.simphony.random.RandomHelper;

import LHP.Cell;
import LHP.FoodSourceMem;
import LHP.ModelSetup;
import LHP.Parameter;
import LHP.Primate;

public final strictfp class MemoryUtils {
	
	/*
	 * Method list:
	 * 		(1)	buildInitialMemory()
	 * 		(2)	setInitialLandmark()
	 * 		(3)	setLandmark()
	 * 		(4)	ageMemory()
	 * 		(5) removeRememberedCount()
	 */
	

	//(1)
	public static void buildInitialMemory(Primate primate,boolean updateSleepingSite){
		//get cells nearby
		Iterable<Cell> resInArea = SimUtils.getObjectsWithin(primate.getCoord(), Parameter.memRadius, Cell.class);
		ArrayList<Cell> availableRes = SimUtils.copyIterable(resInArea, true);
		
		//find best 20
		double maxResource=0;
		Cell site=null;
		ArrayList<Cell> bestSites = new ArrayList<Cell>();
		for(int i = 0 ; i<20 ; i++){
			for(Cell c: availableRes){
				if(c.getResourceLevel()>maxResource){
					maxResource=c.getResourceLevel();
					site = c;
				}
			}
			bestSites.add(site);
			availableRes.remove(site);
			maxResource=0;
		}
		
		//add best 20 sites to memory
		for(Cell c : bestSites){
			FoodSourceMem mem = new FoodSourceMem(c);
			primate.getMemory().add(mem);
		}
		
		//add links to memory sites (defined by all sites within 100 meters of each other --> nearby sites)
		for (FoodSourceMem fm: primate.getMemory()){
			fm.setNearbySites(primate.getMemory());
		}
		

		if (updateSleepingSite==true){

			for(FoodSourceMem mem:primate.getMemory()){
				mem.setSleepSite(true);
			}
		}
	}
	
	//(2)
	public static void setInitialLandmark(Primate primate){
		FoodSourceMem fLandmark = null;
		double dist = 99999, minDist=99999;

		//find nearest landmark
		for(FoodSourceMem fm: primate.getMemory()){
			dist = fm.getCoord().distance(primate.getCoord()); 
			if (dist<minDist){										//simply the nearest landmark will be used at the start
				minDist = fm.getCoord().distance(primate.getCoord());
				fLandmark = fm;
			}
		}
		if (fLandmark!=null)primate.setLastRememberedSite(fLandmark); 
	}
	
	//(3)
	public static void setLandmark(Primate primate){
		FoodSourceMem fLandmark = null;
		double dist = 99999, minDist=99999;

		//find nearest landmark
		for(FoodSourceMem fm: primate.getMemory()){
			dist = fm.getCoord().distance(primate.getCoord()); 
			if (dist<minDist && dist < Parameter.foodSearchRange){ //within visual food search radius
				minDist = fm.getCoord().distance(primate.getCoord());
				fLandmark = fm;
			}
		}
		if (fLandmark!=null)primate.setLastRememberedSite(fLandmark); 
	}
	
	public static void setSleepingSites(Primate primate){
		
	}
	
	//(5)
	public static void ageMemory(Primate primate){
		ArrayList<FoodSourceMem> remove = new ArrayList<FoodSourceMem>();
		for(FoodSourceMem fm: primate.getMemory()){
			boolean forget = fm.step();
			if(forget==true)remove.add(fm);
		}
		for (FoodSourceMem fm : remove){
			primate.getMemory().remove(fm);
		}
	}
	
	//(6)
	public static void removeRememberedCount(Primate primate){
		for(FoodSourceMem fm: primate.getMemory()){
			fm.getResource().adjustRemembered(-1);
		}
	}

}
