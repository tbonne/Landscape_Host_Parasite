package LHP;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.awt.event.MouseEvent;

import edu.uci.ics.jung.algorithms.cluster.BicomponentClusterer;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ScalingControl;

import LHP.PGMReader;
import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import tools.LandscapeUtils;
import tools.MoranCal;
import tools.SimUtils;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.*;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

//This file builds the model: creating the environment and populating it with primate hosts and trichuris parasites
public class ModelSetup implements ContextBuilder<Object>{

	private static Context mainContext;
	private static Geography geog;
	private static int cellsAdded;
	private static double resAdded;
	public static int primatesAdded;
	public static ArrayList<Primate> primatesAll;
	public static ArrayList<Cell> cellsToUpdate;
	public static ArrayList<Cell> removeCellsToUpdate;
	public static ArrayList<Cell> allCells;
	public static double timeRecord;
	public static double timeRecord_start;
	public static UndirectedGraph<HabitatNode, HabitatAssociation> habitatG;

	public Context<Object> build(Context<Object> context){

		System.out.println("Running a host-parasite model on a landscape");

		/********************************
		 * 								*
		 * initialize model parameters	*
		 * 								*
		 * ******************************/

		cellsAdded = 0;
		resAdded=0;
		primatesAdded = 0;
		primatesAll= new ArrayList<Primate>();
		mainContext = null;
		geog=null;
		cellsToUpdate = new ArrayList<Cell>();
		removeCellsToUpdate = new ArrayList<Cell>();
		allCells = new ArrayList<Cell>();
		Parameter parameter = new Parameter();
		mainContext = context; //static link to context
		timeRecord = System.currentTimeMillis();
		timeRecord_start = System.currentTimeMillis();
		habitatG = new UndirectedSparseGraph();

		/****************************
		 * 							*
		 * Building the landscape	*
		 * 							*
		 * *************************/

		//Create Geometry factory; used to create gis shapes (points=primates; polygons=resources)
		GeometryFactory fac = new GeometryFactory();

		//read in a raster file using a modifier PGMreader
		PGMReader reader = new PGMReader(Parameter.landscape);
		double matrix[][] = reader.getMatrix();
		double noData = -9999;

		//x and y dims of the map file
		int xdim = reader.xSize;
		int ydim = reader.ySize;

		//corners of map file (lower left)
		double xcoordStart = reader.getTopLeftX();
		double ycoord= reader.getTopLeftY();
		double xcoord= reader.getTopLeftX();
		int cellSize = reader.getCellSize();

		//Create Geography/GIS 
		GeographyParameters<Object> params= new GeographyParameters<Object>();
		GeographyFactory factory = GeographyFactoryFinder.createGeographyFactory(null);
		geog = factory.createGeography(Parameter.geog, context, params);
		//geog.setCRS("EPSG:32636"); //WGS 84 / UTM zone 36N

		//Add Resources to the environment *****************************************//
		int cellCount=0,forestCount=0;
		for (int i = 0; i < ydim; ++i) {
			for (int j = 0; j < xdim; ++j) {
				if (matrix[i][j]!=noData && matrix[i][j]>0 && matrix[i][j]!=-9999){

					double food = Math.max(Parameter.rcBiteSize, matrix[i][j]); //this adds the estimate of the RC food DBH +- error N(0,294) +RandomHelper.createNormal(0, 294).nextInt()
					int lClass = 0;		//forest
					final Cell cell = new Cell(context,xcoord,ycoord,food,j,i,lClass,cellCount);
					resAdded=resAdded+food;
					cellsAdded++;
					allCells.add(cell);
					forestCount++;

				}else if (matrix[i][j]<=0){

					double food = 0;	
					int lClass = 1;		
					final Cell cell = new Cell(context,xcoord,ycoord,food,j,i,lClass,cellCount);
					resAdded=resAdded+food;
					cellsAdded++;
					allCells.add(cell);

				}else{
					//no value in matrix
					System.out.println("Martix of resources returned a NA value");
				}

				cellCount++;
				//shift ycoord by cell size value
				xcoord=xcoord+cellSize;
			}
			//Set ycoord back to the start and shift xcoord up by cell size value
			xcoord=xcoordStart;
			ycoord = ycoord-cellSize;
		}

		System.out.println("done adding forest cells, total: "+forestCount+"  total cells: "+allCells.size());

		//converts DBH calculated from the remote sensing data to food values in the model 
		setTotalResources(cellsAdded);

		//modifies the landscape: simulate logging
		LandscapeUtils.modifyLandscape(allCells,resAdded);

		//to simplify the model all cells record the visible neighbouring cells (within visible range for a primate)
		for (Cell c: allCells){
			c.setVisibleNeigh_dist();
		}
		
		/************************************
		 * 							        *
		 * Adding hosts to the landscape	*
		 * 							        *
		 * *********************************/
		
		//adding red colobus hosts to the landscape
		for (int i = 0; i<Parameter.rcGroups;i++){

			//keep track of groups being added
			ArrayList<Primate> group = new ArrayList<Primate>();

			//Center of group
			double xCenter=Parameter.rcLocations[i*2];
			double yCenter=Parameter.rcLocations[i*2+1];
			
			boolean isForestSite = false,proximityRule = false;

			while(isForestSite==false || proximityRule == false){ //
				xCenter =  RandomHelper.nextDoubleFromTo(xcoord+30, xcoord+ xdim*cellSize-30); 
				yCenter =  RandomHelper.nextDoubleFromTo(ycoord+30, ycoord+ ydim*cellSize-30);

				//this is a good group center if it is in a forest cell 
				if(SimUtils.getCellAtThisCoord(new Coordinate(xCenter,yCenter)).getDBH()>0)isForestSite=true;

				//this is a good group center if it is X distance away from another group center (initial territoriality)
				boolean otherRC = (SimUtils.getObjectsWithin(new Coordinate(xCenter,yCenter), Parameter.teritoriality, RedColobus.class)).iterator().hasNext();
				if(otherRC==false)proximityRule=true;
			}

			int groupSize = Parameter.rcGroupSizes[i];

			//first RC add in this group will be set as the leader
			boolean isLeader = true;

			//add individuals
			for (int j = 0; j < groupSize; j++){
				Coordinate coord = new Coordinate(xCenter+0.005+ThreadLocalRandom.current().nextDouble(-(30),(30)), yCenter-0.005+ThreadLocalRandom.current().nextDouble(-30,(30)));
				RedColobus rc = new RedColobus("RC",i,primatesAdded++,coord,groupSize,isLeader);
				isLeader=false;
				context.add(rc);
				primatesAll.add(rc);
				group.add(rc);
				Point geom = fac.createPoint(coord);
				geog.move(rc, geom);
				rc.myPatch = SimUtils.getCellAtThisCoord(coord);
			}
		}

		System.out.println("done adding primates, total: "+primatesAll.size()+" PARAMS-> D.host="+Parameter.death_host+" D.env="+Parameter.death_env+"  trans = "+Parameter.probIngestEgg + "  dbh cost = "+Parameter.Denv_reduction);
		System.out.println("landscape: patch = "+Parameter.patch_size+", "+Parameter.totalAlteration);
		
		/************************************
		 * 							        *
		 * Adding parasites to the landscape*
		 * 							        *
		 * *********************************/

		//randomize order of the primate list
		Collections.shuffle(this.primatesAll);
		
		//infect 100 individuals with 100 adult larvae in their gut 
		for(int i = 0; i<Parameter.numbInitiallyInfected;i++){
			Gut gut =primatesAll.get(i).myGut; 
			gut.getLarvae().add(new NGroup_Larvae(30,gut,Parameter.mature_infectiousEgg_larvae));
		}
		
		/************************************
		 * 							        *
		 * create the scheduling			*
		 * 							        *
		 * *********************************/

		// Ordering processes
		// (1) get inputs for agents						- threaded
		// (2) behavioural responses of agents (movement) 	- threaded
		// (3) behavioural responses of agents (energy/gut)	- threaded
		// (4) environment growback							- threaded
		// (5) add/remove cells to modify					- not threaded
		// (6) Mark the center of the group (watcher)		- not threaded

		//executor takes care of the processing of the schedule
		Executor executor = new Executor();
		createSchedule(executor);

		/************************************
		 * 							        *
		 * Adding watcher agent				*
		 * 							        *
		 * *********************************/

		//watcher agent records all output
		Watcher w = new Watcher(executor);
		context.add(w);


		return context;

	}

	
	/***********************************************Model Methods****************************************************************************/
	
	
	private void createSchedule(Executor executor){

		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();

		ScheduleParameters agentStepParamsPrimate = ScheduleParameters.createRepeating(1, 1, 6); //start, interval, priority (high number = higher priority)
		schedule.schedule(agentStepParamsPrimate,executor,"getInputs");

		ScheduleParameters agentStepParamsPrimateBehaviour = ScheduleParameters.createRepeating(1, 1, 5); //start, interval, priority (high number = higher priority)
		schedule.schedule(agentStepParamsPrimateBehaviour,executor,"behaviour");

		ScheduleParameters agentStepParamsPrimateEnergy = ScheduleParameters.createRepeating(1, 1, 4); //start, interval, priority (high number = higher priority)
		schedule.schedule(agentStepParamsPrimateEnergy,executor,"energyUpdate");

		ScheduleParameters agentStepParamsPrimateMemory = ScheduleParameters.createRepeating(1, 1, 3); //start, interval, priority (high number = higher priority)
		schedule.schedule(agentStepParamsPrimateMemory,executor,"memoryUpdate");

		ScheduleParameters agentStepParams = ScheduleParameters.createRepeating(1, 1, 2);
		schedule.schedule(agentStepParams,executor,"envUpdate");
	}


