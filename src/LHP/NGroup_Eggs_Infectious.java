package LHP;

//representation of a infectious egg group (infectious within the environment)
public class NGroup_Eggs_Infectious {

	int eggs_infectious=0;
	Cell myCell;
	int age;
	boolean dead=false;
	int death_env=0;
	
	public NGroup_Eggs_Infectious(int e, Cell c,int a,int d){
		eggs_infectious=e;
		myCell = c;
		age=a;
		death_env = d;
	}
	
	public boolean step(){
		
		//age this group
		age++;
		
		//remove this group when past age limit 
		if(death_env < age){
			dead=true;
		}
		
		return dead;
	}
	
	public int getInfectiousEggs(){
		return eggs_infectious;
	}
	public void removeInfectiousEggs(int r){
		eggs_infectious = eggs_infectious - r; 
	}
}
