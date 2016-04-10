package ticketline;

import jade.core.AID;
import java.util.Random;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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

public class Client extends Agent 
{
    private boolean finished = false;

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
        sd.setType("client");
        dfd.addServices(sd);

        try{ DFService.register(this, dfd );}
        catch (FIPAException fe) { fe.printStackTrace(); }

        System.out.println(this.getLocalName()+" starting!");
        
        /*
        this.addBehaviour(new OneShotBehaviour() {

            @Override
            public void action() {
                ACLMessage msg = new ACLMessage();
                msg.setPerformative(ACLMessage.REQUEST);
                AID receiver = new AID();
                receiver.setLocalName("salesman");
                msg.addReceiver(receiver);
                   
                String msgContent = "buyTicket";
                msg.setContent(msgContent);

                myAgent.send(msg);
            }
        });*/
        
        this.addBehaviour(new CyclicBehaviour() {

            @Override
            public void action() {
                ACLMessage req = receive();
                if (req != null) {   
                    ACLMessage msg = new ACLMessage();
                    msg.setPerformative(ACLMessage.REQUEST);
                    AID receiver = new AID();
                    receiver.setLocalName("salesman");
                    msg.addReceiver(receiver);

                    String msgContent = "buy"+req.getContent();
                    msg.setContent(msgContent);

                    myAgent.send(msg);
                }
            }
        });
    }

    public boolean isFinished() {
            return finished;
    }

    public void setFinished(boolean finished) {
            this.finished = finished;
    }
}
