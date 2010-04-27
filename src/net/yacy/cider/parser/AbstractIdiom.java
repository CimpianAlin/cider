/**
 *  AbstractIdiom.java
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
import net.yacy.cider.util.FileUtils;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;


public abstract class AbstractIdiom implements Idiom {
    
    protected static final Logger log = Logger.getLogger(FileUtils.class.getName());
    private final String parserName;
    
    public AbstractIdiom(String parserName) {
        this.parserName = parserName;
    }
    
    public abstract Set<MimeType> supportedMimeTypes();
    
    public abstract Set<Extension> supportedExtensions();

    public String getName() {
        return this.parserName;
    }

    public abstract Model parse(DataSource source) throws ParserException;

    /**
     * helper method to concatenate all string lists into one string
     * the strings are always separated by a semicolon
     * @param a list of strings
     * @return a semicolon-separated string with the listed strings
     */
    public static final String concat(String[] s) {
        if (s == null || s.length == 0) return "";
        if (s.length == 1) return s[0];
        StringBuilder sb = new StringBuilder(s.length * 8);
        for (int i = 0; i < s.length - 1; i++) sb.append(s[i]).append(';');
        sb.append(s[s.length - 1]);
        return sb.toString();
    }
}
