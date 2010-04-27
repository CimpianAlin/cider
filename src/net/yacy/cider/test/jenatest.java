/**
 *  jenatest.java
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

package net.yacy.cider.test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.VCARD;

public class jenatest {
    // some definitions
    static String personURI    = "http://somewhere/JohnSmith";
    static String givenName    = "John";
    static String familyName   = "Smith";
    static String fullName     = givenName + " " + familyName;

    public static void main(String[] args) {
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
    
        // create the resource
        Resource johnSmith = model.createResource(personURI)
                  .addProperty(VCARD.FN, fullName)
                  .addProperty(VCARD.N,
                               model.createResource()
                                    .addProperty(VCARD.Given, givenName)
                                    .addProperty(VCARD.Family, familyName));
        
        System.out.println(johnSmith.toString());
        
        // list the statements in the Model
        StmtIterator iter = model.listStatements();
    
        // print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();  // get next statement
            Resource  subject   = stmt.getSubject();     // get the subject
            Property  predicate = stmt.getPredicate();   // get the predicate
            RDFNode   object    = stmt.getObject();      // get the object
    
            System.out.print(subject.toString());
            System.out.print(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
               System.out.print(object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }
    
            System.out.println(" .");
        }
        
        // now write the model in XML form to a file
        model.write(System.out);
        
        // now write the model in XML form to a file
        model.write(System.out, "RDF/XML-ABBREV");

        // now write the model in XML form to a file
        model.write(System.out, "N-TRIPLE");

        
    }
}
