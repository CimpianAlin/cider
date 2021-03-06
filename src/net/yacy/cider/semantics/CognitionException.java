/**
 *  CognitionException.java
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

package net.yacy.cider.semantics;

import net.yacy.cider.document.URI;

public class CognitionException extends Exception {
    
private URI url = null;
    
    private static final long serialVersionUID = 1L;

    public CognitionException() {
        super();
    }

    public CognitionException(final String message, final URI url) {
        super(message + "; url = " + url.toNormalform(true, false));
        this.url = url;
    }

    public CognitionException(final String message, final URI url, Throwable cause) {
        super(message + "; url = " + url.toNormalform(true, false), cause);
        this.url = url;
    }
    
    public URI getURL() {
        return this.url;
    }
}
