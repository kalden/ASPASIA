/**
 * \package launch
 * 
 * \brief Package containing the main method to launch ASPASIA, reading in the settings file location and calling the relevant method
 */

package launch;

import modelCreation.Create_LHC_Models;
import modelCreation.Create_Robustness_Models;
import modelCreation.Create_eFAST_Models;
import modelCreation.SBMLFileFromSolverOutput;

/**
 * \brief Launch class of the runnable jar file
 * 
 * Begins the process of sensitivity analysis experiment setup from the command
 * line
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 */
public class LaunchASPASIA
{

    /**
     * Called by the BASH script to run the sensitivity analysis protocol. The
     * argument specifies that method to run: r, l, or e
     * 
     * @param args Input arguments from script. First will be the method
     *            (r,l,e,s) second will be the location of the settings file
     */
    @SuppressWarnings("unused")
    public static void main(String[] args)
    {
	// Read in the method and settings file location
	String method = args[0];
	String settingsFileLocation = args[1];
	System.out
	        .println("**************************************************************************");
	System.out.println("ASPASIA Version 1");
	System.out
	        .println("York Computational Immunology Lab, University of York. www.york.ac.uk/ycil");
	System.out
	        .println("**************************************************************************");
	System.out.println();
	System.out.println("Reading Settings File: " + settingsFileLocation);
	System.out.println();

	if (method.equals("r"))
	{
	    // Robustness Analysis
	    System.out.println("ASPASIA Method: Robustness Analysis");
	    Create_Robustness_Models oat = new Create_Robustness_Models(settingsFileLocation);
	}
	else if (method.equals("l"))
	{
	    // Latin-Hypercube Sensitivity Analysis
	    System.out.println("ASPASIA Method: Latin-Hypercube Analysis");
	    Create_LHC_Models lhc = new Create_LHC_Models(settingsFileLocation);
	}
	else if (method.equals("e"))
	{
	    // eFAST Global Sensitivity Analysis
	    System.out.println("ASPASIA Method: eFAST Analysis");
	    Create_eFAST_Models efast = new Create_eFAST_Models(settingsFileLocation);
	}
	else if (method.equals("s"))
	{
	    // SBML Intervention from Steady State
	    System.out.println("ASPASIA Method: Intervention from Steady State");
	    SBMLFileFromSolverOutput newSBML = new SBMLFileFromSolverOutput(settingsFileLocation);
	}

    }

    /**
     * \mainpage ASPASIA Code Reference Manual
     * 
     * This toolkit has been created to assist with the process of analysing
     * simulations. Functionality is provided to achieve the following:
     * <ul>
     * <li>Creating parameter value sets and SBML Models to perform Robustness
     * Analysis
     * <li>Creating parameter value sets and SBML Models to perform Global
     * Sensitivity Analysis (Latin-Hypercube and eFAST)
     * <li>Creating parameter value sets and SBML Models where an intervention
     * is introduced after steady state
     * </ul>
     * 
     * Instructions on how each is performed can be found on the ASPASIA section
     * of the lab website.
     * 
     */

}
