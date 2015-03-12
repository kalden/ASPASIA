/**
 * Runs the spartan sampling in R, without the need to do this externally
 */
package spartanLink;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * \brief Generates the script to perform Latin-Hypercube Parameter Sampling in
 * Spartan
 * 
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 *
 */
public class GenerateSpartanLHCFile
{

    /**
     * Takes the information from the XML settings file and creates an R script
     * that performs LHC sampling for the specified parameters
     * 
     * @param outputPath Where the parameter sample CSV file spartan produces
     *            should be stored
     * @param parameters ArrayList of parameters from the XML file, for which
     *            values will be created
     * @param numSamples The number of LHC samples to perform
     * @param minVals ArrayList of the minimum values of each parameter
     * @param maxVals ArrayList of the maximum values of each parameter
     * @param algorithm LHC algorithm to use - can be normal or optimal
     */
    public static void CreateSpartanLHCScript(String outputPath, ArrayList<String> parameters,
	    int numSamples, ArrayList<String> minVals, ArrayList<String> maxVals, String algorithm)
    {
	try
	{
	    PrintWriter writer = new PrintWriter(outputPath + "/LHC_Sampling.R", "UTF-8");
	    writer.println("library(spartan)");
	    writer.println("library(lhs)");

	    // Write the filepath
	    writer.println("FILEPATH<-\"" + outputPath + "\"");

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

	    // Number of samples to generate
	    writer.println("NUMSAMPLES<-" + numSamples);

	    // Algorithm to use
	    writer.println("ALGORITHM<-\"" + algorithm + "\"");

	    // Write the call
	    writer.println("lhc_generate_lhc_sample(FILEPATH,PARAMETERS,NUMSAMPLES,PMIN,PMAX,ALGORITHM)");

	    writer.close();

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }

    public static void main(String[] args)
    {
	String outputPath = "/home/kieran/Desktop/";
	ArrayList<String> p = new ArrayList<String>();
	p.add("SpleenTCellArr");
	p.add("SpleenTCellArr2");
	ArrayList<String> min = new ArrayList<String>();
	ArrayList<String> max = new ArrayList<String>();
	min.add("0.01");
	min.add("0.01");
	max.add("0.1");
	max.add("0.4");

	GenerateSpartanLHCFile.CreateSpartanLHCScript(outputPath, p, 200, min, max, "normal");
    }
}
