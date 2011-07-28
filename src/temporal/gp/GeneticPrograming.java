package temporal.gp;

import java.util.Hashtable;
import java.util.Enumeration;

public class GeneticPrograming implements Runnable {

	public int populationSize;

	public int maxDepthForNewIndividuals;
	
	public int maxDepthForIndividualsAfterCrossover;
	
	public int maxDepthForNewSubtreesInMutants;
	
	public double crossoverFraction;
	
	public double fitnessProportionateReproFraction;
	
	public double mutationFraction;
	
	public enum MethodOfGeneration { GROW , FULL , RAMPED_HALF_AND_HALF };
	MethodOfGeneration methodOfGeneration;
	
	public enum MethodOfSelection { FITNESS_PROPORTIONATE , TOURNAMENT };
	MethodOfSelection methodOfSelection;
	
	public DataPoint[] fitnessCases;
	
	public ProgramChoice[] terminalSet;
	
	public ProgramChoice[] functionSet;

	Thread thread = null;

	Individual[] population;
	
	int currentGeneration = 0;
	
	int generationOfBestOfRunIndividual = 0;
		
	static int MAX_GENERATIONS = 100;

	static final int SEED = 12345;

	static Random random = new Random(SEED);

	Individual bestOfRunIndividual = null;

	public GeneticPrograming( DataPoint[] fitnessCases ) {
		populationSize = 1000;
		MAX_GENERATIONS = 10;
		maxDepthForNewIndividuals = 10;
		maxDepthForIndividualsAfterCrossover = 20;
		maxDepthForNewSubtreesInMutants = 7;
		crossoverFraction = 100;
		fitnessProportionateReproFraction = 100;
		mutationFraction = 100;
		this.fitnessCases = fitnessCases;
		terminalSet = new ProgramChoice[]{
			new ProgramChoice("Random constant", new ConstantValue().getClass()),
			new ProgramChoice("One second", new ConstantValue(1000).getClass()),
			new ProgramChoice("One minute", new ConstantValue(60 * 1000).getClass()),
			new ProgramChoice("Half hour", new ConstantValue(30 * 60 * 1000).getClass()),
			new ProgramChoice("One hour", new ConstantValue(60 * 60 * 1000).getClass()),
			new ProgramChoice("One day", new ConstantValue(24 * 60 * 60 * 1000).getClass()),
			new ProgramChoice("One week", new ConstantValue(8 * 24 * 60 * 60 * 1000).getClass()),
			new ProgramChoice("One month", new ConstantValue(31 * 24 * 60 * 60 * 1000).getClass()),
			new ProgramChoice("One year", new ConstantValue(364 * 24 * 60 * 60 * 1000).getClass()),
			new ProgramChoice("One decade", new ConstantValue(10 * 364 * 24 * 60 * 60 * 1000).getClass()),
			new ProgramChoice("One century", new ConstantValue(100 * 364 * 60 * 60 * 1000).getClass()),
			new ProgramChoice("Variable from input vector", new Variable().getClass())
		};
		functionSet = new ProgramChoice[]{
			new ProgramChoice("+ (add)", new Addition().getClass()),
			new ProgramChoice("- (sub)", new Subtraction().getClass()),
			new ProgramChoice("* (mul)", new Multiplication().getClass()),
//			new ProgramChoice("/ (div)", new Division().getClass()),
			new ProgramChoice("vec_sel", new Selection().getClass()),
			new ProgramChoice("if_zero", new IfZeroThenElse().getClass()),
			new ProgramChoice("if_nzro", new IfNotZeroThenElse().getClass())
		};
		this.methodOfSelection = MethodOfSelection.TOURNAMENT;
		this.methodOfGeneration = MethodOfGeneration.FULL;
	}

	public synchronized void start() {
		thread = new Thread(this);
		thread.setPriority(Thread.NORM_PRIORITY - 1);
		thread.start();
		state = STARTED;
	}

