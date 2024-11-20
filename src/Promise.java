//this class represent the response given by the Acceptors
public class Promise {
    private final boolean acknowledge; // indicates whether the proposal accept(acknowledge or true) or not(false)
    private final Proposal proposal; // last accept proposal accept by the acceptors

    public Promise(boolean acknowledge, Proposal proposal) {
        this.acknowledge = acknowledge;
        this.proposal = proposal;
    }

    public boolean isAcknowledge() {
        return acknowledge;
    }
    public Proposal getProposal() {
        return proposal;
    }
}
