package LHP;

public class Runnable_agentInputs implements Runnable{
	
	Primate primate;
	
	Runnable_agentInputs(Primate p){
		primate = p;
	}
	
	@Override
	public void run(){
		 Throwable thrown = null;
		    try {
				primate.getInputs();		       
		    } catch (Throwable e) {
		        thrown = e;
		        System.out.println("Problem lies in input code: " + thrown);
		    } finally {
		        //threadExited(this, thrown);
		    	return;
		    }
		
	}

}
