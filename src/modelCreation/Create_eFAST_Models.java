package modelCreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;

import spartanLink.GenerateSpartanEFASTFile;
import spartanLink.SpartanUtilities;
import xmlFileUtilities.FileCreationUtilities;
import xmlFileUtilities.XMLFileUtilities;

/**
 * Program to create simulation parameter files for efast analysis. Steps in
 * this process: 1. Generate efast samples for a subset of simulation parameters
 * using spartan (creates a file for each parameter) 2. Set the location of the
 * folder containing these CSV files, the names of the parameters of interest, a
 * location of a parameter file at which parameters are at calibrated values,
 * the number of resample curves, and a location for the output files, in the
 * main method below 3. Run this java program. A parameter file will be created
 * for each sample set in the analysis, with parameters of interest at their
 * values set by the spartan sample, and those not of interest set from their
 * calibrated value
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 *
 */
public class Create_eFAST_Models
{
    /**
     * Whether we are creating Repast Compatible files: TO COME IN VERSION 2
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
     * Number of resample curves employed by the eFAST algorithm
     */
    public int efastCurves;

    /**
     * Number of samples taken from eFAST Curve
     */
    public int efastCurveSamples;

    /**
     * Counter of samples produced by Spartan. Used in parameter file creation
     */
    public int numEFastSamples;

    /**
     * Number of times to run each simulation
     */
    int numReplicates;

    /**
     * Program to create parameter files for efast experiments. Takes the
     * spreadsheet generated in spartan and replaces the values of parameters of
     * interest in a calibration parameter file with the perturbed values. Does
     * this for all sample sets generated, for all parameters
     * 
     * @param settingsFile Full path to the sensitivity analysis XML settings
     *            file
     */
    public Create_eFAST_Models(String settingsFile)
    {
	// Read settings file
	XMLFileUtilities.readSettingsFile(settingsFile);

	// Determine repast and SBML compatibility - Repast to come in Version 2
	// this.repastCompatible =
	// XMLFileUtilities.getParamBoolean("repastCompatible");
	this.sbmlCompatible = XMLFileUtilities.getParamBoolean("sbmlFormat");

	// The original, calibrated parameter file
	this.paramsMetaDataFilePath = XMLFileUtilities.getParam("pathToSimulationParameterFile");

	// Folder where you want adapted parameter files to be stored
	this.paramFileOutputFolder = XMLFileUtilities.getParam("parameterFileOutputFolder")
	        + "/eFAST/";
	// Make that folder
	new File(this.paramFileOutputFolder).mkdirs();

	// Number of curves in the analysis
	this.efastCurves = Integer.parseInt(XMLFileUtilities.getParam("efastCurves"));

	// Number of samples to take from each curve
	this.efastCurveSamples = Integer.parseInt(XMLFileUtilities.getParam("efastCurveSamples"));

	// Now to read in the information about parameters being analysed
	XMLFileUtilities.readParameterInfo("eFAST");

	// Add the dummy, as this is needed in eFAST
	XMLFileUtilities.parametersAnalysed.add("dummy");
	XMLFileUtilities.parameterType.add("double");
	XMLFileUtilities.minVals.add("1");
	XMLFileUtilities.maxVals.add("10");

	// Generate the Spartan EFAST Sampling File
	GenerateSpartanEFASTFile.CreateSpartanLHCScript(this.paramFileOutputFolder,
	        XMLFileUtilities.parametersAnalysed, this.efastCurves, this.efastCurveSamples,
	        XMLFileUtilities.minVals, XMLFileUtilities.maxVals);

	// set the path to the generated spartan file specifying the parameter
	// sampling
	// SpartanUtilities.spartanScript =
	// XMLFileUtilities.getParam("efast_spartan_script");
	SpartanUtilities.spartanScript = this.paramFileOutputFolder + "/EFAST_Sampling.R";

	// Generate the sample in R
	int runResult = SpartanUtilities.generateParameterSamples();

	// If sample went well, create the parameter files and cluster scripts
	if (runResult == 0)
	{

	    for (int c = 1; c <= this.efastCurves; c++)
	    {
		// Make a directory to store the parameter files for this curve
		new File(this.paramFileOutputFolder + "/" + c).mkdirs();

		// Now create files for each parameter - reading in the spartan
		// generated efast file
		Iterator<String> params = XMLFileUtilities.parametersAnalysed.iterator();

		while (params.hasNext())
		{
		    // Store the param name - useful for naming output file
		    // later
		    String parameterName = params.next();
		    // Create the name of the spartan parameter file
		    String efastParamsForRunsFile = this.paramFileOutputFolder + "/Curve" + c + "_"
			    + parameterName + ".csv";

		    // Make a directory for these parameter files
		    String outputDir = this.paramFileOutputFolder + "/" + c + "/" + parameterName;
		    new File(outputDir).mkdirs();

		    // Now make the parameter files for this parameter, for this
		    // curve
		    this.writeEFASTParameterFiles(efastParamsForRunsFile, outputDir);

		}
	    }

	    // STUB FOR VERSION 2
	    // Now if Repast, we're going to copy the model files and change the
	    // file sink file to
	    // the correct output directory
	    /*
	     * if (this.repastCompatible) { // Null as no map of parameter file
	     * counts, as with robustness // analysis
	     * Setup_Repast_Experiment_Files sref = new
	     * Setup_Repast_Experiment_Files( this.paramFileOutputFolder,
	     * "eFAST", null); }
	     */

	    System.out.println("eFAST Parameter Files and Cluster Scripts Generated");
	}
	else
	{
	    System.out.println("Problem creating parameter samples with SPARTAN");
	}

    }