	public synchronized void suspend() {
		if (thread != null && thread.isAlive()) {
			state = SUSPENDED;
			thread.suspend();
		}
	}

	public synchronized void resume() {
		if (thread != null && thread.isAlive()) {
			thread.resume();
			state = RESUMED;
		}
	}

	public synchronized void stop() {
		if (thread != null && thread.isAlive()) {
			state = STOPPED;
			thread.stop();
		}
	}

	public synchronized void crash() {
		if (thread != null && thread.isAlive()) {
			state = STOPPED;
			thread.stop();
		}
	}

	public static final int IDLE = 0;

	public static final int STARTED = 1;
	
	public static final int SUSPENDED = 2;
	
	public static final int RESUMED = 3;
	
	public static final int STOPPED = 4;

	private int state = IDLE;
	
	private int oldState = IDLE;

	public int getState() {
		return state;
	}

	public synchronized void freeze() {
		oldState = getState();
		if (thread != null && thread.isAlive() &&
				(oldState == STARTED || oldState == RESUMED)) { // i.e. running
			thread.suspend();
		}
	}

	public synchronized void thaw() {
		if (thread != null && thread.isAlive() &&
				(oldState == STARTED || oldState == RESUMED)) {
			thread.resume();
		}
	}

	/**
	* @return a terminal, randomly chosen from the terminal set
	*/
	Terminal chooseFromTerminalSet() {
		Terminal choice;
		try {
			int index = random.nextInt(terminalSet.length);
			Class cls = ((ProgramChoice)terminalSet[index]).value();
		    choice = (Terminal)(cls.newInstance());
		} catch(Exception e) {
			choice = null;
		}
		return choice;
	}

	/**
	*	Creates arguments for a function
	*/
	void createArgumentsForFunction(
		Function function,
		int allowableDepth,
		boolean fullP) {
		for (int i = 0; i < function.arg.length; i++) {
			function.arg[i] = createIndividualProgram(
					allowableDepth,
					false,
					fullP);
		}
	}
	
	/**
	*	Creates a program recursively using functions and terminals from
	* the respective sets.
	* @param allowableDepth the remaining depth of the tree we can create,
	*		when we hit zero we will only select terminals
	* @param topNodeP is true only when we are being called as the top 
	*		node in the tree. This allows us to make sure that we always put
	*		a function at the top	of the tree.  
	* @param fullP indicates whether this individual is to be maximally
	*		bushy or not
	*/
	Program createIndividualProgram(
		int allowableDepth,
		boolean topNodeP,
		boolean fullP) {
		Program p;
		int choice;
		Function function;
		if (allowableDepth <= 0) {
			p = chooseFromTerminalSet();
		} else {
			if (fullP || topNodeP) {
				choice = random.nextInt(functionSet.length);
				try {
					Class cls = ((ProgramChoice) functionSet[choice]).value();
					function = (Function)cls.newInstance();
				} catch(Exception e) {
					function = null;
				}
				createArgumentsForFunction(
						function,
						allowableDepth - 1,
						fullP);
				p = function;
			} else {
				//	Choose one from the bag of functions and terminals.
				choice = random.nextInt(terminalSet.length + functionSet.length);
				if (choice < functionSet.length) {
					//	We chose a function, so pick it out and go on creating the tree down from here.
					try {
						Class cls = ((ProgramChoice)functionSet[choice]).value();
						function = (Function)cls.newInstance();
					} catch(Exception e) {
						function = null;
					}
					createArgumentsForFunction(
							function,
							allowableDepth - 1,
							fullP);
					p = function;
				} else {
					p = chooseFromTerminalSet();
				}
			}
		}
		return p;
	}

