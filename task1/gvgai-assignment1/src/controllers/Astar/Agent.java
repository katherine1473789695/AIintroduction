package controllers.Astar;

import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.competition.CompetitionParameters;

public class Agent extends AbstractPlayer{
    //public static int action_num;
    //public static Types.ACTIONS[] actions;
    //public MyNode root;
    public ArrayList<Types.ACTIONS> actions;
    public ArrayList<StateObservation> searched_state;

    //private AstarPlayer astarplayer;


    public Agent(StateObservation StateOb, ElapsedCpuTimer elapsedTimer)
    {
        actions = new ArrayList<Types.ACTIONS>();
        searched_state = new ArrayList<StateObservation>();
        //searched_node = new ArrayList<MyNode>();
        //ArrayList<Types.ACTIONS> act = StateOb.getAvailableActions();
        //actions = new Types.ACTIONS[act.size()];
        //root = new MyNode(StateOb,null);
        //for(int i=0;i<actions.length;i++){
            //actions[i]=act.get(i);
        //}
        //action_num=actions.length;

        //astarplayer = new AstarPlayer(StateOb);
    }

    public Types.ACTIONS act(StateObservation StateOb, ElapsedCpuTimer elapsedTimer){
        ArrayList<Observation> obs[] = StateOb.getFromAvatarSpritesPositions();
        ArrayList<Observation> grid[][] = StateOb.getObservationGrid();

        actions.clear();
        actions=StateOb.getAvailableActions();
        ArrayList<MyNode> active_nodes=new ArrayList<MyNode>();

        MyNode root = new MyNode(StateOb,null);
        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        int remainingLimit = 5;
        //active_nodes = new ArrayList<MyNode>();
        root.expand();
        for(MyNode child:root.children)active_nodes.add(child);
        //for(MyNode node:children){
        //    active_nodes.add(node);
        //}
        //MyNode newnode = new MyNode(null,null);

        //for(MyNode node:children)active_nodes.add(node);
        //active_nodes.add(this);
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

            double best_value = Double.MAX_VALUE;
            MyNode best_node = active_nodes.get(0);
            for(MyNode node :active_nodes){
                if(node.value<=best_value){
                    best_value=node.value;
                    best_node=node;
                }
            }
            best_node.expand();
            for(MyNode child:best_node.children)active_nodes.add(child);
            //ArrayList<Types.ACTIONS> ac = best_node.state.getAvailableActions();
            //for(int i=0;i<best_node.children.length;i++){
                //StateObservation stCopy = best_node.state.copy();
                //stCopy.advance(ac.get(i));
                //best_node.children[i]=new MyNode(stCopy,best_node);
                //active_nodes.add(children[i]);
            //}
            //best_node.expand();
            //for(int i=0;i<children.length;i++){
            //    active_nodes.add(children[i]);
            //}
            active_nodes.remove(best_node);

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;

            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }
        double best_value = Double.MAX_VALUE;
        MyNode best_Node = active_nodes.get(0);
        for(MyNode all_node :active_nodes){
            if(all_node.value<=best_value){
                best_value=all_node.value;
                best_Node=all_node;
            }
        }


        //MyNode best_node = root.find_best();
        MyNode mother=best_Node.find_mother();
        //MyNode mother = root.find_mother();
        int index=0;
        for(int i=0;i<root.children.length;i++){
            if(root.children[i]==mother)
                index=i;
        }

        //astarplayer.init(StateOb);
        //int action = astarplayer.run(elapsedTimer);
        return actions.get(index);
    }



}
