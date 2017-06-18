// HW 1. MDPSolver

// For this homework assignment, first watch the tutorial lesson Introduction to BURLAP 
// to learn how to build and solve an MDP using the BURLAP framework in Java.

// Next, write a Java class called MDPSolver. The constructor for the MDPSolver class 
// should accept the following arguments:

// An int numStates. The states in the MDP are identified by an int from 0 to 
//     numStates-1, inclusive.

// An int numActions. The actions in the MDP are identified by an int from 0 to 
//     numActions-1, inclusive.

// A double[][][] probabilitiesOfTransitions. probabilitiesOfTransitions[i][j][k] 
//     is the probability of ending in state k by taking action j in state i. For 
//     all i and j, summing probabilitiesOfTransitions[i][j][k] over the index k 
//     will equal 1.

// A double[][][] rewards. rewards[i][j][k] is the reward obtained by ending in 
//     state k by taking action j in state i.

// These all represent elements of an MDP. Note that there will be no explicitly defined 
// terminal states. MDPSolver should also contain a method called valueOfState that 
// accepts two arguments, an int state that represents a state in the MDP, and a double 
// gamma that represents the discount factor to apply. It should output a double giving 
// the value of the appropriate state using the given discount factor. The value given 
// should be accurate within a tolerance of 0.001, inclusive.

import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.domain.singleagent.graphdefined.GraphDefinedDomain;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class MDPSolver {
        
    DomainGenerator             dg;
    Domain                      domain;
    State                       initState;
    RewardFunction              rf;
    TerminalFunction            tf;
    DiscreteStateHashFactory    hashFactory;

    public MDPSolver(int numStates, int numActions, double[][][] probabilitiesOfTransitions, 
                     double[][][] rewards) {
        this.dg = new GraphDefinedDomain(numStates);
        
        for(int numState = 0; numState<numStates; numState++)
        {
            for(int numStatePrime = 0; numStatePrime<numStates; numStatePrime++)
            {
                for(int numAction = 0; numAction<numActions; numAction++)
                {
                    ((GraphDefinedDomain) this.dg).setTransition(numState,numAction,numStatePrime,
                        probabilitiesOfTransitions[numState][numAction][numStatePrime]);
                }
            }
        }

        this.domain = this.dg.generateDomain();
        this.initState = GraphDefinedDomain.getState(this.domain,0);
        this.rf = new RF(rewards);
        this.tf = new NullTermination();
        this.hashFactory = new DiscreteStateHashFactory();
    }
    
    public static class RF implements RewardFunction {
        double[][][] rewards;

        public RF(double[][][] rewards) {
            this.rewards = rewards;
        }
        
        @Override
        public double reward(State s, GroundedAction a, State sprime) { 
            int sid  = GraphDefinedDomain.getNodeId(s);
            int sprimeid = GraphDefinedDomain.getNodeId(sprime);
            int actionId = Integer.parseInt(a.toString().replaceAll(
                GraphDefinedDomain.BASEACTIONNAME, ""));

            return this.rewards[sid][actionId][sprimeid];
        }
    }
    
    private ValueIteration computeValue(double gamma) {
        double maxDelta = 0.0001;
        int maxIterations = 1000;
        ValueIteration vi = new ValueIteration(this.domain, this.rf, this.tf, gamma, 
            this.hashFactory, maxDelta, maxIterations);
        vi.planFromState(this.initState);
        return vi;
    }

    public double valueOfState(int state, double gamma) {
        ValueIteration vi = computeValue(gamma);
        return vi.value(GraphDefinedDomain.getState(this.domain, state));
    }
}

