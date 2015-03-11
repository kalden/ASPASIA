package xmlFileUtilities;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Utilities to aid the creation of SBML and parameter files
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 */
public class FileCreationUtilities
{
    /**
     * Makes a map from the spartan file, mapping the parameter name to the
     * sampled value. Saves the need for the user to enter the parameter names
     * in the same order as in the file
     * 
     * @param spartanLine One line from the spartan CSV file
     * @return Map of parameter value to sampled value
     */
    public static Map<String, Double> makeParameterValMap(String spartanLine)
    {
	// The parameter values in the spartan value line will be split by
	// commas,
	// so a string tokenizer can be used to break these apart
	StringTokenizer st = new StringTokenizer(spartanLine, ",");

	Map<String, Double> parameterValMap = new HashMap<String, Double>();
	for (int q = 0; q < XMLFileUtilities.parametersAnalysed.size(); q++)
	{
	    parameterValMap.put(XMLFileUtilities.parametersAnalysed.get(q),
		    Double.parseDouble(st.nextToken()));
	}

	return parameterValMap;
    }

    /**
     * Makes a map from two arguments: a comma separated list of parameters and
     * a comma separated list of values, mapping the parameter name to the
     * sampled value. Saves the need for the user to enter the parameter names
     * in the same order as in the file
     * 
     * @param parameters Comma separated text string of parameters
     * @param values Comma separated text string of values
     * @return Map of parameter value to sampled value
     */
    public static Map<String, Double> makeParameterValMap(String parameters, String values)
    {
	// Now to iterate through both strings, which should be the same length
	// and separated by ,
	StringTokenizer params = new StringTokenizer(parameters, ",");
	StringTokenizer vals = new StringTokenizer(values, ",");

	Map<String, Double> parameterValMap = new HashMap<String, Double>();

	while (params.hasMoreTokens())
	{
	    // Remove any padded double quotes around the parameter value. If in
	    // the result file, ASPASIA reads in two sets of quotes
	    // Do the same with the parameter name
	    String paramVal = vals.nextToken().replace("\"", "");
	    String paramName = params.nextToken().replace("\"", "");

	    if (paramVal.contains("e-"))
	    {
		if (Double.parseDouble(paramVal.substring(paramVal.lastIndexOf("-") + 1)) > 300)
		{
		    parameterValMap.put(paramName, 0.0);
		}
		else
		{
		    parameterValMap.put(paramName, Double.parseDouble(paramVal));
		}
	    }
	    else
	    {
		parameterValMap.put(paramName, Double.parseDouble(paramVal));
	    }
	}

	return parameterValMap;
    }
}
