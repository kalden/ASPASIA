/**
 * \package modelCreation
 * 
 * Package containing utilities to process parameter value sets generated by spartan, creating simulation parameter scripts for each parameter value set, or to introduce interventions into an SBML Model
 */
package modelCreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

import org.apache.commons.io.input.ReversedLinesFileReader;

import xmlFileUtilities.FileCreationUtilities;
import xmlFileUtilities.XMLFileUtilities;

/**
 * \brief Takes output from an SBML Solver and uses this to create a new SBML
 * Model.
 * 
 * Also capable of then changing the value of some of these responses to
 * introduce an intervention. Then outputs the new SBML Model file
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 *
 */
public class SBMLFileFromSolverOutput
{
    /**
     * Full path to the results file to read in, from an SBML simulation
     */
    public String sbmlRunResultsFile;

    /**
     * Name to give the parameter file produced from the results file
     */
    public String newParamFileName;

    /**
     * Folder where the new parameter file should be stored
     */
    public String paramFileOutputFolder;

    /**
     * Path to full calibrated parameter file
     */
    public String paramsMetaDataFilePath;

    /**
     * Map of parameter/species name with the value from the results file
     */
    public Map<String, Double> parameterValMap;

    /**
     * Constructor - reads in the values from the settings file then creates the
     * new SBML model from Solver output, including the intervention
     * 
     * @param settingsFile Full path to the location of the XML settings file
     */
    public SBMLFileFromSolverOutput(String settingsFile)
    {
	// Read settings file
	XMLFileUtilities.readSettingsFile(settingsFile);

	// Get the path to the parameter files
	this.sbmlRunResultsFile = XMLFileUtilities.getParam("sbmlRunResultsFile");

	// Folder where you want adapted parameter files to be stored
	this.paramFileOutputFolder = XMLFileUtilities.getParam("parameterFileOutputFolder");

	// Get the file name for the new parameter file
	this.newParamFileName = XMLFileUtilities.getParam("newParamFileName");

	// The original, calibrated parameter file
	this.paramsMetaDataFilePath = XMLFileUtilities.getParam("pathToSimulationParameterFile");

	if (this.sbmlRunResultsFile == null || this.paramFileOutputFolder == null
	        || this.newParamFileName == null || this.paramsMetaDataFilePath == null)
	{
	    System.out.println("Error in Settings File. Address these and run ASPASIA again");
	}
	else
	{

	    // Make output folder
	    new File(this.paramFileOutputFolder).mkdirs();

	    // Some of the species and parameter values will come from the the
	    // results file, whereas others
	    // will be in the settings file. Thus we need to read in the
	    // parameter
	    // info from the file
	    XMLFileUtilities.readParameterInfo("SBMLMod");

	    this.newModelFromSteadyState();

	    System.out.println("New SBML Model File Created");
	    System.out.println("Check " + this.paramFileOutputFolder + " for new file "
		    + this.newParamFileName);
	}
    }

    /**
     * Creates a new SBML model file by reading the final line of the SBML
     * solver output file. Creates a map of parameter to value such that the
     * parameters that are being altered via an intervention can easily be
     * found. These are then assigned values set in the settings file.
     */
    public void newModelFromSteadyState()
    {
	try
	{
	    // Going to do this in two ways - read the header in as normal, yet
	    // use ReversedLinesReader
	    // to read in the final line
	    // Header:
	    BufferedReader br = new BufferedReader(new FileReader(this.sbmlRunResultsFile));
	    String headers = br.readLine();
	    br.close();

	    // Now final line of result file
	    File file = new File(this.sbmlRunResultsFile);
	    ReversedLinesFileReader sbmlResult = new ReversedLinesFileReader(file);
	    String result = sbmlResult.readLine();
	    sbmlResult.close();

	    // Now we can map the species/parameter name to the value in the
	    // file using a map
	    this.parameterValMap = FileCreationUtilities.makeParameterValMap(headers, result);

	    // Now we need to change some of the values in the map, to those in
	    // the settings file
	    for (int param = 0; param < XMLFileUtilities.parametersAnalysed.size(); param++)
	    {
		String paramToChange = XMLFileUtilities.parametersAnalysed.get(param);
		if (this.parameterValMap.containsKey(paramToChange))
		{
		    // Change the value
		    this.parameterValMap.remove(paramToChange);
		    this.parameterValMap.put(paramToChange,
			    XMLFileUtilities.sbmlSetValues.get(param));
		}
	    }

	    // Now create the SBML file
	    // Note the 0 on param file number - won't be read so not a worry
	    WriteParameterFile_SBML.writeSimParameterFiles(this.paramsMetaDataFilePath,
		    parameterValMap, XMLFileUtilities.parametersAnalysed,
		    XMLFileUtilities.parameterType, this.paramFileOutputFolder, 0, "SBMLMod",
		    this.newParamFileName);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }

}
