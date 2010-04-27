/**
 *  pdfIdiom.java
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

package net.yacy.cider.parser.idiom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.util.PDFTextStripper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.VCARD;

import net.yacy.cider.document.DataSource;
import net.yacy.cider.document.Extension;
import net.yacy.cider.document.MimeType;
import net.yacy.cider.parser.AbstractIdiom;
import net.yacy.cider.parser.Idiom;
import net.yacy.cider.parser.ParserException;
import net.yacy.cider.vocabulary.CIDER;

public class pdfIdiom extends AbstractIdiom implements Idiom {

    private static final Set<MimeType>  SUPPORTED_MIME_TYPES = new HashSet<MimeType>();
    private static final Set<Extension> SUPPORTED_EXTENSIONS = new HashSet<Extension>();
    private static final Set<String>    USED_VOCABULARIES    = new HashSet<String>();
    
    static {
        SUPPORTED_EXTENSIONS.add(Extension.PDF);
        SUPPORTED_MIME_TYPES.add(MimeType.APPLICATION_PDF);
        SUPPORTED_MIME_TYPES.add(MimeType.APPLICATION_XPDF);
        SUPPORTED_MIME_TYPES.add(MimeType.APPLICATION_ACROBAT);
        SUPPORTED_MIME_TYPES.add(MimeType.APPLICATION_VNDPDF);
        SUPPORTED_MIME_TYPES.add(MimeType.TEXT_PDF);
        SUPPORTED_MIME_TYPES.add(MimeType.TEXT_XPDF);
        USED_VOCABULARIES.add(DC.getURI());
        USED_VOCABULARIES.add(VCARD.getURI());
        USED_VOCABULARIES.add(CIDER.getDataURI());
    }
    
    public pdfIdiom() {        
        super("Acrobat Portable Document Parser"); 
    }

    @Override
    public Set<MimeType> supportedMimeTypes() {
        return SUPPORTED_MIME_TYPES;
    }

    @Override
    public Set<Extension> supportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }

    @Override
    public Set<String> usedVocabularies() {
        return USED_VOCABULARIES;
    }
    
    @Override
    public Model parse(DataSource source) throws ParserException {
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
        Resource resource = source.hasURI() ? model.createResource(source.getURI().toNormalform(true, true)) : model.createResource();
        
        // open pdf document
        final PDDocument theDocument;
        final PDFParser parser;
        try {
            parser = new PDFParser(source.getStream());
            parser.parse();
            theDocument = parser.getPDDocument();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ParserException(e.getMessage(), source.getURI());
        }
        
        if (theDocument.isEncrypted()) {
            try {
                theDocument.openProtection(new StandardDecryptionMaterial(""));
            } catch (BadSecurityHandlerException e) {
                throw new ParserException("PDF Encrypted (BadSecurityHandlerException): " + e.getMessage(), source.getURI(), e);
            } catch (IOException e) {
                throw new ParserException("PDF Encrypted (IOException): " + e.getMessage(), source.getURI(), e);
            } catch (CryptographyException e) {
                throw new ParserException("PDF Encrypted (CryptographyException): " + e.getMessage(), source.getURI(), e);
            }
            final AccessPermission perm = theDocument.getCurrentAccessPermission();
            if (perm == null || !perm.canExtractContent())
                throw new ParserException("PDF cannot be decrypted", source.getURI());
        }
        
        // get metadata
        final PDDocumentInformation theDocInfo = theDocument.getDocumentInformation();            
        String docTitle = null, docSubject = null, docAuthor = null, docKeywordStr = null;
        if (theDocInfo != null) {
            docTitle = theDocInfo.getTitle();
            docSubject = theDocInfo.getSubject();
            docAuthor = theDocInfo.getAuthor();
            docKeywordStr = theDocInfo.getKeywords();
        }
        
        if (docAuthor != null && docAuthor.length() > 0) {
            resource.addProperty(VCARD.FN, docAuthor);
            resource.addProperty(DC.creator, docAuthor);
        }
        if (docSubject != null && docSubject.length() > 0) {
            resource.addProperty(DC.subject, docSubject);
        }
        if (docTitle != null && docTitle.length() > 0) {
            resource.addProperty(DC.title, docTitle);
        }
        String[] docKeywords = null;
        if (docKeywordStr != null && docKeywordStr.length() > 0) {
            docKeywords = docKeywordStr.split(" |,");
            resource.addProperty(DC.coverage, concat(docKeywords));
        }
        
        // get the content
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer;
        try {
            writer = new OutputStreamWriter(baos, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            writer = new OutputStreamWriter(baos);
        }
        try {
            final PDFTextStripper stripper = new PDFTextStripper();
            stripper.writeText(theDocument, writer);
            theDocument.close();           
            writer.close();
        } catch (IOException e) {
            if (writer != null) try { writer.close(); } catch (final Exception ex) {}
            throw new ParserException("PDF content reader", source.getURI(), e);
        }
        String content;
        try {
            content = new String(baos.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            content = new String(baos.toByteArray());
        }
        if (content != null && content.length() > 0) {
            resource.addProperty(CIDER.data_content_text, content);
        }
        
        return model;
    }
    
}
