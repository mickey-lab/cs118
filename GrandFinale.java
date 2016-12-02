import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.*;
public class GrandFinale {
	private int pollRun = 0;
	private static int maxJunctions = 10000;
	private static int junctionCounter;
	private int explorerMode;
	private int explore = 1;
	class RobotData {
		public ArrayList<Integer> junctions = new ArrayList<Integer>();
		public void resetJunctionCounter(){
			junctionCounter = 0;
		}
		public void add(int arrived){
			this.junctions.add(arrived);
			this.printJunction();
		}
		public void printJunction(){
			int dir = junctions.get(junctions.size() - 1);
			int index = junctions.indexOf(dir);
			String str = print(dir);
			System.out.println("Junction " + index + " heading " + str);
		}

	}
	public String print(int dir){
		String str="";	
		switch(dir){
			case IRobot.NORTH:
				str = "NORTH";
				break;
			case IRobot.SOUTH:
				str = "SOUTH";
				break;
			case IRobot.WEST:
				str = "WEST";
				break;
			case IRobot.EAST:
				str= "EAST";
				break;
		}
		return str;
	}
	private RobotData robotData;
	public void controlRobot(IRobot robot) {
		if((robot.getRuns() == 0) && (pollRun == 0)){
			robotData = new RobotData();
			explorerMode = 1;
			explore = 1;
		} else if(robot.getRuns() != 0){
			explore=0;
		}
		pollRun++;
		robot.setHeading(exploreControl(robot));
	}
	public int exploreControl(IRobot robot){	
		ArrayList<Integer> exits = nonWallExits(robot);
		int exit = exits.size();
		int direction = 0;
		switch (exit){
			case 1:
				direction = deadEnd(robot, exits);
				break;
			case 2:
				direction = corridor(robot, exits);
				break;
			case 3:
				direction = junction(robot, exits);
				break;
			case 4:
				direction = crossRoad(robot, exits);
				break;
		}
		return direction;
	}
	public void reset(){
		robotData.resetJunctionCounter();
		explorerMode=1;
	}
	//method which converts an absolute direction into a relative one
	private int lookHeading(int direction, IRobot robot) {
		int heading = robot.getHeading();
		int relative = ((direction - heading) % 4 + 4) % 4;
		int absolute = IRobot.AHEAD + relative;
		return robot.look(absolute);

	}


	private int deadEnd (IRobot robot, ArrayList<Integer> exits){
		explorerMode = 0;
		return exits.get(0);
	}
	private int crossRoad (IRobot robot, ArrayList<Integer> exits){
		System.out.println(explore);
		if(explore==1){
			int heading = robot.getHeading();
			ArrayList<Integer> passage = passageExits(robot);
			int passageSize = passage.size();
			if (passageSize == 3){
				neverBefore(robot, heading);
			}
			if(passageSize!=0){
				explorerMode=1;
				return passage.get(randomizer(passageSize));
			} else{
					if(explorerMode==1){
						explorerMode=0;
						return IRobot.NORTH+(((robot.getHeading()+2)%4+4)%4);
					} else{
						int dir = IRobot.NORTH+(((robotData.junctions.get(robotData.junctions.size()-1)+2)%4+4)%4);
						robotData.junctions.remove(robotData.junctions.size()-1);
						return dir;
					}			

			}
		} else {
			junctionCounter++;
			System.out.println(junctionCounter);
			if(junctionCounter<robotData.junctions.size()){
				System.out.println("not last");
				return robotData.junctions.get(junctionCounter);
			} else{
				System.out.println("last");
				return lastDir(robot);
			}
		}
	}
	private void neverBefore(IRobot robot, int heading){
		robotData.add(heading);
		junctionCounter++;
	}
	private int junction (IRobot robot, ArrayList<Integer> exits){
		if(explore==1){
			int heading = robot.getHeading();
			ArrayList<Integer> passage = passageExits(robot);
			int passageSize = passage.size();
			switch(passageSize){
				case 2:
					neverBefore(robot, heading);
				case 1:
					explorerMode=1;
					return passage.get(randomizer(passageSize));	
				default: 
					if(explorerMode==1){
						explorerMode=0;
						return IRobot.NORTH+(((robot.getHeading()+2)%4+4)%4);
					} else{
						int dir = IRobot.NORTH+(((robotData.junctions.get(robotData.junctions.size()-1)+2)%4+4)%4);
						robotData.junctions.remove(robotData.junctions.size()-1);
						return dir;
					}			

			}

		} else {
			junctionCounter++;
			if(junctionCounter<robotData.junctions.size()){
				System.out.println("not last");
				return robotData.junctions.get(junctionCounter);
			} else{
				System.out.println("last");
				return lastDir(robot);
			}
		}	

	}
	private int randomizer (int n){
		return (int) (Math.random()*n);

	}
	private int lastDir(IRobot robot){
		if(robot.getLocation().x<robot.getTargetLocation().x){
			return IRobot.EAST;
		} else if(robot.getLocation().x>robot.getTargetLocation().x){
			return IRobot.WEST;
		} else if(robot.getLocation().y<robot.getTargetLocation().y){
			return IRobot.SOUTH;
		} else{
			return IRobot.NORTH;
		}
	}
	private int corridor(IRobot robot, ArrayList<Integer> exits){
		int heading = robot.getHeading();
		ArrayList<Integer> passage = passageExits(robot);
		int coming = IRobot.NORTH + (((robot.getHeading()-IRobot.NORTH)+2)%4+4)%4;
		int index = exits.indexOf(coming);
		int going = exits.indexOf(robot.getHeading());
		System.out.println(print(coming));
		if(going!=-1){
			if(index!=-1){
				exits.remove(index);	
			}
			return exits.get(0);
		} else{
			if(explore==1){
				if(passage.size()>=1){
					neverBefore(robot, heading);
					System.out.println("corridor");
					explorerMode=1;
					return passage.get(randomizer(passage.size()));
				} else{ 
					if(explorerMode==1){
						explorerMode=0;
						return IRobot.NORTH+(((robot.getHeading()+2)%4+4)%4);
					} else{
						int dir = IRobot.NORTH+(((robotData.junctions.get(robotData.junctions.size()-1)+2)%4+4)%4);
						robotData.junctions.remove(robotData.junctions.size()-1);
						return dir;
					}			
				}
			} else {
				junctionCounter++;
				if(junctionCounter<robotData.junctions.size()){
					System.out.println("not last");
					return robotData.junctions.get(junctionCounter);
				} else{
					System.out.println("last");
					return lastDir(robot);
				}
			}	
		}

	}
	//checks if there is a wall in the direction to the robot which was parsed in
	private int noWallAhead(int direction, IRobot robot) {
		if (lookHeading(direction, robot) != IRobot.WALL) {
			return direction;
		} else {
			return 0;

		}
	}
	private ArrayList<Integer> passageExits(IRobot robot){
		ArrayList<Integer> passage = new ArrayList<Integer>();
		for(int i= 0; i<4; i++){
			int direction= IRobot.NORTH + i;
			if(lookHeading(direction, robot)==IRobot.PASSAGE){
				passage.add(direction);
			}

		}
		return passage;
	}
	private ArrayList<Integer> nonWallExits(IRobot robot){	
		//creates an ArrayList for all directions where there is no wall.
		ArrayList<Integer> exits = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++) {
			int direction = IRobot.NORTH + i;
			int noWall = noWallAhead(direction, robot);
			if (noWall != 0) {
				exits.add(noWall);
			}
		}
		return exits;
	}

}
