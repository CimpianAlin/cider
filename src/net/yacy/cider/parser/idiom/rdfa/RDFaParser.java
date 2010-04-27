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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;



/**
 * @author fgandon
 *
 */
public class RDFaParser {
	
	public void parse(Reader in, String base) throws IOException,
	TransformerException, TransformerConfigurationException {
		
		RDFaParserImp anImp = new RDFaParserImp()
		{
			public void reportDataProperty(String subjectURI, String subjectNodeID,
					String propertyURI, String value, String datatype, String lang) {
				handleDataProperty( subjectURI,  subjectNodeID,
						 propertyURI,  value,  datatype,  lang);
			}

			public void reportObjectProperty(String subjectURI, String subjectNodeID,
					String propertyURI, String objectURI, String objectNodeID) {

				handleObjectProperty( subjectURI,  subjectNodeID,
						 propertyURI,  objectURI,  objectNodeID);

			}
		};
		anImp.parse(in, base);
		
	}
	
	public void handleDataProperty(String subjectURI, String subjectNodeID,
			String propertyURI, String value, String datatype, String lang) {

	}

	public void handleObjectProperty(String subjectURI, String subjectNodeID,
			String propertyURI, String objectURI, String objectNodeID) {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length<1){
			System.out.println("Usage: one and only one argument giving a file path or a URL.");
		}
		else
		{
			File aFile = new File(args[0]);
			String aBase = null;
			Reader aReader = null;
			if(aFile.exists()){
				try {
					aReader = new FileReader(aFile);
					aBase = aFile.getAbsolutePath();
				} catch (FileNotFoundException e) {	aReader = null; }
			} else
			{
				try {
					URL aURL = new URL(args[0]);
					aReader =  new InputStreamReader (aURL.openStream());
					aBase = args[0];
				} catch (MalformedURLException e) {	}
				  catch (IOException e) {
					e.printStackTrace();
					aReader = null;
				}
				 
			} 
			
			if (aReader!=null){
				RDFaParser aParser = new RDFaParser()
				{

					public void handleDataProperty(String subjectURI, String subjectNodeID,
							String propertyURI, String value, String datatype, String lang) {
						if(subjectURI !=null ) System.out.print("<"+subjectURI+"> ");
						else System.out.print("_:"+subjectNodeID+" ");
						System.out.print(propertyURI+" \""+value+"\"");
						if(datatype !=null ) System.out.print("^^"+datatype);
						if(lang !=null ) System.out.print("@"+lang);
						System.out.println(" .");
					}

					public void handleObjectProperty(String subjectURI, String subjectNodeID,
							String propertyURI, String objectURI, String objectNodeID) {
						if(subjectURI !=null ) System.out.print("<"+subjectURI+"> ");
						else System.out.print("_:"+subjectNodeID+" ");
						System.out.print(propertyURI+" ");
						if(objectURI !=null ) System.out.print("<"+objectURI+"> ");
						else System.out.print("_:"+objectNodeID+" ");
						System.out.println(" .");
					}
				};
				
				try {
					aParser.parse(aReader, aBase);
				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				}
			} else System.out.println("File or URL not recognized.");
			
		}

	}

}
