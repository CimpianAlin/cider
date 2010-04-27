/**
 *  CSV.java
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

package net.yacy.cider.util;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * helper class that provides parsing of smicolon-separated value lists
 * @author admin
 *
 */
public class CSV<A extends StringInit> extends ArrayList<A> {

    private static final long serialVersionUID = 6155284155595619965L;
    private static final Pattern semicolon_matcher = Pattern.compile(";");
    
    @SuppressWarnings("unchecked")
    public CSV(String s) {
        super();
        A a;
        for (String t: semicolon_matcher.split(s, 0)) {
            a = (A) new Object();
            a.init(t);
            this.add(a);
        }
    }
    
}
