/*
 * Jorge Martinez-Gil,  Jose Manuel Chaves-Gonzalez: 
 * Transfer learning for semantic similarity measures based on symbolic regression.
 * J. Intell. Fuzzy Syst. 45(1): 37-49 (2023)
 *
 * @author: Jorge Martinez-Gil
 */

package symregression;

public class FitnessCalc {

    // Calculates the fitness of an individual. The fitness is determined by how well
    // the individual's genes match up to the optimal solution represented by the model's training function.
    static double getFitness(Individual individual) throws Exception {
        double fitness = 0;
        
        // Create a solution array to store the individual's genes
        int[] sol = new int[15];
        
        // Copy genes from the individual to the solution array
        for (int i = 0; i < 15; i++) {        	
        	sol[i] = individual.getGene(i);
        }
        
        // Calculate the fitness of the individual based on the training model
        // The fitness is typically a measure of how close the individual is to an optimal solution
        fitness = Model.calculateTraining(sol);
        
        // Uncomment to debug: Print the solution and its fitness value
        // System.out.println(toString(sol) + "->" + fitness);
        // System.out.println(individual.vectorize() + "->" + fitness);
        
        return fitness;
    }
    
    // Converts an integer array to a string representation. Useful for debugging purposes.
    public static String toString(int[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        // Use StringBuilder for efficient string concatenation
        StringBuilder b = new StringBuilder();
        b.append('[');
        // Append each element of the array to the StringBuilder
        for (int i = 0; ; i++) {
            b.append(a[i]);
            // If this is the last element, close the bracket and return the string
            if (i == iMax)
                return b.append(']').toString();
            // Otherwise, add a comma and space before the next element
            b.append(", ");
        }
    }
    
    // Returns the maximum fitness value. This is a theoretical value that represents the best possible fitness.
    // In a real scenario, this value might be the best known solution or an arbitrarily high value indicating
    // that the higher the fitness, the better.
    static int getMaxFitness() {
        int maxFitness = 99999999; // Arbitrary large value as a placeholder for maximum fitness
        return maxFitness;
    }
}
