import java.util.Objects;

//objects of Proposals can be compared through Comparable Interface
public class Proposal implements Comparable<Proposal>{
    private final long proposalNumber; //unique number for each proposal
    private final String content; //content or proposer name of the proposal

    public Proposal(long proposalNumber, String content) {
        this.proposalNumber = proposalNumber;
        this.content = content;
    }
    public long getProposalNumber() {
        return proposalNumber;
    }

    public String getContent() {
        return content;
    }

    //this method used to compare two proposals based on their proposal number.
    // the comparison is essential because the proposal with the highest proposal number will get high priority
    @Override
    public int compareTo(Proposal o) {
        return Long.compare(this.proposalNumber, o.getProposalNumber());
    }

    //check if the two proposal are equal or not
    @Override
    public boolean equals(Object o) {
        //check if object o is the same instance of current object
        if(this == o) return true;
        //check if object is null or in different class, then return false
        if(o == null || getClass() != o.getClass()) return false;
        else {
            Proposal proposal = (Proposal) o;
            return proposalNumber == proposal.getProposalNumber() && Objects.equals(content, proposal.getContent());
        }
    }

    //generates hash codes based on the proposal number and content
    @Override
    public int hashCode() {
        return Objects.hash(proposalNumber, content);
    }

    //print out the proposal as string
    @Override
    public String toString() {
        return "proposalNumber: " + proposalNumber + ", content: " + content;
    }
}
