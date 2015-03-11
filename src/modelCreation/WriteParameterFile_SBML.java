/**
 * \package modelCreation
 * 
 * Package containing utilities to process parameter value sets generated by spartan, creating simulation parameter scripts for each parameter value set, or to introduce interventions into an SBML Model
 */
package modelCreation;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Used by the Create_[Analysis]_Classes to write the simulation parameter files
 * for each value set generated by spartan, for Repast Sims
 * 
 * @author Kieran Alden, York Computational Immunology Lab
 * @version 1.0
 * @since February 2015
 *
 */
public class WriteParameterFile_SBML
{
    /**
     * Count of the file produced - used in file numbering
     */
    public static int count = 1;

    /**
     * Determines if a parameter is of interest to the analysis. If so, returns
     * true and the value is assigned to that in the spartan CSV file. If not,
     * returns false and the parameter is assigned its calibrated value
     * 
     * @param parameters Arraylist of parameter names of interest to the
     *            analysis
     * @param paramToFind The name of the parameter currently being written to
     *            the XML file - the parameter to check
     * @return True if the parameter is of interest to the analysis, false if
     *         not
     */
    public static String checkArrayList(ArrayList<String> parameters, String paramToFind)
    {
	String found = "null";

	for (int param = 0; param < parameters.size(); param++)
	{
	    String paramInArray = parameters.get(param);

	    if (paramInArray.equals(paramToFind))
	    {
		// Different from non-repast generator as returns any string,
		// not the type - as we already have this in repast file
		found = "yes";
	    }
	}

	return found;

    }

    /**
     * Cleans the XML file to remove spaces that will get in the way of
     * iterating through the nodes. Credit for this function:
     * http://www.sitepoint.com/removing-useless-nodes-from-the-dom/
     * 
     * @param node Node for which the whitespace is being removed
     */
    public static void clean(Node node)
    {
	NodeList childNodes = node.getChildNodes();

	for (int n = childNodes.getLength() - 1; n >= 0; n--)
	{
	    Node child = childNodes.item(n);
	    short nodeType = child.getNodeType();

	    if (nodeType == Node.ELEMENT_NODE)
		clean(child);
	    else if (nodeType == Node.TEXT_NODE)
	    {
		String trimmedNodeVal = child.getNodeValue().trim();
		if (trimmedNodeVal.length() == 0)
		    node.removeChild(child);
		else
		    child.setNodeValue(trimmedNodeVal);
	    }
	    else if (nodeType == Node.COMMENT_NODE)
		node.removeChild(child);
	}
    }

    /**
     * Adds all attributes of an XML tag to the tag in the clone of the
     * parameter file
     * 
     * @param nn Nodelist of attributes of a tag
     * @param attributeElement The element the attributes are being added to
     */
    public static void addAllTagAttributes(NamedNodeMap nn, Element attributeElement)
    {
	for (int i = 0; i < nn.getLength(); i++)
	{
	    Node n = nn.item(i);
	    attributeElement.setAttribute(n.getNodeName(), n.getNodeValue());
	}
    }

    /**
     * Returns the value of a tag - excluding the values of all tag descendents
     * (as the built in tag would do) Code taken from
     * http://stackoverflow.com/questions
     * /12191414/node-gettextcontent-is-there-a
     * -way-to-get-text-content-of-the-current-node-no
     * 
     * @param node The node for which the value should be returned
     * @return Text string containing the content of that node
     */
    public static String getFirstLevelTextContent(Node node)
    {
	NodeList list = node.getChildNodes();
	StringBuilder textContent = new StringBuilder();
	for (int i = 0; i < list.getLength(); ++i)
	{
	    Node child = list.item(i);
	    if (child.getNodeType() == Node.TEXT_NODE)
		textContent.append(child.getTextContent());
	}
	return textContent.toString();
    }

