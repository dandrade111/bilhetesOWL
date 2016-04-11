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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.DefaultListModel;
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
    public String query = "";
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
        final ClientGUI i = new ClientGUI();
        i.setVisible(true);
        i.getjButton2().addActionListener(new ActionListener(){
              public void actionPerformed(ActionEvent e)
              {
                query = i.getjTextField1().getText();
                ACLMessage msg = new ACLMessage();
                msg.setPerformative(ACLMessage.REQUEST);

                AID receiver = new AID();
                receiver.setLocalName("salesman");
                msg.addReceiver(receiver);

                String msgContent = "check"+query;
                msg.setContent(msgContent);

                System.out.println("["+ getLocalName() + "] Query: " + msgContent);
                send(msg);
              }
            });
        
        i.getjButton1().addActionListener(new ActionListener(){
              public void actionPerformed(ActionEvent e)
              {
                query = i.getjTextField1().getText();
                ACLMessage msg = new ACLMessage();
                msg.setPerformative(ACLMessage.REQUEST);

                AID receiver = new AID();
                receiver.setLocalName("salesman");
                msg.addReceiver(receiver);

                String msgContent = "buy" + i.getjList1().getSelectedValue().toString();
                msg.setContent(msgContent);
                send(msg);
              }
            });
        
        this.addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage req = receive();
                if (req != null) {   
                    if(req.getContent().contains("QUERY")){
                        ACLMessage msg = new ACLMessage();
                        msg.setPerformative(ACLMessage.REQUEST);

                        AID receiver = new AID();
                        receiver.setLocalName("salesman");
                        msg.addReceiver(receiver);
                        
                        //System.out.println(req.getContent());
                        String [] split = req.getContent().split("\\|");
                        String msgContent = "";
                        if(split[4].contains("[NONE]")){
                            msgContent = "buyNo tickets found!";
                            i.getjTextField2().setBackground(Color.red);
                            i.getjTextField2().setText("No tickets available!");
                            i.getjList1().setModel(new DefaultListModel());
                        }
                        else {
                            i.getjTextField2().setBackground(Color.yellow);
                            i.getjTextField2().setText("Tickets available!");
                            DefaultListModel listModel = new DefaultListModel();
                            String [] p = split[4].split(",");
                            String f = "";
                            for(int i = 1; i < p.length; i++)
                                listModel.addElement(p[i]);
                            
                            i.getjList1().setModel(listModel);
                            
                            //msgContent = "buy"+p[1];
                        }
                        //msg.setContent(msgContent);
                        //myAgent.send(msg);
                    }
                    else if(req.getContent().contains("OK")){
                        i.getjTextField2().setBackground(Color.green);
                        i.getjTextField2().setText("Ticket bought!");
                    }
                    else if(req.getContent().contains("SOLD")){
                        i.getjTextField2().setBackground(Color.red);
                        i.getjTextField2().setText("Ticket already bought!");
                    }
                    else {
                        ACLMessage msg = new ACLMessage();
                        msg.setPerformative(ACLMessage.REQUEST);

                        AID receiver = new AID();
                        receiver.setLocalName("salesman");
                        msg.addReceiver(receiver);

                        String msgContent = "check"+req.getContent();
                        msg.setContent(msgContent);
                        
                        System.out.println("["+ myAgent.getLocalName() + "] Query: " + msgContent);
                        myAgent.send(msg);
                    }
                }
            }
        });
    }
}
