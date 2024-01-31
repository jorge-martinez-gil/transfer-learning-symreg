/*
 * Jorge Martinez-Gil,  Jose Manuel Chaves-Gonzalez: 
 * Transfer learning for semantic similarity measures based on symbolic regression.
 * J. Intell. Fuzzy Syst. 45(1): 37-49 (2023)
 *
 * @author: Jorge Martinez-Gil
 */

package symregression;

public class GA {

    // Main method for the primary execution flow
    public static void main(String[] args) {

        // Training a solution. The context does not matter
        loadData(".//data//rg.txt");
        Population myPop = initializePopulation(25);
        evolvePopulation(myPop);
        int[] IndividualforTransplant = Model.getBestTrainingSolution();
        Model.printBestTrainingSolution();
        Model.printBestValidationSolution();
        
        Model.reset();
        
        // Case 1. Accelerating the training phase of other problems
        // Transplant of the best individual to another problem
        // Example to see how fast the training converges
        loadData(".//data//mc.txt");
        Population myPop2 = initializePopulation(25, IndividualforTransplant);
        evolvePopulation(myPop2);
        Model.printBestTrainingSolution();
        Model.printBestValidationSolution();
        
        // Reset the model to its initial state for a new problem
        Model.reset();
        
        // Case 2. Quick obtaining of highest value for the test
        // This time, the population is initialized with prior knowledge from a previous problem
        // Example to see how fast the optimal test can be achieved
        loadData(".//data//rg.txt", ".//data//mc.txt");
        Population myPop3 = initializePopulation(25, IndividualforTransplant);
        // Evolve the new population and print the best solutions
        evolvePopulation(myPop3);
        Model.printBestTrainingSolution();
        Model.printBestValidationSolution();
        
    }
    
    // Loads training and validation data from the same path
    private static void loadData(String path1) {
        Model.load_training(path1);
        Model.load_validation(path1);
    }
    
    // Loads training and validation data from two different paths
    private static void loadData(String path1, String path2) {
        Model.load_training(path1);
        Model.load_validation(path2);
    }

    // Initializes a population with a specified size
    private static Population initializePopulation(int size) {
        try {
            return new Population(size, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Initializes a population with a specified size and a predefined solution
    private static Population initializePopulation(int size, int[] sol) {
        try {
            Individual in = Individual.createWithGenes(sol);
            return new Population(size, in);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Evolves a given population through generations until certain criteria are met
    private static void evolvePopulation(Population myPop) {
        int generationCount = 0;
        try {
            while (isEvolutionContinuing(myPop, generationCount)) {
                generationCount++;
                System.out.println("Generation: " + generationCount);

                // Calculate and print fitness scores for training and validation
                double trainingScore = Model.calculateTraining((int[]) myPop.getFittest().vectorizeNumeric());
                System.out.println("##Train##" + trainingScore);

                double validationScore = Model.calculateValidation((int[]) myPop.getFittest().vectorizeNumeric());
                System.out.println("##Validation##" + validationScore);

                // Evolve the population to the next generation
                myPop = Algorithm.evolvePopulation(myPop);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Determines whether the evolution process should continue based on fitness and generation count
    private static boolean isEvolutionContinuing(Population myPop, int generationCount) throws Exception {
        return myPop.getFittest().getFitness() < FitnessCalc.getMaxFitness() && generationCount < 300;
    }
}