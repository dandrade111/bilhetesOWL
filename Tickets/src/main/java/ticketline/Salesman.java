package ticketline;

import java.util.Random;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import ontologyquery.DLQueryEngine;
import ontologyquery.DLQueryPrinter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import uk.ac.manchester.cs.jfact.JFactFactory;

public class Salesman extends Agent 
{
    private boolean finished = false;
    
    // Estruturas para guardar bilhetes disponiveis, vendidos?

    @Override
    protected void takeDown()
    {
        super.takeDown();

        try { DFService.deregister(this); }
        catch (Exception e) {e.printStackTrace();}

        System.out.println("deregistering "+this.getLocalName()+" from service list...");
    }

    @Override
    protected void setup()
    {
        super.setup();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("salesman");
        dfd.addServices(sd);

        try{ DFService.register(this, dfd );}
        catch (FIPAException fe) { fe.printStackTrace(); }

        System.out.println(this.getLocalName()+" starting!");

        this.addBehaviour(new ReceiveBehaviour());
    }

    public boolean isFinished() {
            return finished;
    }

    public void setFinished(boolean finished) {
            this.finished = finished;
    }
    
    private static OWLReasoner createReasoner(final OWLOntology rootOntology) {
        // We need to create an instance of OWLReasoner. An OWLReasoner provides
        // the basic query functionality that we need, for example the ability
        // obtain the subclasses of a class etc. To do this we use a reasoner
        // factory.
        // Create a reasoner factory.
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        return reasonerFactory.createReasoner(rootOntology);
    }

    private class ReceiveBehaviour extends CyclicBehaviour
    {
        @Override
        public void action() 
        {
            ACLMessage msg = receive();
            if (msg != null) 
            {            	
                ACLMessage reply = msg.createReply();
                reply.setConversationId(msg.getConversationId());
                
                if (msg.getPerformative() == ACLMessage.REQUEST)
                {
                    if (msg.getContent().equals("shutdown"))
                    {
                        System.out.println("["+ myAgent.getLocalName() + "] Agent "+myAgent.getLocalName()+" exiting...");
                        setFinished(true);
                        reply.setPerformative(ACLMessage.CONFIRM);
                        myAgent.send(reply);
                    }
                    
                    if(msg.getContent().contains("check")){
                        try {
                            // Load ontology
                            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                            OWLOntology ontology= Ticketline.load(manager);
                            System.out.println("["+ myAgent.getLocalName() + "] Loaded ontology: " + ontology.getOntologyID().getOntologyIRI().get());
                            
                            //OWLReasoner reasoner = createReasoner(ontology);
                            
                            OWLReasonerConfiguration config = new SimpleConfiguration(50000);
                            OWLReasonerFactory reasonerFactory= new JFactFactory();
                            OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
                            
                           //System.out.println(reasoner.getReasonerName());
                            // Entities are named using IRIs. These are usually too long for use
                            // in user interfaces. To solve this
                            // problem, and so a query can be written using short class,
                            // property, individual names we use a short form
                            // provider. In this case, we'll just use a simple short form
                            // provider that generates short froms from IRI
                            // fragments.
                            ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
                            // Create the DLQueryPrinter helper class. This will manage the
                            // parsing of input and printing of results
                            DLQueryPrinter dlQueryPrinter = new DLQueryPrinter(
                                    new DLQueryEngine(reasoner, shortFormProvider),
                                    shortFormProvider);
                            
                            // Execute the query received
                            String result = dlQueryPrinter.askQuery(msg.getContent().replace("check", ""));
                            System.out.println("["+ myAgent.getLocalName() + "] "+result);
                            
                            reply.setContent(result);
                            myAgent.send(reply);
                            
                        } catch (OWLOntologyCreationException e) {
                            System.out.println("["+ myAgent.getLocalName() + "] Could not load ontology: " + e.getMessage());
                        }
                    }
                    if(msg.getContent().contains("buy")){
                        String received =  msg.getContent().replace("buy", "");
                        if(received.equals("No tickets found!")){
                            System.out.println("["+ myAgent.getLocalName() + "] "+received);
                        }
                        else {
                            System.out.println("["+ myAgent.getLocalName() + "] Ticket " + msg.getContent().replace("buy", "") + " bought.");
                        }
                        reply.setContent("OK");
                        myAgent.send(reply);
                    }
                }
                else
                {
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    reply.setContent("["+ myAgent.getLocalName() + "] Unrecognized request performative. Must be ACLMessage.REQUEST!");
                    myAgent.send(reply);
                }
            }

        if (isFinished())
            myAgent.doDelete();
        
        block();
        }
    }
}
