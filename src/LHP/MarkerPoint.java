package LHP;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

//Marker point used to record the position of groups on the landscape
public class MarkerPoint {

	long id = 0;
	Geography geog;
	Coordinate coord;
	int route;
	double envResAtPoint;
	double stepLength;
	double turnAngle;
	int month,groupID;
	Context context;
	
	public MarkerPoint(Context c,Geography g,Coordinate tag, long tagID, double sl, double a,int m,int group) {
		geog=g;
		id = tagID;
		coord = tag;
		route = 0;
		envResAtPoint = 0;
		stepLength = sl;
		turnAngle = a;
		month=m;
		groupID=group;
		context=c;
	}
	
	public void createPoint(){
		GeometryFactory fac2 = new GeometryFactory();
		Point geom = fac2.createPoint(getCoord());
		geog.move(this, geom);
		context.add(this);
	}
	
	
	/*****************************************get and set methods ***********************************/
	
	public void setPointID(int i){
		id = i;
	}
	public long getPointID(){
		return id;
	}
	public void setMonth(int i){
		month = i;
	}
	public long getMonth(){
		return id;
	}
	public int getDay(){
		return (int)id/26;
	}
	public Coordinate getCoord(){
		return coord;
	}
	public void setRoute(int c){
		route = c;
	}
	public int getRoute(){
		return route;
	}
	public void setGroupID(int c){
		groupID = c;
	}
	public int getGroupID(){
		return groupID;
	}
	public double getEnvResAtPoint(){
		return envResAtPoint;
	}
	public void setEnvResAtPoint(double c){
		envResAtPoint = c;
	}
	public double getStepLength(){
		return stepLength;
	}
	public void setStepLength(double er){
		stepLength = er;
	}
	public double getAngel(){
		return turnAngle;
	}
	public void setAngle(double s){
		turnAngle = s;
	}
}
