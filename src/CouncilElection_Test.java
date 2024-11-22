public class CouncilElection_Test {

    public static void main(String[] args) {

        //start all the acceptors
        startAcceptors();

        //Test scenario 1 for two proposers(M1, M2) sending proposals at the same time
        System.out.println("\n===Testing Multiple Simultaneous Proposals for Council Election===\n");
        runSimultaneousProposals();

        //Test scenario 2 for immediate response for all members
        System.out.println("\n===Testing Immediate Response for All Members===\n");
        runImmediateResponse();

        //Test scenario 3 for simulate behaviour of M1 to M9 as their response times are vary
        System.out.println("\n===Testing Responses for Each Member with immediate response, Delays or Going Offline===\n");
        runDelayedOrNoResponse();
    }

    private static void startAcceptors() {
        for(int i = 1; i <= 9; i++){
            CouncilElection.acceptors.put(i, new Acceptor("M" + i)); //put acceptors with specific member id
            int memberId = i;
            new Thread(() -> CouncilElection.runAcceptors(memberId)).start(); // start a new thread for each acceptor and run each acceptor for separate thread, allowing it to run concurrently with other threads.

        }
    }

    private static void runDelayedOrNoResponse() {
        try{
            Thread.sleep(2000);
            try{
                //allow 3 members to propose
                for(int i = 1; i <= 3; i++){
                    int proposerId = i;
                    new Thread(() -> CouncilElection.runProposers("M" + proposerId, proposerId)).start();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    //simulate immediate responses for all members
    private static void runImmediateResponse() {
        //run proposers for two members and simulate immediate response for all members.
        runSimultaneousProposals();
    }

    //test the Paxos implementation behaviour when multiple proposer try to send propose at the same time
    private static void runSimultaneousProposals() {
        try{
            Thread.sleep(2000);
            for(int i = 1; i <= 2; i++){
                int proposerId = i;
                new Thread(()-> CouncilElection.runProposers("M" + proposerId, proposerId)).start();
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}

