/*
 * Jorge Martinez-Gil,  Jose Manuel Chaves-Gonzalez: 
 * Transfer learning for semantic similarity measures based on symbolic regression.
 * J. Intell. Fuzzy Syst. 45(1): 37-49 (2023)
 *
 * @author: Jorge Martinez-Gil
 */

package symregression;

public class Population {

    // Array to store individuals in the population
    Individual[] individuals;

    /*
     * Constructors
     */

    // Constructor to create a population with a given size.
    // If initialise is true, each individual in the population is generated and initialized.
    public Population(int populationSize, boolean initialise) throws Exception {
        individuals = new Individual[populationSize];
        // Initialise population if required
        if (initialise) {
            // Loop through the population array and create new individuals
            for (int i = 0; i < size(); i++) {
                Individual newIndividual = new Individual();
                newIndividual.generateIndividual(); // Generates the attributes of an individual
                saveIndividual(i, newIndividual); // Store the new individual in the population
            }
        }
    }
    
    // Constructor to create a population with a predefined individual at the first position.
    // The rest of the population is filled with newly generated individuals.
    public Population(int populationSize, Individual predefinedIndividual) throws Exception {
        individuals = new Individual[populationSize];
        
        // Check if a predefined individual is provided
        if (predefinedIndividual != null) {
            individuals[0] = predefinedIndividual; // Set the first individual as the predefined one
            // Generate new individuals for the rest of the population
            for (int i = 1; i < populationSize; i++) {
                Individual newIndividual = new Individual();
                newIndividual.generateIndividual(); // Generates the attributes of an individual
                individuals[i] = newIndividual; // Store the new individual in the population
            }
        }
    }
    
    /* Getters */

    // Get an individual from the population by index
    public Individual getIndividual(int index) {
        return individuals[index];
    }

    // Get the fittest individual from the population based on the fitness value
    public Individual getFittest() throws Exception {
        Individual fittest = individuals[0];
        // Loop through the population to find the individual with the highest fitness
        for (int i = 0; i < size(); i++) {
            if (fittest.getFitness() <= getIndividual(i).getFitness()) {
                fittest = getIndividual(i);
            }
        }
        
        return fittest;
    }

    /* Public methods */

    // Get the size of the population
    public int size() {
        return individuals.length;
    }

    // Save an individual at a specific index in the population array
    public void saveIndividual(int index, Individual indiv) throws Exception {
        individuals[index] = indiv;
    }
}

