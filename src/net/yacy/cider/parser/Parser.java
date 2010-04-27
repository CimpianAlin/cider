/**
 *  Parser.java
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.yacy.cider.document.Charset;
import net.yacy.cider.document.DataSource;
import net.yacy.cider.document.Extension;
import net.yacy.cider.document.MimeType;
import net.yacy.cider.document.URI;
import net.yacy.cider.parser.idiom.pdfIdiom;
import net.yacy.cider.util.FileUtils;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

public class Parser {

    private static final Logger log = Logger.getLogger(FileUtils.class.getName());

    private static final Map<MimeType, Idiom> mime2parser = new ConcurrentHashMap<MimeType, Idiom>();
    private static final Map<Extension, Idiom> ext2parser = new ConcurrentHashMap<Extension, Idiom>();
    private static final Map<Extension, MimeType> ext2mime = new ConcurrentHashMap<Extension, MimeType>();
    
    static {
        initParser(new pdfIdiom());
    }
    
    public static Set<Idiom> idioms() {
        Set<Idiom> c = new HashSet<Idiom>();
        c.addAll(ext2parser.values());
        c.addAll(mime2parser.values());
        return c;
    }

    private static void initParser(Idiom parser) {
        MimeType prototypeMime = null;
        for (MimeType mime: parser.supportedMimeTypes()) {
            // process the mime types
            if (prototypeMime == null) prototypeMime = mime;
            Idiom p0 = mime2parser.get(mime);
            if (p0 != null) log.error("parser for mime '" + mime + "' was set to '" + p0.getName() + "', overwriting with new parser '" + parser.getName() + "'.");
            mime2parser.put(mime, parser);
            log.info("Parser for mime type '" + mime + "': " + parser.getName());
        }
        
        if (prototypeMime != null) for (Extension ext: parser.supportedExtensions()) {
            MimeType s = ext2mime.get(ext);
            if (s != null) log.error("parser for extension '" + ext + "' was set to mime '" + s + "', overwriting with new mime '" + prototypeMime + "'.");
            ext2mime.put(ext, prototypeMime);
        }
        
        for (Extension ext: parser.supportedExtensions()) {
            // process the extensions
            Idiom p0 = ext2parser.get(ext);
            if (p0 != null) log.error("parser for extension '" + ext + "' was set to '" + p0.getName() + "', overwriting with new parser '" + parser.getName() + "'.");
            ext2parser.put(ext, parser);
            log.info("Parser for extension '" + ext + "': " + parser.getName());
        }
    }
    
    public static Model parseSource(final DataSource source) throws InterruptedException, ParserException {
        if (log.isDebugEnabled()) log.debug("Parsing '" + source.getURI().toNormalform(true, true) + "' from DataSource");
        List<Idiom> idioms = null;
        idioms = idiomParser(source.getURI(), source.getMimeType());
        return parseSource(source, idioms);
    }
    
    public static Model parseSource(final URI location) throws InterruptedException, ParserException {
        try {
            return parseSource(location, mimeOf(location), "UTF-8", location.length(), location.getInputStream());
        } catch (IOException e) {
            throw new ParserException("cannot read file: " + e.getMessage(), location, e);
        } 
    }
    
    public static Model parseSource(
            final URI location,
            final MimeType mimeType,
            final String charset,
            final File sourceFile
        ) throws InterruptedException, ParserException {

        BufferedInputStream sourceStream = null;
        try {
            if (log.isDebugEnabled()) log.debug("Parsing '" + location + "' from file");
            if (!sourceFile.exists() || !sourceFile.canRead() || sourceFile.length() == 0) {
                final String errorMsg = sourceFile.exists() ? "Empty resource file." : "No resource content available (2).";
                log.info("Unable to parse '" + location + "'. " + errorMsg);
                throw new ParserException(errorMsg, location);
            }
            sourceStream = new BufferedInputStream(new FileInputStream(sourceFile));
            return parseSource(location, mimeType, charset, sourceFile.length(), sourceStream);
        } catch (final Exception e) {
            if (e instanceof InterruptedException) throw (InterruptedException) e;
            if (e instanceof ParserException) throw (ParserException) e;
            log.error("Unexpected exception in parseSource from File: " + e.getMessage(), e);
            throw new ParserException("Unexpected exception: " + e.getMessage(), location);
        } finally {
            if (sourceStream != null)try {
                sourceStream.close();
            } catch (final Exception ex) {}
        }
    }
    
    public static Model parseSource(
            final URI location,
            MimeType mimeType,
            final String charset,
            final long contentLength,
            final InputStream sourceStream
        ) throws InterruptedException, ParserException {
        if (log.isDebugEnabled()) log.debug("Parsing '" + location + "' from stream");
        List<Idiom> idioms = null;
        idioms = idiomParser(location, mimeType);

        assert !idioms.isEmpty();
        
        // if we do not have more than one parser or the content size is over MaxInt
        // then we use only one stream-oriented parser.
        if (idioms.size() == 1 || contentLength > Integer.MAX_VALUE) {
            // use a specific stream-oriented parser
            return parseSource(location, mimeType, idioms.get(0), charset, contentLength, sourceStream);
        }
        
        // in case that we know more parsers we first transform the content into a byte[] and use that as base
        // for a number of different parse attempts.
        try {
            return parseSource(new DataSource(location, mimeType, charset, FileUtils.read(sourceStream, (int) contentLength)), idioms);
        } catch (IOException e) {
            throw new ParserException(e.getMessage(), location);
        }
    }
    
    public static Model parseSource(
            final URI location,
            String mimeTypeString,
            final String charset,
            final byte[] sourceArray
        ) throws InterruptedException, ParserException {
        if (log.isDebugEnabled()) log.debug("Parsing '" + location + "' from stream");
        MimeType mimeType = MimeType.getMimetype(mimeTypeString);
        List<Idiom> idioms = null;
        idioms = idiomParser(location, mimeType);

        assert !idioms.isEmpty();        
        return parseSource(new DataSource(location, mimeType, charset, sourceArray), idioms);
    }

    private final static Model parseSource(
            final URI location,
            MimeType mimeType,
            Idiom idiom,
            final String charset,
            final long contentLength,
            final InputStream sourceStream
        ) throws InterruptedException, ParserException {
        if (log.isDebugEnabled()) log.debug("Parsing '" + location + "' from stream");
        final Extension ext = location.getFileExtension();
        final String documentCharset = Charset.patchCharsetEncoding(charset);
        assert idiom != null;

        if (log.isDebugEnabled()) log.info("Parsing " + location + " with mimeType '" + mimeType + "' and file extension '" + ((ext == null) ? "null" : ext.toString())  + "'.");
        try {
            return idiom.parse(new DataSource(location, mimeType, documentCharset, sourceStream));
        } catch (ParserException e) {
            throw new ParserException("parser failed: " + idiom.getName(), location);
        }
    }

    public final static Model parseSource(
            final DataSource source,
            List<Idiom> idioms
        ) throws InterruptedException, ParserException {
        if (log.isDebugEnabled()) log.debug("Parsing " + source.getURI());
        assert !idioms.isEmpty();

        Model doc = null;
        HashMap<Idiom, ParserException> failedParser = new HashMap<Idiom, ParserException>();
        for (Idiom parser: idioms) {
            try {
                doc = parser.parse(source);
            } catch (ParserException e) {
                failedParser.put(parser, e);
                //log.warn("tried parser '" + parser.getName() + "' to parse " + location.toNormalform(true, false) + " but failed: " + e.getMessage(), e);
            }
            if (doc != null) break;
        }
        
        if (doc == null) {
            if (failedParser.size() == 0) {
                //log.warn("Unable to parse '" + location + "'. " + errorMsg);
                throw new ParserException("parsing failed", source.getURI());
            } else {
                String failedParsers = "";
                for (Map.Entry<Idiom, ParserException> error: failedParser.entrySet()) {
                    log.warn("tried parser '" + error.getKey().getName() + "' to parse " + source.getURI().toNormalform(true, false) + " but failed: " + error.getValue().getMessage(), error.getValue());
                    failedParsers += error.getKey().getName() + " ";
                }
                throw new ParserException("all parser failed: " + failedParsers, source.getURI());
            }
        }
        return doc;
    }
    
    /**
     * check if the parser supports the given content.
     * @param url
     * @param mimeType
     * @return returns null if the content is supported. If the content is not supported, return a error string.
     */
    public static String supports(final URI url, MimeType mimeType) {
        try {
            // try to get a parser. If this works, we don't need the parser itself, we just return null to show that everything is ok.
            List<Idiom> idioms = idiomParser(url, mimeType);
            return (idioms == null || idioms.isEmpty()) ? "no parser found" : null;
        } catch (ParserException e) {
            // in case that a parser is not available, return a error string describing the problem.
            return e.getMessage();
        }
    }
    
    /**
     * find a parser for a given url and mime type
     * because mime types returned by web severs are sometimes wrong, we also compute the mime type again
     * from the extension that can be extracted from the url path. That means that there are 3 criteria
     * that can be used to select a parser:
     * - the given extension
     * - the given mime type
     * - the mime type computed from the extension
     * @param url the given url
     * @param mimeType the given mime type
     * @return a list of Idiom parsers that may be appropriate for the given criteria
     * @throws ParserException
     */
    private static List<Idiom> idiomParser(final URI url, MimeType mimeType) throws ParserException {
        List<Idiom> idioms = new ArrayList<Idiom>(2);
        
        // check extension
        Extension ext = url.getFileExtension();
        Idiom idiom;
        if (ext != null) {
            idiom = ext2parser.get(ext);
            if (idiom != null) idioms.add(idiom);
        }
        
        // check given mime type
        if (mimeType != null) {
            idiom = mime2parser.get(mimeType);
            if (idiom != null && !idioms.contains(idiom)) idioms.add(idiom);
        }
        
        // check mime type computed from extension
        MimeType mimeType2 = ext2mime.get(ext);
        if (mimeType2 == null) return idioms; // in this case we are a bit more lazy
        idiom = mime2parser.get(mimeType2);
        if (idiom != null && !idioms.contains(idiom)) idioms.add(idiom);
        
        // finally check if we found any parser
        if (idioms.isEmpty()) throw new ParserException("no parser found for extension '" + ext + "' and mime type '" + mimeType.toString() + "'", url);
        
        return idioms;
    }
    
    public static String supportsMime(MimeType mimeType) {
        if (mimeType == null) return "mimeType == null";
        if (mime2parser.get(mimeType) == null) return "no parser for mime '" + mimeType + "' available";
        return null;
    }
    
    public static String supportsExtension(final URI url) {
        if (url == null) return "url == null";
        Extension ext = url.getFileExtension();
        if (ext == null) return null;
        MimeType mimeType = ext2mime.get(ext);
        if (mimeType == null) return "no parser available";
        Idiom idiom = mime2parser.get(mimeType);
        assert idiom != null;
        if (idiom == null) return "no parser available (internal error!)";
        return null;
    }
    
    public static MimeType mimeOf(URI url) {
        if (url == null) return null;
        return mimeOf(url.getFileExtension());
    }
    
    public static MimeType mimeOf(Extension ext) {
        if (ext == null) return null;
        return ext2mime.get(ext);
    }
 
}
