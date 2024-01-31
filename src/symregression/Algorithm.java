/*
 * Jorge Martinez-Gil,  Jose Manuel Chaves-Gonzalez: 
 * Transfer learning for semantic similarity measures based on symbolic regression.
 * J. Intell. Fuzzy Syst. 45(1): 37-49 (2023)
 *
 * @author: Jorge Martinez-Gil
 */

package symregression;

import java.math.BigDecimal;
import java.util.Random;

public class Algorithm {

    /* GA parameters */
    // Probability of using the same gene from one of the parents
    private static final double uniformRate = 0.7;
    // Probability of mutating a gene
    private static final double mutationRate = 0.2;
    // Number of individuals selected for tournament selection
    private static final int tournamentSize = 5;
    // Determines whether the best individual is passed on to the next generation
    private static final boolean elitism = true;

    /* Public methods */

    // Evolves a given population over one generation
    public static Population evolvePopulation(Population pop) throws Exception {
        Population newPopulation = new Population(pop.size(), false);

        // If elitism is enabled, keep the best individual without changes
        if (elitism) {
            newPopulation.saveIndividual(0, pop.getFittest());
        }

        // Determine the starting point for crossover operations based on elitism
        int elitismOffset = elitism ? 1 : 0;

        // Apply crossover to the rest of the population
        for (int i = elitismOffset; i < pop.size(); i++) {
            Individual indiv1 = tournamentSelection(pop);
            Individual indiv2 = tournamentSelection(pop);
            Individual newIndiv = crossover(indiv1, indiv2);
            newPopulation.saveIndividual(i, newIndiv);
        }

        // Mutate the new population to introduce genetic diversity
        for (int i = elitismOffset; i < newPopulation.size(); i++) {
            mutate(newPopulation.getIndividual(i));
        }

        return newPopulation;
    }
    
    // Overloaded method to evolve a population with a predefined individual
    public static Population evolvePopulation(Population pop, Individual predefinedIndividual) throws Exception {
        Population newPopulation = new Population(pop.size(), predefinedIndividual);

        // Keep the best individual if elitism is enabled
        if (elitism) {
            newPopulation.saveIndividual(0, pop.getFittest());
        }

        // Apply crossover and mutation as in the original evolvePopulation method
        int elitismOffset = elitism ? 1 : 0;
        for (int i = elitismOffset; i < pop.size(); i++) {
            Individual indiv1 = tournamentSelection(pop);
            Individual indiv2 = tournamentSelection(pop);
            Individual newIndiv = crossover(indiv1, indiv2);
            newPopulation.saveIndividual(i, newIndiv);
        }

        for (int i = elitismOffset; i < newPopulation.size(); i++) {
            mutate(newPopulation.getIndividual(i));
        }

        return newPopulation;
    }

    // Combines two individuals to produce a new offspring
    private static Individual crossover(Individual indiv1, Individual indiv2) {
        Individual newSol = new Individual();
        // Loop through genes and decide from which parent to inherit each gene
        for (int i = 0; i < 15; i++) {
            if (Math.random() <= uniformRate) {
                newSol.setGene(i, indiv1.getGene(i));
            } else {
                newSol.setGene(i, indiv2.getGene(i));
            }
        }
        return newSol;
    }
    
    // Truncates a double to a specified number of decimal places
    @SuppressWarnings("deprecation")
	public static BigDecimal truncateDecimal(double x, int numberofDecimals) {
        if (x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
        }
    }

    // Randomly mutates an individual's genes to introduce variation
    private static void mutate(Individual indiv) {
        Random randomGenerator = new Random();
        for (int i = 0; i < 15; i++) {
            if (Math.random() <= mutationRate) {
                // Generate a random gene and replace the current one
                int gene = randomGenerator.nextInt(7);
                indiv.setGene(i, gene);
            }
        }
    }
    
    // Selects individuals for crossover using tournament selection
    private static Individual tournamentSelection(Population pop) throws Exception {
        // Create a temporary population for the tournament
        Population tournament = new Population(tournamentSize, false);
        // Fill the tournament with random individuals from the population
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.size());
            tournament.saveIndividual(i, pop.getIndividual(randomId));
        }
        // Return the fittest individual from the tournament
        Individual fittest = tournament.getFittest();
        return fittest;
    }
}

