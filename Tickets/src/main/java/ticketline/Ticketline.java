/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ticketline;

import java.io.File;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author andregeraldes
 */
public class Ticketline {

    // Load ontology from file
    public static OWLOntology load(OWLOntologyManager manager) throws OWLOntologyCreationException {
        // The ontology is loaded from a file	 
        File ontologyfile = new File("/Users/andregeraldes/Documents/Mestrado/SI/Comp. Nat./bilhetesOWL/bilhetes.owl");
        return manager.loadOntologyFromOntologyDocument(ontologyfile);
    }
	
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        try {
            OWLOntology ontology= load(manager);
            System.out.println(ontology.getOntologyID().getOntologyIRI().get());
        } catch (OWLOntologyCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
