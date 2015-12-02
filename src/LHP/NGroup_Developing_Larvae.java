package LHP;

//representation of a developing larvae group (latent within host gut)
public class NGroup_Developing_Larvae {

	int age=0,death=0;
	int maturation;
	double eggs;
	Gut myGut;
	boolean dead;

	public NGroup_Developing_Larvae(int e, Gut g){
		maturation = Parameter.mature_infectiousEgg_larvae;
		death = Parameter.death_host;
		eggs=e;
		myGut = g;
	}

	public boolean step(){
		//age
		age++;

		if(age>maturation){
			//create new larvae group
			myGut.getLarvae().add(new NGroup_Larvae((int)eggs,myGut,age));

			//remove the old immature group
			dead = true;

		} else if (age>=death){
			dead = true;
		}

		return dead;
	}

	public void setAge(int a){
		age=a;
	}
	public int getAge(){
		return age;
	}
}
