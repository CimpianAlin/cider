/**
 *  testdata.java
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

package net.yacy.cider.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import net.yacy.cider.document.URI;
import net.yacy.cider.parser.Parser;
import net.yacy.cider.parser.ParserException;
import net.yacy.cider.util.FileUtils;

import com.hp.hpl.jena.rdf.model.Model;

public class testdata {
    
    private static final Logger log = Logger.getLogger(FileUtils.class.getName());

    public static void main(String[] args) {
        
        // check assertion status
        boolean assertionenabled = false;
        assert assertionenabled = true;
        if (assertionenabled) System.out.println("Asserts are enabled");
        
        // go into headless awt mode
        System.setProperty("java.awt.headless", "true");
        
        // find test data
        File testdata = new File("ciderdict/testfiles");
        String[] testfilenames = testdata.list();
        
        for (String testfilename: testfilenames) {
            File testfile = new File(testdata, testfilename);
            try {
                URI testfileuri = new URI(testfile);
                Model model = Parser.parseSource(testfileuri);
                log.info("parsed " + testfileuri.toNormalform(true, true));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8");
                model.write(osw);
                osw.close();
                log.info("\n" + new String(baos.toByteArray(), "UTF-8") + "\n");
            } catch (ParserException e) {
                log.warn("no parser available for " + testfile.toString() + ": " + e.getMessage());
            } catch (MalformedURLException e) {
                log.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
