package edu.usfca.vas.analytics;

/**
 * Created by Thomas Schweich on 2/26/2017.
 *
 * Methods for getting data from the server
 */
public interface IDataServer {

    /**
     * @param columns The titles of the columns who's values should be returned
     * @return The row which is considered to be occurring at the current time
     */
    String[] getNext(String[] columns);

    /**
     * Get rows containing values for the given columns between a given start and end within a certain column
     * @param columns The titles of the columns who's values should be returned
     * @param indexColumn The column to index by
     * @param start The starting value in the index column
     * @param end The ending value in the index column
     * @param sorted Whether or not the index column is sorted
     * @return The rows between the given start and end, compared lexically
     */
    String[][] getBetween(String[] columns, String indexColumn, String start, String end, boolean sorted);

    /**
     * Get rows containing values for the given columns who's values under compareColumn are lexically between
     * upperBound and lowerBound
     * {@code getFor({"Prices"}, "latitude", "100", "-100")}
     * Would return an array of the prices of all transactions which occurred between latitudes 100 and -100
     * @param columns The titles of the columns who's values should be returned
     * @param compareColumn The column who's values upperBound and lowerBound should be compared against
     * @param upperBound The lexical maximum of the range to be returned
     * @param lowerBound The lexical minimum of the range to be returned
     * @return The rows who's values of compareColumn are between the given upper and lower bound, compared lexically
     */
    String[][] getFor(String[] columns, String compareColumn, String upperBound, String lowerBound);


    /**
     * Get rows containing values for the given columns which satisfy the given expression
     * @param columns The titles of the columns who's values should be returned
     * @param expression The expression to evaluate
     * @return The rows which satisfy the expression
     */
    String[][] getWhere(String[] columns, String expression);
}