	/**
	* Creates the initial population. This is an array of individuals.
	* Its size is sizeOfPopulation.
	* The program slot of each individual is initialized
	* to a suitable random program.
	*/
	void createPopulation() {
		int allowableDepth;
		boolean fullP;
		Hashtable generation0UniquifierTable = new Hashtable();
		population = new Individual[populationSize];
		int minimumDepthOfTrees = 1;
		boolean fullCycleP = false;
		int maxDepthForNewIndivs = maxDepthForNewIndividuals;
		int attemptsAtThisIndividual = 0;
		int individualIndex = 0;
		while (individualIndex < population.length) {
			switch (methodOfGeneration) {
			case FULL:
					allowableDepth = maxDepthForNewIndivs;
					fullP = true;
					break;
			case GROW:
					allowableDepth = maxDepthForNewIndivs;
					fullP = false;
					break;
			case RAMPED_HALF_AND_HALF:
					allowableDepth =
							minimumDepthOfTrees +
							(individualIndex %
							(maxDepthForNewIndivs - minimumDepthOfTrees + 1));
					if (attemptsAtThisIndividual == 0 &&
							individualIndex % 
							(maxDepthForNewIndivs - minimumDepthOfTrees + 1) == 0) {
						fullCycleP = !fullCycleP;
					}
					fullP = fullCycleP;
					break;
			default:
					allowableDepth = maxDepthForNewIndivs;
					fullP = false;
					break;
			}

			Program newProgram = createIndividualProgram(
					allowableDepth,  
					true,
					fullP);
			String hashKey = newProgram.toString(0);
			if (!generation0UniquifierTable.containsKey(hashKey)) {
				population[individualIndex] = new Individual(newProgram);
				individualIndex++;
				generation0UniquifierTable.put(hashKey, newProgram);
				attemptsAtThisIndividual = 0;
			} else {
				attemptsAtThisIndividual++;
				if (attemptsAtThisIndividual > 20) {
					minimumDepthOfTrees++;
					maxDepthForNewIndivs = Math.max(maxDepthForNewIndivs, minimumDepthOfTrees);
					attemptsAtThisIndividual = 0;
				}
			}
		}
	}

	static final Condition isProgram = new IsProgram();

	static final Condition isFunction = new IsFunction();

	/**
	* Produces two new individuals by crossing two parents.
	* First, a crossover point is selected randomly in each parent.
	* Experience shows that it is more useful to select inner nodes
	* (functions, not terminals) as crossover points.
	* The ratio of cuts at inner points vs. cuts at terminals is fixed
	* in this function.
	* Then the two parents are cut at their crossover points, giving
	* two tree fragments and two cut-off subtrees. The two subtrees
	* are swapped and spliced with the fragments again.
	* We have to make sure that the newly formed trees do not exceed a
	* certain depth. If they do, one of the parents is used as offspring.
	*/
	Program[] crossover(Program male, Program female) {
		double CrossoverAtFunctionFraction = 0.9;
		boolean crossoverAtFunction;
		//	Make copies of the parents first, because they will be destructively modified:
		Program[] offspring = new Program[2];
		offspring[0] = (Program)male.clone();
		offspring[1] = (Program)female.clone();

		int malePoint, femalePoint;
		TreeHook maleHook, femaleHook;
		crossoverAtFunction = (random.nextDouble() < CrossoverAtFunctionFraction);
		if (crossoverAtFunction) {
			malePoint = random.nextInt(male.countNodes(isFunction));
			maleHook = getSubtree(offspring[0], malePoint, isFunction);
		} else {
			malePoint = random.nextInt(male.countNodes());
			maleHook = getSubtree(offspring[0], malePoint, isProgram);
		}
		crossoverAtFunction = (random.nextDouble() < CrossoverAtFunctionFraction);
		if (crossoverAtFunction) {
			femalePoint = random.nextInt(female.countNodes(isFunction));
			femaleHook = getSubtree(offspring[1], femalePoint, isFunction);
		} else {
			femalePoint = random.nextInt(female.countNodes());
			femaleHook = getSubtree(offspring[1], femalePoint, isProgram);
		}

		//	Modify the new individuals by smashing in the (copied) subtree from the old individual.
		if (maleHook.parent == null) {
			offspring[0] = femaleHook.subtree;
		} else {
			maleHook.parent.arg[maleHook.childIndex] = femaleHook.subtree;
		}
		if (femaleHook.parent == null) {
			offspring[1] = maleHook.subtree;
		} else {
			femaleHook.parent.arg[femaleHook.childIndex] = maleHook.subtree;
		}

		//	Make sure that the new individuals aren't too big.
		validateCrossover(male, female, offspring);
		return offspring;
	}

