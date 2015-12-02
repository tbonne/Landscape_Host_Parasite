package tools;

import java.util.ArrayList;
import java.util.List;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import LHP.Cell;
import LHP.Parameter;

public class MoranCal {
	
	public static double estimateMoransI_DBH(ArrayList<Cell> cells){
		double m_i=0;
		
		double N = cells.size();
		
		double totalWeights = 0;
		
		double meanValue = 0;
		for(Cell c:cells){
			meanValue=meanValue+c.getDBH();
		}
		meanValue=meanValue/(double)cells.size();
		
		//numerator / denominator
		double numerator=0, denominator=0;
		for(Cell c:cells){
			for(Cell vis : cells){
				double dist = vis.getCoord().distance(c.getCoord()); 
				if(dist>0 ){//dist <= Parameter.cellSize*Math.pow(2, 0.5)+1 && 
					double diff1 = c.getDBH()-meanValue;
					double diff2 = vis.getDBH()-meanValue;
					double weight = 1/Math.pow(dist,1);
					totalWeights=totalWeights+weight;
					numerator = numerator + diff1*diff2*weight;
					denominator=denominator+Math.pow(c.getDBH()-meanValue, 2);
				}
			}
		}
		
		//combined
		m_i = (N / totalWeights) * 	(numerator/denominator);
		
		return m_i;
	}

	public static double estimateMoransI_contaminated(ArrayList<Cell> cells){
		double m_i=0;
		
		double N = cells.size();
		
		double totalWeights = 0;
		
		double meanValue = 0;
		for(Cell c:cells){
			meanValue=meanValue+c.getInfectiousEggCount();
		}
		
		//numerator / denominator
		double numerator=0, denominator=0;
		for(Cell c:cells){
			for(Cell vis : c.getVisibleNeigh()){
				if(vis.getCoord().distance(c.getCoord())<=Parameter.cellSize*Math.pow(2, 0.5)+1){
					double diff1 = c.getInfectiousEggCount()-meanValue;
					double diff2 = vis.getInfectiousEggCount()-meanValue;
					double weight = 1;
					totalWeights=totalWeights+1;
					numerator = numerator + diff1*diff2*weight;
					denominator=denominator+Math.pow(c.getInfectiousEggCount()-meanValue, 2);
				}
			}
		}
		
		//combined
		m_i = (N / totalWeights) * 	(numerator/denominator);
		
		return m_i;
	}
	
	
	public static double[] estimateMoransI_R(ArrayList<Cell> cells){
		double m_i=0,m_i_c=0;
		
		double[] xCoord = new double[cells.size()];
		double[] yCoord = new double[cells.size()];
		double[] dbh = new double[cells.size()];
		double[] cont = new double[cells.size()];
		
		for(int i=0;i<cells.size();i++){
			xCoord[i]=cells.get(i).getCoord().x;
			yCoord[i]=cells.get(i).getCoord().y;
			dbh[i]=cells.get(i).getDBH();
			cont[i]=cells.get(i).getInfectiousFecalCount();
		}
		
		try {
			//System.out.println("Attempting to use R");
			REXP x,x2;
			RList l;
			RConnection c = new RConnection();
			//System.out.println("Connection set");
			c.eval("library(ape)");
			//System.out.println("Library loaded");

			try{

				//double[][] D = {xCoord,yCoord};
				
			c.assign("dbh", dbh);
			c.assign("cont", cont);
			c.assign("x",xCoord);
			c.assign("y", yCoord);
			
			//String s1=c.eval("paste(capture.output(summary(cont)),collapse='\\n')").asString();
			//System.out.println(s1);
			
			//calc distance matrix 
			c.eval("cell.dists<-as.matrix(cbind(x,y))");
			c.eval("cell.dists <- as.matrix(dist(cell.dists))");
			c.eval("cell.dists.inv <- 1/cell.dists");
			c.eval("diag(cell.dists.inv) <- 0");
			c.eval("cell.dists.inv[is.infinite(cell.dists.inv)] <- 0");
//			c.eval("write.table(cell.dists.inv, file = 'foo.csv', sep = ',', col.names = NA,qmethod = 'double')");

			//calc morans i
			c.eval("mi <- Moran.I(dbh,cell.dists.inv,scaled = TRUE, na.rm = TRUE,alternative = 'two.sided')");
			c.eval("mi2 <- Moran.I(cont,cell.dists.inv,scaled = TRUE, na.rm = TRUE,alternative = 'two.sided')");
			
			//REXP r = c.parseAndEval("try(mi2 <- Moran.I(cont,cell.dists.inv,scaled = TRUE, na.rm = TRUE,alternative = 'two.sided'),silent=TRUE)");
			//if (r.inherits("try-error")) System.err.println("Error: "+r.asString());
			//else { // success ... 
			//	
			//}
			
			//String s1=c.eval("paste(capture.output(print(mi)),collapse='\\n')").asString();
			//System.out.println(s1);
			
			x = c.eval("mi$observed[1]");
			m_i = x.asDouble();
			x2 = c.eval("mi2$observed[1]");
			m_i_c = x2.asDouble();
			System.out.println("mi = "+m_i);
			

		}catch (RserveException r){
			r.printStackTrace();
			System.out.println("Failed to estimate moran's I");
		}
			
			c.close();

		}catch (Exception e){
			System.out.println("EX:"+e);
			e.printStackTrace();
		}
		double[] ans ={m_i,m_i_c}; 
		return ans;
	}
}
