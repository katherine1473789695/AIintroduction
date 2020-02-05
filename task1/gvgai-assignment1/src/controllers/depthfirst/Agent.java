package controllers.depthfirst;

import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: Zhang Yiyang
 * Date: 19/10/03
 * Time: 23:18
 */

public class Agent extends AbstractPlayer{
    /**
     * all the observations that have been searched
     */
    ArrayList<StateObservation> searched_observation;

    /**
     * the path that has been found already
     */
    ArrayList<Types.ACTIONS> searched_path;

    /**
     * the index in the searched_path
     */
    int path_index;


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        searched_observation = new ArrayList<StateObservation>();
        searched_path = new ArrayList<Types.ACTIONS>();
        path_index = 0;
    }

    /**
     * judge whether a stateObservation has been searched.
     * judged by if a new StateObservation is in the ArrayList searched_observation or not.
     * if a StateObservation is searched,return true.Else return false.
     */
    boolean Observation_searched(StateObservation so)
    {
        for(StateObservation state : searched_observation)
        {
            if(so.equalPosition(state))return true;
        }
        return false;
    }

    boolean success = false;
    /**
     * the depthfirst function
     */
    private void depthfirst(StateObservation so){
        if(success)return;
        searched_observation.add(so);
        if(so.isGameOver()){
            //judge if it is win or lose
            if(so.getGameWinner() == Types.WINNER.PLAYER_WINS)success = true;
            return;
        }
        for(Types.ACTIONS available_action : so.getAvailableActions()){
            searched_path.add(available_action);
            StateObservation stCopy = so.copy();
            stCopy.advance(available_action);
            if(!Observation_searched(stCopy))depthfirst(stCopy);
            if(!success)searched_path.remove(searched_path.size()-1);
        }
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if(!success)depthfirst(stateObs);
        Types.ACTIONS action = searched_path.get(path_index);
        path_index++;
        return action;
    }


}
