package LHP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import cern.jet.random.Binomial;
import cern.jet.random.NegativeBinomial;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;

import tools.SimUtils;

public class Cell {

	//geometry
	Context context;
	Geography geog;
	Geometry geom;
	Coordinate coord; //centroid of the grid cell
	int id;

	//resources
	double resources,dbh;
	int maxResources;
	double regrowRate =Parameter.regrowthRate;
	ArrayList<Cell> visibleSites;
	int land_class;

	//parasite
	ArrayList<NGroup_Eggs_Infectious> infectiousGroup;
	ArrayList<NGroup_Eggs_Non_Infectious> nonInfectiousGroup;
	int eggsTotal=0;

	//memory
	int remembered =0;
	boolean sleepingSite=false;
	int sleep=0;

	//random generator
	private Random randomGenerator;

	//visit counts
	double visit=0;
	int[] groupsVisited;
	int exposureCount=0;
	long depositCount=0;

	public Cell(Context con,double x,double y,double r,int gx, int gy, int lClass,int i){

		//set initial variables
		resources = (int)r;
		dbh=r;
		maxResources = (int)resources; //maintains initial conditions
		visibleSites = new ArrayList<Cell>();
		con.add(this);
		land_class = lClass;
		id=i;
		groupsVisited = new int[Parameter.rcGroups];

		//place the cell on a gis landscape
		geog = (Geography)con.getProjection("geog");
		GeometryFactory fac = new GeometryFactory();
		Coordinate cTR = new Coordinate(x+(Parameter.cellSize/(2.0)),y+(Parameter.cellSize/(2.0)));//top right
		Coordinate cTL = new Coordinate(x-(Parameter.cellSize/(2.0)),y+(Parameter.cellSize/(2.0)));//top left
		Coordinate cBL = new Coordinate(x-(Parameter.cellSize/(2.0)),y-(Parameter.cellSize/(2.0)));//bottom left
		Coordinate cBR = new Coordinate(x+(Parameter.cellSize/(2.0)),y-(Parameter.cellSize/(2.0)));//botton right
		Coordinate[] boundingCoords = {cTR,cTL,cBL,cBR,cTR};
		LinearRing ring = fac.createLinearRing(boundingCoords);
		geom = fac.createPolygon(ring, null);
		geog.move(this, geom);//sets the polygon onto the geography surface
		this.setCoord(geom.getCentroid().getCoordinate());

		//Initialise parasite holders
		infectiousGroup = new ArrayList<NGroup_Eggs_Infectious>();
		nonInfectiousGroup = new ArrayList<NGroup_Eggs_Non_Infectious>();

		//assign a random generator to each cell
		randomGenerator = new Random();

		//Initial contamination
		eggsTotal=0;

	}

	//@ScheduledMethod(start=0, interval = 1,priority=2,shuffle=true)
	public void stepThreaded(){

		try{
			regrow();
		}catch (NullPointerException e){
			System.out.println("problem in the regrow method");
		} catch (ArrayIndexOutOfBoundsException d){
			System.out.println("problem in the regrow method");
		}

		try{
			parasiteUpdate();
		} catch (NullPointerException e){
			System.out.println("problem in the parasite method");
		} catch (ArrayIndexOutOfBoundsException d){
			System.out.println("problem in the parasite method");
		}
	}

	private void regrow(){
		//check to see if resource is at max levels
		if (resources!=maxResources){
			//the resource will grow back until it's maximum level is reached
			if (resources<maxResources-regrowRate){
				resources = (resources+regrowRate);
			} else {
				resources = (maxResources);
				if(getIsInfected()==false)ModelSetup.removeCellToUpdate(this);
			}
			//will degenerate if above max resource level (seasonality)
			if(resources>maxResources+regrowRate){
				resources = (resources-regrowRate);
			}else if (resources>maxResources && resources<maxResources+regrowRate){
				resources = maxResources;
				if(getIsInfected()==false)ModelSetup.removeCellToUpdate(this);
			}
		} else {
			if(getIsInfected()==false)ModelSetup.removeCellToUpdate(this);
		}
	}

	public synchronized double eatMe(int bite){
		double biteSize =0;
		if (this.getResourceLevel() - bite > 0){
			setResourceLevel((int) (this.getResourceLevel() - bite));
			biteSize = bite;
		}else{
			biteSize = this.getResourceLevel();
			setResourceLevel(0);
		}

		ModelSetup.addToCellUpdateList(this);

		return biteSize;
	}

