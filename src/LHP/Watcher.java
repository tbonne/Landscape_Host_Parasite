package LHP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.ShapefileWriter;
import tools.GroupUtils;
import tools.MoranCal;


//This represents an observer recording patterns of interest
public class Watcher {

	ArrayList<ArrayList<Integer>> groupSize;
	ArrayList<Integer> fecalSample, prevelence;
	ArrayList<Long> cellsInfected;
	ArrayList<Double> intensity;
	long markerPointTag,areaContaminated=0,cellsInfectedCount;
	int month,zeroInfectedCounter=0;
	int[] group_in,group_out,group_size,observed_faecalCount,observed_intensity;
	double observed_prevalence;
	final RunEnvironment runEnvironment;
	WritableSheet excelSheet_size,excelSheet_homeRange,excelSheet_fecal, excelSheet_pop;
	WritableWorkbook workbook;
	public static long scrambleCount;
	public static long immigrations;
	private static double  dist_abun=0,dist_int=0,dist_prev=0, r_naught_abundance=0, r_naught_larvae=0,averageVisits,geomeanVisits; //k_aggregation=0, k_sd=0,
	Executor executor;
	public static boolean maxReached=false;
	BufferedWriter writer3;
	public static Double contagion,connectance,iic,lcp,avg_patch_size,avg_number_of_patches,avg_betweenness,median_betweenness;
	public static Double cor_between_contamination,cor_between_contamination_p,cor_between_visit,cor_between_visit_p,cor_visit_contamination,cor_visit_contamination_p;
	public static Double cor_between_contamination_infected,cor_between_contamination_p_infected,cor_between_visit_infected,cor_between_visit_p_infected,cor_visit_contamination_infected,cor_visit_contamination_p_infected;
	public static double meanEnergy=0,finalMeanEnergy=0,finalMeanHomeRange=0;

	//
	//
	///Watcher class is used to monitor and record model variables
	//
	//

	public Watcher(Executor exe) {

		markerPointTag = 0;
		month=0;
		groupSize = new ArrayList<ArrayList<Integer>>();
		fecalSample = new ArrayList<Integer>();
		prevelence = new ArrayList<Integer>();
		intensity = new ArrayList<Double>();
		cellsInfected = new ArrayList<Long>();
		cellsInfectedCount=0;
		scrambleCount=0;
		immigrations=0;
		executor = exe;
		contagion = 0.0;
		connectance = 0.0;
		iic=0.0;
		lcp=0.0;
		avg_betweenness=0.0;
		median_betweenness=0.0;
		avg_patch_size = 0.0;
		avg_number_of_patches=0.0;
		cor_between_contamination=0.0;
		cor_between_contamination_p=0.0;
		cor_between_visit=0.0;
		cor_between_visit_p=0.0;
		cor_visit_contamination=0.0;
		cor_visit_contamination_p=0.0;	
		cor_between_contamination_infected=0.0;
		cor_between_contamination_p_infected=0.0;
		cor_between_visit_infected=0.0;
		cor_between_visit_p_infected=0.0;
		cor_visit_contamination_infected=0.0;
		cor_visit_contamination_p_infected=0.0;

		observed_faecalCount = importFaecalCounts(true); //abundance
		observed_intensity = importFaecalCounts(false);  //intensity
		observed_prevalence = importPrevalence(observed_faecalCount); //prevalence

		finalMeanEnergy=0;
		finalMeanHomeRange=0;

		//initialize arraylists to store variables
		for (int i=0;i<Parameter.rcGroups;i++){
			groupSize.add(new ArrayList<Integer>());
		}

		runEnvironment = RunEnvironment.getInstance();

		/*
		 * creating a file to store the output of the model
		 */
		try {
			File fileOut = new File("C:/Users/t-work/Dropbox/code/SpatialMemory/LHP_dispersal/LHP/data/MovementStats_Frag2_" + Parameter.death_env + "_"+Parameter.death_host+"_"+Parameter.mature_egg_infectiousEgg+"_"+Parameter.mature_infectiousEgg_larvae+"_"+Parameter.probIngestEgg+"_"+Parameter.patch_size+"_"+Parameter.totalAlteration+"_"+Parameter.randomSeed+"_"+System.currentTimeMillis()+ ".xls");
			WorkbookSettings wbSettings = new WorkbookSettings();
			wbSettings.setLocale(new Locale("en", "EN"));

			workbook = Workbook.createWorkbook(fileOut, wbSettings);
			workbook.createSheet("GroupSize", 0);
			workbook.createSheet("HomeRange", 1);
			workbook.createSheet("fecal", 2);
			workbook.createSheet("pop", 3);
			excelSheet_size = workbook.getSheet(0);
			excelSheet_homeRange = workbook.getSheet(1);
			excelSheet_fecal = workbook.getSheet(2);
			excelSheet_pop = workbook.getSheet(3);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/********************************	
	 * 								*
	 *         Stepper				*
	 * 								*
	 *******************************/

	@ScheduledMethod(start=26, interval = 1,priority=0)
	public void step(){

		//	to do every tick
		//System.out.println("step: "+this.runEnvironment.getCurrentSchedule().getTickCount());
		//exportPrimatePoints(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());

		//	to do every day
		if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()%26==0){
			recordInfectionDynamics();
		}

		//	to do every month
		if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()%(26*30)==0){
			month++;

			finalMeanHomeRange = finalMeanHomeRange + getHomeRangeSize();
			finalMeanEnergy = finalMeanEnergy + getAVGPrimateEnergy();
			
			System.out.println("month = "+month + ", prev = "+prevelence.get(prevelence.size()-1)+", intent = "+intensity.get(intensity.size()-1));
			
			if(month<Parameter.endMonth){

				for(Cell cell:ModelSetup.allCells){
					cell.clearHR();
				}

			} else {
				finalMeanHomeRange = finalMeanHomeRange/month;
				finalMeanEnergy = finalMeanEnergy/month;
			}
		}

		//	to do at the end of the simulation
		if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()>=26*30*Parameter.endMonth){
			endModel();
		}
		
