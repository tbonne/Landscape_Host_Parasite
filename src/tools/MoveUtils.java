package tools;

import java.util.concurrent.ThreadLocalRandom;

import repast.simphony.random.RandomHelper;

import LHP.Cell;
import LHP.Parameter;
import LHP.Primate;
import LHP.ModelSetup;

import com.vividsolutions.jts.geom.Coordinate;

public final strictfp  class MoveUtils {
	
	public synchronized static void moveTo(Primate primate, Coordinate c, boolean isCellSite){

		//Calculate where to move inside a patch
		boolean inPatch = false;
		Coordinate newCoord = null;
		Cell patch;
		
		if (isCellSite==false){ //moving towards the safety of the leader
			//while (inPatch==false){
				//double newX = c.x;//+ThreadLocalRandom.current().nextDouble( (-(double)Parameter.safeRadius/2.0), ((double)Parameter.safeRadius/2.0));
				//double newY = c.y;//+ThreadLocalRandom.current().nextDouble( (-(double)Parameter.safeRadius/2.0), ((double)Parameter.safeRadius/2.0));
				newCoord = c;//new Coordinate(newX,newY);
				//patch = SimUtils.getCellAtThisCoord(newCoord);
				//if (patch != null)inPatch=true;
			//}
		}else{ //moving to a desired foraging site
			double newX = c.x+ThreadLocalRandom.current().nextDouble( (-(double)Parameter.cellSize/2.0), ((double)Parameter.cellSize/2.0));
			double newY = c.y+ThreadLocalRandom.current().nextDouble( (-(double)Parameter.cellSize/2.0), ((double)Parameter.cellSize/2.0));
			newCoord = new Coordinate(newX,newY);
		}
		
		//calculate distance
		double moveDist = (primate.getCoord().distance(newCoord));
		if(moveDist>0){
		//calculate angle at which to move
		double adjasent = (double)(newCoord.x-(primate.getCoord().x));
		double opposite = (double)(newCoord.y-(primate.getCoord().y));
		double angle=0;

		angle = Math.atan(Math.abs(opposite/adjasent));
		if (adjasent<0) angle=(Math.PI)-angle;
		if (opposite<0) angle=2*(Math.PI)-angle;

		//check to make sure not moving more than the max distance
		if (moveDist>Parameter.maxDistancePerStep)moveDist=Parameter.maxDistancePerStep;

		//Perform move
		ModelSetup.getGeog().moveByDisplacement(primate, Math.cos(angle)*moveDist, Math.sin(angle)*moveDist);
		primate.addMoveDist(moveDist);
		
		}
	}
}