	/**
	* @return the depth of the deepest branch of the tree
	*/
	int maxDepthOfTree(Program tree) {
		int maxDepth = 0;
		if (tree instanceof Function) {
			for (int a = 0; a < ((Function)tree).arg.length; a++) {
				Program s = ((Function)tree).arg[a];
				int depth = maxDepthOfTree(s);
				maxDepth = Math.max(maxDepth, depth);
			}
			return (1 + maxDepth);
		} else {
			return 0;
		}
	}

	/**
	* Given the parents and two offsprings from a crossover operation
	* check to see whether we have exceeded the maximum
	* allowed depth. If a new individual has exceeded the maximum depth
	* then one of the parents is used.
	*/
	void validateCrossover(Program male, Program female,
		Program[] offspring) {
		int depth;
		for (int i = 0; i < offspring.length; i++) {
			if (offspring[i] == null) {
				depth = 0;
			} else {
				depth = maxDepthOfTree(offspring[i]);
			}
			if (depth < 1 || depth > maxDepthForIndividualsAfterCrossover) {
				int whichParent = random.nextInt(2);
				if (whichParent == 0) {
					offspring[i] = (Program)male.clone();
				} else {
					offspring[i] = (Program)female.clone();
				}
			}
		}
	}

	/**
	* Mutates the argument program by picking a random point in
	* the tree and substituting in a brand new subtree created in
	* the same way that we create the initial random population.
	* @return a mutated copy of the original program
	*/
	Program mutate(Program program) {
		//	Pick the mutation point.
		int mutationPoint = random.nextInt(program.countNodes());
		//	Create a brand new subtree.
		Program newSubtree = createIndividualProgram(
				maxDepthForNewSubtreesInMutants,
				true,
				false);
		Program newProgram = (Program)program.clone();
		//	Smash in the new subtree.
		TreeHook hook = getSubtree(program, mutationPoint, isProgram);
		if (hook.parent == null) {
			newProgram = hook.subtree;
		} else {
			hook.parent.arg[hook.childIndex] = newSubtree;
		}
		return newProgram;
	}

	/**
	* Controls the actual breeding of the new population.  
	* Loops through the population executing each operation
	* (e.g., crossover, fitness-proportionate reproduction,
	* mutation) until it has reached the specified fraction.  
	* The new programs that are created are stashed in newPrograms
	* until we have exhausted the population, then we copy the new
	* individuals into the old ones.
	*/
	void breedNewPopulation() {
		Program[] newPrograms;
		double fraction;
		int index;
		Individual individual1, individual2;
		int i;
		double sumOfFractions = this.crossoverFraction + this.fitnessProportionateReproFraction + this.mutationFraction;
		double crossoverFraction = this.crossoverFraction / sumOfFractions;
		double reproductionFraction = this.fitnessProportionateReproFraction / sumOfFractions;
		newPrograms = new Program[population.length];
		fraction = 0.0;
		index = 0;
		newPrograms[index] = (Program)bestOfRunIndividual.program.clone();
		index++;
		while (index < population.length) {
			fraction = (double)index / (double)population.length;
			individual1 = findIndividual();
			if (fraction < crossoverFraction) {
				individual2 = findIndividual();
				Program[]	offspring = crossover(individual1.program, individual2.program);
    		newPrograms[index] = offspring[0];
				index++;
				if (index < population.length) {
    			newPrograms[index] = offspring[1];
					index = index++;
				}
			} else {
				if (fraction < 	reproductionFraction + crossoverFraction) {
					newPrograms[index] = (Program)individual1.program.clone();
					index ++;
				} else {
					newPrograms[index] = mutate(individual1.program);
					index++;
				}
			}
		}
		for (index = 0; index < population.length; index++) {
			population[index].program = newPrograms[index];
		}
	}