    /**
     * Processes Child Nodes of the SBML file - copying these to a new version
     * if not the list of parameters, while ensuring that when the list of
     * parameters is found, the calibrated values are replaced with values
     * generated by spartan
     * 
     * @param docWriting The XML document being created
     * @param child The list of child nodes to be processed
     * @param sectionRoot The root element of the child nodes being processed
     */
    public static void processChildNodes(Document docWriting, NodeList child, Element sectionRoot,
	    Map<String, Double> parameterValMap, boolean speciesChange)
    {
	for (int c = 0; c < child.getLength(); c++)
	{
	    // Ignore any spaces that may remain in the XML file (even after
	    // cleaning)
	    if (!child.item(c).getNodeName().equals("#text"))
	    {
		// Write the node to the file
		Element newSectionRoot = docWriting.createElement(child.item(c).getNodeName());
		addAllTagAttributes(child.item(c).getAttributes(), newSectionRoot);
		// Append any values of that tag
		if (getFirstLevelTextContent(child.item(c)).length() > 0)
		{
		    newSectionRoot.setTextContent(getFirstLevelTextContent(child.item(c)));
		}
		sectionRoot.appendChild(newSectionRoot);

		// FIRST WE CHECK WHETHER WE HAVE REACHED THE "LISTOFPARAMETERS"
		// IT IS THIS WE WANT TO CHANGE, SWAPPING IN THE VALUE GENERATED
		// BY SPARTAN
		// IF NOT, WE JUST CARRY ON RECURSIVELY PROCESSING THE SBML FILE
		// OVER TO THE NEW FILE
		if (child.item(c).getNodeName().equals("listOfSpecies"))
		{
		    // Get the children of this node
		    clean(child.item(c));
		    NodeList tagSetBeingExamined = child.item(c).getChildNodes();

		    if (speciesChange)
		    {
			// We need to do a bit more work here, we need to check
			// whether the value is being changed
			// Now iterate through the species tags
			findAndSetTagValues(tagSetBeingExamined, parameterValMap, docWriting,
			        speciesChange, newSectionRoot, "initialConcentration");

		    }
		    else
		    {
			if (tagSetBeingExamined.getLength() > 0)
			{
			    processChildNodes(docWriting, tagSetBeingExamined, newSectionRoot,
				    parameterValMap, speciesChange);
			}
		    }
		}
		else if (child.item(c).getNodeName().equals("listOfParameters"))
		{
		    // We need to do a bit more work here, we need to check
		    // whether the value is being changed
		    // Get the children of this node
		    NodeList tagSetBeingExamined = child.item(c).getChildNodes();

		    findAndSetTagValues(tagSetBeingExamined, parameterValMap, docWriting,
			    speciesChange, newSectionRoot, "value");
		}
		else
		{
		    clean(child.item(c));
		    NodeList newChildren = child.item(c).getChildNodes();

		    if (newChildren.getLength() > 0)
		    {
			processChildNodes(docWriting, newChildren, newSectionRoot, parameterValMap,
			        speciesChange);
		    }
		}

	    }
	}

    }

