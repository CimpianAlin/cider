/**
 *  ConsecutiveOfflineLexer.java
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

package net.yacy.cider;


import org.apache.log4j.Logger;

import net.yacy.cider.document.DataSource;
import net.yacy.cider.parser.Parser;
import net.yacy.cider.parser.ParserException;
import net.yacy.cider.util.FileUtils;

import com.hp.hpl.jena.rdf.model.Model;

public class ConsecutiveOfflineExtractor implements ConsecutiveExtractor {
    
    private static final Logger log = Logger.getLogger(FileUtils.class.getName());
    
    @Override
    public Model process(DataSource source) {
     
        try {
            return Parser.parseSource(source);
        } catch (InterruptedException e) {
            log.error("interrupted", e);
        } catch (ParserException e) {
            log.error("parser failed", e);
        }
        return null;
    }

}
