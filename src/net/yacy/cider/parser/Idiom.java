/**
 *  Idiom.java
 *  Copyright 2010 by Michael Peter Christen
 *  First released 27.4.2010 at http://yacy.net
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

package net.yacy.cider.parser;

import java.util.Set;

import net.yacy.cider.document.DataSource;
import net.yacy.cider.document.Extension;
import net.yacy.cider.document.MimeType;

import com.hp.hpl.jena.rdf.model.Model;

public interface Idiom {

    /**
     * Get the MimeType(s) that are supported by the parser
     * @return a set of MimeType denoting the supported mime types
     */
    public Set<MimeType> supportedMimeTypes();
    
    /**
     * Get the File extension(s) that are supported by the parser
     * @return a set of FileNameExtension denoting the supported file extensions
     */
    public Set<Extension> supportedExtensions();

    /**
     * Get the Vocabularies(s) that are used by the cognition module
     * @return a set of URI Strings denoting the used Vocabularies
     */
    public Set<String> usedVocabularies();
    
    /**
     * parse the given source and return a model containing resource objects
     * @param source the source as DataSource
     * @return
     * @throws ParserException
     */
    public Model parse(DataSource source) throws ParserException;

    /**
     * Returns the name of the parser
     * @return parser name
     */
    public String getName();
    
    /**
     * equal method is needed for Hashtable usage of this object
     * @param o
     * @return true if this object is equal to another object
     */
    public boolean equals(Object o);
    
    /**
     * hash code is needed for TreeMap usage of this object
     * @return a hash code for this object instance
     */
    public int hashCode();
    
}
