import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class CustomBatch_land {

	
	public static void main(String[] args){
		
		//convert to doubles
		Double[] r= new Double[args.length];
		for (int i=0; i< args.length;i++){
			r[i]=Double.parseDouble(args[i]);
		}
		
		//make file
		generateFile(r);
		
	}
	
	
	private static void generateFile(Double[] params){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/t-work/Dropbox/Project_Managment_LHP_simulaiton/batchParams.xml",false));
			writer.append("<?xml version=\"1.0\"?>");
			writer.append("<sweep runs=\"1\">");
			
			writer.append("<parameter name=\"randomSeed\" type=\"constant\" constant_type=\"int\" value=\"");
			writer.append(Long.toString(System.currentTimeMillis() % 100000));
			writer.append("\"/>");
			
			writer.append("<parameter name=\"dbh_cost\" type=\"constant\" constant_type=\"java.lang.Double\" value=\"");
			writer.append("0.0");
			writer.append("\"/>");
			
			writer.append("<parameter name=\"Death_host\" type=\"constant\" constant_type=\"int\" value=\"");
			writer.append(Long.toString(Math.round(params[0])));
			writer.append("\"/>");
			
			writer.append("<parameter name=\"Death_env\" type=\"constant\" constant_type=\"int\" value=\"");
			writer.append(Long.toString(Math.round(params[1])));
			writer.append("\"/>");
			
			writer.append("<parameter name=\"mature_e_i\" type=\"constant\" constant_type=\"int\" value=\"");
			writer.append(Long.toString(Math.round(params[2])));
			writer.append("\"/>");
			
			writer.append("<parameter name=\"mature_i_l\" type=\"constant\" constant_type=\"int\" value=\"");
			writer.append(Long.toString(Math.round(params[3])));
			writer.append("\"/>");
		
			writer.append("<parameter name=\"prob_ingest\" type=\"constant\" constant_type=\"java.lang.Double\" value=\"");
			writer.append(params[4].toString());
			writer.append("\"/>");
			
			writer.append("<parameter name=\"alteration\" type=\"constant\" constant_type=\"java.lang.Double\" value=\"");
			writer.append(params[5].toString());
			writer.append("\"/>");
			
			writer.append("<parameter name=\"patch_size\" type=\"constant\" constant_type=\"java.lang.Double\" value=\"");
			writer.append(params[6].toString());
			writer.append("\"/>");
			
			writer.append("</sweep>");
			
			writer.flush();
		    writer.close();
		
		} catch (IOException e){				e.printStackTrace();
		}
	}
}
