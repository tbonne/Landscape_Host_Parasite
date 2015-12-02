package LHP;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import repast.simphony.engine.environment.RunEnvironment;
import tools.ForagingUtils;
import tools.GroupUtils;
import tools.SimUtils;
import tools.MoveUtils;
import tools.MemoryUtils;
import com.vividsolutions.jts.geom.Coordinate;

public class RedColobus extends Primate{
	
	/****************************
	 * 							*
	 * Building a red colobus 	*
	 * 							*
	 * *************************/
	
	//initialize a red colobus agent
	public RedColobus(String sp, int group, int ID, Coordinate c, int groupSize,boolean leader){
		this.species = sp;
		this.groupId = group;
		this.id = ID;
		moveDist=0;
		homeRange=0;
		homeRangeList=new ArrayList<Cell>();
		this.coordinate = c;
		this.safeNeighSize = ThreadLocalRandom.current().nextInt(groupSize-5, groupSize);
		this.energy = ThreadLocalRandom.current().nextInt(Parameter.targetEnergy_mean-5, Parameter.targetEnergy_mean);
		memory = new ArrayList<FoodSourceMem>();
		sleepSites = new ArrayList<FoodSourceMem>();
		this.hungry = false;
		groupMates = new ArrayList();
		myGut = new Gut((Primate)this);
		setIsLeader(leader);
		if(leader==true)MemoryUtils.buildInitialMemory(this,true);
		if(leader==false)MemoryUtils.buildInitialMemory(this,false);
		MemoryUtils.setInitialLandmark(this);
	}
	
	
	/************************************
	 * 									*
	 * Stimuli (internal + external) 	*
	 * 									*
	 * *********************************/
		
	public void getInputs(){
		
		//update where the agent is on the geography and grid landscapes
		this.coordinate = ModelSetup.getAgentGeometry(this).getCoordinates()[0];

		//identify who is near me
		this.groupMates = GroupUtils.getVisibleGroupMates(this);

		//if this is a dispersing primate look for any new groups
		if (this.getGroupID()==-1) GroupUtils.joinGroup(this);
		
		//identify nearby visual patches
		if(this.myPatch!=null)this.visualPatches = myPatch.getVisibleNeigh();

		//identify whether i am hungry or not
		if(Parameter.targetEnergy_mean > this.getEnergy()){
			this.hungry = true;
		} else {
			this.hungry = false;
		}

		//update my gut
		myGut.step();
	}

	/****************************
	 * 							*
	 * Behavioural response 	*
	 * 							*
	 * *************************/


	public void behaviouralResponse(){
		
		Coordinate destination = null; 
		
		//check safety
		if(GroupUtils.checkIfSafe(this)==false){
			
			//move towards safety
			if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()%26>=22 && this.getIsLeader()==true && Parameter.findSleepingSite==true){
				//sleeping site
				destination = GroupUtils.getNearestSleepingSite(this);
				move(destination,true);
			} else {
				//group leader
				destination = GroupUtils.getLeaderPosition(this);
				move(destination,false);
			}
			
		} else {
			//check if hungry
			if(this.hungry){
				//move towards food
				destination = ForagingUtils.chooseFeedingSite(this, RedColobus.class,true).getCoord();
				move(destination,true);
			} else {
				//rest
			}
		}
		
		//chance of dispersal: not if a leader or is already dispersing
		if(ThreadLocalRandom.current().nextDouble()<Parameter.probDispersal && this.groupId!=-1 &&this.getIsLeader()==false)GroupUtils.dispersal(this);
	}
	
	
	/****************************
	 * 							*
	 * 		Methods				*
	 * 							*
	 * *************************/
	
	public void energyUpdate(){
		
		//eat + adjust desiered neigh size based on energy level
		if(this.hungry==true){
			try {
				double bite = myPatch.eatMe(Parameter.rcBiteSize);
				energy = energy + bite;

				//experiencing scramble competition
				if(bite<Parameter.rcBiteSize){ 
					Watcher.addScrambleCount();
					safeNeighSizeDown();
				}
				
			}catch(NullPointerException np){
				System.out.println("can't eat, not in a resource");
				safeNeighSizeDown();
			}
			
		}else{
			safeNeighSizeUp(this.groupMates.size());
		}

		//constant energy loss per step
		this.energy = this.energy-Parameter.rcEnergyLoss;

		//transmission of parasites (from gut to environment)
		if(ThreadLocalRandom.current().nextDouble()<Parameter.probDefication)myGut.deficate(this.myPatch);
		
		//transmission of parasites (from environment to gut)
		this.myPatch.exposure((Primate)this);
		
	}

	public void updateMemory(){

		//where am i?
		MemoryUtils.setLandmark(this);	
		
	}

	
	private void move(Coordinate destination,boolean isCellSite){
		if(destination!=null){
			
			//perform move
			MoveUtils.moveTo((Primate)this, destination,isCellSite); 
			
			//update my location
			this.setCoord(ModelSetup.getAgentGeometry(this).getCoordinates()[0]);
			Cell newPatch = SimUtils.getCellAtThisCoord(this.getMyPatch(),this);
			if(this.groupId!=-1)newPatch.setGroupVisited(this.getGroupID());
			this.myPatch = newPatch;
			
			//record visit to this new location
			this.myPatch.addVisitCount(1);
		}
	}
	
}