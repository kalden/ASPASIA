/**
 * Runs the spartan sampling in R, without the need to do this externally
 */
package spartanLink;

/**
 * \brief Utility methods for connecting ASPASIA to the Spartan package
 * 
 * Generates parameter samples by running the specified spartan analysis. This
 * creates CSV files containing parameter samples that are later processed into
 * parameter XML files
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 *
 */
public class SpartanUtilities
{
    /**
     * Full path to the spartan file created to perform parameter sampling
     */
    public static String spartanScript;

    /**
     * Run the Spartan R script that generates the parameter samples
     */
    public static int generateParameterSamples()
    {
	try
	{
	    // Use an array to get around the possibility that the file path
	    // will have spaces
	    String[] args =
	    { "Rscript", spartanScript };
	    Process pr = Runtime.getRuntime().exec(args);

	    int code = pr.waitFor();

	    switch (code)
	    {
	    case 0:
		return 0;
	    case 1:
		System.out.println(pr.getErrorStream());
	    }

	    return 1;

	}

	catch (Exception e)
	{
	    e.printStackTrace();
	    return 1;
	}
    }
}