	//used to update only the cells which have been modified, have parasites, or are growing back
	@ScheduledMethod(start=1, interval = 1,priority=1)
	public static void removeCellsUpdated(){
		for(Cell c : removeCellsToUpdate){
			cellsToUpdate.remove(c);
		}
		removeCellsToUpdate.clear();
	}

	//used to convert DBH to food values in the model
	private void setTotalResources(int numbCells){
		//calculate the percent difference between the total resource level now and the target level
		double targetRes = numbCells*Parameter.foodDensity;
		double perDiff = resAdded / targetRes;
		System.out.println("conversion = "+perDiff);
		double newTotal=0;
		int count=0;

		//Divide each resource by the percent difference to make the total equal the target resource amount
		for (Cell c : getAllCellAgents()){
			c.setMaxResourceLevel((int)((double)c.getMaxResourceLevel()/(double)perDiff));
			c.setResourceLevel((int)((double)c.getResourceLevel()/(double)perDiff));
			newTotal = newTotal + c.getResourceLevel();
			count++;
		}
	}

		public static void stopSim(Exception ex, Class<?> clazz) {
			ISchedule sched = RunEnvironment.getInstance().getCurrentSchedule();
			sched.setFinishing(true);
			sched.executeEndActions();
		}

		public static Iterable<Cell> getAllCellAgents() {
			return allCells;
		}

