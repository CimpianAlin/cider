/**
 *  DataSource.java
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


package net.yacy.cider.document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;

import net.yacy.cider.util.FileUtils;

import org.apache.log4j.Logger;

public class DataSource extends HashMap<DataSource.Names, Object> {
    
    private static final Logger log = Logger.getLogger(FileUtils.class.getName());
    private static final long serialVersionUID = 2854710546889566386L;

    public DataSource() {
        super();
    }

    public DataSource(
            final URI location,
            MimeType mimeType,
            final String charset,
            final InputStream sourceStream
        ) {
        super();
        this.setURI(location);
        this.setMimeType(mimeType);
        this.setCharset(charset);
        this.setStream(sourceStream);
    }

    public DataSource(
            final URI location,
            MimeType mimeType,
            final String charset,
            final byte[] sourceArray
        ) {
        super();
        this.setURI(location);
        this.setMimeType(mimeType);
        this.setCharset(charset);
        this.setArray(sourceArray);
    }

    public void setURI(URI uri) {
        super.put(Names.URI, uri);
    }
    
    public void setStream(InputStream is) {
        super.put(Names.STREAM, is);
    }
    
    public void setArray(byte[] b) {
        super.put(Names.ARRAY, b);
    }
    
    public void setMimeType(MimeType mime) {
        super.put(Names.MIMETYPE, mime);
    }
    
    public void setFileExtension(Extension ext) {
        super.put(Names.FILEEXT, ext);
    }
    
    public void setSize(long size) {
        super.put(Names.SIZE, Long.valueOf(size));
    }
    
    public void setCharset(String charset) {
        super.put(Names.CHARSET, charset);
    }
    
    public boolean hasURI() { return this.containsKey(Names.URI); }
    public boolean hasStream() { return this.containsKey(Names.STREAM); }
    public boolean hasArray() { return this.containsKey(Names.ARRAY); }
    public boolean hasMimeType() { return this.containsKey(Names.MIMETYPE); }
    public boolean hasFileExtension() { return this.containsKey(Names.FILEEXT); }
    public boolean hasSize() { return this.containsKey(Names.SIZE); }
    public boolean hasCharset() { return this.containsKey(Names.CHARSET); }

    public URI getURI() {
        Object o = this.get(Names.URI);
        if (o == null) return null;
        if (o instanceof URI) return (URI) o;
        if (o instanceof String)
            try {
                return new URI((String) o);
            } catch (MalformedURLException e) {
                log.error(e.getMessage(), e);
                return null;
            }
       return null;
    }
    
    public InputStream getStream() {
        if (this.containsKey(Names.STREAM)) {
            return (InputStream) this.get(Names.STREAM);
        }
        if (this.containsKey(Names.ARRAY)) {
            return new ByteArrayInputStream((byte[]) this.get(Names.ARRAY));
        }
        if (this.containsKey(Names.URI)) {
            try {
                return ((URI) this.get(Names.URI)).getInputStream();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }
        return null;
    }
    
    public byte[] getArray() {
        if (this.containsKey(Names.ARRAY)) {
            return (byte[]) this.get(Names.ARRAY);
        }
        if (this.containsKey(Names.STREAM)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                FileUtils.copy((InputStream) this.get(Names.STREAM), baos);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return null;
            }
            byte[] b = baos.toByteArray();
            this.put(Names.ARRAY, b);
            return b;
        }
        if (this.containsKey(Names.URI)) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileUtils.copy(((URI) this.get(Names.URI)).getInputStream(), baos);
                byte[] b = baos.toByteArray();
                this.put(Names.ARRAY, b);
                return b;
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }
        }
        return null;
    }
    
    public MimeType getMimeType() {
        Object o = this.get(Names.MIMETYPE);
        if (o == null) return null;
        if (o instanceof MimeType) return (MimeType) o;
        if (o instanceof String) return MimeType.getMimetype((String) o);
        return null;
    }
    
    public Extension getFileExtension() {
        Object o = this.get(Names.FILEEXT);
        if (o == null) return null;
        if (o instanceof Extension) return (Extension) o;
        if (o instanceof String) return Extension.getFileNameExtension((String) o);
        return null;
    }
    
    public long getSize() {
        Object o = this.get(Names.SIZE);
        if (o == null) return -1;
        if (o instanceof Long) return ((Long) o).longValue();
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof String) return (Long.parseLong((String) o));
        return -1;
    }
    
    public String getCharset() {
        Object o = this.get(Names.CHARSET);
        if (o == null) return "UTF-8";
        if (o instanceof String) return (String) o;
        return "UTF-8";
    }

    public enum Names {

        URI,           // the string representation of an uri.
                       // If neither an stream nor an array is given,
                       // then the source can be loaded from that uri
        
        STREAM,        // an java.io.InputStream that delivers the content
        
        ARRAY,         // a byte[] that contains the content.
                       // if this is present than no data is loaded over network.

        MIMETYPE,      // a mime type (guess) of the content

        FILEEXT,       // a file name extension that may apply also
                       // if the URI has not an extension
        
        SIZE,          // the size of the source data in bytes

        CHARSET,       // the encoding charset of the source
        
        VOCABULARIES;  // names of the vocabularies
                       // that shall be used to generate a parsing result.
                       // names are semicolon-separated
    }
}