    /**
     * Iterates through the set of listOfParameters and, if specified,
     * listOfSpecies, setting the values to those sampled by spartan or within
     * provided results file (these are already in the parameter value map).
     * 
     * @param tagSetBeingExamined The set of tags being examined
     *            (listOfParameters/listOfSpecies)
     * @param parameterValMap The map containing the new parameter/value
     *            pairings
     * @param docWriting The XML document being created
     * @param speciesChange Whether listOfSpecies is being considered
     * @param newSectionRoot The current XML tag being created
     * @param tagAttributeToSet The name of the attribute to set - either
     *            "value" or "initialConcentration" (for species)
     */
    public static void findAndSetTagValues(NodeList tagSetBeingExamined,
	    Map<String, Double> parameterValMap, Document docWriting, boolean speciesChange,
	    Element newSectionRoot, String tagAttributeToSet)
    {

	// Now we are going to iterate through this here:
	for (int param = 0; param < tagSetBeingExamined.getLength(); param++)
	{

	    // Create the node
	    Element paramRoot = docWriting.createElement(tagSetBeingExamined.item(param)
		    .getNodeName());

	    // Now to create the SBML attributes
	    NamedNodeMap paramAttrs = tagSetBeingExamined.item(param).getAttributes();

	    String paramName = "";
	    // Now iterate through these, finding value
	    for (int i = 0; i < paramAttrs.getLength(); i++)
	    {
		// Get the attribute
		Node n = paramAttrs.item(i);
		// Get the parameter id - useful for finding the value later
		if (n.getNodeName().equals("id"))
		{
		    paramName = n.getNodeValue();
		    paramRoot.setAttribute("id", n.getNodeValue());
		}
		// Else write to the tag if not value:
		else if (!n.getNodeName().equals(tagAttributeToSet))
		{
		    paramRoot.setAttribute(n.getNodeName(), n.getNodeValue());
		}
		else
		{
		    // Now we need to determine whether the value should be
		    // replaced by that generated by spartan
		    // THIS IS MAKING THE ASSUMPTION THAT THE SBML FILE IS
		    // STRUCTURED SUCH THAT NAME APPEARS BEFORE VALUE!
		    if (parameterValMap.containsKey(paramName))
		    {
			// Change for the value in the map, generated by Spartan
			paramRoot.setAttribute(tagAttributeToSet,
			        Double.toString(parameterValMap.get(paramName)));
		    }
		    else
		    {
			// Keep the original value
			paramRoot.setAttribute(n.getNodeName(), n.getNodeValue());
		    }
		}
	    }

	    // See if the node has any children and append them
	    // This will definitely be the case if we are changing species
	    NodeList subNodes = tagSetBeingExamined.item(param).getChildNodes();

	    if (subNodes.getLength() > 0)
	    {
		processChildNodes(docWriting, subNodes, paramRoot, parameterValMap, speciesChange);
	    }

	    // Write the parameter to the file
	    newSectionRoot.appendChild(paramRoot);
	}
    }

    /**
     * Parse each parameter value set created by spartan, producing a Repast
     * compatible simulation parameter file for each. Parameters not involved in
     * the analysis remain at their calibrated value, with parameters of
     * interest taking their value created in the spartan sample
     * 
     * @param calibratedParamFilePath Full path to the simulation parameter file
     *            at calibrated values
     * @param parameterValLine A line from the spartan parameter value file
     * @param params The names of the parameters of interest
     * @param types The data type of each parameter
     * @param outputFolder The full path to the output folder where these
     *            parameter files are stored
     * @param paramFileNum The number of the parameter file (i.e. the sample
     *            number)
     * @param analysis The type of spartan analysis being performed.
     * @param parameterFileName If used for SBML species/param alteration for
     *            Steph, name of the parameter file
     */
    public static void writeSimParameterFiles(String calibratedParamFilePath,
	    Map<String, Double> parameterValMap, ArrayList<String> params, ArrayList<String> types,
	    String outputFolder, int paramFileNum, String analysis, String parameterFileName)
    {
	try
	{
	    // Firstly, set up a new XML file, that will become the parameter
	    // file
	    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	    Document doc;
	    doc = docBuilder.parse(new File(calibratedParamFilePath));

	    // normalize text representation of the document
	    doc.getDocumentElement().normalize();

	    // Write the required root elements
	    Document docWriting = docBuilder.newDocument();
	    // Clean removes all comments and #text spaces
	    clean(doc.getDocumentElement());

	    Element rootElement = docWriting.createElement(doc.getDocumentElement().getNodeName());
	    NamedNodeMap nn = doc.getDocumentElement().getAttributes();
	    // Add all attributes of this root node - we may move this to make
	    // this generic function
	    addAllTagAttributes(nn, rootElement);
	    docWriting.appendChild(rootElement);

	    // Now work on the children of every node, recursively

	    NodeList child = doc.getDocumentElement().getChildNodes();

	    // Call the correct process child nodes dependent on the analysis -
	    // just changing SBML listOfParameters, or
	    // changing listOfSpecies too
	    // Also set the output file name while here
	    String paramFileName;

	    processChildNodes(docWriting, child, rootElement, parameterValMap, true);
	    paramFileName = outputFolder + "/" + parameterFileName;

	    // Now write the file
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");

	    docWriting.normalizeDocument();
	    DOMSource source = new DOMSource(docWriting);

	    StreamResult result = new StreamResult(new File(paramFileName));
	    transformer.transform(source, result);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
}
