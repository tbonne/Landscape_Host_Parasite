package LHP;

import java.util.concurrent.ThreadLocalRandom;

import cern.jet.random.Distributions;
import cern.jet.random.NegativeBinomial;
import cern.jet.random.engine.RandomEngine;
import repast.simphony.random.RandomHelper;

//representation of a developing egg group (latent within the environment)
public class NGroup_Eggs_Non_Infectious {

	int age,maturation,death;
	int eggs;
	boolean inEnv, dead=false;
	Cell myCell=null;

	public NGroup_Eggs_Non_Infectious(){
		eggs=0;
		age=0;
		death= NegativeBinomial.staticNextInt(1, 1-(1/((double)Parameter.death_env+1)));//Parameter.deathNgBino.nextInt(1,0.998718); Parameter.deathNgBino.nextInt(1,1-(1.0/(double)(Parameter.death_env)));//
		maturation=Parameter.mature_egg_infectiousEgg;
		inEnv=false;
	}

	public boolean step(){
		//age this group
		age++;

		if (age>maturation){
			//mature this group into an infectious group
			try{
				myCell.getInfectiousGroups().add(new NGroup_Eggs_Infectious(eggs,myCell,age,death));
			} catch (NullPointerException e){
				System.out.println("adding group");
			}
			//remove this immature group
			dead = true;
		} else if (age>=death) {
			dead = true;
		}
		return dead;
	}

	
	/**************get and set****************/
	
	public void setEnv(Cell c){ 
		myCell=c;
		inEnv = true;
	}
	public void addEggs(int e){
		eggs=eggs+e;
	}
	public void setDeath(int i){
		death=Math.max(0, i);
	}
	public int getDeath(){
		return death;
	}
}
