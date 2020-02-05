package controllers.limitdepthfirst;

import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Zhang Yiyang
 * Date: 19/10/05
 * Time: 16:58
 */

public class Agent extends AbstractPlayer{
    /**
     * all the observations that have been searched，which is to be cleared every time a new action is return.
     */
    ArrayList<StateObservation> searched_observation;

    /**
     * the path that has been found already
     * because it can only return an action at a time, so it's better to change the type into StateObservation
     * in order to compare the state.
     */
    ArrayList<StateObservation> searched_path;

    /**
     * the index in the searched_path
     * no need in this task since it cannot return a series of actions
     */
    //int path_index;

    /**
     * a series of initialization
     */
    double best_heuristic;
    Types.ACTIONS present_action;
    Types.ACTIONS best_action;
    boolean action_founded = false;


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        searched_observation = new ArrayList<StateObservation>();
        searched_path = new ArrayList<StateObservation>();
    }

    /**
     * judge whether a stateObservation has been searched.
     * judged by if a new StateObservation is in the ArrayList searched_observation or not.
     * if a StateObservation is searched,return true.Else return false.
     * @param so the StateObservation to be judged
     * @return true or false
     */
    boolean Observation_searched(StateObservation so)
    {
        for(StateObservation state : searched_observation)
        {
            if(so.equalPosition(state))return true;
        }
        for(StateObservation state : searched_path)
        {
            if(so.equalPosition(state))return true;
        }
        return false;
    }

    /**
     * const LIMIT that define the depth of the search
     */
    private final int LIMIT = 7;

    /**
     * the limitdepthfirst function
     * @param so the present StateObservation
     * @param limit the limit the confine the search
     */
    private void limitdepthfirst(StateObservation so,int limit){
        if(action_founded)return;
        searched_observation.add(so);
        if(so.isGameOver()){
            //judge if it is win or lose
            if(so.getGameWinner() == Types.WINNER.PLAYER_WINS){
                action_founded = true;
                best_action = present_action;
                best_heuristic = 0;
            }
            return;
        }else if(limit == 0){
            double present_heuristic = countHeuristicValue(so);
            if(present_heuristic < best_heuristic){
                //choose the action that has the best heuristic value
                best_heuristic = present_heuristic;
                best_action = present_action;
            }
        }else{
            for(Types.ACTIONS available_action : so.getAvailableActions()){
                //searched_path.add(available_action);
                //no need for this
                StateObservation stCopy = so.copy();
                stCopy.advance(available_action);
                if(!Observation_searched(stCopy)){
                    if(limit == LIMIT)present_action = available_action;
                    limitdepthfirst(stCopy,limit-1);
                }
                //if(!success)searched_path.remove(searched_path.size()-1);
                //no need for this
            }
        }
    }
    /**
     * the function to count the heuristic value
     * @param so the present StateObservation
     * @return the counted heuristic value
     */
    private double countHeuristicValue(StateObservation so)
    {
        ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = so.getMovablePositions();

        Vector2d goalpos = fixedPositions[1].get(0).position; //the position of the goal
        Vector2d keypos = movingPositions[0].get(0).position; //the position of the key
        /**
         * get the position of the agent
         * get the type(with key or without key) of the agent
         */
        Vector2d agentpos = so.getAvatarPosition();
        int agenttype = so.getAvatarType();
        double heuristic;
        if(agenttype == 1){
            //without key
            heuristic = Math.abs(agentpos.x-keypos.x)+Math.abs(agentpos.y-keypos.y)+Math.abs(keypos.x-goalpos.x)+Math.abs(keypos.y-goalpos.y);
        }else{
            //with key
            heuristic = Math.abs(agentpos.x-goalpos.x)+Math.abs(agentpos.y-goalpos.y);
        }
        return heuristic;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        //some initialization
        action_founded = false;
        searched_observation.clear();
        best_action = Types.ACTIONS.ACTION_NIL;
        best_heuristic = Double.MAX_VALUE;

        //start search
        limitdepthfirst(stateObs,LIMIT);
        searched_path.add(stateObs);
        return best_action;
    }


}