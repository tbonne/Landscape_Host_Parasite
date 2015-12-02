package LHP;

//representation of a larvae group (reproducing within host gut)
public class NGroup_Larvae {

	int larvae=0;
	Gut myGut;
	int age=0;
	boolean dead=false;
	
	
	public NGroup_Larvae(int l, Gut g,int a){
		larvae=l;
		myGut = g;
		age = a;
	}
	
	public boolean step(){
		
		age++;
		
		//remove this group if past life expectancy
		if(age>Parameter.death_host)dead=true;
		return dead;
	}
}
