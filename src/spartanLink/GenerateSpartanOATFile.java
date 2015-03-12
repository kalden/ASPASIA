/**
 * Runs the spartan sampling in R, without the need to do this externally
 */
package spartanLink;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * \brief Generates the script to perform Robustness Analysis Parameter Sampling
 * in Spartan
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 *
 */
public class GenerateSpartanOATFile
{

    /**
     * Takes the information from the XML settings file and creates an R script
     * that performs LHC sampling for the specified parameters
     * 
     * @param outputPath Where the parameter sample CSV file spartan produces
     *            should be stored
     * @param parameters ArrayList of parameters from the XML file, for which
     *            values will be created
     * @param minVals ArrayList of the minimum values of each parameter
     * @param maxVals ArrayList of the maximum values of each parameter
     * @param maxVals ArrayList of the baseline values of each parameter
     * @param increment ArrayList of the values for which sampling should be
     *            incremented, for each parameter
     */
    public static void CreateSpartanLHCScript(String outputPath, ArrayList<String> parameters,
	    ArrayList<String> minVals, ArrayList<String> maxVals, ArrayList<String> baseline,
	    ArrayList<String> increment)
    {
	try
	{
	    PrintWriter writer = new PrintWriter(outputPath + "/OAT_Sampling.R", "UTF-8");
	    writer.println("library(spartan)");

	    // Write the filepath
	    writer.println("FILEPATH<-\"" + outputPath + "\"");

	    // Now add the Parameters
	    String parameterLine = "PARAMETERS<-c(";
	    String minLine = "PMIN<-c(";
	    String maxLine = "PMAX<-c(";
	    String baselineLine = "BASELINE<-c(";
	    String incLine = "PINC<-c(";

	    for (int param = 0; param < parameters.size() - 1; param++)
	    {
		parameterLine = parameterLine + "\"" + parameters.get(param) + "\",";
		minLine = minLine + "" + minVals.get(param) + ",";
		maxLine = maxLine + "" + maxVals.get(param) + ",";
		baselineLine = baselineLine + "" + baseline.get(param) + ",";
		incLine = incLine + "" + increment.get(param) + ",";
	    }
	    // Now add the final parameter
	    parameterLine = parameterLine + "\"" + parameters.get(parameters.size() - 1) + "\")";
	    minLine = minLine + "" + minVals.get(parameters.size() - 1) + ")";
	    maxLine = maxLine + "" + maxVals.get(parameters.size() - 1) + ")";
	    baselineLine = baselineLine + "" + baseline.get(parameters.size() - 1) + ")";
	    incLine = incLine + "" + increment.get(parameters.size() - 1) + ")";

	    writer.println(parameterLine);
	    writer.println(minLine);
	    writer.println(maxLine);
	    writer.println(baselineLine);
	    writer.println(incLine);

	    // Write the call
	    writer.println("oat_parameter_sampling(FILEPATH,PARAMETERS,BASELINE,PMIN,PMAX,PINC)");

	    writer.close();

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }
}