    /**
     * Write the parameter files for each value set created by Spartan
     * 
     * @param efastParamsForRunsFile The full path to the parameter values file
     *            created by spartan
     * @param paramFileOutputDirectory The full path to where the generated
     *            parameter files should be stored
     */
    public void writeEFASTParameterFiles(String efastParamsForRunsFile,
	    String paramFileOutputDirectory)
    {
	try
	{
	    // Read in the spartan generated file, named above
	    String efastDesignFile = efastParamsForRunsFile;
	    BufferedReader br = new BufferedReader(new FileReader(efastDesignFile));

	    // Firstly, skip over the first line - the parameter headings
	    String strLine = br.readLine();

	    // Counter for number of simulation files made per parameter.
	    // Appended to file name
	    int i = 0;

	    // Get first line of the simulation parameters generated in spartan
	    strLine = br.readLine();

	    // Loop through all parameter sets
	    while (strLine != null)
	    {
		// Make the parameter name-value map so this is ready for
		// processing in parameter
		// files
		Map<String, Double> parameterValMap = FileCreationUtilities
		        .makeParameterValMap(strLine);

		// Now call the right script dependent on Repast Compatibility
		// Repast stub for Version 2
		/*
	         * if (this.repastCompatible) { // In repast these go in their
	         * own folders, so make a // folder: new
	         * File(paramFileOutputDirectory + "/" + (i + 1)).mkdirs(); //
	         * System.out.println(paramFileOutputDirectory+"/"+(i+1));
	         * WriteParameterFile_Repast
	         * .writeSimParameterFiles(this.paramsMetaDataFilePath,
	         * parameterValMap, XMLFileUtilities.parametersAnalysed,
	         * XMLFileUtilities.parameterType, paramFileOutputDirectory +
	         * "/" + (i + 1), i, ""); }
	         */
		if (this.sbmlCompatible)
		{
		    WriteParameterFile_SBML.writeSimParameterFiles(this.paramsMetaDataFilePath,
			    parameterValMap, XMLFileUtilities.parametersAnalysed,
			    XMLFileUtilities.parameterType, paramFileOutputDirectory, i, "", null);
		}
		else
		{
		    WriteParameterFile.writeSimParameterFiles(this.paramsMetaDataFilePath,
			    parameterValMap, XMLFileUtilities.parametersAnalysed,
			    XMLFileUtilities.parameterType, paramFileOutputDirectory, i, "");
		}

		i++;

		// Read next line of spartan file
		strLine = br.readLine();

	    }

	    br.close();

	    this.numEFastSamples = i + 1;

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
}
