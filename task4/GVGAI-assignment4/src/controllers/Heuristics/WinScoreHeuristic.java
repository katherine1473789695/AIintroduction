package controllers.Heuristics;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 11/02/14
 * Time: 15:44
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WinScoreHeuristic extends StateHeuristic {

    private static final double HUGE_NEGATIVE = -1000.0;
    private static final double HUGE_POSITIVE =  1000.0;

    double initialNpcCounter = 0;

    public WinScoreHeuristic(StateObservation stateObs) {

    }

    public double evaluateState(StateObservation stateObs) {
        boolean gameOver = stateObs.isGameOver();
        Types.WINNER win = stateObs.getGameWinner();
        double rawScore = stateObs.getGameScore();

        //get the y-distance  between avatar and the y=0
        Vector2d avatar_pos = stateObs.getAvatarPosition();
        int avatar_x = (int)(avatar_pos.x)/28;
        int avatar_y = (int)(avatar_pos.y)/28;
        //System.out.println(avatar_y);
        //rawScore+=60*(14-avatar_y);
        double distance = 0;
        int health = stateObs.getAvatarHealthPoints();
        //System.out.println(health);

        LinkedList<Observation> allobj = new LinkedList<>();
        if( stateObs.getPortalsPositions()!=null )
            for(ArrayList<Observation> l : stateObs.getPortalsPositions()) allobj.addAll(l);
        for(Observation o : allobj){
            Vector2d p = o.position;
            int x = (int)(p.x/28);
            int y= (int)(p.y/28);
            distance = Math.abs(avatar_y-y)+Math.abs(avatar_x-x);
            //System.out.println("x="+x+",y="+y+",type="+o.itype);
        }
        rawScore-=6*distance;
        rawScore+=5*health;
        if(avatar_y==3||avatar_y==7||avatar_y==11)rawScore+=3;
        if(avatar_y==1||avatar_y==6||avatar_y==10)rawScore+=2;

        //get the distance between avatar and the portal
        //Vector2d portal_pos = stateObs.getPortalsPositions();
        //if( stateObs.getPortalsPositions()!=null )
            //for()



        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            return HUGE_NEGATIVE;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            return HUGE_POSITIVE;

        return rawScore;
    }


}