		if(prevelence.get(prevelence.size()-1)==0){
			//zeroInfectedCounter++;
		}
		if(zeroInfectedCounter>5){
			endModel();
		}
	}

	/********************************	
	 * 								*
	 *          Methods				*
	 * 								*
	 *******************************/

	private void endModel(){
		ModelSetup.measureHabitatGraph();
		ModelSetup.measureConnectance();
		executor.shutdown();
		estimateAreaContaminated(); //Count the grids contaminated
		samplePopulation();   		//compare simulation distribution to observed distribution
		recordData();				//record the data to an excel sheet and txt file
		//exportLandscape();		//export the final landscape

		System.out.println("total time " + (System.currentTimeMillis() - ModelSetup.timeRecord_start) + "  : SrambleCount - " + scrambleCount);
		RunEnvironment.getInstance().endAt(this.runEnvironment.getCurrentSchedule().getTickCount());
	}
	
	private int[] importFaecalCounts(Boolean abundance){

		java.io.InputStream stream = null;
		try {
			File file1;
			if(abundance == true){
				file1 = new File("C:/Users/t-work/Dropbox/Project_Managment_LHP_simulaiton/data/fecalPatterns/T_abundance.csv");
			} else {
				file1 = new File("C:/Users/t-work/Dropbox/Project_Managment_LHP_simulaiton/data/fecalPatterns/T_intensity.csv");
			}

			stream = new FileInputStream(file1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));

		return init(in,abundance);
	}
	
	private double importPrevalence(int[] abundance){
		double prevalence=0;
		for(Integer i : abundance){
			if(i>0)prevalence++;
		}
		System.out.println(prevalence/abundance.length);
		return prevalence/abundance.length;
	}

	private int[] init(BufferedReader in, boolean abundance){
		int[] vector = new int[98]; 
		if(abundance==false)vector = new int[43];

		try {
			StringTokenizer tok;

			String str = "";
			String line = in.readLine();

			while (line != null) {
				str += line + " ";
				line = in.readLine();
			}
			in.close();

			tok = new StringTokenizer(str);

			for (int i = 0; i < vector.length; i++){

				vector[i] = Integer.valueOf(tok.nextToken());
				//System.out.println("sample "+vector[i]);
			}

		} catch (IOException ex) {
			System.out.println("Error Reading csv file");
			ex.printStackTrace();
			System.exit(0);
		}

		return vector;
	}

	private void estimateAreaContaminated(){
		long count=0;
		for(Cell c: ModelSetup.getAllCellAgents()){
			if(c.getInfectiousGroups().size()>0 || c.getNonInfectiousGroups().size()>0)count++;
		}
		areaContaminated = count * Parameter.cellSize * Parameter.cellSize;
	}

	private void samplePopulation(){

		//get all agents
		Iterable<Primate> primates = ModelSetup.getAllPrimateAgents(); //random order of all primates

		for (Primate p: primates){
			fecalSample.add(p.myGut.getLastDeficationEggs());
		}
		//aggregation estimates (intensty and prevelence)
		estimateAggregation(fecalSample);
	}

	private void estimateAggregation(ArrayList<Integer> sample){

		List<Integer> total = sample;
		int sampleSize = Parameter.fecalSampleSize;
		int[] sub=new int[sampleSize];
		ArrayList<Integer> sub2 = new ArrayList<Integer>();
		double prevCount = 0,avgInten=0;
		dist_abun=0;
		dist_int=0;
		dist_prev=0;

		try {
			//System.out.println("Attempting to use R");
			REXP x,x2;
			RList l;
			RConnection c = new RConnection();
			//System.out.println("Connection set");
			c.eval("library(MASS)");
			c.eval("library(dgof)");
			//System.out.println("Library loaded");

			try{


				//recursive sampling 100 times for sample distribution
				for(int i=0;i<100;i++){

					//randomize sample
					Collections.shuffle(total);

					//create the intensity abundance, prevalence and intensity sub-samples
					prevCount=0;
					avgInten=0;
					
					for (int j=0;j<sampleSize;j++){                               
						sub[j]=total.get(j);
						if(total.get(j)>0){
							sub2.add(total.get(j));
							avgInten+=total.get(j);
							prevCount++;
						}
					}

					//convert to array
					int[] sub2Array = new int[sub2.size()];
					for(int j=0;j<sub2.size();j++){
						sub2Array[j]=sub2.get(j);
					}

					//calculate prev dist
					prevCount = prevCount/(double)sampleSize;
					dist_prev = dist_prev+Math.pow((prevCount-observed_prevalence),2);
					//if(prevCount==0)dist_prev=60;

					//caculate in R the distance D from the observed distributions
					c.assign("abun", observed_faecalCount);
					c.assign("intensity", observed_intensity);
					c.assign("x", sub);
					c.assign("x2",sub2Array); 
					try{
						c.eval("d <- dgof::ks.test(x,abun)");
						c.eval("d2 <- dgof::ks.test(x2,intensity)");
					}catch(RserveException r){
						c.eval("d2 <- 0");
					}
					x = c.eval("as.double(d[1])");
					x2 = c.eval("as.double(d2[1])");

					dist_abun = dist_abun + x.asDouble();
					dist_int = dist_int + x2.asDouble();
					if(prevCount==0)dist_int=60; //corrects for zero intensity distance when prevelance is 0 
				}
				
				System.out.println("infected = "+prevCount+" intensity = "+avgInten+"  %="+avgInten/prevCount*(double)sampleSize);

			}catch (RserveException r){
				r.printStackTrace();
				System.out.println("Failed to estimate k aggregation");
				dist_abun=99999999;
				dist_int=99999999;
			}

			try{
				//estimate r naught for abundance of infections
				int[] abun = convertIntegers(prevelence);
				c.assign("abun", abun);
				//System.out.println("assigning abundance data to R");
				c.eval("library(grofit)");
				//System.out.println("Library loaded");
				c.eval("days <- 1:length(abun)");
				c.eval("fit <- gcFitSpline(days,abun)");
				x = c.eval("summary(fit)[1]");
				l = x.asList();
				double slopeA = l.at(0).asDouble();
				//System.out.println("mu = "+slopeA + " this is r0 for abundance");
				r_naught_abundance = slopeA;

			}catch (RserveException r){
				r.printStackTrace();
				System.out.println("Failed to estimate R naught for the abundance data");
				r_naught_abundance=0;
			}

			/*	try{
				
				
				//estimate r naught for growth of larvae
				double[] la = convertLongToDouble(cellsInfected);
				c.assign("la", la);
				//System.out.println("assigning infection data to R");
				c.eval("library(grofit)");
				//System.out.println("Library loaded");
				c.eval("days <- 1:length(la)");
				c.eval("fit <- gcFitSpline(days,la)");
				x = c.eval("summary(fit)[1]");
				l = x.asList();
				double slopeA = l.at(0).asDouble();
				//System.out.println("mu = "+slopeA +" for larvae growth");
				r_naught_larvae = slopeA;
				

			}catch (RserveException r){
				r.printStackTrace();
				System.out.println("Failed to estimate R naught for larvae growth");
				r_naught_larvae=0;r_naught_larvae=0;
			}
*/
			c.close();


		}catch (Exception e){
			System.out.println("EX:"+e);
			e.printStackTrace();
		}



	}

	public static int[] convertIntegers(List<Integer> integers)
	{
		int[] ret = new int[integers.size()];
		for (int i=0; i < ret.length; i++)
		{
			ret[i] = integers.get(i).intValue();
		}
		return ret;
	}
	public static double[] convertLongToDouble(List<Long> integers)
	{
		double[] ret = new double[integers.size()];
		for (int i=0; i < ret.length; i++)
		{
			ret[i] = integers.get(i).doubleValue();
		}
		return ret;
	}

	private static double getHomeRangeSize(){

		double average_homeRange_size=0;

		for (int i =0; i < Parameter.rcGroups;i++){

			double groupHomeRangeSize = 0;

			for(Cell cell:ModelSetup.getAllCellAgents()){
				if(cell.getGroupsVisited()[i]==1){
					groupHomeRangeSize = groupHomeRangeSize + Parameter.cellSize*Parameter.cellSize;
				}
			}

			average_homeRange_size = average_homeRange_size + groupHomeRangeSize;

		}

		average_homeRange_size = average_homeRange_size / Parameter.rcGroups;

		return average_homeRange_size;
	}
	
	
	private static double[] getEggOutput(){
		double eggs=0,count=0;
		for(Primate p: ModelSetup.primatesAll){
			eggs = eggs + p.myGut.lastDefication;
			if(p.myGut.lastDefication>0)count=count+1;;
		}
		eggs=eggs/count;
		double[] eggsOUT = new double[2];
		eggsOUT[0]=eggs;
		eggsOUT[1]=count;
		return eggsOUT;
	}

	private static double getAVGPrimateEnergy(){
		double average_energy=0,count=0;

		for(Primate p:ModelSetup.getAllPrimateAgents()){
			average_energy = average_energy + p.getEnergy();
			count++;
		}

		average_energy = average_energy / count;

		return average_energy;
	}

	private void recordInfectionDynamics(){
		//reset counter variables
		double inten = 0;
		int prev=0;
		//long larvae_count=0;

		//get all agents
		Iterable<Primate> primates = ModelSetup.getAllPrimateAgents();

		//calculate intensity (load of infected individuals)
		//calculate prevalence (% of pop infected)
		for (Primate p: primates){
			if(p.myGut.getLarvae().size()>0){
				prev++;
			//	larvae_count = larvae_count + p.myGut.getLarvaeNumbs();
				inten=inten + p.myGut.getIntensityCount();
			}else{
				p.setInfected(false);
			}
		}

		//calculate intensity 
		inten = inten/prev;

		//add to arraylists
		intensity.add(inten);
		prevelence.add(prev);
		//larvae.add(larvae_count);
		
		//System.out.println("intensity = "+inten+"   prevelance = "+prev + "  larvae count = "+larvae_count);
		
		//if(prev==0)endModel();
		//long infectiousEggs=0;
		//for(Cell c:ModelSetup.allCells){
		//	infectiousEggs+= c.getInfectiousEggCount();
		//}
		//System.out.println("infectious eggs ENV = "+infectiousEggs);
		//if(infectiousEggs==0 && prev==0)endModel();
		
		
	}

	/********************************	
	 * 								*
	 *         Output data			*
	 * 								*
	 *******************************/


	private void recordData(){

		//double avg_immigrations=0;
		double avg_homeRange=0, sd_homeRange=0;
		double total_dist=0;

		int row=1,col=1;

		try {

			//add home range estimate to excel output
			String title = "HomeRange estimates (total sim)";
			Label label;
			label = new Label(col, row,title);
			row++;
			excelSheet_homeRange.addCell(label);

			int countP=0;
			for (Primate p: ModelSetup.getAllPrimateAgents()){
				Number number;
				number = new Number(col,row,p.getHomeRangeList().size());
				excelSheet_homeRange.addCell(number);
				meanEnergy = meanEnergy + p.energy;
				row++;
				countP++;
			}
			avg_homeRange =	getHomeRangeSize();

			//population avg
			Number number1;
			number1 = new Number(col,row,avg_homeRange);
			excelSheet_homeRange.addCell(number1);
			meanEnergy=meanEnergy/countP;

			//System.out.println("hr:"+finalMeanHomeRange+"  meanEnergy:"+finalMeanEnergy);

			//add movement measure
			for (Primate p: ModelSetup.getAllPrimateAgents()){
				Number number;
				number = new Number(col,row,p.getMoveDist());
				excelSheet_homeRange.addCell(number);
				total_dist = total_dist + p.getMoveDist();
				row++;
			}

			total_dist = total_dist / row;

			Number number3;
			number3 = new Number(col,row,total_dist/row);
			excelSheet_homeRange.addCell(number3);

			//add group size data to excel output
			col=1;row=1;
 			
			String title_group = "Group sizes (dayly)";
			Label lable_group;
			lable_group = new Label(col, row,title_group);
			row++;
			excelSheet_size.addCell(lable_group);

			for (ArrayList<Integer> group: groupSize){
				for(Integer i=0;i<group.size();i++){
					Number number;
					number = new Number(col,row,group.get(i));
					excelSheet_size.addCell(number);
					row++;
				}

				row++;
				//number of immigrations occuring in group
				int immigrations=0,emigrations=0;
				for (Integer i=1;i<group.size();i++){
					if(group.get(i)-group.get(i-1)==1)immigrations++;
					if(group.get(i)-group.get(i-1)==-1)emigrations++;
				}

				Number in = new Number(col,row,immigrations);
				excelSheet_size.addCell(in);
				//			avg_immigrations = avg_immigrations + immigrations;

				row++;
				Number out = new Number(col,row,emigrations);
				excelSheet_size.addCell(out);

				//reset for next group
				row=2;
				col++;
			}

			//add fecal sample to excel output
			col=1;row=1;
			String title_fecal = "Fecal samples (end)";
			Label lable_fecal;
			lable_fecal = new Label(col, row,title_fecal);
			row++;
			excelSheet_fecal.addCell(lable_fecal);

			for (Integer sample: fecalSample){
				Number number;
				number = new Number(col,row,sample);
				excelSheet_fecal.addCell(number);
				row++;
			}

			col++; row++;

			//calculate final prevalence and intensity measured in a fecal samples
			double mean=0,prev=0;
			int count_infected=0,count_total=0;
			for (Integer sample: fecalSample){
				mean = mean + sample;
				count_total++;
				if(sample>0)count_infected++;
			}

			mean = mean/count_infected;

			Number number;
			number = new Number(col,row,mean);
			excelSheet_fecal.addCell(number);
			row++;

			prev = (double)count_infected/(double)count_total;

			Number number2;
			number2 = new Number(col,row,prev);
			excelSheet_fecal.addCell(number2);

			//add population dynamics to excel output
			col=1;row=1;

			String title_pop = "Population dynamics (end)";
			Label lable_pop;
			lable_pop = new Label(col, row,title_pop);
			row++;
			excelSheet_pop.addCell(lable_pop);
			double final_prevalence = (double)prevelence.get(prevelence.size()-1),final_intensity = intensity.get(intensity.size()-1);

			for (Integer sample: prevelence){
				Number number5;
				number5 = new Number(col,row,sample);
				excelSheet_pop.addCell(number5);
				row++;
			}

			col++;row=2;

			for (Double sample: intensity){
				Number number4;
				number4 = new Number(col,row,sample);
				excelSheet_pop.addCell(number4);
				row++;
			}

			workbook.write();
			workbook.close();

			//calculate average visit counts
			averageVisits = 0;
			long count=0;
			DescriptiveStatistics stats_visits = new DescriptiveStatistics();
			for(Cell cell: ModelSetup.getAllCellAgents()){
				double v = cell.getVisitCount();
				if(v > 0)stats_visits.addValue(v);
				//averageVisits = averageVisits + cell.getVisitCount();
				//count++;
			}
			//System.out.println("count-"+count+" average-"+averageVisits);
			//averageVisits = averageVisits/count;
			averageVisits = stats_visits.getMean();
			//geomeanVisits = stats_visits.getGeometricMean();
			
			//calculate meadian transmission events by cell
			DescriptiveStatistics stats_expo = new DescriptiveStatistics();
			DescriptiveStatistics stats_depo = new DescriptiveStatistics();
			for(Cell c:ModelSetup.getAllCellAgents()){
				stats_expo.addValue(c.getExposureCount());
				stats_depo.addValue(c.getDepositCount());
			}
			double median_expo = stats_expo.getPercentile(50);
			double mean_expo = stats_expo.getMean();
			
			//calculate correlation between (average transmission events per cell, contamination) 
			
			double pearsonCorr_expo_depo = new PearsonsCorrelation().correlation(stats_expo.getValues(), stats_depo.getValues());
			
			//Spatial distribution: calculate moran's I for contamination and DBH
			
			
			
			//host distribution: calculate prevalence by group
			double groupMeanPrev=0;
			int withinGroupInfCount = 0,groupSizeCount=0;
			double[] groupPrev=new double[Parameter.rcGroups];
			
			for(int i=0;i<Parameter.rcGroups;i++){
			
			for(Primate p:ModelSetup.getAllPrimateAgents()){
				if(p.getGroupID()==i){
					groupSizeCount++;
					if(p.myGut.getIsInfected())withinGroupInfCount++;
				}
			}
			
			groupPrev[i]=(double)withinGroupInfCount/(double)groupSizeCount;
			groupMeanPrev=groupMeanPrev+groupPrev[i];
			
			withinGroupInfCount=0;
			groupSizeCount=0;
		
			}
			
			groupMeanPrev=groupMeanPrev/(double)Parameter.rcGroups;
			
			double groupSdPrev=0;
			for(double d : groupPrev){
				groupSdPrev=groupSdPrev+Math.pow((d-groupMeanPrev),2);
			}
			groupSdPrev=groupSdPrev/(Parameter.rcGroups-1);
			
			double[] mo =null;
			//double[] mo = MoranCal.estimateMoransI_R(ModelSetup.allCells);
			
			//*****************************************//
			//write to result file: temporary
			BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/t-work/Dropbox/code/SpatialMemory/LHP_dispersal/LHP/data/Frag_Results.csv",false));
			
			writer.append(Double.toString(dist_prev));
			writer.append(",");
			writer.append(Double.toString(dist_int));
			//System.out.println("total distance from observed = "+(dist_int+dist_prev)+"  d.int = "+dist_int+"  d.prev = "+dist_prev);
			
			writer.newLine();

			writer.flush();
			writer.close();

			//write to result file: permanent
			BufferedWriter writer2 = new BufferedWriter(new FileWriter("C:/Users/t-work/Dropbox/code/SpatialMemory/LHP_dispersal/LHP/data/Frag_Results_permanet2.csv",true));
			writer2.append(Double.toString(finalMeanHomeRange));
			writer2.append(",");
			writer2.append(Double.toString(averageVisits));
			writer2.append(",");
			writer2.append(Double.toString(scrambleCount));
			writer2.append(",");
			writer2.append(Double.toString(Parameter.totalAlteration));
			writer2.append(",");
			writer2.append(Double.toString(Parameter.patch_size));
			writer2.append(",");
			writer2.append(Double.toString(dist_abun));
			writer2.append(",");
			writer2.append(Double.toString(dist_int));
			writer2.append(",");
			writer2.append(Double.toString(r_naught_larvae));
			writer2.append(",");
			writer2.append(Double.toString(r_naught_abundance));
			writer2.append(",");
			writer2.append(Double.toString(final_prevalence/ModelSetup.primatesAdded));
			writer2.append(",");
			writer2.append(Double.toString(final_intensity));
			writer2.append(",");
			writer2.append(Double.toString(groupMeanPrev));
			writer2.append(",");
			writer2.append(Double.toString(groupSdPrev));
			writer2.append(",");
			writer2.append(Double.toString(areaContaminated));
			writer2.append(",");
			writer2.append(Long.toString(Parameter.mature_egg_infectiousEgg));
			writer2.append(",");
			writer2.append(Long.toString(Parameter.mature_infectiousEgg_larvae));
			writer2.append(",");
			writer2.append(Double.toString(Parameter.death_env));
			writer2.append(",");
			writer2.append(Double.toString(Parameter.death_host));
			writer2.append(",");
			//writer2.append(Double.toString(Parameter.reproductive_Host_larvae));
			//writer2.append(",");
			writer2.append(Double.toString(Parameter.probIngestEgg));
			writer2.append(",");
			//writer2.append(Integer.toString(Parameter.max_burden));
			//writer2.append(",");
			writer2.append(Integer.toString(Parameter.randomSeed));
			writer2.append(",");
			writer2.append(Boolean.toString(maxReached));
			writer2.append(",");
			writer2.append(Double.toString(total_dist));
			writer2.append(",");
			writer2.append(Double.toString(connectance));
			writer2.append(",");
			writer2.append(Double.toString(contagion));
			writer2.append(",");
			writer2.append(Double.toString(iic));
			writer2.append(",");
			writer2.append(Double.toString(ModelSetup.primatesAll.size()));
			writer2.append(",");
			writer2.append(Double.toString(pearsonCorr_expo_depo));
			writer2.append(",");
			writer2.append(Double.toString(median_expo));
			writer2.append(",");
			writer2.append(Double.toString(mean_expo));
			writer2.append(",");
			writer2.append(Double.toString(0.0));//(mo[0]));
			writer2.append(",");
			writer2.append(Double.toString(0.0));//(mo[1]));
			writer2.append(",");
			writer2.append(Double.toString(cor_between_contamination));
			writer2.append(",");
			writer2.append(Double.toString(cor_between_contamination_p));
			writer2.append(",");
			writer2.append(Double.toString(cor_between_visit));
			writer2.append(",");
			writer2.append(Double.toString(cor_between_visit_p));
			writer2.append(",");
			writer2.append(Double.toString(cor_visit_contamination));
			writer2.append(",");
			writer2.append(Double.toString(cor_visit_contamination_p));
			writer2.append(",");
			writer2.append(Double.toString(cor_between_contamination_infected));
			writer2.append(",");
			writer2.append(Double.toString(cor_between_contamination_p_infected));
			writer2.append(",");
			writer2.append(Double.toString(cor_between_visit_infected));
			writer2.append(",");
			writer2.append(Double.toString(cor_between_visit_p_infected));
			writer2.append(",");
			writer2.append(Double.toString(cor_visit_contamination_infected));
			writer2.append(",");
			writer2.append(Double.toString(cor_visit_contamination_p_infected));
			writer2.append(",");
			writer2.append(Double.toString(avg_betweenness));
			writer2.append(",");
			writer2.append(Double.toString(avg_number_of_patches));
			writer2.append(",");
			writer2.append(Double.toString(avg_patch_size));
			writer2.append(",");
			writer2.append(Double.toString(Parameter.Denv_reduction));
			writer2.append(",");
			writer2.append(Double.toString(finalMeanEnergy));
			writer2.append(",");
			writer2.append(Double.toString((double)immigrations/(double)Parameter.rcGroups));
			writer2.append(",");
			writer2.append(Double.toString(dist_prev));
			writer2.newLine();

			writer2.flush();
			writer2.close();

			//landscape metric results
			BufferedWriter writer3 = new BufferedWriter(new FileWriter("C:/Users/t-work/Dropbox/code/SpatialMemory/LHP_dispersal/LHP/data/Results_landscape_Patches.csv",true));

			//patch area
			ArrayList<Double[]> patches= new ArrayList<Double[]>();
			for (Double[] patch : patches){
				//patch area
				writer3.append(Double.toString(patch[0]));
				writer3.append(",");
				//patch quality
				writer3.append(Double.toString(patch[0]));
				writer3.append(",");	
				//patch contamination
				writer3.append(Double.toString(patch[1]));
				writer3.append(",");
				//patch visits
				writer3.append(Double.toString(patch[2]));
				writer3.newLine();	
			}

			writer3.flush();
			writer3.close();


		} catch (RowsExceededException e) {		e.printStackTrace();
		} catch (WriteException e) {			e.printStackTrace();
		} catch (IOException e){				e.printStackTrace();
		}

	}


	private void exportPrimatePoints(double timeStamp){
		//System.out.println("Exporting primate locations and times to be analyzed");

		try {			
			for (Primate p:ModelSetup.primatesAll){
				writer3.append(p.getId()+","+p.getCoord().x+","+p.getCoord().y+","+timeStamp);
				writer3.newLine();
			}
			writer3.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void exportLandscape(){
		System.out.println("Exporting landscape to be analyzed");
		File point = new File("C:/Users/t-work/Dropbox/code/SpatialMemory/LHP_dispersal/LHP/data/Landscape_stats_Frag_" + Parameter.death_env + "_"+Parameter.death_host+"_"+Parameter.mature_egg_infectiousEgg+"_"+Parameter.mature_infectiousEgg_larvae+"_"+Parameter.probIngestEgg+"_"+Parameter.patch_size+"_"+Parameter.totalAlteration+"_"+Parameter.randomSeed+"_"+System.currentTimeMillis()+"_r_"+Parameter.Denv_reduction+".shp");

		ShapefileWriter shapeOut = new ShapefileWriter(ModelSetup.getGeog());
		try {
			shapeOut.write(ModelSetup.getGeog().getLayer(Cell.class).getName(), point.toURI().toURL());
		} catch (MalformedURLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException n){
			n.printStackTrace();
		}

	}

	public synchronized static void addScrambleCount(){
		scrambleCount++;
	}
	public synchronized static void addImmigrationCount(){
		immigrations++;
	}
	public synchronized int getTickCount(){
		return (int) this.runEnvironment.getCurrentSchedule().getTickCount(); 
	}
	public static void setMaxReached(boolean br){
		maxReached = br;
	}
	public static void setContagion(double d){
		contagion = d;
	}
	public static void setConnectance(double d){
		connectance = d;
	}
	public static void setIIC(double d){
		iic = d;
	}
	public static void setLCP(double d){
		lcp = d;
	}
	public static void setAvgBetweenness(double d){
		avg_betweenness = d;
	}
	public static void setAvgPatchSize(double d){
		avg_patch_size = d;
	}
	public static void setAvgNumberOfPatches(double d){
		avg_number_of_patches=d;
	}
	public static void setcor_bc(double d){
		cor_between_contamination=d;
	}
	public static void setcor_bc_p(double d){
		cor_between_contamination_p=d;
	}
	public static void setcor_bv(double d){
		cor_between_visit=d;
	}
	public static void setcor_bv_p(double d){
		cor_between_visit_p=d;
	}
	public static void setcor_cv(double d){
		cor_visit_contamination=d;
	}
	public static void setcor_cv_p(double d){
		cor_visit_contamination_p=d;
	}
	public static void setcor_bc_infected(double d){
		cor_between_contamination_infected=d;
	}
	public static void setcor_bc_p_infected(double d){
		cor_between_contamination_p_infected=d;
	}
	public static void setcor_bv_infected(double d){
		cor_between_visit_infected=d;
	}
	public static void setcor_bv_p_infected(double d){
		cor_between_visit_p_infected=d;
	}
	public static void setcor_cv_infected(double d){
		cor_visit_contamination_infected=d;
	}
	public static void setcor_cv_p_infected(double d){
		cor_visit_contamination_p_infected=d;
	}
	public static void setMedianBTW(double d){
		median_betweenness=d;
	}

}