		public static Iterable<Primate> getAllPrimateAgents(){
			Collections.shuffle(primatesAll, new Random(System.currentTimeMillis()));
			Iterable<Primate> agents = primatesAll;
			return agents;
		}
		
		public static Context<Cell> getContext() {
			return ModelSetup.mainContext;
		}
		public static Geography<Primate> getGeog(){
			return geog;
		}
		public synchronized static void addToCellUpdateList(Cell c){ 
			if(cellsToUpdate.contains(c)==false)cellsToUpdate.add(c);
		}
		public synchronized static void removeCellToUpdate(Cell c){
			removeCellsToUpdate.add(c);
		}
		public static ArrayList<Cell> getCellsToUpdate(){
			return cellsToUpdate;
		}
		public static <T> Geometry getAgentGeometry(T agent) {
			return getGeog().getGeometry(agent);
		}
		public static double getCorrelationPValue(double r,int nObs){
			TDistribution tDistribution=new TDistributionImpl(nObs - 2);
	        double t=Math.abs(r * Math.sqrt((nObs - 2) / (1 - r * r)));
	        double p =-1;
	        try{
	        p = (2 * tDistribution.cumulativeProbability(-t));
	        } catch (MathException e){
	        	e.printStackTrace();
	        }
	        return p;
		}

	
	/************************************************************
	 * 							                                *
	 * Methods for a graph analysis of the landscape			*
	 * 							                                *
	 * *********************************************************/
		
	public static void measureConnectance(){

		double count_connections=0,total_possible_connections=0,count_totalForest_cells=0;
		double connectivityIndex=0;

		//get adjacency counts: sum the number of like forest cells within visual range of each forest cell 
		for (Cell current : getAllCellAgents()){
			if(current.land_class==0)count_totalForest_cells++;
			for (Cell neigh : current.getVisibleNeigh()){ 
				if(current.land_class==0 && neigh.land_class==0 && current != neigh){
					count_connections++;
				}
				total_possible_connections++;
			}
		}

		connectivityIndex =  ((count_connections) / ((count_totalForest_cells*(count_totalForest_cells-1))/2 ) )*100;
		double connectivityIndex2 =  ((count_connections) / total_possible_connections  )*100;

		Watcher.setConnectance(connectivityIndex2);

	}

