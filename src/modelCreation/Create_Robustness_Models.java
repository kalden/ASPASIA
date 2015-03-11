package modelCreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;

import spartanLink.GenerateSpartanOATFile;
import spartanLink.SpartanUtilities;
import xmlFileUtilities.FileCreationUtilities;
import xmlFileUtilities.XMLFileUtilities;

/**
 * Program to create simulation parameter files for parameter robustness
 * analysis. Steps in this process: 1. Generate robustness analysis samples for
 * a subset of simulation parameters using spartan (creates a file for each
 * parameter) 2. Set the location of the folder containing these CSV files, the
 * names of the parameters of interest, a location of a parameter file at which
 * parameters are at calibrated values, and a location for the output files, in
 * the main method below 3. Run this java program. A parameter file will be
 * created for each sample set in the analysis, with parameters of interest at
 * their values set by the spartan sample, and those not of interest set from
 * their calibrated value
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 *
 */
public class Create_Robustness_Models
{
    /**
     * Whether we are creating Repast Compatible files - PLANNED FOR VERSION 2
     */
    // public boolean repastCompatible;

    /**
     * Whether we are creating SBML files
     */
    public boolean sbmlCompatible;

    /**
     * The original, calibrated parameter file
     */
    public String paramsMetaDataFilePath;

    /**
     * Folder where you want adapted parameter files to be stored
     */
    public String paramFileOutputFolder;

    /**
     * Count of number of parameter files generated per parameter. Used for
     * repast model building - PLANNED INTRODUCTION IN VERSION 2
     */
    // Map<String, Integer> parameterFilesCount = new HashMap<String,
    // Integer>();

    /**
     * Program to create parameter files for parameter robustness experiments.
     * Takes the spreadsheet generated in spartan and replaces the values of
     * parameters of interest in a calibration parameter file with the perturbed
     * values. Does this for all sample sets generated, for all parameters
     * 
     * @param settingsFile Full path to the sensitivity analysis XML settings
     *            file
     */
    public Create_Robustness_Models(String settingsFile)
    {
	// Read settings file
	XMLFileUtilities.readSettingsFile(settingsFile);

	// Determine repast and SBML compatibility
	// this.repastCompatible =
	// XMLFileUtilities.getParamBoolean("repastCompatible");
	this.sbmlCompatible = XMLFileUtilities.getParamBoolean("sbmlFormat");

	// The original, calibrated parameter file
	this.paramsMetaDataFilePath = XMLFileUtilities.getParam("pathToSimulationParameterFile");

	// Folder where you want adapted parameter files to be stored
	this.paramFileOutputFolder = XMLFileUtilities.getParam("parameterFileOutputFolder")
	        + "/Robustness/";
	// Make that folder
	new File(this.paramFileOutputFolder).mkdirs();

	// Now to read in the information about parameters being analysed
	boolean paramCheckedOk = XMLFileUtilities.readParameterInfo("OAT");

	if (paramCheckedOk)
	{
	    // Generate the Spartan File
	    GenerateSpartanOATFile.CreateSpartanLHCScript(this.paramFileOutputFolder,
		    XMLFileUtilities.parametersAnalysed, XMLFileUtilities.minVals,
		    XMLFileUtilities.maxVals, XMLFileUtilities.baselineVals,
		    XMLFileUtilities.incVals);

	    // set the path to the generated spartan file specifying the
	    // parameter sampling
	    SpartanUtilities.spartanScript = this.paramFileOutputFolder + "OAT_Sampling.R";

	    // Generate the sample in R
	    int runResult = SpartanUtilities.generateParameterSamples();

	    if (runResult == 0)
	    {
		// Create the simulation parameter files
		this.writeOATParameterFiles();

		// STUB FOR VERSION 2: REPAST COMPATIBLE ASPASIA
		// Now if Repast, we're going to copy the model files and change
		// the file sink file to the correct output directory
		/*
	         * if (this.repastCompatible) { Setup_Repast_Experiment_Files
	         * sref = new Setup_Repast_Experiment_Files(
	         * this.paramFileOutputFolder, "OAT", this.parameterFilesCount);
	         * }
	         */

		System.out.println("Robustness Analysis Parameter Files Generated");
	    }
	    else
	    {
		System.out.println("Problem creating parameter samples with SPARTAN");
	    }
	}
	else
	{
	    System.out.println("No Samples Created - Problem with Settings XML file");
	}
    }

    /**
     * Write the parameter files for each value set created by Spartan
     */
    public void writeOATParameterFiles()
    {
	try
	{
	    Iterator<String> params = XMLFileUtilities.parametersAnalysed.iterator();

	    // Now create files for each parameter - reading in the spartan
	    // generated file
	    while (params.hasNext())
	    {
		// Store the param name - useful for naming output file later
		String parameterName = params.next();

		// Create the name of the spartan parameter file
		String oatParamsForRunsFilePath = this.paramFileOutputFolder + "/" + parameterName
		        + "_OAT_Values.csv";

		// Create a folder for the XML parameter files
		new File(this.paramFileOutputFolder + "/" + parameterName).mkdirs();

		try
		{
		    // Read in the spartan generated file, named above
		    String oatDesignFile = oatParamsForRunsFilePath;
		    BufferedReader br = new BufferedReader(new FileReader(oatDesignFile));

		    // Firstly, skip over the first line - the parameter
		    // headings
		    String strLine = br.readLine();

		    // Counter for number of simulation files made per
		    // parameter. Appended to file name
		    int i = 0;

		    // Get first line of the simulation parameters generated in
		    // spartan
		    strLine = br.readLine();

		    // Loop through all parameter sets
		    while (strLine != null)
		    {
			// Make the parameter name-value map so this is ready
			// for processing in parameter
			// files
			Map<String, Double> parameterValMap = FileCreationUtilities
			        .makeParameterValMap(strLine);

			// STUB FOR REPAST INTRODUCTION IN VERSION 2
			// Create the parameter file
			// Now call the right script dependent on Repast
			// Compatibility
			/*
		         * if (this.repastCompatible) { // In repast these go in
		         * their own folders, so make // a folder: new
		         * File(this.paramFileOutputFolder + "/" + parameterName
		         * + "/" + (i + 1)).mkdirs(); WriteParameterFile_Repast
		         * .writeSimParameterFiles(this.paramsMetaDataFilePath,
		         * parameterValMap, XMLFileUtilities.parametersAnalysed,
		         * XMLFileUtilities.parameterType,
		         * this.paramFileOutputFolder + "/" + parameterName +
		         * "/" + (i + 1), i, parameterName); }
		         */
			if (this.sbmlCompatible)
			{
			    WriteParameterFile_SBML.writeSimParameterFiles(
				    this.paramsMetaDataFilePath, parameterValMap,
				    XMLFileUtilities.parametersAnalysed,
				    XMLFileUtilities.parameterType, this.paramFileOutputFolder
				            + "/" + parameterName, i, "", null);
			}
			else
			{
			    WriteParameterFile.writeSimParameterFiles(this.paramsMetaDataFilePath,
				    parameterValMap, XMLFileUtilities.parametersAnalysed,
				    XMLFileUtilities.parameterType, this.paramFileOutputFolder
				            + "/" + parameterName, i, parameterName);
			}

			i++;

			// Make the map of the next line of the spartan file
			strLine = br.readLine();
		    }

		    // Put the count into the map - used for Repast Methods
		    // this.parameterFilesCount.put(parameterName, i);

		    br.close();

		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

}
