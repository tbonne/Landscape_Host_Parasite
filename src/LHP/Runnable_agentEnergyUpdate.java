package LHP;

public class Runnable_agentEnergyUpdate implements Runnable{
	
	Primate primate;
	
	Runnable_agentEnergyUpdate(Primate p){
		primate = p;
	}
	
	@Override
	public void run(){
		Throwable thrown = null;
	    try {
		primate.energyUpdate();
	    } catch (Throwable e) {
	      //  String fullStackTrace = org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(e);
	        System.out.println("Problem lies in energy update code!");
	        //System.out.println(fullStackTrace);
	        System.out.println("Priamte" + primate.getEggsInGut());
	    } finally {
	    	return;
	    }
	}

}
