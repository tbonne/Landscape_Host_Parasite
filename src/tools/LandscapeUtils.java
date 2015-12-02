package tools;

import java.util.ArrayList;

import LHP.Cell;
import LHP.FoodSourceMem;
import LHP.ModelSetup;
import LHP.Parameter;
import LHP.Primate;
import LHP.RedColobus;
import LHP.Watcher;

import repast.simphony.random.RandomHelper;
import tools.SimUtils;

public final strictfp  class LandscapeUtils {
	
	/*
	 * Method list:
	 * 		(1)	setTotalResources()     - converts from DBH estimates to food estimates
	 * 		(2)	modifyLandscape()		- used to remove forest patches from the landscape
	 * 			(2.1)	findStartLocaition()
	 * 			(2.2)	cut()
	 * 			(2.3)	testModification()
	 */


	//(1)
	//used to convert DBH to food values in the model
	public static void setTotalResources(ArrayList<Cell> allCells, int numbCells, double resAdded){
		
		//calculate the percent difference between the total resource level now and the target level
		double targetRes = numbCells*Parameter.foodDensity;
		double perDiff = resAdded / targetRes;
		double newTotal=0;
		int count=0;

		//Divide each resource by the percent difference to make the total equal the target resource amount
		for (Cell c : allCells){
			c.setMaxResourceLevel((int)((double)c.getMaxResourceLevel()/(double)perDiff));
			c.setResourceLevel((int)((double)c.getResourceLevel()/(double)perDiff));
			newTotal = newTotal + c.getResourceLevel();
			count++;
		}
	}
	
	
	//(2)
	public static void modifyLandscape(ArrayList<Cell> allCells, double resAdded){
		double removedDBH = 0.0;
		while (removedDBH < resAdded*Parameter.totalAlteration){

			//find location
			Cell location = findStartLocation(allCells);
			removedDBH = removedDBH+cut(location);

			Iterable<Cell> neigh = SimUtils.getObjectsWithin(location.getCoord(), Parameter.patch_size, Cell.class);
			for(Cell cell:neigh){
				if(removedDBH<resAdded*Parameter.totalAlteration)removedDBH = removedDBH + cut(cell);
			}
		}

	}
	
	//(2.1)
	private static Cell findStartLocation(ArrayList<Cell> allCells){
		Cell randomLocation = allCells.get(RandomHelper.nextIntFromTo(0, allCells.size()-1));
		if(randomLocation==null)System.out.println("no cells of greater than 100 DBH - random location selection did not work");
		return randomLocation;
	}
	
	//(2.2)
	private static double cut(Cell randomLocation){
		double removedDBH = randomLocation.getDBH();
		randomLocation.setDBH(0);
		randomLocation.setLandClass(1);
		randomLocation.setResourceLevel(0);//Parameter.rcBiteSize
		randomLocation.setMaxResourceLevel(0);//Parameter.rcBiteSize
		
		return removedDBH;
	}
	//(2.3)
	public static void testModification(ArrayList<Cell> allCells, double resAdded){
		double total=0;
		for(Cell cell:allCells){
			total = total + cell.getDBH();
		}
	}
}