	void zeroizeFitnessMeasuresOfPopulation() {
		for (int i = 0; i < population.length; i++) { 
			population[i].standardizedFitness = 0.0;
			population[i].adjustedFitness = 0.0;
			population[i].normalizedFitness = 0.0;
			population[i].hits = 0;
		}
	}

	/**
	* Loops over the individuals in the population evaluating and
	* recording the fitness and hits.
	*/
	void evaluateFitnessOfPopulation() {
		for (int i = 0; i < population.length; i++) {
			fitnessFunction(population[i], i);
		}
	}

	/**
	* Computes the normalized and adjusted fitness of each
	* individual in the population.
	*/
	void normalizeFitnessOfPopulation() {
		double sumOfAdjustedFitnesses = 0.0;
		for (int i = 0; i < population.length; i++) {
			//	Set the adjusted fitness.
			population[i].adjustedFitness = 
    			1.0 / (population[i].standardizedFitness + 1.0);
			sumOfAdjustedFitnesses =
					sumOfAdjustedFitnesses + population[i].adjustedFitness;
		}
		//	Loop through population normalizing the adjusted fitness.
		for (int i = 0; i < population.length; i++) {
			population[i].normalizedFitness =
					population[i].adjustedFitness / sumOfAdjustedFitnesses;
		}
	}

	/**
	* Uses a quicksort to sort the population destructively
	* into descending order of normalized fitness.
	*/
	private void sort(int low, int high) {
		int index1, index2;
		double pivot;
		Individual temp;
		index1 = low;
		index2 = high;
		pivot = population[(low + high) / 2].normalizedFitness;
		do {
			while (population[index1].normalizedFitness > pivot) {
				index1++;
			}
			while (population[index2].normalizedFitness < pivot) {
				index2--;
			}
			if (index1 <= index2) {
				temp = population[index2];
				population[index2] = population[index1];
				population[index1] = temp;
				index1++;
				index2--;
			}
		} while (index1 <= index2);
		if (low < index2) {
			sort(low, index2);
		}
		if (index1 < high) {
			sort(index1, high);
		}
	}
		
	/**
	* Sorts the population according to normalized fitness. 
	* The population array is destructively modified.
	*/
	void sortPopulationByFitness() {
		sort(0, population.length - 1);
	}

	/**
	* This function has to evaluate the fitness for a specific individual.
	* As a side-effect, it notfies any observers of the GP that an individual
	* has been evaluated and supplies the test cases.
	*/
	void fitnessFunction(Individual ind, int individualNr) {
		double rawFitness = 0.0;
		ind.hits = 0;
		DataPoint[] testCases = new DataPoint[fitnessCases.length];
		for (int index = 0; index < fitnessCases.length; index++) {
			long data[] = fitnessCases[index].data;
			long valueFromProgram = ind.program.eval(data);
			long difference = Math.abs(valueFromProgram - fitnessCases[index].result);
			rawFitness = rawFitness + difference;
			if (difference < 0.01) ind.hits++;
			testCases[index] = new DataPoint(data, valueFromProgram);
		}
		ind.standardizedFitness = rawFitness;
	}

	boolean terminationPredicate() {
		return currentGeneration >= MAX_GENERATIONS;
	}

	void evolve() {
		Individual bestOfGeneration;
		if (currentGeneration > 0) breedNewPopulation();
		zeroizeFitnessMeasuresOfPopulation();
		evaluateFitnessOfPopulation();
		normalizeFitnessOfPopulation();
		sortPopulationByFitness();
		bestOfGeneration = population[0];
		if (bestOfRunIndividual == null || bestOfRunIndividual.standardizedFitness > bestOfGeneration.standardizedFitness) {
				bestOfRunIndividual = bestOfGeneration.copy();
				generationOfBestOfRunIndividual = currentGeneration;
				DataPoint[] testCases = new DataPoint[fitnessCases.length];
				for (int i = 0; i < fitnessCases.length; i++) {
					long data[] = fitnessCases[i].data;
					long y = bestOfRunIndividual.program.eval(data);
					testCases[i] = new DataPoint(data, y);
				}
		}
		double[] fitness = new double[population.length];
		for (int i = 0; i < fitness.length; i++) {
				fitness[i] = population[i].adjustedFitness;
		}
		currentGeneration++;
	}

