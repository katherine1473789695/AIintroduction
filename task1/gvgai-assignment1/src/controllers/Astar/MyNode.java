package controllers.Astar;

import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

public class MyNode {
    public StateObservation state;
    public MyNode parent;
    public MyNode[] children;
    private int depth;
    public double value;
    //ArrayList<MyNode> active_nodes;
    //public MyNode(StateObservation state)
    //{
     //   this.state = state;
     //   parent=null;

    //}


    public MyNode(StateObservation state, MyNode parent ){
        this.state=state;
        this.parent=parent;
        this.children = new MyNode[this.state.getAvailableActions().size()];
        if(parent!=null){
            this.depth = parent.depth+1;
        }else{
            this.depth=0;
        }
        this.value = this.depth+distance(this.state);
        //active_nodes = new ArrayList<MyNode>();
    }
    //public MyNode astarsearch(ElapsedCpuTimer elapsedTimer){
        //double avgTimeTaken = 0;
        //double acumTimeTaken = 0;
        //long remaining = elapsedTimer.remainingTimeMillis();
        //int numIters = 0;

        //int remainingLimit = 5;
        //active_nodes = new ArrayList<MyNode>();
        //expand();
        //for(MyNode node:children){
        //    active_nodes.add(node);
        //}
        //MyNode newnode = new MyNode(null,null);

        //for(MyNode node:children)active_nodes.add(node);
        //active_nodes.add(this);
        //while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
            //ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

            //double best_value = Double.MAX_VALUE;
            //MyNode best_node = active_nodes.get(0);
         //   for(MyNode node :active_nodes){
           //     if(node.value<=best_value){
             //       best_value=node.value;
               //     best_node=node;
              //  }
           // }
           // ArrayList<Types.ACTIONS> ac = best_node.state.getAvailableActions();
           // for(int i=0;i<best_node.children.length;i++){
             //  StateObservation stCopy = best_node.state.copy();
               // stCopy.advance(ac.get(i));
               // best_node.children[i]=new MyNode(stCopy,best_node);
               // active_nodes.add(children[i]);
            //}
            //best_node.expand();
            //for(int i=0;i<children.length;i++){
            //    active_nodes.add(children[i]);
            //}
           // active_nodes.remove(best_node);

           // numIters++;
           // acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;

           // avgTimeTaken  = acumTimeTaken/numIters;
           // remaining = elapsedTimer.remainingTimeMillis();
       // }
       // MyNode best_node = find_best();
       // return best_node;
    //}
    //public MyNode find_best(){
    //    double best_value = Double.MAX_VALUE;
    //    MyNode best_Node = active_nodes.get(0);
    //    for(MyNode all_node :active_nodes){
    //        if(all_node.value<=best_value){
    //            best_value=all_node.value;
    //            best_Node=all_node;
    //        }
    //    }
      //  return best_Node;
    //}
    public MyNode find_mother(){
        MyNode cur = this;
        while (cur.parent!=null){
            while(cur.parent.parent!=null)
                cur = cur.parent;
        }
        return cur;
    }

    public void expand(){
        ArrayList<Types.ACTIONS> available_actions = this.state.getAvailableActions();
        //ArrayList<Types.ACTIONS> actions = cur_node.state.getAvailableActions();
        //int size = actions.size();
        //cur_node.children = new MyNode[size];
        for(int i=0;i<this.children.length;i++){
            StateObservation stCopy = this.state.copy();
            stCopy.advance(available_actions.get(i));
            //if(!is_searched(stCopy)){
                MyNode tn = new MyNode(stCopy,this);
                //children[i].state = stCopy;
                //children[i].parent = ;
                this.children[i]=tn;
                //active_nodes.add(tn);
            //}

        }
    }

    //private boolean is_searched(StateObservation so){
        //for(StateObservation state:Agent.)
    //}
    private double distance(StateObservation state)
    {
        ArrayList<Observation>[] fixedPositions = state.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = state.getMovablePositions();

        Vector2d goalpos = fixedPositions[1].get(0).position; //the position of the goal
        Vector2d keypos = movingPositions[0].get(0).position; //the position of the key

        Vector2d avatarpos = state.getAvatarPosition();
        int avatartype = state.getAvatarType();
        double manhattendistance;
        if(avatartype == 1){
            //without key
            manhattendistance = Math.abs(avatarpos.x-keypos.x)+Math.abs(avatarpos.y-keypos.y)+Math.abs(keypos.x-goalpos.x)+Math.abs(keypos.y-goalpos.y);
        }else{
            //with key
            manhattendistance = Math.abs(avatarpos.x-goalpos.x)+Math.abs(avatarpos.y-goalpos.y);
        }
        return manhattendistance;
    }
}
