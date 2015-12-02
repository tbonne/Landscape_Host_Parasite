package tools;

import LHP.Cell;
import LHP.Parameter;
import LHP.Primate;
import LHP.FoodSourceMem;
import tools.GroupUtils;

import com.vividsolutions.jts.geom.Coordinate;


public final strictfp class ForagingUtils {

	/* Method list:
	 * 		(1)	choosefeedingSite()							- compares visual and remembered sites, chooses best one
	 * 		(2) findBestUnknownFoodSource()					- finds best visual site
	 * 		(3) findBestKnownFoodSourceTopo()				- finds best remembered site
	 * 		(4)	chooseFoodSourceTowardsMyMigrationGoal()	- finds best site on way to my chosen site
	 */
	
	
	
	//(1)
	//Compares and finds the best resource site, either from memory or from visible sites.
	public static <T extends Primate> Cell chooseFeedingSite(T agent, Class<T> clazz,boolean topo){

		//System.out.println("choosing feeding site");
		Cell bestFoodSite = null;
		Cell bestRemembered = findBestKnownFoodSourceTopo(((Primate)agent));//find best remembered food source
		Cell bestNearMe = findBestUnknownFoodSource(((Primate)agent));//find best nearby food source

		
		//1:Both a remembered and unknown site found
		if (bestRemembered!=null&&bestNearMe!=null){

			double remdist = ((Primate)agent).getCoord().distance(bestRemembered.getCoord());
			double unknowndist = ((Primate)agent).getCoord().distance(bestNearMe.getCoord());

				if (remdist<Parameter.cellSize)remdist=Parameter.cellSize;
				if (unknowndist<Parameter.cellSize)unknowndist=Parameter.cellSize;


			double remResWeight = remdist/(bestRemembered.getResourceLevel());  
			double unknResWeight = unknowndist/bestNearMe.getResourceLevel();
			
			//1.1 remembered site is chosen
			if (remResWeight<=unknResWeight){//A remembered site is chosen

				if(GroupUtils.checkIfSafe(agent,bestRemembered.getCoord(),clazz)==true){//safe to move... agent chooses this site
					return bestRemembered;

				}else{ // not safe to move... agent looks for a resource on the way to goal
					Cell resTowardsGoal = chooseFoodSourceTowardsMyMigrationGoal(((Primate)agent),bestRemembered.getCoord(),true);
					if(resTowardsGoal!=null){  //found resource on the way to goal... agent chooses this intermediate resource
						return resTowardsGoal;
					}else{
						return ((Primate)agent).getMyPatch();
					}
				}
			
			//1.2: A unknown nearby food site is chosen
			}else{

				if(GroupUtils.checkIfSafe(agent,bestNearMe.getCoord(),clazz)==true){
					return bestNearMe;
				}else{
					Cell resTowardsGoal = chooseFoodSourceTowardsMyMigrationGoal(((Primate)agent),bestNearMe.getCoord(), true);

					if(resTowardsGoal!=null){  
						return resTowardsGoal;
					}else{
					return ((Primate)agent).getMyPatch();
					}
				}
			}

		//2: There are no remembered sites; an unknown food source is chosen
		}else if (bestRemembered==null){

			if(GroupUtils.checkIfSafe(agent,bestNearMe.getCoord(),clazz)==true){
				return bestNearMe;
			}else{
				Cell resTowardsGoal = chooseFoodSourceTowardsMyMigrationGoal(((Primate)agent),bestNearMe.getCoord(),true);
				if(resTowardsGoal!=null){  
					return resTowardsGoal;
				}else{
					return ((Primate)agent).getMyPatch();
				}
			}

		//3: There are no nearby sites; a remembered food source is chosen
		}else if (bestNearMe==null){

			if(GroupUtils.checkIfSafe(agent,bestRemembered.getCoord(),clazz)==true){
				return bestRemembered;
			}else{
				Cell resTowardsGoal = chooseFoodSourceTowardsMyMigrationGoal(((Primate)agent),bestRemembered.getCoord(),true);
				if(resTowardsGoal!=null){  
					return resTowardsGoal;
				}else{
					return ((Primate)agent).getMyPatch();
				}
			}
		}else{
			System.out.println("5.0: Error in chooseFoodSource() within foragingUtils class");
		}


		if (bestFoodSite==null)System.out.println("6.0: Error in chooseFoodSource() within foragingUtils class");

		return bestFoodSite;

	}
	
//(2)
	//finds the best resource that the agent can "see"
		public static Cell findBestUnknownFoodSource(Primate primate){//find the best unknown resource; all resources within search radius

			//System.out.println("choosing unknown site");
			double dist = 99999;
			double resourceWeight = 99999;
			double minResourceWeight = 9999;
			Cell bestCloseSite = null;

			//Calculate each resources food weight and choose the lowest one (weight = distance / food)
			for (Cell r:primate.getVisualPatches()){

				dist = primate.getCoord().distance(r.getCoord());
				if (dist<Parameter.cellSize)dist=Parameter.cellSize;

				resourceWeight=dist/r.getResourceLevel();
				if(resourceWeight<minResourceWeight){
					minResourceWeight = resourceWeight;
					bestCloseSite = r;
				}
			}

			return bestCloseSite;
		}

//(3)
	//finds the best resource within an agents memory
	private static Cell findBestKnownFoodSourceTopo(Primate primate){ //find best remembered site

		double dist=999999,resourceWeight=999999;
		double minResourceWeight=9999999;
		Cell bestRes = null;

		//compare known food sources by comparing site weights; lowest one is the best
		try {
			for(FoodSourceMem f:primate.getLastRememberedSite().getNearBySites()){

				Cell considered_cell = f.getResource();
				dist = primate.getCoord().distance(considered_cell.getCoord());

				// if primate can "see" the remembered resource (within sensory range)
				if (dist<=Parameter.foodSearchRange){
					
					f.setFoodRemembered(considered_cell.getResourceLevel());  // update memory, as primate can see the resource
					resourceWeight=dist/f.getFoodRemembered();

				// if primate cannot "see" the remembered resource (outside sensory range)
				}else{
					resourceWeight=dist/f.getFoodRemembered();
				}

				//compare weights and update bestRes at the site with the lowest resource weight (dist/food)
				if (resourceWeight<minResourceWeight){  
					minResourceWeight=resourceWeight;
					bestRes = considered_cell;
				}
			}
			
		} catch (Exception NullPointerException) {
			//System.out.println("no memory set as my last memory site, yet...");
		}

		return bestRes;
	}
	
//(4)
		public static Cell chooseFoodSourceTowardsMyMigrationGoal(Primate agent,Coordinate bestRes, boolean considerFood){//in case the primate has a goal to migrate towards; chose best site in that direction

			Cell towardsMyGoal=null;
			boolean found=false;

			double dist=9999,newDist=9999,resourceWeight=9999;
			double minResourceWeight=99999;
			Cell bestCloseSite=null;

			Iterable<Cell> primateFood = agent.getVisualPatches();
			int count = 0;

			//choose the "best" site; distance vs food + is safe
			for (Cell r:primateFood){

				if(agent.getCoord().distance(r.getCoord())<=Parameter.foodSearchRange){
					count++;
					dist = agent.getCoord().distance(bestRes);
					newDist = r.getCoord().distance(bestRes);

					if (agent.getCoord().distance(r.getCoord()) <= Parameter.foodSearchRange && dist>newDist){ 
						resourceWeight=(r.getCoord().distance(bestRes))/r.getResourceLevel();//choose safe site that is the closest to the migration goal\
						if(considerFood==false)resourceWeight=r.getCoord().distance(bestRes);
						if(resourceWeight<minResourceWeight){
							minResourceWeight = resourceWeight;
							bestCloseSite = r;
							found=true;
						}
					}
				}
			}
			if (found == true)towardsMyGoal=bestCloseSite;//return site; if a acceptable site has been chosen


			if(towardsMyGoal==null){
				//System.out.println("Tried to find a site towards my goal but non available!! ");
			}

			return towardsMyGoal;
		}


}
