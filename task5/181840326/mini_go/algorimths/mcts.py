import random, os
import numpy as np
import sonnet as snt
import tensorflow as tf
from operator import itemgetter
import copy
from agent.agent import Agent
from numpy.random import normal

class TreeNode(object):
    #tree class for monte carlo search
    def __init__(self,parent,prior_probility):
        self._parent = parent
        self._P = prior_probility
        self._u = prior_probility
        self._Q = 0
        self._nvisits = 0
        self._children = {}

    #expand a new node
    def expand(self,action_probility_pair):
        for action,probility in action_probility_pair:
            if action not in self._children:
                self._children[action] = TreeNode(self,probility)

    #return the value Q+u
    def get_value(self):
        return self._Q + self._u

    #juege if it is a leaf node
    def is_leaf_node(self):
        return self._children == {}

    #judge if it is root
    def is_root(self):
        return self._parent is None

    #select a node with the most value
    def select(self):
        return max(self._children.items(),key = lambda x:x[1].get_value())


    #update a single node
    def update(self,value,c_puct):
        self._nvisits += 1
        self._Q += (value-self._Q)/self._nvisits
        if not self.is_root():
            self._u = c_puct*self._P*np.sqrt(self._parent._nvisits)/(1+self._nvisits)

    #update all the nodes
    def update_all(self,value,c_puct):
        if self._parent:
            self._parent.update_all(value,c_puct)
        self.update(value,c_puct)



class MCTS(object):

    def __init__(self,value_function,policy_function,rollout_policy_function,
                 lmda=0.5,c_puct = 1,rollout_limit = 100,playout_depth = 10,n_playout = 100):
        self._root = TreeNode(None,1.0)
        self._value_function = value_function
        self._policy_function = policy_function
        self._rollout_policy_function = rollout_policy_function
        self._lmda = lmda
        self._c_puct = c_puct
        self._rollout_limit = rollout_limit
        self._playout_depth = playout_depth
        self._n_playout = n_playout
        self._player_id = 0

    #change the player
    def change_player(self):
        self._player_id == 1 if self._player_id == 0 else 0

    def evaluate_winner(self,state,env,rollout_limit):
        #rollout to the end of the game and return the winner, +1 is the current play wins,0 if a tie
        player = state.observations["current_player"]
        for i in range(rollout_limit):
            action_probiliy = self._rollout_policy_function(state,self._player_id)
            max_action = max(action_probiliy,key = itemgetter(1))[0]
            state = env.step(max_action)
            self.change_player()
            if state.last():
                break
        return state.rewards[0]

    def playout(self,state,env,leaf_depth):
        node = self._root
        for i in range(leaf_depth):
            #only expand when the node has not been expanded yet
            if node.is_leaf_node():
                action_probility = self._policy_function(state,self._player_id)
                if len(action_probility) == 0 :#end of game
                    break
                node.expand(action_probility)
            #choose the next move
            action, node = node.select()
            #update the state
            state = env.step(action)
            self.change_player()

        v = self._value_function(state,self._player_id) if self._lmda < 1 else 0
        z = self.evaluate_winner(state, env, self._rollout_limit) if self._lmda > 0 else 0
        value = (1-self._lmda)*v + self._lmda*z

        #update
        node.update_all(value,self._c_puct)


    def get_move(self,state,env):
        self._player_id = 0
        for n in range(self._n_playout):
            state_copy = copy.deepcopy(state)
            env_copy = copy.deepcopy(env)
            self.playout(state_copy,env_copy,self._playout_depth)
        #choose the most visited action
        return max(self._root._children.items(),key = lambda x:x[1]._nvisits)[0]

    def update_after_get_move(self,last_move):
        #assume the get_move already called
        if last_move in self._root._children:
            self._root = self._root._children[last_move]
            self._root._parent = None
        else:
            self._root = TreeNode(None,1.0)
NUM_ACTIONS = 26
def random_policy_function(time_step, player_id):
    legal_actions = time_step.observations["legal_actions"][player_id]
    probilitys = np.zeros(NUM_ACTIONS)
    probilitys[legal_actions] = 1
    probilitys/=sum(probilitys)
    return [i for i in zip(range(len(probilitys)),probilitys)]
def random_value_function(time_step, player_id):
    return normal(scale=0.3)

class MCTSAgent():
    def __init__(self,policy,rollout,playout_depth=10,n_playout=100):
        if policy == None and rollout == None:
            self.policy_function = self.rollout_policy_function = random_policy_function
            self.value_function = random_value_function
        else:
            self.value_function = policy.value_function
            self.policy_function = policy.policy_function
            self.rollout_policy_function = rollout.policy_function
        self.mcts = MCTS(value_function=self.value_function,
                         policy_function=self.policy_function,
                         rollout_policy_function=self.rollout_policy_function,
                         playout_depth=playout_depth,
                         n_playout = n_playout)

    def step(self,timestep,env):
        move = self.mcts.get_move(timestep,env)
        self.mcts.update_after_get_move(move)
        return move