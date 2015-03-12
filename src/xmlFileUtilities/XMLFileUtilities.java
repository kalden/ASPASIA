/**
 * \package xmlFileUtilities
 * 
 * \brief Package containing utilities to process simulation and sensitivity analysis XML files.
 */
package xmlFileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * \brief Provides functions to read in and process information from XML files.
 * 
 * This can be used to read in simulation information and process the
 * sensitivity analysis settings file
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 *
 */
public class XMLFileUtilities
{
    /**
     * Root of the XML document being read by the class
     */
    public static Element xmlDocRoot;

    /**
     * Whether XML files are being built that are compatible with Repast
     * Simulations
     */
    public static boolean repastCompatible;

    /**
     * Array list of the names of parameters being analysed
     */
    public static ArrayList<String> parametersAnalysed;

    /**
     * Array list of the data type of each parameter being analysed
     */
    public static ArrayList<String> parameterType;

    /**
     * Min values for each parameter
     */
    public static ArrayList<String> minVals;

    /**
     * Max values for each parameter
     */
    public static ArrayList<String> maxVals;

    /**
     * Increment values for each parameter
     */
    public static ArrayList<String> incVals;

    /**
     * Baseline values of each parameter
     */
    public static ArrayList<String> baselineVals;

    /**
     * For cases where an increment is not used, but specific values, the list
     * of the values to use
     */
    public static ArrayList<String> values;

    /**
     * Used for Steph's method to set certain SBML values when making a
     * parameter file from a results file
     */
    public static ArrayList<Double> sbmlSetValues;

    /**
     * Returned to note whether all parameter details have been specified as
     * they should have been
     */
    public static boolean errorCheck = true;

    /**
     * Flag to determine if the user has specified OAT values in a traditional
     * increment manner, or as a list
     */
    public static boolean oatValuesFlag = false;