	/**
	* @return an individual in the specified population whose
	* normalized fitness is greater than the specified value.
	*/
	Individual findFitnessProportionateIndividual(double afterThisFitness) {
		int indexOfSelectedIndividual;
		double sumOfFitness = 0.0;
		int index = 0;
		while (index < population.length && sumOfFitness < afterThisFitness) {
			sumOfFitness = sumOfFitness + population[index].normalizedFitness; 
			index++;
		}
		if (index >= population.length) {
			indexOfSelectedIndividual = population.length - 1;
		} else {
			indexOfSelectedIndividual = index - 1;
		}
		return population[indexOfSelectedIndividual];
	}

	/**
	* @return picks some individuals from the population at random and
	* returns the best one.
	*/
	Individual findIndividualUsingTournamentSelection() {
		int TournamentSize = Math.min(population.length, 7);
		Hashtable table = new Hashtable();
		while (table.size() < TournamentSize) {
			int key = random.nextInt(population.length);
			table.put(new Integer(key), population[key]);
		}
		Enumeration e = table.elements();
		Individual best	= (Individual)e.nextElement();
		double bestFitness = best.standardizedFitness;
		while (e.hasMoreElements()) {
			Individual individual = (Individual)e.nextElement();
			double thisFitness = individual.standardizedFitness;
			if (thisFitness < bestFitness) {
				best = individual;
				bestFitness = thisFitness;
			}
		}
		return best;
	}

	Individual findIndividual() {
		Individual ind = null;
		switch (methodOfSelection) {
			case TOURNAMENT:
				ind = findIndividualUsingTournamentSelection();
				break;
			case FITNESS_PROPORTIONATE:
				ind = findFitnessProportionateIndividual(random.nextDouble());
				break;
		}
		return ind;
	}

	public void run() {
		random.setSeed(SEED);
		generationOfBestOfRunIndividual = 0;
		bestOfRunIndividual = null;
		createPopulation();
		currentGeneration = 0;
		while (!terminationPredicate()) {
			try {
				evolve();
				thread.sleep(1);
			} catch(Exception e) { crash(); }
		}
		stop();
	}
 
	/**
	* @return the index'th subtree satisfying a specific condition,
	* for example the	3rd terminal of a tree.
	* Subtrees are numbered from left to right,
	* depth first. The test function of the condition is applied to all nodes
	* of the tree. It must evaluate to true for the desired kind of subtrees.
	* getSubtree() returns not only a pointer to the specified subtree, but
	* also a pointer to the subtree's parent and and index for its position
	* in the list of the parent's children.
	* If the subtree is not found, the function returns null.
	*/
	TreeHook getSubtree(Program tree, int index, Condition cond) {
		int[] count = { index };
		return Walk(tree, count, cond, null, -1);
	}

	private TreeHook Walk(Program tree, int[] count, Condition cond, Function parent, int childIndex) {
		if (cond.test(tree) && count[0] == 0) {
			return new TreeHook(tree, parent, childIndex);
		} else {
			TreeHook hook = null;
			if (tree instanceof Function) {
				Function func = (Function)tree;
				for (int a = 0; a < func.arg.length && count[0] > 0; a++) {
					if (cond.test(func.arg[a])) {
						count[0]--;
					}
					hook = Walk(func.arg[a], count, cond, func, a);
				}
			}
			return hook;
		}
	}

}

class Random extends java.util.Random {
	Random(int seed) { super(seed); }
	public int nextInt(int n) {
		return (int)(nextDouble() * n);
	}
}

