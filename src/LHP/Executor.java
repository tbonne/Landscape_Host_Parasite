package LHP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/*
 * 
 * This class controls the multi-threading processes, breaking the model up into 5 separate multi-threaded sequences:
 * 			1) getInputs
 * 			2) Behavioural response
 * 			3) Energy update
 * 			4) Memory update
 * 			5) Environment regrowth
 */

public class Executor {

	private static final int pThreads = Parameter.numbOfThreads;
	private static ExecutorService executor;
	
	public Executor(){
		executor = Executors.newFixedThreadPool(pThreads);
	}
	
	public static void getInputs(){
		Collection<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		Iterable<Primate> primates = ModelSetup.getAllPrimateAgents();
		for (Primate p:primates){
			Runnable worker = new Runnable_agentInputs(p);
			tasks.add(Executors.callable(worker,(Void)null));
		}
		
		try {
			for (Future<?> f : executor.invokeAll(tasks)) { //invokeAll() blocks until ALL tasks submitted to executor complete
			   f.get(); 
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}catch (NullPointerException e){
			e.printStackTrace();
		}
	    //System.out.println("Finished all input threads");
	}
	
	public static void behaviour(){
		Collection<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		Iterable<Primate> primates = ModelSetup.getAllPrimateAgents();
		for (Primate p : primates){
			Runnable worker = new Runnable_agentBehaviour(p);
			tasks.add(Executors.callable(worker,(Void)null));
		}
		
		// Wait until all threads are finish
		try {
			for (Future<?> f : executor.invokeAll(tasks)) { //invokeAll() blocks until ALL tasks submitted to executor complete
			    f.get(); 
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
			System.out.println("null in behaviour");
		}
		
	    //System.out.println("Finished all behaviour threads");
	}


	public static void energyUpdate(){
		Collection<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		Iterable<Primate> primates = ModelSetup.getAllPrimateAgents();
		for (Primate p : primates){
			Runnable worker = new Runnable_agentEnergyUpdate(p);
			tasks.add(Executors.callable(worker,(Void)null));
		}
		
		// Wait until all threads are finish
		try {
			for (Future<?> f : executor.invokeAll(tasks)) { //invokeAll() blocks until ALL tasks submitted to executor complete
			    f.get(); 
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
			System.out.println("null in energy updates");
		}
		
	    //System.out.println("Finished all energy threads");
	}

	public static void memoryUpdate(){
		Collection<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		Iterable<Primate> primates = ModelSetup.getAllPrimateAgents();
		for (Primate p : primates){
			Runnable worker = new Runnable_agentMemoryUpdate(p);
			tasks.add(Executors.callable(worker,(Void)null));
		}
		
		// Wait until all threads are finish
		try {
			for (Future<?> f : executor.invokeAll(tasks)) { //invokeAll() blocks until ALL tasks submitted to executor complete
			    f.get(); 
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
			System.out.println("null in memory updates");
		}
		
	    //System.out.println("Finished all memory threads");
	}
	
	public static void envUpdate(){
		Collection<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		
		Iterable<Cell> cells = ModelSetup.getCellsToUpdate();
		for (Cell c: cells){
			Runnable worker = new Runnable_envUpdate(c);
			tasks.add(Executors.callable(worker,(Void)null));
		}
		
		// Wait until all threads are finish
		try {
			for (Future<?> f : executor.invokeAll(tasks)) { //invokeAll() blocks until ALL tasks submitted to executor complete
			    f.get(); 
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}catch (NullPointerException e){
			e.printStackTrace();
		}
	    //System.out.println("Finished all cell threads");
	}
	
	public void shutdown(){
		executor.shutdown();
	}
}
