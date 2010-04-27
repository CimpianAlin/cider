/**
 *  CIDER.java
 *  Copyright 2010 by Michael Peter Christen
 *  First released 22.4.2010 at http://yacy.net
 *  
 *  This file is part of YaCy Content Integration
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file COPYING.LESSER.
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy.cider.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.shared.PrefixMapping;

public class CIDER {

    
    // The steering name space contains information of how the resource was obtained, loaded and processed
    // it is information that has no static relation to the content and may be specific to the user's actions
    public static final String STEERING_NS      = "http://cider.yacy.net/vocabularies/cider_steering#";
    public static final Model  steering_model   = ModelFactory.createDefaultModel();
    
    // the references name space contains information about the hiarachy and linking of the content.
    // This may i.e. address that a document is linked or cited by other document or that the document
    // is embedded into another document or vice versa (it may list all resources that are embedded in this document)
    public static final String REFERENCES_NS    = "http://cider.yacy.net/vocabularies/cider_references#";
    public static final Model  references_model = ModelFactory.createDefaultModel();

    // the data name space contains information that the parser extracted from the content. The content information
    // is stored into metadata object of appropriate name spaces (i.e. DC, FOAF) where applicable, but for some information
    // there are not enough describing metadata vocabualries available. This applied i.e. for the complete plain text 'bag' that
    // shall be passed to an indexing service or for hierarchical data structures that can 'artificially' be defined to fill
    // sources that contain ranking information (i.e. headlines or emphasized text) and are also usefull for indexer and ranking
    public static final String DATA_NS          = "http://cider.yacy.net/vocabularies/cider_data#";
    public static final Model  data_model       = ModelFactory.createDefaultModel();
    
    static {
        // extend the prefix mapping with short forms for the CIDER vocabulary
        PrefixMapping prefixMapping = ModelFactory.getDefaultModelPrefixes();
        prefixMapping.setNsPrefix("ciderSteering", STEERING_NS);
        prefixMapping.setNsPrefix("ciderReferences", REFERENCES_NS);
        prefixMapping.setNsPrefix("ciderData", DATA_NS);
        ModelFactory.setDefaultModelPrefixes(prefixMapping);
    }
    
    public static String getSteeringURI() {
        return STEERING_NS;
    }
  
    public static String getReferencesURI() {
        return REFERENCES_NS;
    }
  
    public static String getDataURI() {
        return DATA_NS;
    }
  
    public static final Property
        // definition of the cider steering vocabulary 
        steering_loaded_date    = steering_model.createProperty(STEERING_NS, "loadedDate"), // date when the content was loaded into application from 'outside'
        steering_parser_name    = steering_model.createProperty(STEERING_NS, "parserName"), // name of the parser that was used to parse the content
        steering_parsed_date    = steering_model.createProperty(STEERING_NS, "parsedDate"), // date when the content was parsed

        // definition of the cider references vocabulary 
        references_referrer_URI = references_model.createProperty(REFERENCES_NS, "referrerURI"), // as defined in http://www.ietf.org/rfc/rfc2616.txt 14.36 as 'referer'

        // definition of the cider data vocabulary 
        data_content_text       = data_model.createProperty(DATA_NS, "contentText"); // the whole content as plain text, encoded as UTF-8
        
}
