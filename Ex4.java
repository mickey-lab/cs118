import uk.ac.warwick.dcs.maze.logic.IRobot;

import java.util.*;

public class Ex4 {
    private int pollRun = 0;
    private static int maxJunctions = 10000;
    private static int junctionCounter;
    private int explorerMode;

    class RobotData {
        public ArrayList<junctionRecorder> junctions = new ArrayList<junctionRecorder>();

        public void resetJunctionCounter() {
            junctionCounter = 0;
            junctions = new ArrayList<junctionRecorder>();
        }

        public void add(int x, int y, int arrived){
            junctionRecorder arrive = new junctionRecorder(x,y,arrived);
            this.junctions.add(arrive);
            this.printJunction();
        }

        public void printJunction(){
            junctionRecorder last = junctions.get(junctions.size() - 1);
            int index = junctions.indexOf(last);
            String str = "";
            int dir = last.getDir();
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
            System.out.println("Junction " + index + " (x="+last.getX()+" ,y="+last.getY()
                    + ") heading " + str);
        }
    }
    class junctionRecorder{
        private int x;
        private int y;
        private int arrived;
        public junctionRecorder(int x, int y, int arrived){
            this.x = x;
            this.y = y;
            this.arrived = arrived;
        }
        public int getX(){
            return this.x;
        }
        public int getY(){
            return this.y;
        }
        public int getDir(){
            return this.arrived;
        }
    }

    private String direction(int dir) {
        switch (dir) {
            case IRobot.NORTH:
                return "NORTH";
            case IRobot.SOUTH:
                return "SOUTH";
            case IRobot.WEST:
                return "WEST";
            case IRobot.EAST:
                return "EAST";
        }
        return "";
    }

    private RobotData robotData;

    public void controlRobot(IRobot robot) {
        if ((robot.getRuns() == 0) && (pollRun == 0)) {
            robotData = new RobotData();
            explorerMode = 1;
        }
        int direction = exploreControl(robot);
        pollRun++;
        robot.setHeading(direction);
    }

    public int exploreControl(IRobot robot) {
        ArrayList<Integer> exits = nonWallExits(robot);
        int exit = exits.size();
        int direction = 0;
        switch (exit) {
            case 1:
                direction = deadEnd(robot, exits);
                break;
            case 2:
                direction = corridor(robot, exits);
                break;
            case 3:
            case 4:
                direction = crossRoad(robot, exits);
                break;
        }
        return direction;
    }

    public void reset() {
        pollRun = 0;
        robotData.resetJunctionCounter();
        explorerMode = 1;
    }

    //method which converts an absolute direction into a relative one
    private int lookHeading(int direction, IRobot robot) {
        int heading = robot.getHeading();
        int relative = ((direction - heading) % 4 + 4) % 4;
        int absolute = IRobot.AHEAD + relative;
        return robot.look(absolute);
    }

