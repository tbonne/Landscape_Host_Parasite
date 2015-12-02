package LHP;

import cern.jet.random.Binomial;
import cern.jet.random.Exponential;
import cern.jet.random.NegativeBinomial;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

public class Parameter {
	
	final static Parameters p = RunEnvironment.getInstance().getParameters();
	
	//repast simphony setup
	public final static String agent_context = "cellContext"; 
	public final static String geog = "geog";
	public static int randomSeed = 1847620; 			//number varied in constructor
	public static int numbOfThreads = 7;				//multiple cores (e.g. 8 * 2 = 16)
	public static RandomEngine randGen; 
	
	
	//Model time scales
	public final static int stepsPerDay = 26;			//each step represents 30 min, for a total of 13hours of active time per day
	public final static int endMonth = 12*5;			//will run the simulation until it has completed this many months (1 month = 30 days = 780 steps)
	
	
	//Resource landscape
	public final static double regrowthRate = 1.7; 			//calibrated to fit homerange estimes of observed RC group in study area
	public final static int foodDensity = 500;              //calibrated to fit homerange estimes of observed RC group in study area
	public final static int cellSize = 30;					//size of the resource grids
	public final static String landscape =   "C:/Users/t-work/Dropbox/code/SpatialMemory/LHP_dispersal/LHP/src/data/rc_food_green_tnir_forestonly_subset_30x30_4km2.pgm"; 	//4km^2 image taken from SPOT-5, and used to estimate DBH in each resource cell
	
	
	//Primate: capabilities
	public final static int foodSearchRange = 50;			//distance (meter) at which individuals can see resource grids
	public final static int primateSearchRange = 200;		//distance (meter) at which individuals can see other individuals
	public final static int safeRadius = 50;				//distance (meter) at which individuals gain safety from other individuals
	public final static int maxDistancePerStep = 100; 		//distance (meter) that an individual can travel within one time-step
	public final static int memRetention = 20;				//number of resource grids an individual can maintain in memory
	public final static int topoMemDist = 100;				//distance (meter) at which remembered sites are linked as a set of nearby sites
	public final static int memRadius = 500;				//initial memory surface during initialisation of the top resource sites
	
	//Primate: energetics
	public final static int targetEnergy_mean = 100;												//Target energy an individual try's to maintain
	public final static double rcDaySpentFeeding = 0.43;											//proportion of the day individuals feed, used to calibrate intake rate of individuals
	public final static int rcBiteSize =  (int)(targetEnergy_mean / (stepsPerDay * 0.43) );			//energy gained from each feeding (set to meet rcDaySpentFeeding)
	public final static int rcEnergyLoss = (int)(targetEnergy_mean / stepsPerDay);					//energy lost per time step
	
	//Primates: population dynamics 
	public final static int rcGroups = 	17;					//number of groups within the 4km^2 study area
	public final static int[] rcGroupSizes = {45,42,53,47,50,49,43,54,48,43,47,45,45,51,49,42,47};  //randomly derived sample (fixed between runs) from observed distribution of groups ( ~ normal distribution (47, 4)) 
	public final static double[] rcLocations = {207610.4,59782.4,206424.5,60562.3,207161.8,60011.4,207603.9,60331.6,206627.5,60885.0,206200.5,59506.5,205866.3,60841.0,206590.8,59880.9,207368.6,60938.2,205769.0,59787.2,206768.9,60916.3,205948.5,60415.1,207030.1,60527.3,206379.2,59757.8,207368.3,60223.1,205850.1,60794.4,206952.7,59400.8}; //starting location of groups (fixed between runs)
	public static double probDispersal = 0.000008;			//probability that an individual will choose to migrate from a group, set to observed data, set to match ~3 individuals/ (year * group)
	public final static boolean findSleepingSite = true; 	//include sleeping site behaviour: individuals find sleeping site at the end of the day
	public final static int rcObsHomeRange = 336; 			//this is the average observed homerange size of RC groups used 
	public final static int teritoriality = 100;			//initialize the landscape so that all groups are at least this distance apart
	
	
	//Epidemiology: host
	public static int mature_egg_infectiousEgg ;			//time it takes (days) for eggs to mature into infecious eggs in the environment
	public static int mature_infectiousEgg_larvae ;			//time it takes (days) for ingested eggs to mature to be able to reproduce in the host
	public static int death_env ;							//time it takes (days) for eggs to die or become unavailable for ingestion
	public static int death_host ;							//time it takes (days) for ingested eggs to die in the host
	public static double Denv_reduction = 0.0;				//reduction in time it takes (days) for eggs to die in the environment based on vegetation in a cell
	public static double maxDBH = 2592;						//Maximum DBH value on the resource landscape
	
	//Epidemiology: parasite transmission
	public final static double probDefication = 5.00 / stepsPerDay; //chance that an individual defecates 
	public static double probIngestEgg = 0.00000585; 				//probability of ingesting one egg from the environment, and that the egg survives and matures to an adult larvae stage
	public static int numbInitiallyInfected = 10;					//initial infected population
	   
	//landscape alterations
	public static double totalAlteration = 0.0;				//total amount of forest removed (DBH)
	public static double patch_size = 0.0;					//size of forest patches to remove (m) 
	
	//Watcher agent (for observations)
	public final static int fecalSampleSize = 98;		//simulation fecal sample to match observation sample 
	public final static int minPatchSize = 3; 			//used to calculate components when converting the landscape to a graph
	
	
	//Output measures:
		// Prevalence and intensity at end of simulation
		// Ro estimates: logistic growth parameter from incident data
		// Aggregation: distribution of eggs in stool from a random sample of 98 individuals 
	
	//Landscape measures
		// Connectance (FRAGSTAT)
		// cell correlation between (infected cells only): visits,contamination,and betweeness scores
		// patch sizes
		// average visits to individual cells
	
	//Behavioural measures
		//monthly home range size (intensity of use)
		//distance travelled per day (intensity of use)
		//amount of scramble competition (host-env interaction)
	
	
	
	//Constructor: used to set values from batch runs or the GUI
	public Parameter(){
		randomSeed = (Integer)p.getValue("randomSeed");
		
		//env
		mature_egg_infectiousEgg = (Integer)p.getValue("mature_e_i") * stepsPerDay;
		death_env = (Integer)p.getValue("Death_env") * stepsPerDay;
		
		//host
		mature_infectiousEgg_larvae = (Integer)p.getValue("mature_i_l") * stepsPerDay;
		death_host = (Integer)p.getValue("Death_host") * stepsPerDay;
		death_host = death_host + mature_infectiousEgg_larvae;
		
		randGen = new cern.jet.random.engine.MersenneTwister(new java.util.Date());
		//transmission
		probIngestEgg = (Double)p.getValue("prob_ingest");
		Denv_reduction = (Double)p.getValue("dbh_cost");
		
		//Landscape
		totalAlteration = (Double)p.getValue("alteration");
		patch_size = (Double)p.getValue("patch_size");
	}
}
