package LHP;

public class Runnable_agentMemoryUpdate implements Runnable{
	
Primate primate;
	
	Runnable_agentMemoryUpdate(Primate p){
		primate = p;
	}

	@Override
	public void run(){
		Throwable thrown = null;
	    try {
		primate.updateMemory();
	    } catch (Throwable e) {
	        thrown = e;
	        System.out.println("Problem lies in memory update code" + thrown);
	    } finally {
	    	return;
	    }
	}

}
