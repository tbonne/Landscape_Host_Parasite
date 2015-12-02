package LHP;

public class Runnable_envUpdate implements Runnable{
	
	Cell cell;
	
	Runnable_envUpdate(Cell c){
		cell = c;
	}
	
	@Override
	public void run(){
		Throwable thrown = null;
	    try {
		cell.stepThreaded();
	    } catch (Throwable e) {
	        thrown = e;
	        System.out.println("Problem lies in cell update: "+ thrown);
	        //String fullStackTrace = org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(e);
	        //System.out.println(fullStackTrace);
	    } finally {
	        //threadExited(this, thrown);
	    	return;
	    }
	}
}