    /**
     * Reads in the XML file specified in the input argument, initialising the
     * class ready to process information contained within that file
     * 
     * @param settingsFilePath Full path to the simulation parameter file, or
     *            senstivity analysis settings file
     */
    public static void readSettingsFile(String settingsFilePath)
    {
	// First we need to read in the settings XML, the path to this should
	// have been specified as an argument
	try
	{
	    Document document = (new SAXBuilder(false)).build(new File(settingsFilePath));
	    xmlDocRoot = document.getRootElement();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    /**
     * Return a string parameter from the XML file, or sensitivity analysis
     * settings file
     * 
     * @param paramName The name of the parameter for which the value is
     *            requested
     * @return String value of that parameter in the XML file. Null is returned
     *         if no tag is found
     */
    public static String getParam(String paramName)
    {
	return xmlDocRoot.getChildText(paramName);
    }

    /**
     * Return a boolean parameter from the XML file, or sensitivity analysis
     * settings file
     * 
     * @param paramName The name of the parameter for which the value is
     *            requested
     * @return Boolean value of that parameter in the XML file. Null is returned
     *         if no tag is found
     */
    public static Boolean getParamBoolean(String paramName)
    {
	return Boolean.parseBoolean(xmlDocRoot.getChildText(paramName));
    }

    /**
     * Return an integer parameter from the XML file, or sensitivity analysis
     * settings file
     * 
     * @param paramName The name of the parameter for which the value is
     *            requested
     * @return Integer value of that parameter in the XML file. Null is returned
     *         if no tag is found
     */
    public static int getParamInteger(String paramName)
    {
	return Integer.parseInt(xmlDocRoot.getChildText(paramName));
    }

    /**
     * Return a double parameter from the XML file, or sensitivity analysis
     * settings file
     * 
     * @param paramName The name of the parameter for which the value is
     *            requested
     * @return Double value of that parameter in the XML file. Null is returned
     *         if no tag is found
     */
    public static double getParamDouble(String paramName)
    {
	return Double.parseDouble(xmlDocRoot.getChildText(paramName));
    }

    /**
     * Return an ArrayList comprised of string elements in the parameter file
     * that were specified as a comma separated list.
     * 
     * @param paramName The name of the parameter for which the value is
     *            requested
     * @return ArrayList of string values associated with this parameter
     */
    public static ArrayList<String> getParamStringList(String paramName)
    {
	// Get the list from the XML file and split on the comma
	StringTokenizer st = new StringTokenizer(getParam(paramName), ",");

	// now add each to an array list so this can be returned to the
	// simulation
	ArrayList<String> ar = new ArrayList<String>();
	while (st.hasMoreTokens())
	{
	    ar.add(st.nextToken());
	}

	return ar;
    }

    /**
     * Return an ArrayList comprised of integer elements in the parameter file
     * that were specified as a comma separated list.
     * 
     * @param paramName The name of the parameter for which the value is
     *            requested
     * @return ArrayList of integer values associated with this parameter
     */
    public static ArrayList<Integer> getParamIntegerList(String paramName)
    {
	// Get the list from the XML file and split on the comma
	StringTokenizer st = new StringTokenizer(getParam(paramName), ",");

	// now add each to an array list so this can be returned to the
	// simulation
	ArrayList<Integer> ar = new ArrayList<Integer>();
	while (st.hasMoreTokens())
	{
	    ar.add(Integer.parseInt(st.nextToken()));
	}

	return ar;
    }

    /**
     * Return an ArrayList comprised of double elements in the parameter file
     * that were specified as a comma separated list.
     * 
     * @param paramName The name of the parameter for which the value is
     *            requested
     * @return ArrayList of double values associated with this parameter
     */
    public static ArrayList<Double> getParamDoubleList(String paramName)
    {
	// Get the list from the XML file and split on the comma
	StringTokenizer st = new StringTokenizer(getParam(paramName), ",");

	// now add each to an array list so this can be returned to the
	// simulation
	ArrayList<Double> ar = new ArrayList<Double>();
	while (st.hasMoreTokens())
	{
	    ar.add(Double.parseDouble(st.nextToken()));
	}

	return ar;
    }

    /**
     * Used in Sensitivity analysis experiments - reads in parameter information
     * for the analysis. This is stored in the appropriate arrays: parameter
     * name, type of parameter, and min, max, and increment values. The latter
     * three are only required for robustness analysis and as such may not exist
     * in LHC and eFAST analyses.
     */
    public static boolean readParameterInfo(String analysisMethod)
    {
	parametersAnalysed = new ArrayList<String>();
	parameterType = new ArrayList<String>();
	minVals = new ArrayList<String>();
	maxVals = new ArrayList<String>();
	incVals = new ArrayList<String>();
	values = new ArrayList<String>();
	sbmlSetValues = new ArrayList<Double>();
	baselineVals = new ArrayList<String>();

	// Get all the parameters
	@SuppressWarnings("unchecked")
	List<Element> parameterList = xmlDocRoot.getChildren("parameter");
	// Now process each
	for (Object parameter : parameterList)
	{
	    Element indParam = (Element) parameter;
	    // Add the parameter
	    parametersAnalysed.add(indParam.getText());
	    // Add the data type
	    parameterType.add(indParam.getAttributeValue("type"));
	    minVals.add(indParam.getAttributeValue("min"));
	    maxVals.add(indParam.getAttributeValue("max"));
	    incVals.add(indParam.getAttributeValue("inc"));
	    baselineVals.add(indParam.getAttributeValue("baseline"));

	    if (indParam.getAttributeValue("value") != null)
	    {
		sbmlSetValues.add(Double.parseDouble(indParam.getAttributeValue("value")));
	    }

	    // Now we see how the user has specified their values - this is
	    // important for OAT as you can specify by increment or list of
	    // values. For all other methods, a max and min MUST be specified -
	    // we will raise an error if not the case

	    if (analysisMethod.equals("OAT"))
	    {
		try
		{
		    // Try to get the length - if this fails the user should
		    // have specified max, min, and increment, and catch will
		    // deal
		    // with this
		    indParam.getAttributeValue("values").length();

		    // Add to the array of values for this parameter if present
		    // in the file
		    values.add(indParam.getAttributeValue("values"));

		    // Set the flag to state the values have been used rather
		    // than the increment - the cluster writing routine needs to
		    // know this
		    oatValuesFlag = true;
		}
		catch (NullPointerException e)
		{
		    // Not specified a list of values - so must have specified
		    // max, min, inc, baseline
		    checkValueEntered(indParam.getAttributeValue("min"), "min", indParam.getText(),
			    analysisMethod);
		    checkValueEntered(indParam.getAttributeValue("max"), "max", indParam.getText(),
			    analysisMethod);
		    checkValueEntered(indParam.getAttributeValue("inc"), "inc", indParam.getText(),
			    analysisMethod);
		    checkValueEntered(indParam.getAttributeValue("baseline"), "baseline",
			    indParam.getText(), analysisMethod);
		}
	    }
	    else if (analysisMethod.equals("eFAST") || analysisMethod.equals("LHC"))
	    {
		// Must have specified max and min
		checkValueEntered(indParam.getAttributeValue("min"), "min", indParam.getText(),
		        analysisMethod);
		checkValueEntered(indParam.getAttributeValue("max"), "max", indParam.getText(),
		        analysisMethod);
	    }
	    else if (analysisMethod.equals("SBMLMod"))
	    {
		checkValueEntered(indParam.getAttributeValue("value"), "value", indParam.getText(),
		        analysisMethod);
	    }
	}
	return errorCheck;

    }

    /**
     * Checks that the user has entered a value for a specified XML tag - min,
     * max, or increment
     * 
     * @param xmlParamFieldValue The XML field to check
     * @param description The field - min, max, or inc
     * @param parameter The parameter being processed
     * @param method The analysis method being applied
     */
    public static void checkValueEntered(String xmlParamFieldValue, String description,
	    String parameter, String method)
    {
	if (xmlParamFieldValue == null)
	{
	    System.out.println("Analysis Method: " + method + " , Parameter: " + parameter
		    + " . No entry in XML file for " + description + " field");
	    errorCheck = false;
	}
    }
}