	private void parasiteUpdate(){

		ArrayList<NGroup_Eggs_Non_Infectious> removeNG = new ArrayList<NGroup_Eggs_Non_Infectious>();
		ArrayList<NGroup_Eggs_Infectious> removeIG = new ArrayList<NGroup_Eggs_Infectious>();

		nonInfectiousGroup.removeAll(Collections.singleton(null));
		infectiousGroup.removeAll(Collections.singleton(null));

		//step non-infectious groups
		for(NGroup_Eggs_Non_Infectious ng: nonInfectiousGroup){
			if(ng.step()==true)removeNG.add(ng);
		}
		//step infectious groups
		eggsTotal=0;
		for(NGroup_Eggs_Infectious ig: infectiousGroup){
			if(ig.step()==true)removeIG.add(ig);
			eggsTotal=eggsTotal+ig.getInfectiousEggs();
		}

		//remove groups that are dead (no eggs left)
		nonInfectiousGroup.removeAll(removeNG);
		infectiousGroup.removeAll(removeIG);
	}

	public synchronized void exposure(Primate primate){

		//individual gets exposed to each infected fecal deposit in the cell
		for(int i=0;i<infectiousGroup.size();i++){
			if(Parameter.probIngestEgg>randomGenerator.nextDouble()){    
				NGroup_Eggs_Infectious infect = this.getRandomInfectiousGroup();
				primate.myGut.ingestion(1);
				this.addExposureCount();
				break;
			}
		}
	}

	/****************************Get and set methods************************************/

	public void setVisibleNeigh(){
		Iterable neigh = SimUtils.getObjectsWithin(this.getCoord(), Parameter.foodSearchRange, Cell.class);
		while (neigh.iterator().hasNext()){
			visibleSites.add((Cell)neigh.iterator().next());
			System.out.print("t");
		}
	}
	public void setVisibleNeigh_dist(){
		for (Cell neigh:ModelSetup.getAllCellAgents()){
			if(neigh.getCoord().distance(this.getCoord())<=Parameter.foodSearchRange)visibleSites.add((Cell)neigh);
			if(visibleSites.size()>=9)break;
		}
	}
	public void setSleepSite(boolean b){
		sleepingSite=b;
	}
	public boolean getSleepSite(){
		return sleepingSite;
	}
	public ArrayList<Cell> getVisibleNeigh(){
		return visibleSites;
	}
	private NGroup_Eggs_Infectious getRandomInfectiousGroup(){
		int n = infectiousGroup.size();
		NGroup_Eggs_Infectious ng=null;
		if(n>0){
			ng = infectiousGroup.get(randomGenerator.nextInt(n) );
		}
		return ng;
	}
	public ArrayList<NGroup_Eggs_Infectious> getInfectiousGroups(){
		return infectiousGroup;
	}
	public ArrayList<NGroup_Eggs_Non_Infectious> getNonInfectiousGroups(){
		return nonInfectiousGroup;
	}
	public void addNewNonInfectiousGroup(NGroup_Eggs_Non_Infectious ng){
		nonInfectiousGroup.add(ng);
		ng.setEnv(this);
		double dbh = Math.max(0, this.getDBH());
		double be = ng.getDeath();
		ng.setDeath( (int)Math.floor(ng.getDeath()-((Parameter.maxDBH-dbh)*Parameter.Denv_reduction)) );
		depositCount++;
	}
	public long getDepositCount(){
		return depositCount;
	}
	public void addExposureCount(){
		exposureCount++;
	}
	public int getExposureCount(){
		return exposureCount;
	}
	public double getResourceLevel(){
		return resources;
	}
	public void setResourceLevel(int r){
		resources = r;
	}
	public int getMaxResourceLevel(){
		return maxResources;
	}
	public void setMaxResourceLevel(int rm){
		maxResources = rm;
	}
	public Coordinate getCoord(){
		return coord;
	}
	private void setCoord(Coordinate c){
		coord = c;
	}
	public void adjustRemembered(int i){
		remembered = remembered + i;
	}
	public int getRemembered(){
		return remembered;
	}
	public boolean getIsInfected(){
		boolean infected = false;
		if(getInfectiousGroups().size()>0 || getNonInfectiousGroups().size()>0)infected=true;
		return infected;
	}
	public int getInfectiousFecalCount(){
		return infectiousGroup.size();
	}
	public double getInfectiousEggCount(){
		double count=0;
		for(NGroup_Eggs_Infectious ngi:infectiousGroup){
			count = count + ngi.eggs_infectious;
		}
		return count; 	
	}
	public void setDBH(double d){
		dbh=d;
	}
	public double getDBH(){
		return dbh;
	}
	public double getVisitCount(){
		return visit;
	}
	public void addVisitCount(int i){
		visit = visit + i;
	}
	public void setLandClass(int i){
		land_class = i;
	}
	public int getID(){
		return id;
	}
	public void setGroupVisited(int i){
		groupsVisited[i]=1;
	}
	public int[] getGroupsVisited(){
		return groupsVisited;
	}
	public void clearHR(){
		groupsVisited = new int[Parameter.rcGroups];
	}
}
