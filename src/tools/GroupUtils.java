package tools;

import java.util.ArrayList;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

import LHP.Cell;
import LHP.FoodSourceMem;
import LHP.ModelSetup;
import LHP.Parameter;
import LHP.Primate;
import LHP.RedColobus;
import LHP.Watcher;

import com.vividsolutions.jts.geom.Coordinate;

public final strictfp class GroupUtils {
	
	/* Method list:
	 * 		(1)	checkIfSafe() leader and group mates (check which one is used in the spatial memory tests)
	 * 		(2) checkIfSafe() leader only
	 * 		(3) getLeader position()
	 * 		(4) leader present()
	 * 		(5) myNearestNeighs()
	 * 		(6) getNearestSleepingSite()
	 * 		(7) getBestSleepingSite()
	 * 		(8) getVisiblePrimates()
	 * 		(9) getVisibleGroupMates()
	 * 		(10) dispersal()
	 * 		(11) JoinGroup()
	 */
	
	//(1) //checks to see if there is enough neighbours with safe radius
	public static <T> boolean checkIfSafe(T agent, Coordinate myFutureP, Class<T> clazz){ 

		boolean retval = false;
		int numbPrimatesNearBy=0;
		boolean leaderPresent = false;
		ArrayList<Primate> primatesInArea = new ArrayList<Primate>();

			//find primates within safe radius
			for(Primate p : ((Primate)agent).getGroupMates() ){
			if(p.getCoord().distance(((Primate)agent).getCoord())<Parameter.safeRadius)primatesInArea.add(p);
			}
			
			//count the number of group mates within my safe radius + check for leader
			try{
				for (Primate p : primatesInArea){
					if (((Primate) p).getGroupID()==((Primate)agent).getGroupID()&& ((Primate)p).getId()!=((Primate)agent).getId())numbPrimatesNearBy++;
					if (((Primate)p).getIsLeader())leaderPresent = true;
				}
			}catch(NullPointerException e){
				System.out.println("Warrning: no nearby neighbours found");
			}

			//if there is enough and the leader is nearby, then return value is true
			if(numbPrimatesNearBy>=((Primate)agent).getSafeNeighSize() && leaderPresent==true ){
				retval = true;
			}
			
			//migrating individual not considered safe until they find a group
			if(((Primate)agent).getId()==-1)retval=false;

		return retval;
	}
	
	public static boolean checkIfSafe(Primate p){ //checks to see if there is enough neighbours with safe radius

		boolean retval = false;
		//if there leader is present 
		retval = leaderPresent(p);

		//do i need a sleeping site
		if(Parameter.findSleepingSite==true){
			if(p.getIsLeader()==true && RunEnvironment.getInstance().getCurrentSchedule().getTickCount()%26>=22){
				retval = false; //reset retval as sleeping site is needed to be safe
				for(FoodSourceMem fm:p.getSleepSites()){
					if(fm.getCoord().distance(p.getMyPatch().getCoord())<Parameter.cellSize)retval=fm.getSleepSite(); 
				}
			}
		}
		
		return retval;
	}
	
	public static Coordinate getLeaderPosition(RedColobus p){
		Coordinate leader= null;
		for(Primate primate: p.getGroupMates()){
			if(primate.getIsLeader() && p.getGroupID()==primate.getGroupID())leader=primate.getCoord();
		}
		
		if(leader==null && p.getGroupID()!=-1)leader=myNearestNeighs(p);//if no nearby neigh then this returns null
		if(leader==null && p.getGroupID()!=-1)leader=ForagingUtils.chooseFeedingSite(p, RedColobus.class, true).getCoord();//if no neigh or leader, then forage
		
		if(leader==null)System.out.println("something wrong with getLeaderPosition");
		
		return leader;
	}
	
	public static boolean leaderPresent(Primate p){
		boolean present = false;

		if(p.getIsLeader()==true){
			present = true;
		}else {
			for(Primate primate:p.getGroupMates()){
				if(p.getCoord().distance(primate.getCoord())<Parameter.safeRadius){
					if(primate.getIsLeader()==true && p.getGroupID()==primate.getGroupID() )present=true;
				}
			}
		}
		return present;
	}
	
	//(2)
	public static <T> Coordinate myNearestNeighs(T agent){//calculates the center of a group of my nearest neighbours: used when primate is not considered safe
		ArrayList<Primate> closestP = new ArrayList<Primate>();
		double minDist=99999999;
		Coordinate c = null;
		Primate pClose=null;
		
		//get all visible neighbours
		ArrayList<Primate> allNeigh = new ArrayList( ((Primate)agent).getGroupMates() ); 
		
		//get the only the X closest neighbours: X = safeNeighSize
		if (allNeigh.size()>0 && allNeigh.size()>((Primate)agent).getSafeNeighSize()){
			
			while (closestP.size()<((Primate)agent).getSafeNeighSize() && allNeigh.size()>0){
				minDist = 9999999;
				pClose=null;
				
				//take subset of primate group
				for (Primate p: allNeigh){
					double dist = ((Primate)agent).getCoord().distance(p.getCoord());
					if (closestP.contains(p)==false && dist < minDist && p.getId()!=((Primate)agent).getId()){
						minDist = dist;
						pClose = p;
					}
				}

				allNeigh.remove(pClose);

				//add closest neighbour to group
				if (pClose!=null)closestP.add(pClose);
			}
			//else use all visible neighbours
		} else {
			closestP = allNeigh;
		}

		//find the center of this group (closestP)
		double xCoordAvg=0;
		double yCoordAvg=0;
		double numbPrimates=0;

		if (closestP.size()>0){
			
		//calculate their average x and y coordinates
		for (Primate p: closestP){
			if (p.getGroupID()==((Primate)agent).getGroupID()){
				xCoordAvg = xCoordAvg + p.getCoord().x;
				yCoordAvg = yCoordAvg + p.getCoord().y;
				numbPrimates++;
			}
		}

		xCoordAvg = xCoordAvg/numbPrimates;
		yCoordAvg = yCoordAvg/numbPrimates;

		//return the center coordinate
		c = new Coordinate(xCoordAvg,yCoordAvg);
		
		} else { //no nearby neighbours
			
		}
		
		
		if (c == null){
			//System.out.println("I can't find any near-by neigh! - groupUtils class, myNearestNeighs    -  " + ((Primate)agent).getGroupID());
		}

		return c;
	}
	
	public static Coordinate getNearestSleepingSite(Primate p){
		
		double minDist = 99999,dist=999999;
		Coordinate bestSite = null;
		for(FoodSourceMem fm : p.getMemory()){
			if(fm.getSleepSite()==true){
				dist = fm.getCoord().distance(p.getCoord());
				if (dist < minDist && fm.getResource().getResourceLevel() > Parameter.rcBiteSize){
					minDist = dist;
					bestSite = fm.getCoord();
				}
			}
		}
		return bestSite;
	}
	
	public static Coordinate getBestSleepingSite(Primate p){
		
		double maxFood = 0,food=999999;
		Coordinate bestSite = null;
		for(FoodSourceMem fm : p.getSleepSites()){
			if(fm.getSleepSite()==true){
				food = fm.getFoodRemembered();
				if (food > maxFood){
					maxFood = food;
					bestSite = fm.getCoord();
				}
			}
		}
		
		return bestSite;
	}

	//(3)
	public static <T> ArrayList<Primate> getVisiblePrimates(T agent, Class<T> clazz){

		ArrayList<Primate> myGroupies = new ArrayList<Primate>();
		
		for (Primate p:((Primate)agent).getTotalGroup()){ 
			if (p.getGroupID()==((Primate)agent).getGroupID()){
				if(p.getCoord().distance(((Primate)agent).getCoord())<Parameter.primateSearchRange)myGroupies.add(p);
			}
		}
		
		if(myGroupies.size()<1 && ((Primate)agent).getGroupID()!=-1)System.out.println("cannot find individuals - groupUtils class, getVisiblePrimates");
		return myGroupies;

	}
	
	//(4)
	public static <T> Coordinate getGroupCenter(Iterable<T> primates){
		
		//calculate group center
		double averageX=0;
		double averageY=0;
		int numberOfInd=0;
		
		for (T pri:primates){
			averageX = averageX + ((Primate)pri).getCoord().x;
			averageY = averageY + ((Primate)pri).getCoord().y;
			numberOfInd++;
		}
		
		averageX = averageX/numberOfInd;
		averageY = averageY/numberOfInd;
		
		Coordinate center = new Coordinate(averageX,averageY);
		
		return center;
		
	}
	
	
	//(6)
	public static ArrayList<Primate> getVisibleGroupMates(Primate primate){
		ArrayList<Primate> groupMates = new ArrayList<Primate>();
		//ArrayList<Primate> aroundMe = SimUtils.copyIterable(SimUtils.getObjectsWithin(primate.getCoord(), Parameter.primateSearchRange, RedColobus.class));
		//ArrayList<Primate> aroundMe = SimUtils.getPrimatesWithin(primate.getCoord(), Parameter.primateSearchRange);
		for(Primate p: ModelSetup.primatesAll){
			double dist = p.getCoord().distance(primate.getCoord());
			if(dist<Parameter.primateSearchRange){
				if(groupMates.contains(p)==false){
					groupMates.add(p);	
				}
			}
		}
		
		/*for (Primate p : aroundMe){
			if(p.getGroupID()==primate.getGroupID())groupMates.add(p);
		}*/
		
		return groupMates;
	}
	
	//(7)
	public static void dispersal(Primate primate){
		primate.setLastGroupId(primate.getGroupID());
		primate.setGroupID(-1);
		primate.setIsLeader(true); //i am my own leader now
	}
	
	//(8)
	public static void joinGroup(Primate primate){
		//if the dispersing primate see another group it will join it
		ArrayList<Primate> aroundMe = SimUtils.copyIterable(SimUtils.getObjectsWithin(primate.getCoord(), Parameter.primateSearchRange, RedColobus.class));
		for(Primate p: aroundMe){
			if (p.getGroupID()!=primate.getLastGroupId() && p.getId()!=primate.getId() && p.getGroupID()!=-1){
				primate.setGroupID(p.getGroupID());
				primate.setIsLeader(false);
				Watcher.addImmigrationCount(); 
				break;
			}
		}
	}
}
