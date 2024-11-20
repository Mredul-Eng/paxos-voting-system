import java.util.concurrent.locks.ReentrantLock;

public class Acceptor {
    private Proposal lastAcceptedProposal; // keep track of last accepted proposal
    private final String memberName;
    private final ReentrantLock acceptedProposalLock = new ReentrantLock();

    public Acceptor(String memberName) {
        this.memberName = memberName;
        this.lastAcceptedProposal = new Proposal(0, null);
    }

    public Promise onPrepare(Proposal newProposal) {
        acceptedProposalLock.lock();
        try{
            //if new proposal number is getter than existing proposal number then acceptor accept that proposal and return a promise as a response
            if(newProposal.getProposalNumber() > lastAcceptedProposal.getProposalNumber()) {
                return new Promise(true, lastAcceptedProposal);
            }
            else{
                return new Promise(false, null);
            }
        }finally {
            acceptedProposalLock.unlock();
        }
    }

    //check if the last proposal and new proposal are equal or not. if equal then accept the proposal
    public boolean acceptProposal(Proposal newProposal) {
        acceptedProposalLock.lock();
        try{
            if(newProposal.getProposalNumber() >= lastAcceptedProposal.getProposalNumber()) {
                lastAcceptedProposal = newProposal;
                return true;
            }
            else return false;
        }finally {
            acceptedProposalLock.unlock();
        }
    }
}
