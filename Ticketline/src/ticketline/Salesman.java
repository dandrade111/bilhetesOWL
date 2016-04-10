package ticketline;

import java.util.Random;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Salesman extends Agent 
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
                        System.out.println("Agent "+myAgent.getLocalName()+" exiting...");
                        setFinished(true);
                        reply.setPerformative(ACLMessage.CONFIRM);
                        myAgent.send(reply);
                    }
                    
                    if(msg.getContent().contains("buy")){
                        // Verificar na ontologia
                        // QUERY
                    }
                }
                else
                {
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    reply.setContent("Unrecognized request performative. Must be ACLMessage.REQUEST!");
                    myAgent.send(reply);
                }
            }

        if (isFinished())
            myAgent.doDelete();
        
        block();
        }
    }
}
