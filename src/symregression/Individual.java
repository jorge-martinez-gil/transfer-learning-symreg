/*
 * Jorge Martinez-Gil,  Jose Manuel Chaves-Gonzalez: 
 * Transfer learning for semantic similarity measures based on symbolic regression.
 * J. Intell. Fuzzy Syst. 45(1): 37-49 (2023)
 *
 * @author: Jorge Martinez-Gil
 */

package symregression;

import java.util.Random;

public class Individual {

    // The default length for the genes array
    static int defaultGeneLength = 15;
    // Array to store the genes of the individual
    public int[] genes = new int[defaultGeneLength];
    // Cache for the fitness value to avoid repeated calculations
    private double fitness = 0.0;
    // Elements representing operands in a symbolic expression
    String[] elementsA = new String[] {"a", "b", "c", "d", "1", "0", "0.333", "0.666"};  
    // Elements representing operators in a symbolic expression
    String[] elementsB = new String[] {"+", "-", "*", "/", "m", "n", "p"};
    
    // Initializes an individual with random genes
    public void generateIndividual() {
        genes = new int[defaultGeneLength];
        Random randomGenerator = new Random();
        for (int i = 0; i < defaultGeneLength; i++) {
            int gene = randomGenerator.nextInt(7); // Randomly pick an index for elementsB
            genes[i] = gene;
        }
    }
    
    // Initializes an individual with predefined genes
    public void generateIndividual(int[] predefinedGenes) {
        if (predefinedGenes != null && predefinedGenes.length == defaultGeneLength) {
            System.arraycopy(predefinedGenes, 0, genes, 0, defaultGeneLength);
        } else {
            // Fallback to random gene generation if input is invalid
            generateIndividual();
        }
    }
    
    // Factory method to create an Individual with predefined genes
    public static Individual createWithGenes(int[] predefinedGenes) {
        Individual newIndividual = new Individual();
        newIndividual.generateIndividual(predefinedGenes);
        return newIndividual;
    }

    /* Getters and setters */

    // Sets a new default gene length. Useful for experiments with different gene lengths
    public static void setDefaultGeneLength(int length) {
        defaultGeneLength = length;
    }
    
    // Gets the gene at a specific index
    public int getGene(int index) {
        return genes[index];
    }

    // Sets the gene at a specific index and resets the cached fitness value
    public void setGene(int index, int value) {
        genes[index] = value;
        fitness = 0; // Fitness needs to be recalculated
    }

    /* Public methods */
    
    // Returns the size of the gene array
    public int size() {
        return genes.length;
    }

    // Gets the fitness of the individual. If not calculated before, it's calculated here
    public double getFitness() throws Exception {
        if (fitness == 0) {
            fitness = FitnessCalc.getFitness(this);
        }
        return fitness;
    }

    // Converts the individual's gene array to a string representation
    @Override
    public String toString() {
        StringBuilder geneString = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            geneString.append(getGene(i)).append("\n");
        }
        return geneString.toString();
    }
    
    // Converts the individual's genes to a symbolic expression, mixing elementsA and elementsB
    public String vectorize() {
        String[] geneString = new String[defaultGeneLength];
        StringBuilder result = new StringBuilder("0mc-b-dma-0.333n1p0"); // Starting point for the expression
        for (int i = 0; i < defaultGeneLength; i++) {
            int num = genes[i];
            // Alternate between elementsA and elementsB based on index
            geneString[i] = (i % 2 == 0) ? elementsA[num] : elementsB[num];
            result.append(geneString[i]);
        }
        return result.toString();
    }
    
    // Converts the individual's genes to a numeric array
    public int[] vectorizeNumeric() {
        return genes.clone(); // Returns a copy of the genes array
    }
}