	public static void measureHabitatGraph(){
		//use graph theory to measure IIC, LCP, and patch statistics

		long forestEdgeCounts=0;

		//add all patches as nodes
		for (Cell current : getAllCellAgents()){
			habitatG.addVertex(new HabitatNode(current.getID(),current));
		}

		//connect all adjacent forest cells 
		HabitatNode currentNode = null,neighNode=null;
		for (Cell current : getAllCellAgents()){
			if(current.land_class==0){
				for (Cell neigh : current.getVisibleNeigh()){
					double dist = neigh.getCoord().distance(current.coord);
					int count=0;
					if(dist <=Math.pow(Parameter.cellSize*Parameter.cellSize*2, 0.5)){
						count++;
						if(neigh.land_class==0 && current.id != neigh.id){ //both forest patches -> add connection
							for(HabitatNode node: habitatG.getVertices()){
								if(node.id==current.getID())currentNode=node;
								if(node.id==neigh.getID())neighNode=node;
							}
							HabitatAssociation hb = null;
							hb = habitatG.findEdge(currentNode, neighNode);
							if(hb==null){ //undirected graph, keep only one association 
							habitatG.addEdge(new HabitatAssociation(neigh.getID(),current.getID()),neighNode,currentNode);
							forestEdgeCounts++;
							}
						}
					}
				}
				neighNode = null;
			}
			currentNode = null;
		}


		//
		//***************measure landscape graph statistics
		//
		
		int number_of_components = 0;

		//1.component parts
		Transformer<Graph<HabitatNode, HabitatAssociation>, Set<Set<HabitatNode>>> trns = new WeakComponentClusterer<HabitatNode,HabitatAssociation>();
		Set<Set<HabitatNode>> clusters = trns.transform(habitatG);
		number_of_components = clusters.size();

		//2. patch sizes
		ArrayList<Integer> clusterSizes = new ArrayList<Integer>();
		ArrayList<ArrayList<HabitatNode>> clusterGroups = new ArrayList<ArrayList<HabitatNode>>();
		
		//for each cluster
		for (Set<HabitatNode> s:clusters){
			int count=0;
			Iterator<HabitatNode> clust = s.iterator();
			
			//count how many cells are contained + within patch visits and contamination
			while(clust.hasNext()){
				count++;
				clust.next();
			}
			
			//if count is larger than min add to component list group
			if(count>Parameter.minPatchSize){
				clusterSizes.add(count); //add up the forest cells 
			} 
		}
		
		//average number of patches and their avg size
		double avg_patch_size=0; 
		for(Integer i:clusterSizes){
			avg_patch_size = avg_patch_size + i;
		}
		avg_patch_size = avg_patch_size/ (double)(clusterSizes.size());
		
		Watcher.setAvgPatchSize(avg_patch_size);
		Watcher.setAvgNumberOfPatches(clusterSizes.size());

		//3. Assess intensity of habitat use and contamination within patches
		double averageVisits=0,averageContamination=0,size=0;

		for(Set<HabitatNode> hb: clusters){
			averageVisits=0;
			averageContamination=0;
			size=0;
			for(HabitatNode hn:hb){
				size++;
				averageVisits = averageVisits+hn.getMyCell().visit;
				averageContamination = averageContamination + hn.getMyCell().getInfectiousEggCount();
			}
		}

		//4. Assess correlation between betweeness of cell, contamination, and visits
		
		//calculate degree of betweeness (or class) and habitat contamination
		BetweennessCentrality<HabitatNode,HabitatAssociation> btw = new BetweennessCentrality<HabitatNode,HabitatAssociation>(habitatG);

		//set betweeness for each node
		for(HabitatNode hn:habitatG.getVertices()){ 
			hn.setBTW(btw.getVertexScore(hn)); 
		}

		//get correlation between betweeness an contamination/visits
		PearsonsCorrelation pc = new PearsonsCorrelation();
		int observations = habitatG.getVertices().size();
		double[] betweeness = new double[observations];
		double[] contamination = new double[observations];
		double[] visits = new double[observations];
		ArrayList<Double> betweeness_infected = new ArrayList<Double>();
		ArrayList<Double> contamination_infected = new ArrayList<Double>();
		ArrayList<Double> visits_infected = new ArrayList<Double>();
		
		int count = 0;
		double avgBetween=0;
		ArrayList<Double> nodesBTW=new ArrayList<Double>();
		for(HabitatNode hn:habitatG.getVertices()){
			betweeness[count]=(hn.getBTW());
			avgBetween = avgBetween + hn.getBTW();
			nodesBTW.add(hn.getBTW());
			contamination[count]=(hn.getMyCell().getInfectiousEggCount());
			visits[count]=hn.getMyCell().getVisitCount();
			//only include those cells infected
			if(hn.getMyCell().getInfectiousEggCount()>0){
				betweeness_infected.add(hn.getBTW());
				contamination_infected.add(hn.getMyCell().getInfectiousEggCount());
				visits_infected.add(hn.getMyCell().getVisitCount());
			}
			count++;
		}
		
		//median betweeness value
		Collections.sort(nodesBTW);
		int middle = nodesBTW.size()/2;
		if (nodesBTW.size()%2 == 1) {
	        nodesBTW.get(middle);
	    } else {
	        Watcher.setMedianBTW((nodesBTW.get(middle-1) + nodesBTW.get(middle)) / 2.0);
	    }
		
		double cor_bc=pc.correlation(betweeness, contamination);
		double cor_bc_p = getCorrelationPValue(cor_bc,observations);
		double cor_bv=pc.correlation(betweeness, visits);
		double cor_bv_p= getCorrelationPValue(cor_bv, observations);
		double cor_cv=pc.correlation(contamination, visits);
		double cor_cv_p= getCorrelationPValue(cor_cv, observations);

		Watcher.setcor_bc(cor_bc);
		Watcher.setcor_bv(cor_bv);
		Watcher.setcor_cv(cor_cv);
		Watcher.setcor_bc_p(cor_bc_p);
		Watcher.setcor_bv_p(cor_bv_p);
		Watcher.setcor_cv_p(cor_cv_p);
		
		//only cells infected
		double cor_bc_infected=pc.correlation(betweeness, contamination);
		double cor_bc_p_infected = getCorrelationPValue(cor_bc_infected,count);
		double cor_bv_infected=pc.correlation(betweeness, visits);
		double cor_bv_p_infected=getCorrelationPValue(cor_bv_infected,count);
		double cor_cv_infected=pc.correlation(contamination, visits);
		double cor_cv_p_infected=getCorrelationPValue(cor_cv_infected,count);
		
		Watcher.setcor_bc_infected(cor_bc_infected);
		Watcher.setcor_bv_infected(cor_bv_infected);
		Watcher.setcor_cv_infected(cor_cv_infected);
		Watcher.setcor_bc_p_infected(cor_bc_p_infected);
		Watcher.setcor_bv_p_infected(cor_bv_p_infected);
		Watcher.setcor_cv_p_infected(cor_cv_p_infected);
		
		Watcher.setAvgBetweenness((double)avgBetween/(double)observations);

	}

