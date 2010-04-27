/*
<!-- Version 0.21 by Fabien.Gandon@sophia.inria.fr -->
<!-- This software is distributed under either the CeCILL-C license or the GNU Lesser General Public License version 3 license. -->
<!-- This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License -->
<!-- as published by the Free Software Foundation version 3 of the License or under the terms of the CeCILL-C license. -->
<!-- This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied -->
<!-- warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. -->
<!-- See the GNU Lesser General Public License version 3 at http://www.gnu.org/licenses/  -->
<!-- and the CeCILL-C license at http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.html for more details -->
 */

package net.yacy.cider.parser.idiom.rdfa;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class RDFaParserImp {
	private static Templates theTemplates = null;

	private String thePropertyURI = null;

	private String theSubjectURI = null;

	private String theSubjectNodeID = null;

	private String theObjectURI = null;
	
	private String theObjectNodeID = null;

	private String theValue = null;

	private String theDatatype = null;

	private String theLanguage = null;

	public void parse(Reader in, String base) throws IOException,
			TransformerException, TransformerConfigurationException {
		if (theTemplates == null) {
			this.getClass().getClassLoader();
            StreamSource aSource = new StreamSource(ClassLoader.getSystemResource("net/yacy/serlex/parser/rdfa/RDFaParser.xsl").openStream());
			
			TransformerFactory aFactory = TransformerFactory.newInstance();
			theTemplates = aFactory.newTemplates(aSource); 
		}
		Transformer aTransformer = theTemplates.newTransformer();
		aTransformer.setParameter("parser", this);
		aTransformer.setParameter("url", base);
		aTransformer.transform(new StreamSource(in), new StreamResult(
				new OutputStream()
				// output stream to /dev/null
				{
					public void write(int b) throws IOException {
					}
				}));
	}

	public static boolean flushDataProperty(Object oparser) {
		RDFaParserImp parser = ((RDFaParserImp)oparser);
		parser.reportDataProperty(parser.theSubjectURI, parser.theSubjectNodeID, parser.thePropertyURI,
				parser.theValue, parser.theDatatype, parser.theLanguage);
		parser.thePropertyURI = null;
		parser.theSubjectURI = null;
		parser.theSubjectNodeID = null;
		parser.theObjectURI = null;
		parser.theObjectNodeID = null;
		parser.theValue = null;
		parser.theDatatype = null;
		parser.theLanguage = null;
		return true;
	}

	public static boolean flushObjectProperty(Object oparser) {
		RDFaParserImp parser = ((RDFaParserImp)oparser);
		parser.reportObjectProperty(parser.theSubjectURI, parser.theSubjectNodeID, parser.thePropertyURI,
				parser.theObjectURI, parser.theObjectNodeID);
		parser.thePropertyURI = null;
		parser.theSubjectURI = null;
		parser.theSubjectNodeID = null;
		parser.theObjectURI = null;
		parser.theObjectNodeID = null;
		parser.theValue = null;
		parser.theDatatype = null;
		parser.theLanguage = null;
		return true;
	}
	
	public void reportDataProperty(String subjectURI, String subjectNodeID,
			String propertyURI, String value, String datatype, String lang) {

	}

	public void reportObjectProperty(String subjectURI, String subjectNodeID,
			String propertyURI, String objectURI, String objectNodeID) {

	}

	public static boolean setTheDatatype(Object parser, String theDatatype) {
		((RDFaParserImp)parser).theDatatype = theDatatype;
		return true;
	}

	public static boolean setTheLanguage(Object parser, String theLanguage) {
		((RDFaParserImp)parser).theLanguage = theLanguage;
		return true;
	}

	public static boolean setTheObjectNodeID(Object parser, String theObjectNodeID) {
		((RDFaParserImp)parser).theObjectNodeID = theObjectNodeID;
		return true;
	}

	public static boolean setTheObjectURI(Object parser, String theObjectURI) {
		((RDFaParserImp)parser).theObjectURI = theObjectURI;
		return true;
	}

	public static boolean setThePropertyURI(Object parser, String thePropertyURI) {
		((RDFaParserImp)parser).thePropertyURI = thePropertyURI;
		return true;
	}


	public static boolean setTheSubjectNodeID(Object parser, String theSubjectNodeID) {
		((RDFaParserImp)parser).theSubjectNodeID = theSubjectNodeID;
		return true;
	}

	public static boolean setTheSubjectURI(Object parser, String theSubjectURI) {
		((RDFaParserImp)parser).theSubjectURI = theSubjectURI;
		return true;
	}

	public static boolean setTheValue(Object parser, String theValue) {
		((RDFaParserImp)parser).theValue = theValue;
		return true;
	}

}
