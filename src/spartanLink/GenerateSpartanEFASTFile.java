/**
 * \package spartanLink 
 * \brief Runs the spartan sampling in R, without the need to do this externally
 */
package spartanLink;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * \brief Generates the script to perform eFAST Parameter Sampling in Spartan
 * 
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 *
 */
public class GenerateSpartanEFASTFile
{

    /**
     * Takes the information from the XML settings file and creates an R script
     * that performs EFAST sampling for the specified parameters
     * 
     * @param outputPath Where the parameter sample CSV file spartan produces
     *            should be stored
     * @param parameters ArrayList of parameters from the XML file, for which
     *            values will be created
     * @param numCurves The number of EFAST Curves to perform
     * @param numSamples The number of samples to take from each curve
     * @param minVals ArrayList of the minimum values of each parameter
     * @param maxVals ArrayList of the maximum values of each parameter
     */
    public static void CreateSpartanLHCScript(String outputPath, ArrayList<String> parameters,
	    int numCurves, int numSamples, ArrayList<String> minVals, ArrayList<String> maxVals)
    {
	try
	{
	    PrintWriter writer = new PrintWriter(outputPath + "/EFAST_Sampling.R", "UTF-8");
	    writer.println("library(spartan)");

	    // Write the filepath
	    String OS = System.getProperty("os.name").toLowerCase();
	    if (OS.indexOf("win") >= 0)
	    {
		String winOutputPath = outputPath.replace("\\", "/");
		writer.println("FILEPATH<-\"" + winOutputPath + "\"");
	    }
	    else
	    {
		// Not Windows
		writer.println("FILEPATH<-\"" + outputPath + "\"");
	    }

	    writer.println("NUMCURVES<-" + numCurves);
	    writer.println("NUMSAMPLES<-" + numSamples);

	    // Now add the Parameters
	    String parameterLine = "PARAMETERS<-c(";
	    String minLine = "PMIN<-c(";
	    String maxLine = "PMAX<-c(";

	    for (int param = 0; param < parameters.size() - 1; param++)
	    {
		parameterLine = parameterLine + "\"" + parameters.get(param) + "\",";
		minLine = minLine + "" + minVals.get(param) + ",";
		maxLine = maxLine + "" + maxVals.get(param) + ",";
	    }
	    // Now add the final parameter
	    parameterLine = parameterLine + "\"" + parameters.get(parameters.size() - 1) + "\")";
	    minLine = minLine + "" + minVals.get(parameters.size() - 1) + ")";
	    maxLine = maxLine + "" + maxVals.get(parameters.size() - 1) + ")";

	    writer.println(parameterLine);
	    writer.println(minLine);
	    writer.println(maxLine);

	    // Write the call
	    writer.println("efast_generate_sample(FILEPATH,NUMCURVES,NUMSAMPLES,PARAMETERS,PMIN,PMAX)");

	    writer.close();

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }
}
