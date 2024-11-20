import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CouncilElection_Test {
    private static final int NUMBER_OF_MEMBERS = 9;

    public static void main(String[] args) {

        //Test scenario 1 for Multiple proposals M1, M2, M3
        System.out.println("Testing Multiple Simultaneous Proposals for Council Election...");
        runSimultaneousProposals();

        //Test scenario 2 for immediate response for all members
        System.out.println("\nTesting Immediate Response...");
        runImmediateResponse();

        //Test scenario 3 for simulate behaviour of M2 to M9 as their response times are vary
        System.out.println("\nTesting Response with Delays or Going Offline...");
        runDelayedOrNoResponse();
    }

    private static void runDelayedOrNoResponse() {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_MEMBERS - 1);
        for(int i = 2; i <= NUMBER_OF_MEMBERS; i++) {
            int memberId = i;
            executorService.submit(()->{
               CouncilElection.runAcceptors(memberId);
                System.out.println("Simulating Behaviour for Member M" + memberId);
            });
        }
        executorService.shutdown();
    }

    //simulate immediate responses for all members
    private static void runImmediateResponse() {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_MEMBERS);
        for(int i = 1; i <= NUMBER_OF_MEMBERS; i++) {
            int memberId = i;
            executorService.submit(()->{
                if(memberId == 1){
                    CouncilElection.runProposers("M" + memberId, memberId);
                    System.out.println("Member M" + memberId + " is responding immediately..");
                }
                else{
                    CouncilElection.runProposers("M" + memberId, memberId);
                    System.out.println("Member M" + memberId + " is available for response..");
                }
            });
        }
        executorService.shutdown();
        //as member M1 response immediately, so among all members M1 gives response immediately
//        new Thread(()-> CouncilElection.runProposers("M1", 1)).start();
//        System.out.println("M1 response immediately!");

    }

    //test the Paxos implementation behaviour when multiple proposer try to send propose at the same time
    private static void runSimultaneousProposals() {
        ExecutorService executorService = Executors.newFixedThreadPool(3); //create a thread pool with a fixed thread number (3) for simulating simultaneous proposals
        for(int i = 1; i <= 3; i++) {
            int proposalId = i;
            //for each iteration, a new task is submitted to executor service
            System.out.println("Member M" + proposalId + " is sending proposal!");
            executorService.submit(() -> CouncilElection.runProposers("M" + proposalId, proposalId));
        }
        executorService.shutdown(); // after all task submitted, shut down executor
    }

}