class TreeHook {
	TreeHook(Program subtree, Function parent, int childIndex) {
		this.subtree = subtree;
		this.parent = parent;
		this.childIndex = childIndex;
	}
	Program subtree;
	Function parent;
	int childIndex;
}

abstract class Condition {
	abstract boolean test(Program p);
}

class IsProgram extends Condition {
	boolean test(Program p) { return (p instanceof Program); }
}

class IsFunction extends Condition {
	boolean test(Program p) { return (p instanceof Function); }
}

abstract class Program implements Cloneable {

	/**
	* @param level the current recursion level when descending a subtree
	* @return the text representation of the subtree
	*/
	public abstract String toString(int level);

	/**
	* @return a text description of the node. While toString() prints
	*		the value of a subtree, getName() returns something like the
	*		class name of this very node. getName() can be used to preset
	*		the choice lists for the function and terminal sets.
	*/
	abstract String getName();

	/**
	* @return a series of blanks proportional to the recursion level
	*/
	String indent(int level) {
		String s = new String();
		for (int i = 0; i < level; i++) {
			s = s + "  ";
		}
		return s;
	}

	long eval ( Long data[] ) {
		long aux[] = new long[data.length];
		for ( int i = 0 ; i < aux.length; i++ ) aux[i] = data[i];
		return eval(aux);
	}
	
	/**
	* @return the value of the subtree, evaluated at x, i.e. y = f(x)
	*/
	abstract long eval( long data[] );

	/**
	* @return the number of nodes in this subtree
	*/
	abstract int countNodes();

	/**
	* @return the number of nodes in this subtree satisfying a certain
	*		condition
	*/
	abstract int countNodes(Condition cond);

	/**
	* @return a deep copy of the subtree
	*/
	abstract protected Object clone();
}

abstract class Terminal extends Program {

	int countNodes() { return 1; }

	int countNodes(Condition cond) { return (cond.test(this)) ? 1 : 0; }

}

class ConstantValue extends Terminal {

	int MIN = -4;
	
	int MAX = +4;
	
	int value;

	ConstantValue() {
		value = GeneticPrograming.random.nextInt() * (MAX - MIN) + MIN;
	}

	ConstantValue( int MIN, int MAX ) {
		this.MIN = MIN;
		this.MAX = MAX;
		value = GeneticPrograming.random.nextInt() * (MAX - MIN) + MIN;
	}

	ConstantValue( int value ) {
		this.MIN = this.MAX = this.value;
	}

	public String toString(int level) {
		return indent(level) + value;
	}

	protected Object clone() {
		return new ConstantValue(this.value); };

	String getName() {
		return "Random Constant";
	}

	long eval(long data[]) {
		return value;
	}

}

class Variable extends Terminal {

	int rnd = GeneticPrograming.random.nextInt();
	
	public String toString(int level) {
		return indent(level) + "from_vector(" + rnd + ")";
	}

	protected Object clone() {
		return new Variable();
	}

	String getName() {
		return "variable";
	}

	long eval(long x[]) {
		return x[(int)(rnd % x.length)];
	}

}

abstract class Function extends Program {

	protected Program[] arg;

	public String toString(int level) {
		boolean allArgsAreTerminals = true;
		for (int a = 0; a < arg.length; a++) {
			allArgsAreTerminals = allArgsAreTerminals && (arg[a] instanceof Terminal);
		}
		String s = new String();
		if (!allArgsAreTerminals) {
			s = indent(level) + getName() + "(\n";
			int i = 0;
			while (i < arg.length-1) {
				s = s + arg[i].toString(level+1) + ",\n";
				i++;
			}
			if (i < arg.length) {
				s = s + arg[i].toString(level+1) + "\n";
			}
			s = s + indent(level) + ")";
		} else {
			s = indent(level) + getName() + "(";
			int i = 0;
			while (i < arg.length-1) {
				s = s + arg[i].toString(0) + ",";
				i++;
			}
			if (i < arg.length) {
				s = s + arg[i].toString(0);
			}
			s = s + ")";
		}
		return s;
	}

