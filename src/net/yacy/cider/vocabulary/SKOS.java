/**
 *  SKOS.java
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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * SKOS - Simple Knowledge Organization System
 * Vocabulary for the SKOS classification scheme: http://www.w3.org/2004/02/skos/
 * See also the SKOS primer: http://www.w3.org/TR/skos-primer/
 * @author Michael Christen
 */
public class SKOS {
	
    // define models
	public  static final String CORE_NS          = "http://www.w3.org/2004/02/skos/core#";
	private static       Model  core_model       = ModelFactory.createDefaultModel();
	
    public  static final String MAPPING_NS       = "http://www.w3.org/2004/02/skos/mapping#";
    private static       Model  mapping_model    = ModelFactory.createDefaultModel();
    
    public  static final String EXTENSIONS_NS    = "http://www.w3.org/2004/02/skos/extensions#";
	private static       Model  extensions_model = ModelFactory.createDefaultModel();
	
	static {
        // extend the prefix mapping with short forms for the SKOS vocabulary
        PrefixMapping prefixMapping = ModelFactory.getDefaultModelPrefixes();
        prefixMapping.setNsPrefix("skos", CORE_NS);
        prefixMapping.setNsPrefix("skos", MAPPING_NS);
        prefixMapping.setNsPrefix("skos", EXTENSIONS_NS);
        ModelFactory.setDefaultModelPrefixes(prefixMapping);
    }
	
	public static final Resource
	    ConceptScheme       = core_model.createResource(CORE_NS + "ConceptScheme"),
	    Concept             = core_model.createResource(CORE_NS + "Concept"),
	    Collection          = core_model.createResource(CORE_NS + "Collection"),
        CollectableProperty = core_model.createResource(CORE_NS + "CollectableProperty"),
	    OrderedCollection   = core_model.createResource(CORE_NS + "OrderedCollection"),
        AND                 = mapping_model.createResource(MAPPING_NS + "AND"),
        OR                  = mapping_model.createResource(MAPPING_NS +  "OR"),
        NOT                 = mapping_model.createResource(MAPPING_NS + "NOT");
    
	public static final Property
	    // core vocabulary
    	core_prefLabel                = core_model.createProperty(CORE_NS, "prefLabel"),
    	core_altLabel                 = core_model.createProperty(CORE_NS, "altLabel"),
    	core_hiddenLabel              = core_model.createProperty(CORE_NS, "hiddenLabel"),
    	core_symbol                   = core_model.createProperty(CORE_NS, "symbol"),
    	core_prefSymbol               = core_model.createProperty(CORE_NS, "prefSymbol"),
    	core_altSymbol                = core_model.createProperty(CORE_NS, "altSymbol"),
    	core_note                     = core_model.createProperty(CORE_NS, "note"),
    	core_definition               = core_model.createProperty(CORE_NS, "definition"),
    	core_example                  = core_model.createProperty(CORE_NS, "example"),
    	core_semanticRelation         = core_model.createProperty(CORE_NS, "semanticRelation"),
    	core_broader                  = core_model.createProperty(CORE_NS, "broader"),
    	core_narrower                 = core_model.createProperty(CORE_NS, "narrower"),
    	core_related                  = core_model.createProperty(CORE_NS, "related"),
    	core_inScheme                 = core_model.createProperty(CORE_NS, "inScheme"),
    	core_hasTopConcept            = core_model.createProperty(CORE_NS, "hasTopConcept"),
    	core_member                   = core_model.createProperty(CORE_NS, "member"),
    	core_memberList               = core_model.createProperty(CORE_NS, "memberList"),
    	core_subject                  = core_model.createProperty(CORE_NS, "subject"),
    	core_primarySubject           = core_model.createProperty(CORE_NS, "primarySubject"),
    	core_isSubjectOf              = core_model.createProperty(CORE_NS, "isSubjectOf"),
    	core_isPrimarySubjectOf       = core_model.createProperty(CORE_NS, "isPrimarySubjectOf"),
    	core_subjectIndicator         = core_model.createProperty(CORE_NS, "subjectIndicator"),
    	
    	// mapping vocabulary
    	mapping_mappingRelation       = mapping_model.createProperty(MAPPING_NS, "mappingRelation"),
    	mapping_exactMatch            = mapping_model.createProperty(MAPPING_NS, "exactMatch"),
    	mapping_broadMatch            = mapping_model.createProperty(MAPPING_NS, "broadMatch"),
    	mapping_majorMatch            = mapping_model.createProperty(MAPPING_NS, "majorMatch"),
    	mapping_minorMatch            = mapping_model.createProperty(MAPPING_NS, "minorMatch"),
    	mapping_narrowMatch           = mapping_model.createProperty(MAPPING_NS, "narrowMatch"),
    	
    	// extensions vocabulary
    	extensions_broaderGeneric     = extensions_model.createProperty(EXTENSIONS_NS, "broaderGeneric"),
    	extensions_broaderInstantive  = extensions_model.createProperty(EXTENSIONS_NS, "broaderInstantive"),
    	extensions_broaderPartitive   = extensions_model.createProperty(EXTENSIONS_NS, "broaderPartitive"),
    	extensions_narrowerGeneric    = extensions_model.createProperty(EXTENSIONS_NS, "narrowerGeneric"),
    	extensions_narrowerInstantive = extensions_model.createProperty(EXTENSIONS_NS, "narrowerInstantive"),
    	extensions_narrowerPartitive  = extensions_model.createProperty(EXTENSIONS_NS, "narrowerPartitive"),
    	extensions_relatedHasPart     = extensions_model.createProperty(EXTENSIONS_NS, "relatedHasPart"),
    	extensions_relatedPartOf      = extensions_model.createProperty(EXTENSIONS_NS, "relatedPartOf");
}