    private void println(String str){
        System.out.println(str);
    }
    private int deadEnd(IRobot robot, ArrayList<Integer> exits) {
        explorerMode = 0;
        return exits.get(0);
    }
    private void printJunction(){
        printHash();
        for(int i = 0; i< robotData.junctions.size(); i++){
            System.out.println("i: " + i + " direction: "+ direction(robotData.junctions.get(i)));
        }
        printHash();
    }
    private int crossRoad(IRobot robot, ArrayList<Integer> exits) {
        //printJunction();
        int heading = robot.getHeading();
        ArrayList<Integer> passage = passageExits(robot);
        int passageSize = passage.size();
        //System.out.println("Explore Mode: " + explorerMode + " | case: " + passageSize + " | heading: " + direction(heading));
        String err2;
        //passageSize == 3 && exits.size() == 4 || passageSize==2 && exits.size()==3
        if (explorerMode==1) {
            System.out.println("Explorer Mode: 1 | passage Size: " + passageSize + " exit size: " + exits.size());
            neverBefore(robot, heading);
        }
        if (passageSize != 0) {
            explorerMode = 1;
            return passage.get(randomizer(passageSize));
        } else {
            if (explorerMode == 1) {
                println("Junction | Explorer Mode: 1 | passage Size: " + passageSize + " exit size: " + exits.size());
                explorerMode = 0;
                //neverBefore(robot, heading);
                return IRobot.NORTH + ((((heading - IRobot.NORTH) + 2) % 4 + 4) % 4);
            }
            int junctionSize = robotData.junctions.size() - 1;
            //System.out.println("old table: ");
            //printJunction();
            int dir2 = robotData.junctions.get(junctionSize);
            int dir = IRobot.NORTH + (((dir2-IRobot.NORTH) + 2) % 4 + 4) % 4;
            robotData.junctions.remove(junctionSize);
            err2 = "pulled of " + direction(dir2) + " | new direction: " + direction(dir)+" | new size: "+ robotData.junctions.size();
            //neverBefore(robot,heading);
            println("---------------------------------");
            System.out.println("Explore Mode: " + explorerMode + " | case: " + passageSize + " | heading: " + direction(heading));
            wall(dir, robot, junctionSize);
            //System.out.println(err1);
            System.out.println(err2);
            println("---------------------------------");
            //System.out.println("new table: ");
            //printJunction();
            return dir;
        }
    }
    private void wall(int dir, IRobot robot, int junctionSize){
        if (lookHeading(dir, robot) == IRobot.WALL) {
            System.out.println("WALL AHEAD!");
            System.out.println("size junctions -1: "+junctionSize);
            printJunction();
        } else{
            System.out.println("NO WALL");
            System.out.println("size junctions -1: "+junctionSize);
        }

    }
    private void neverBefore(IRobot robot, int heading) {
        robotData.add(heading);
        junctionCounter++;
    }
    private int randomizer(int n) {
        return (int) (Math.random() * n);
    }
    private void printHash(){
        println("##################################");
    }
    private int corridor(IRobot robot, ArrayList<Integer> exits) {
        int heading = robot.getHeading();
        ArrayList<Integer> passage = passageExits(robot);
        int coming = IRobot.NORTH + (((robot.getHeading() - IRobot.NORTH) + 2) % 4 + 4) % 4;
        int indexGo = exits.indexOf(coming);
        int indexTo = exits.indexOf(heading);
        int passageSize = passage.size();
        //System.out.println( "Explore Mode: " + explorerMode + " | case: " + passageSize + " | heading: " + direction(heading));
        String err2;
        if(indexTo!= -1) {

            if(passage.size()>=1 || explorerMode == 0) {
                if (indexGo != -1) {
                    exits.remove(indexGo);
                    return exits.get(0);
                } else {
                    return exits.get(randomizer(2));
                }
            } else{
                explorerMode = 0;
                return coming;
            }
        }
        if(explorerMode==1){
            System.out.println("Corridor | Explorer Mode: 1 | passage Size: " + passageSize + " exit size: " + exits.size());
            neverBefore(robot, heading);
        }

        if(passage.size()>=1){
            explorerMode=1;
            exits.remove(indexGo);
            return exits.get(0);
        } else{
            if(explorerMode==1){
                explorerMode=0;
                return coming;
            } else{
                System.out.println("---------------------------------");
                System.out.println("Explore Mode: " + explorerMode + " | case: " + passageSize + " | heading: " + direction(heading));
                System.out.println("CORNER");
                int junctionSize = robotData.junctions.size() - 1;

                //System.out.println("old table: ");
                //printJunction();

                int dir2 = robotData.junctions.get(junctionSize);
                int dir = IRobot.NORTH + (((dir2-IRobot.NORTH) + 2) % 4 + 4) % 4;

                robotData.junctions.remove(junctionSize);
                err2 = "pulled of " + direction(dir2) + " | new direction: " + direction(dir)+" | new size: "+ robotData.junctions.size();
                wall(dir, robot, junctionSize);
                System.out.println(err2);
                System.out.println("---------------------------------");
                //System.out.println("new table: ");
                //printJunction();
                return dir;
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

    private ArrayList<Integer> passageExits(IRobot robot) {
        ArrayList<Integer> passage = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) {
            int direction = IRobot.NORTH + i;
            if (lookHeading(direction, robot) == IRobot.PASSAGE) {
                passage.add(direction);
            }

        }
        return passage;
    }

    private ArrayList<Integer> nonWallExits(IRobot robot) {
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