	protected Object clone() {
		Function temp = null;
		try {
			temp = (Function)getClass().newInstance();
			for (int i = 0; i < arg.length; i++) {
				temp.arg[i] = (Program)arg[i].clone();
			}
		} catch (Exception e) {
		}
		return temp;
	}

	int countNodes() {
		int count = 1;
		for (int a = 0; a < arg.length; a++) {
			count = count + arg[a].countNodes();
		}
		return count;
	}

	int countNodes(Condition cond) {
		int count = (cond.test(this) ? 1 : 0);
		for (int a = 0; a < arg.length; a++) {
			count = count + arg[a].countNodes(cond);
		}
		return count;
	}

}

class Addition extends Function {

	Addition() {
		arg = new Program[2];
	}

	String getName() {
		return "add";
	}

	long eval(long x[]) {
		return arg[0].eval(x) + arg[1].eval(x);
	}

}

class Subtraction extends Function {

	Subtraction() {
		arg = new Program[2];
	}

	String getName() {
		return "sub";
	}

	long eval(long x[]) {
		return arg[0].eval(x) - arg[1].eval(x);
	}

}

class Multiplication extends Function {

	Multiplication() {
		arg = new Program[2];
	}

	String getName() {
		return "mul";
	}

	long eval(long x[]) {
		return arg[0].eval(x) * arg[1].eval(x);
	}

}

class Division extends Function {

	Division() {
		arg = new Program[2];
	}

	String getName() {
		return "div";
	}

	long eval(long x[]) {
		long divisor = arg[1].eval(x);
		return (divisor == 0 ? 1 : arg[0].eval(x) / divisor);
	}

}

class Selection extends Function {

	Selection() {
		arg = new Program[1];
	}

	String getName() {
		return "selection";
	}

	long eval(long x[]) {
		long selector = arg[1].eval(x);
		return x[Math.abs((int)(selector % x.length))];
	}

}

class IfZeroThenElse extends Function {

	IfZeroThenElse() {
		arg = new Program[3];
	}

	String getName() {
		return "ifzero-then-else";
	}

	long eval(long x[]) {
		long selector = arg[0].eval(x);
		return (selector == 0 ? arg[1].eval(x) : arg[2].eval(x));
	}

}

class IfNotZeroThenElse extends Function {

	IfNotZeroThenElse() {
		arg = new Program[3];
	}

	String getName() {
		return "if-not-zero-then-else";
	}

	long eval(long x[]) {
		long selector = arg[0].eval(x);
		return (selector != 0 ? arg[1].eval(x) : arg[2].eval(x));
	}

}

class First extends Function {

	First() {
		arg = new Program[2];
	}

	String getName() {
		return "first";
	}

	long eval(long x[]) {
		return arg[0].eval(x);
	}

}

class Second extends Function {

	Second() {
		arg = new Program[2];
	}

	String getName() {
		return "second";
	}

	long eval(long x[]) {
		return arg[1].eval(x);
	}

}

class Individual {

	public Program program;
	
	double standardizedFitness;
	
	double adjustedFitness;
	
	double normalizedFitness;
	
	int hits;

	Individual(Program p) {
		program = (Program)p.clone();
		standardizedFitness = 0.0;
		adjustedFitness = 0.0;
		normalizedFitness = 0.0;
		hits = 0;
	}

	Individual copy() {
		Individual newInd = new Individual(this.program);
		newInd.standardizedFitness = this.standardizedFitness;
		newInd.adjustedFitness = this.adjustedFitness;
		newInd.normalizedFitness = this.normalizedFitness;
		newInd.hits = this.hits;
		return newInd;
	}

}

class ProgramChoice {

	private Class cls;
	
	private String text;

	ProgramChoice(String text, Class cls) {
		this.cls = cls;
		this.text = text;
	}

	public String toString() { return text; }
	
	Class value() { return cls; }
}