	public void vizualizeGraph(){

		Transformer<HabitatNode, Point2D> locationTransformer = new Transformer<HabitatNode, Point2D>() {

			@Override
			public Point2D transform(HabitatNode vertex) {
				//return new Point2D.Double((double) vertex.getMyCell().coord.x-205588, (double) vertex.getMyCell().coord.y-59249);
				return new Point2D.Double((double) vertex.getMyCell().coord.x-59249, (double) vertex.getMyCell().coord.y-205588);
			}
		};

		StaticLayout<HabitatNode, HabitatAssociation> layout = new StaticLayout<HabitatNode, HabitatAssociation>(
				habitatG, locationTransformer);
		layout.setSize(new Dimension(500, 500));
		VisualizationViewer<HabitatNode, HabitatAssociation> vv = new VisualizationViewer<HabitatNode, HabitatAssociation>(
				layout);
		
		//add the listener
        vv.addGraphMouseListener(new GraphMouseListener() {

            @Override
            public void graphClicked(Object v, MouseEvent me) {
                if (me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() == 2) {
                    System.out.println("Double clicked "+ v);
                }
                me.consume();
                
            }

            @Override
            public void graphPressed(Object v, MouseEvent me) {
            }

            @Override
            public void graphReleased(Object v, MouseEvent me) {
            }
        });

		vv.setPreferredSize(new Dimension(550, 550));
		vv.setAutoscrolls(true);
		// for zoom:
		vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).setScale(2, 2, vv.getCenter());
		// for out:
		vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).setScale(0.7, 0.7, vv.getCenter());
		
		// Transformer maps the vertex number to a vertex property
        Transformer<HabitatNode,Paint> vertexColor = new Transformer<HabitatNode,Paint>() {
            public Paint transform(HabitatNode i) {
                if(i.getMyCell().land_class == 0){
                	if (i.getBTW() < 200) return Color.BLUE;
                	if (i.getBTW() < 2000) return Color.CYAN;
                	if (i.getBTW() < 5000) return Color.GREEN;
                	if (i.getBTW() < 10000) return Color.YELLOW;
                	if (i.getBTW() < 50000) return Color.ORANGE;
                	if (i.getBTW() >=50000) return Color.RED;
                }
                return Color.GRAY;
            }
        };
        Transformer<HabitatNode,Shape> vertexSize = new Transformer<HabitatNode,Shape>(){
            public Shape transform(HabitatNode i){
                Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
                // in this case, the vertex is twice as large
                return AffineTransform.getScaleInstance(0.2, 0.2).createTransformedShape(circle);
                //else return circle;
            }
        };
        vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
        vv.getRenderContext().setVertexShapeTransformer(vertexSize);
		

		JFrame frame = new JFrame("Simple Graph View 2");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		vv.setOpaque(false);
		frame.pack();
		frame.setVisible(true);
	}


	public static void measureCONTAGION(){

		double count_cells_Forest=0,count_cells_nonForest=0,numb_patch_types=2;
		double adj_counts_Forest_nonForest=0,adj_counts_Forest_Forest=0,adj_counts_nonForest_nonForest=0;
		double contagionIndex=0;

		//get adjacency counts
		for (Cell c : getAllCellAgents()){
			for (Cell neigh : c.getVisibleNeigh()){
				if(c.getCoord().distance(neigh.getCoord())<=Parameter.cellSize){ //orthagonal neighbours
					if (c.land_class==0){ //forest
						count_cells_Forest++;
						if(neigh.land_class==0)adj_counts_Forest_Forest++;
						if(neigh.land_class==1)adj_counts_Forest_nonForest++;
					} else if (c.land_class == 1){ //non-forest
						count_cells_nonForest++;
						if(neigh.land_class==0)adj_counts_Forest_nonForest++;
						if(neigh.land_class==1)adj_counts_nonForest_nonForest++;
					}
				}
			}
		}

		//calculate CONTAGION      				   proportion of forest cells         
		double sum_forest_forest = 			(	((count_cells_Forest/cellsAdded) * (adj_counts_Forest_Forest /(adj_counts_Forest_Forest+adj_counts_Forest_nonForest))) * (Math.log(count_cells_Forest/cellsAdded) * (adj_counts_Forest_Forest /(adj_counts_Forest_Forest+adj_counts_Forest_nonForest)))    )/ (2*Math.log(numb_patch_types));
		double sum_forest_nonForest = 		(	((count_cells_Forest/cellsAdded) * (adj_counts_Forest_nonForest /(adj_counts_Forest_Forest+adj_counts_Forest_nonForest))) * (Math.log(count_cells_Forest/cellsAdded) * (adj_counts_Forest_nonForest /(adj_counts_Forest_Forest+adj_counts_Forest_nonForest)))  )/ (2*Math.log(numb_patch_types));
		double sum_nonforest_nonforest = 	(	((count_cells_nonForest/cellsAdded) * (adj_counts_nonForest_nonForest /(adj_counts_nonForest_nonForest+adj_counts_Forest_nonForest))) * (Math.log(count_cells_nonForest/cellsAdded) * (adj_counts_nonForest_nonForest /(adj_counts_nonForest_nonForest+adj_counts_Forest_nonForest)))  )/ (2*Math.log(numb_patch_types));
		double sum_nonforest_forest = 		(	((count_cells_nonForest/cellsAdded) * (adj_counts_Forest_nonForest /(adj_counts_nonForest_nonForest+adj_counts_Forest_nonForest))) * (Math.log(count_cells_nonForest/cellsAdded) * (adj_counts_Forest_nonForest /(adj_counts_nonForest_nonForest+adj_counts_Forest_nonForest)))  )/ (2*Math.log(numb_patch_types));

		contagionIndex = (1 + (sum_forest_forest + sum_forest_nonForest + sum_nonforest_nonforest + sum_nonforest_forest) ) * 100;

		Watcher.setContagion(contagionIndex);

	}

}
