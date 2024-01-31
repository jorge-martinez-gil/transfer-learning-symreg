/*
 * Jorge Martinez-Gil,  Jose Manuel Chaves-Gonzalez: 
 * Transfer learning for semantic similarity measures based on symbolic regression.
 * J. Intell. Fuzzy Syst. 45(1): 37-49 (2023)
 *
 * @author: Jorge Martinez-Gil
 */
package symregression;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Model {
    
    // Lists to hold training and validation data
    private static ArrayList<Double[]> data_training;
    private static ArrayList<Double[]> data_validation;
    
    // Arrays to hold source data for training and validation
    static double[] source_validation = null;
    static double[] source_training = null;
    
    // Arrays to hold target outcomes for training and validation
    static double[] target_validation = null;
    static double[] target_training = null;
    
    // Symbolic representation of variables and constants
    static String[] elementsA = new String[] {"a", "b", "c", "d", "1", "0", "0.333", "0.666"}; 
    // Symbolic representation of operations
    static String[] elementsB = new String[] {"+", "-", "*", "/", "m", "n", "p"};
    
    // Default value for expressions that cannot be evaluated
    static double DEFAULT = 0.5d;
    
    // Best fitness values achieved during training and validation
    private static double bestTrainingValue = Double.NEGATIVE_INFINITY;
    private static double bestValidationValue = Double.NEGATIVE_INFINITY;
    // Solutions yielding the best fitness values during training and validation
    private static int[] bestTrainingSolution = null;
    private static int[] bestValidationSolution = null;

    // Interface for expressions, allowing evaluation to a BigRational result
    public interface Expression {
        BigRational eval();
    }

    // Enum for handling parentheses, specifically for left parentheses
    public enum Parentheses { LEFT }
 
    // Enum for binary operators with associated symbols and precedence levels
    public enum BinaryOperator {
        ADD('+', 4), SUB('-', 4), MUL('*', 3), DIV('/', 3),
        MAX('m', 1), MIN('n', 1), POW('p', 2);
 
        public final char symbol;
        public final int precedence;
 
        BinaryOperator(char symbol, int precedence) {
            this.symbol = symbol;
            this.precedence = precedence;
        }
 
        // Evaluate the operation between two BigRational values
        public BigRational eval(BigRational leftValue, BigRational rightValue) {
            switch (this) {
                case ADD: return leftValue.add(rightValue);
                case SUB: return leftValue.subtract(rightValue);
                case MUL: return leftValue.multiply(rightValue);
                case DIV: return leftValue.divide(rightValue);
                case MAX: return leftValue.max(rightValue);  
                case MIN: return leftValue.min(rightValue); 
                case POW: return leftValue.pow(rightValue.intValue());
                default: throw new IllegalStateException("Unexpected operator: " + this);
            }
        }
 
        // Get the BinaryOperator enum based on the input symbol
        public static BinaryOperator forSymbol(char symbol) {
            for (BinaryOperator operator : values()) {
                if (operator.symbol == symbol) {
                    return operator;
                }
            }
            throw new IllegalArgumentException("Invalid operator symbol: " + symbol);
        }
    }
 
    // Represents a numeric value in the expression
    public static class Number implements Expression {
        private final BigRational number;
 
        public Number(BigRational number) {
            this.number = number;
        }
 
        @Override
        public BigRational eval() {
            return number;
        }
 
        @Override
        public String toString() {
            return number.toString();
        }
    }
 
    // Represents a binary operation in the expression
    public static class BinaryExpression implements Expression {
        public final Expression leftOperand;
        public final BinaryOperator operator;
        public final Expression rightOperand;
 
        public BinaryExpression(Expression leftOperand, BinaryOperator operator, Expression rightOperand) {
            this.leftOperand = leftOperand;
            this.operator = operator;
            this.rightOperand = rightOperand;
        }
 
        @Override
        public BigRational eval() {
            return operator.eval(leftOperand.eval(), rightOperand.eval());
        }
 
        @Override
        public String toString() {
            return "(" + leftOperand + " " + operator.symbol + " " + rightOperand + ")";
        }
    }
 
    // Helper method to handle the creation of new operands during parsing
    private static void createNewOperand(BinaryOperator operator, Stack<Expression> operands) {
        Expression rightOperand = operands.pop();
        Expression leftOperand = operands.pop();
        operands.push(new BinaryExpression(leftOperand, operator, rightOperand));
    }
 
    // Expression parse
    public static Expression parse(String input) {
        int curIndex = 0; // Current index in the input string
        boolean afterOperand = false; // Flag to indicate if we're after an operand in the expression
        Stack<Expression> operands = new Stack<>(); // Stack to hold operands as we parse
        Stack<Object> operators = new Stack<>(); // Stack to hold operators, including parentheses

        while (curIndex < input.length()) {
            int startIndex = curIndex; // Start index for numbers in the input
            char c = input.charAt(curIndex++); // Current character being parsed

            // Skip whitespace characters
            if (Character.isWhitespace(c)) continue;

            // Handling characters after an operand
            if (afterOperand) {
                // If closing parenthesis, pop operators and create expressions until matching '('
                if (c == ')') {
                    Object operator;
                    while (!operators.isEmpty() && ((operator = operators.pop()) != Parentheses.LEFT))
                        createNewOperand((BinaryOperator) operator, operands);
                    continue;
                }
                afterOperand = false;
                // Get the binary operator corresponding to the current character
                BinaryOperator operator = BinaryOperator.forSymbol(c);
                // Pop operators with equal or higher precedence and create expressions
                while (!operators.isEmpty() && (operators.peek() != Parentheses.LEFT) &&
                       (((BinaryOperator) operators.peek()).precedence >= operator.precedence))
                    createNewOperand((BinaryOperator) operators.pop(), operands);
                operators.push(operator); // Push the current operator onto the stack
                continue;
            }

            // Handling opening parentheses
            if (c == '(') {
                operators.push(Parentheses.LEFT);
                continue;
            }

            // We're after an operand now, parse the number
            afterOperand = true;
            while (curIndex < input.length()) {
                c = input.charAt(curIndex);
                // Break if the current character is not part of a number
                if (((c < '0') || (c > '9')) && (c != '.')) break;
                curIndex++;
            }
            // Push the parsed number as an operand onto the stack
            operands.push(new Number(BigRational.valueOf(input.substring(startIndex, curIndex))));
        }

        // At the end, pop remaining operators and create expressions
        while (!operators.isEmpty()) {
            Object operator = operators.pop();
            if (operator == Parentheses.LEFT) throw new IllegalArgumentException("Mismatched parentheses");
            createNewOperand((BinaryOperator) operator, operands);
        }

        // The final expression is the last remaining operand, should be the only one if well-formed
        Expression expression = operands.pop();
        if (!operands.isEmpty()) throw new IllegalArgumentException("Malformed expression");
        return expression;
    }

    
    /** 
     * Compute the Pearson Correlation Coefficient between two data sets.
     * @param scores1 The first data set.
     * @param scores2 The second data set, must be the same length as scores1.
     * @return The Pearson Correlation Coefficient between scores1 and scores2.
     */
    public static double getPearson(double[] scores1, double[] scores2) {
        double result = 0;
        double sum_sq_x = 0;
        double sum_sq_y = 0;
        double sum_coproduct = 0;
        double mean_x = scores1[0];
        double mean_y = scores2[0];

        for (int i = 2; i < scores1.length + 1; i++) {
            double sweep = (double) (i - 1) / i;
            double delta_x = scores1[i - 1] - mean_x;
            double delta_y = scores2[i - 1] - mean_y;
            sum_sq_x += delta_x * delta_x * sweep;
            sum_sq_y += delta_y * delta_y * sweep;
            sum_coproduct += delta_x * delta_y * sweep;
            mean_x += delta_x / i;
            mean_y += delta_y / i;
        }

        double pop_sd_x = Math.sqrt(sum_sq_x / scores1.length);
        double pop_sd_y = Math.sqrt(sum_sq_y / scores2.length);
        double cov_x_y = sum_coproduct / scores1.length;
        result = cov_x_y / (pop_sd_x * pop_sd_y);

        if (Double.isNaN(result) || Double.isInfinite(result)) result = 0;

        return result;
    }


    /**
     * Load validation data from a file.
     * @param filename The path to the file containing validation data.
     */
    public static void load_validation(final String filename) { 
        try {
            data_validation = new ArrayList<Double[]>();
            FileInputStream stream = new FileInputStream(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String dat;

            while ((dat = reader.readLine()) != null) {
                if (dat.trim().length() == 0) continue;
                String[] line = dat.split(",");
                if (line.length == 8 || line.length == 9) {
                    try {
                        Double[] g = new Double[6];
                        for (int i = 0; i < 6; i++) {
                            g[i] = Double.parseDouble(line[i]);
                        }
                        data_validation.add(g);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format in validation data: " + e.getMessage());
                    }
                }
            }
            System.out.println("INFO: Validation data loaded from: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        } 

        int dim = data_validation.size();
        source_validation = new double[dim];
        for (int a = 0; a < dim; a++) {
            source_validation[a] = data_validation.get(a)[0];
        }
    }

	
    /**
     * Load training data from a file.
     * @param filename The path to the file containing training data.
     */
    public static void load_training(final String filename) { 
        try {
            data_training = new ArrayList<Double[]>();
            FileInputStream stream = new FileInputStream(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String dat;

            while ((dat = reader.readLine()) != null) {
                if (dat.trim().length() == 0) continue;
                String[] line = dat.split(",");
                if (line.length == 8 || line.length == 9) {
                    try {
                        Double[] g = new Double[6];
                        for (int i = 0; i < 6; i++) {
                            g[i] = Double.parseDouble(line[i]);
                        }
                        data_training.add(g);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format in training data: " + e.getMessage());
                    }
                }
            }
            System.out.println("INFO: Training data loaded from: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int dim = data_training.size();
        source_training = new double[dim];
        for (int a = 0; a < dim; a++) {
            source_training[a] = data_training.get(a)[0];
        }
    }

 
   public static void main(String[] args) {
	   
	  /* String[] testExpressions = {
                "2p3",
                "2p0.5",
                "2*3-4",
                "2*(3+4)+5/6",
                "2 * (3 + (4 * 5 + (6 * 7) * 8) - 9) * 10",
                "2*-3--4+-.35"};
        for (String testExpression : testExpressions) {
            Expression expression = parse(testExpression);
            System.out.printf("Input: \"%s\", AST: \"%s\", value=%s%n", testExpression, expression, expression.eval());
        }*/
    		
    }
   
	@SuppressWarnings("null")
	private static double getSpearman(double [] X, double [] Y) {
	        /* Error check */
	        if (X == null || Y == null || X.length != Y.length) {
	            return (Double) null;
	        }
	        
	        /* Create Rank arrays */
	        int [] rankX = getRanks(X);
	        int [] rankY = getRanks(Y);

	        /* Apply Spearman's formula */
	        int n = X.length;
	        double numerator = 0;
	        for (int i = 0; i < n; i++) {
	            numerator += Math.pow((rankX[i] - rankY[i]), 2);
	        }
	        numerator *= 6;
	        return 1 - numerator / (n * ((n * n) - 1));
	    }
	    
	    /* Returns a new (parallel) array of ranks. Assumes unique array values */
	    public static int[] getRanks(double [] array) {
	        int n = array.length;
	        
	        /* Create Pair[] and sort by values */
	        Pair [] pair = new Pair[n];
	        for (int i = 0; i < n; i++) {
	            pair[i] = new Pair(i, array[i]);
	        }
	        Arrays.sort(pair, new PairValueComparator());

	        /* Create and return ranks[] */
	        int [] ranks = new int[n];
	        int rank = 1;
	        for (Pair p : pair) {
	            ranks[p.index] = rank++;
	        }
	        return ranks;
	    }
	    
	    
	    
	    private static int maximo (double a, double b, double c, double d) {
	    	
	    	if (a >= b && a >= c && a >= d)
	    		return 0;
	    	
	    	if (b >= a && b >= c && b >= d)
	    		return 1;
	    	
	    	if (c >= a && c >= b && c >= d)
	    		return 2;
	    	
	    	return 3;
	    	
	    }
	    
	    


      
    public static double[] calculateTarget (int[] sol) {
    	
    	//elementsA[7] = String.valueOf(sol[14]);
    	
            /*for (int i = 0; i < 15; i++) {
        	
        	if (i % 2 == 0 && sol[i] > 8)
        		sol[i] = 0;
        
        	if (i % 2 == 1 && sol[i] > 7)
        		sol[i] = 0;
        	
        }*/
    	
        target_validation = new double [data_validation.size()];  
        Expression expression = null;
                   
        double fitness = 0;
        String cadena = null;
		        						
		  try {
		        							
		        for (int a = 0; a < data_validation.size(); a++) {
		        	elementsA[0] = String.valueOf(data_validation.get(a)[1]);
		        	elementsA[1] = String.valueOf(data_validation.get(a)[2]);
		        	elementsA[2] = String.valueOf(data_validation.get(a)[3]);
		        	elementsA[3] = String.valueOf(data_validation.get(a)[4]);
		        	//elementsA[4] = String.valueOf(data_validation.get(a)[5]);

			        cadena = elementsA[sol[0]] + elementsB[sol[1]] + elementsA[sol[2]] + elementsB[sol[3]] + elementsA[sol[4]] + elementsB[sol[5]] + elementsA[sol[6]] + elementsB[sol[7]] + elementsA[sol[8]]+
			        		elementsB[sol[9]] + elementsA[sol[10]] + elementsB[sol[11]] + elementsA[sol[12]] + elementsA[sol[13]] + elementsA[sol[14]];
			        	
			        
			        try {
			        	expression = parse(cadena);
			        	fitness = Double.valueOf(expression.eval().doubleValue());
			        } catch (Exception e) {
			        	fitness = DEFAULT;
			        }
			        
			        if (Double.valueOf(fitness).isNaN() || Double.valueOf(fitness).isInfinite())
			        	fitness = DEFAULT;
			        
		        	
		        	target_validation[a] =  Double.valueOf(fitness);
		        	
		        	//System.out.println (elementsA[0] + " " + elementsA[1] + " "+ elementsA[2] + " " + elementsA[3] + " "  + target_validation[a]);
		        	
		        }
		        							
		        							
		  } catch (Exception e) {
		       e.printStackTrace();
		  }
		  
		  
		  for (int a = 0; a < data_validation.size(); a++) {
			  
		  }
		  
		  
    	 return (target_validation);
    	
    }
    
    
    public static double calculateTraining (int[] sol) {
    	
    	//
    	
    	//load_training ("C:\\temp\\mc-training.txt");
    	
        for (int i = 0; i < 15; i++) {
        	
        	if (i % 2 == 0 && sol[i] > 8)
        		sol[i] = 0;
        
        	if (i % 2 == 1 && sol[i] > 7)
        		sol[i] = 0;
        	
        }
        
        //elementsA[7] = String.valueOf(sol[14]);
    	
 
        target_training = new double [data_training.size()];
        
        double fitness = 0;
        String cadena = null;
        double currentTrainingValue = 0;
		        						
		  try {
		        							
		        for (int a = 0; a < data_training.size(); a++) {
		        	elementsA[0] = String.valueOf(data_training.get(a)[1]);
		        	elementsA[1] = String.valueOf(data_training.get(a)[2]);
		        	elementsA[2] = String.valueOf(data_training.get(a)[3]);
		        	elementsA[3] = String.valueOf(data_training.get(a)[4]);
		        	//elementsA[4] = String.valueOf(data_training.get(a)[5]);

			        cadena = elementsA[sol[0]] + elementsB[sol[1]] + elementsA[sol[2]] + elementsB[sol[3]] + elementsA[sol[4]] + elementsB[sol[5]] + elementsA[sol[6]] + elementsB[sol[7]] + elementsA[sol[8]]+
			        		elementsB[sol[9]] + elementsA[sol[10]] + elementsB[sol[11]] + elementsA[sol[12]] + elementsA[sol[13]] + elementsA[sol[14]];
			        
			        Expression expression = null;
			        
			        try {
			        	expression = parse(cadena);
			        	fitness = Double.valueOf(expression.eval().doubleValue());
			        } catch (Exception e) {
			        	fitness = DEFAULT;
			        }
			        
			        if (Double.valueOf(fitness).isNaN() || Double.valueOf(fitness).isInfinite())
			        	fitness = DEFAULT;
			        
		        	target_training[a] =  Double.valueOf(fitness);
		        }
		        							
		       // number = Double.valueOf(getPearson(source_training, target_training));
		       //number = Double.valueOf(getSpearman(source_training, target_training));
		       // number = Double.valueOf(getValid(source_training, target_training));
		       
		        currentTrainingValue = Double.valueOf(getPearson(source_training, target_training));
		        // Update bestTrainingValue if the current value is better
		        if (currentTrainingValue > bestTrainingValue) {
		            bestTrainingValue = currentTrainingValue;
		            bestTrainingSolution = sol.clone(); // Cloning to avoid reference issues
		        }
		        
		        							
		  } catch (Exception e) {
		       e.printStackTrace();
		  }	
		        					
		return (currentTrainingValue);
    
    
    }
    
    

    
    public static double calculateValidation (int[] sol) {
    	
    	//elementsA[7] = String.valueOf(sol[14]);
    	//load_validation ("C:\\temp\\mc-validation.txt");
    	//System.out.println (data_validation.size());
    	
    	target_validation = new double [data_validation.size()];
    	
    	double currentValidationValue = 0;
        double fitness = 0;
        String cadena = null;
		
        
        
		  try {
		        							
		        for (int a = 0; a < data_validation.size(); a++) {
		        	
		        	elementsA[0] = String.valueOf(data_validation.get(a)[1]);
		        	elementsA[1] = String.valueOf(data_validation.get(a)[2]);
		        	elementsA[2] = String.valueOf(data_validation.get(a)[3]);
		        	elementsA[3] = String.valueOf(data_validation.get(a)[4]);
		        	//elementsA[4] = String.valueOf(data_validation.get(a)[5]);

			        cadena = elementsA[sol[0]] + elementsB[sol[1]] + elementsA[sol[2]] + elementsB[sol[3]] + elementsA[sol[4]] + elementsB[sol[5]] + elementsA[sol[6]] + elementsB[sol[7]] + elementsA[sol[8]]+
			        		elementsB[sol[9]] + elementsA[sol[10]] + elementsB[sol[11]] + elementsA[sol[12]] + elementsA[sol[13]] + elementsA[sol[14]];
		        	
			        		        
			        Expression expression = null;
			        
			        try {
			        	expression = parse(cadena);
			        	fitness = Double.valueOf(expression.eval().doubleValue());
			        } catch (Exception e) {
			        	fitness = DEFAULT;
			        }
			        
			        if (Double.valueOf(fitness).isNaN() || Double.valueOf(fitness).isInfinite())
			        	fitness = DEFAULT;
			        
		        	target_validation[a] =  Double.valueOf(fitness);
		        	
		        }
		        							
		        //number = Double.valueOf(getPearson(source_validation, target_validation));
		        //number = Double.valueOf(getSpearman(source_validation, target_validation));
		        //number = Double.valueOf(getValid(source_training, target_training));
		        
		        currentValidationValue = Double.valueOf(getPearson(source_validation, target_validation));
		        // Update bestValidationValue if the current value is better
		        if (currentValidationValue > bestValidationValue) {
		            bestValidationValue = currentValidationValue;
		            bestValidationSolution = sol.clone(); // Cloning to avoid reference issues
		        }
		        
		        return currentValidationValue;
		 
		        							
		  } catch (Exception e) {
		       e.printStackTrace();
		  }	
		  
		  return (currentValidationValue);
    	
    	
    }
    
    
    // Getter methods for best values and best solutions
    public static double getBestTrainingValue() {
        return bestTrainingValue;
    }

    public static int[] getBestTrainingSolution() {
        return bestTrainingSolution;
    }

    public static double getBestValidationValue() {
        return bestValidationValue;
    }

    public static int[] getBestValidationSolution() {
        return bestValidationSolution;
    }
    
    // Method to print the best training solution
    public static void printBestTrainingSolution() {
        if (bestTrainingSolution != null) {
            //System.out.println("Best Training Solution: " + Arrays.toString(bestTrainingSolution));
            System.out.println("Best Training Value: " + bestTrainingValue);
        } else {
            System.out.println("No training solution has been calculated yet.");
        }
    }

    // Method to print the best validation solution
    public static void printBestValidationSolution() {
        if (bestValidationSolution != null) {
            //System.out.println("Best Validation Solution: " + Arrays.toString(bestValidationSolution));
            System.out.println("Best Validation Value: " + bestValidationValue);
        } else {
            System.out.println("No validation solution has been calculated yet.");
        }
    }
    
    
    public static void reset() {
        data_training = new ArrayList<Double[]>();
        data_validation = new ArrayList<Double[]>();

        source_validation = null;
        source_training = null;

        target_validation = null;
        target_training = null;

        bestTrainingValue = Double.NEGATIVE_INFINITY;
        bestValidationValue = Double.NEGATIVE_INFINITY;
        bestTrainingSolution = null;
        bestValidationSolution = null;

        // Reset other necessary fields or data structures
    }
    
    
    
    
}
