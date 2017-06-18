	// HW 2: DieN

	// The game Die4 is played in the following way:

	// You start with 0 dollars.

	// Roll a six-sided die with a different number from 1 to 6 printed on each side.

	// If any number other than 4 is rolled, you receive that many dollars. (E.g., if you roll the 
	// 	number 2, then you receive $2.)

	// If the number 4 is rolled, then you lose all of your obtained money and the game ends.

	// After you roll the die (and don't roll a 4), you have the option to quit the game. If you 
	// 	quit, you	keep all the money you've earned to that point. If you continue to roll, 
	// 	go back to step 2.

	// For this exercise, we will consider a generalization of this game called DieN. The rules 
	// are very similar. Consider a die with N sides (N an integer greater than 1) and a nonempty 
	// set B of integers between 1 and N (inclusive). The rules of the game are then:

	// You start with 0 dollars.

	// Roll an N-sided die with a different number from 1 to N printed on each side.

	// If you roll a number not in B, you receive that many dollars. (E.g., if you roll the 
	// 	number 2 and 2 is not in B, then you receive $2.)

	// If you roll a number in B, then you lose all of your obtained money and the game ends.

	// After you roll the die (and don't roll a number in B), you have the option to quit the 
	// 	game. If you quit, you keep all the money you've earned to that point. If you 
	// 	continue to roll, go back to step 2.

	// Write a Java class called DieN to determine the best way to play DieN. The constructor 
	// for this class should take the following arguments:

	// An int, numSides. The die will have this many sides, from 1 to numSides, inclusive. 
	// 	numSides will be at least 2 in all cases.

	// A boolean[], isBadSide. isBadSide[i] is true if and only if i+1 is in the set B of 
	// 	"bad sides" that cause the game to end with no money. isBadSide will have numSides 
	// 	entries, and at least one entry will be true in all cases.

	// DieN should have the following two functions:

	// actionToTake, which takes an int score and outputs one of two Strings: "roll" or "quit". 
	// 	The String that is output should be the action that is optimal to take for a player 
	// 	with a given score. If the actions are equally optimal, either will be accepted.

	// expectedValue, which takes no inputs and outputs a double giving the expected number of 
	// 	dollars a player will earn from playing the game, assuming that player follows the 
	// 	optimal strategy at every step. This value should be accurate within a tolerance of 
	// 	0.001, inclusive.

	import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
	import burlap.behavior.statehashing.DiscreteStateHashFactory;
	import burlap.domain.singleagent.graphdefined.GraphDefinedDomain;
	import burlap.oomdp.auxiliary.DomainGenerator;
	import burlap.oomdp.auxiliary.common.NullTermination;
	import burlap.oomdp.core.*;
	import burlap.oomdp.singleagent.GroundedAction;
	import burlap.oomdp.singleagent.RewardFunction;
	import java.util.Random;

	public class DieN {

	    DomainGenerator             dg;
	    Domain                      domain;
	    State                       initState;
	    RewardFunction              rf;
	    TerminalFunction            tf;
	    DiscreteStateHashFactory    hashFactory;
	    double 						gamma;
	    
	    public DieN(int numSides, boolean[] isBadSide) {
	    	int numStates = 2;
	    	int numActions = 2;
	    	int numBadSides = 0;
	    	for(int numSide = 0; numSide<numSides; numSide++)
	    	{
	    		if(isBadSide[numSide])
	    		{
	    			numBadSides++;
	    		}
	    	}
	    	this.gamma = 1.0*(numSides-numBadSides)/numSides;
	    	this.dg = new GraphDefinedDomain(numStates);
	        
	    	((GraphDefinedDomain) this.dg).setTransition(0,0,0,1.0*(numSides-numBadSides)/numSides);
	    	((GraphDefinedDomain) this.dg).setTransition(0,0,1,1.0*(numBadSides)/numSides);
	    	((GraphDefinedDomain) this.dg).setTransition(0,1,1,1.0);

	        this.domain = this.dg.generateDomain();
	        this.initState = GraphDefinedDomain.getState(this.domain, 0);
	        this.rf = new RF(numSides, isBadSide, numBadSides);
	        this.tf = new SingleStateTF(1);
	        this.hashFactory = new DiscreteStateHashFactory();
	    }

	    public static class RF implements RewardFunction {

	    	int[] rewards;
	    	Random rand = new Random();

	        public RF(int numSides, boolean[] isBadSide, int numBadSides) {
	        	int rewards[] = new int[numSides-numBadSides];
	        	int rewardIndex = 0;
	        	for(int numSide = 0; numSide<numSides; numSide++)
	        	{
	        		if(!isBadSide[numSide])
	        		{
	        			rewards[rewardIndex] = numSide+1;
	        			rewardIndex++;
	        		}
	        	}

	        	this.rewards = rewards;
	        }
	        
	        @Override
	        public double reward(State s, GroundedAction a, State sPrime) { 
	            int sId  = GraphDefinedDomain.getNodeId(s);
	            int sPrimeId = GraphDefinedDomain.getNodeId(sPrime);
	            int actionId = Integer.parseInt(a.toString().replaceAll(
	                GraphDefinedDomain.BASEACTIONNAME, ""));
	            double r;
	            if(actionId==1)
	            {
	            	r = 0;
	            }
	            else
	            {
	            	if(sPrimeId==1)
	            	{
	            		r = 0;
	            	}
	            	else
	            	{
	            		if(this.rewards.length == 0)
	            		{
	            			r = 0;
	            		}
	            		else
	            		{
		            		r = this.rewards[rand.nextInt(this.rewards.length)];
	            		}
	            	}
	            }
	            return r;
	        }
	    }

	    public static class SingleStateTF implements TerminalFunction
	    {
	    	int terminalSid;

	    	public SingleStateTF(int sid)
	    	{
	    		this.terminalSid = sid;
	    	}

	    	@Override
	    	public boolean isTerminal(State s)
	    	{
	    		int sid = GraphDefinedDomain.getNodeId(s);
	    		return sid == this.terminalSid;
	    	}
	    }
	    
	    public String actionToTake(int score) {
			if(score>=expectedValue())
			{
				return "quit";
			}
			else
			{
				return "roll";
			}
		}

		public double expectedValue() {
			double gamma = this.gamma;
			double maxDelta = 0.0001;
	        int maxIterations = 1000;
	        ValueIteration vi = new ValueIteration(this.domain, this.rf, this.tf, gamma, 
	            this.hashFactory, maxDelta, maxIterations);
	        vi.planFromState(this.initState);
	        return vi.value(GraphDefinedDomain.getState(this.domain, 0));
		}
	    
	}
