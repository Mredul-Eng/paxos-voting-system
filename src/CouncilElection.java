import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class CouncilElection {
    private static final ReentrantLock lock = new ReentrantLock(); // used to synchronized access for shared resources like proposals
    private static boolean electionCompleted = false; // check if all council member response immediately or not
    private static final Map<Integer, Acceptor> acceptors = new ConcurrentHashMap<>(); // map the acceptors bt their ID(member id)
    private static final Random random = new Random(); // used to introduce random delays, message drops and offline behaviour
    private static final Map<String, Integer> votesCount = new ConcurrentHashMap<>(); // track number of votes for each candidate

    //initialize vote counts for each candidate
    static{
        votesCount.put("M1", 0);
        votesCount.put("M2", 0);
        votesCount.put("M3", 0);
    }

    private static final int QUORUM = 5; // as total number of members are 9, so the quorum should be (9 / 2) + 1

    public static void main(String[] args) {

        //initialize the acceptors for each member (M1 to M9) and started in separate thread
        for(int i = 1; i <= 9; i++){
            acceptors.put(i, new Acceptor("M" + i)); //put acceptors with specific member id
            int memberId = i;
            new Thread(() -> runAcceptors(memberId)).start(); // start a new thread for each acceptor and run each acceptor for separate thread, allowing it to run concurrently with other threads.

        }

        //Run multiple proposers after 1 second to propose simultaneously
        try{
            Thread.sleep(1000);
            //as there can be 3 proposers, run thread for 3 proposers
            for(int i = 1; i <= 3; i++){
                int proposerId = i;
                new Thread(()-> runProposers("M" + proposerId, proposerId)).start();
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }

    }

    public static void runProposers(String proposer, int proposerId) {
        if(electionCompleted) return;
        try{
            long voteNumber = System.currentTimeMillis() + proposerId; // unique vote number based on current time
            Proposal proposal = new Proposal(voteNumber, proposer);

            //store promises received from acceptors
            List<Promise> promises = new ArrayList<>();

            //sending prepare request to the Acceptors - Prepare phase
            for(int i = 1; i <= 9; i++){
                if (electionCompleted) return; // Check again before sending any message
                //connect each acceptor to their respective ports
                try(Socket socket = new Socket("localhost", 8000 + i);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    //send a prepare request with proposal number and content
                    out.println("prepare:" + proposal.getProposalNumber() + ":" + proposal.getContent());

                    //read response from acceptors
                    String response = in.readLine();
                    //if the response starts with promise, then the acceptor promised not to accept the proposal with lower proposal number
                    if(response != null && response.startsWith("promise")){
                        promises.add(new Promise(true, proposal));
                    }
                } catch (IOException e) {
                    System.out.println("Member M" + i + " did not respond or offline.");
                }
            }
            //if the number of promises less than quorum, then the proposer can not proceed further to the accept phase and return early
            if(promises.size() < QUORUM){
                System.out.println(proposer + " did not received enough promises..");
                return;
            }

            //sending accept request to the Acceptors
            int countOfAcceptance = 0;
            for(int i = 1; i <= 9; i++) {
                if (electionCompleted) return; // Check again before sending any message
                try (Socket socket = new Socket("localhost", 8000 + i);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    //send an accept request with proposal number and content
                    out.println("accept:" + proposal.getProposalNumber() + ":" + proposal.getContent());

                    //read response from acceptors
                    String response = in.readLine();

                    if(response != null && response.startsWith("accepted")){
                        countOfAcceptance += 1;
                        votesCount.put(proposer, votesCount.getOrDefault(proposer, 0) + 1); //increment vote count
                    }
                }catch (IOException e) {
                    System.out.println("Member M" + i + " did not respond or offline.");
                }
            }
            //check if count of acceptance is greater than or equal to Quorum, then the proposal will be accepted.
            lock.lock();
            try{
                //check election is completed or not
                if(!electionCompleted){
                    if(countOfAcceptance >= QUORUM){
                        System.out.println(proposer + " has enough acceptances");
                        printVoteCounts(); // print all votes count for all candidate
                        String electedCandidate = getMajorVotesValue(); // get the candidate who has major votes

                        if(electedCandidate != null){
                            System.out.println(electedCandidate + " is elected as council president");
                            electionCompleted = true; // Set election as completed
                        }
                    }
                    else {
                        System.out.println(proposer + " is rejected for council president election");
                    }
                }
            }finally {
                lock.unlock();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getMajorVotesValue() {
        String electedCandidate = null; // hold elected candidate name
        int maxVotes = -1;

        //iterate each entry in the votesCount map
        for(Map.Entry<String, Integer> voteCount : votesCount.entrySet()){
            if(voteCount.getValue() > maxVotes){
                maxVotes = voteCount.getValue(); //update maxVotes
                electedCandidate = voteCount.getKey(); //update the candidate name which has maximum votes
            }
        }
        return electedCandidate; // return the candidate who has maximum votes

    }

    //this method handles each council member's behaviour.
    //An acceptor connect in a specific port and response to specific proposals from proposers
    public static void runAcceptors(int memberId) {
        int port = 8000 + memberId;
        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Member " + memberId + " is listening on port " + port);

            //accept incoming connections from proposers in an infinite loop
            while(true){
                try(Socket socket = serverSocket.accept()){
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // read data from socket
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // write data to the socket

                    //read incoming message
                    String proposedMessage = in.readLine(); //read proposal that sent by the proposer
                    System.out.println("Member M" + memberId + " received the proposed message: " + proposedMessage);
                    String[] proposalParts = proposedMessage.split(":");
                    String operation = proposalParts[0]; // extract operation type from proposal
                    Proposal proposal = new Proposal(Long.parseLong(proposalParts[1]), proposalParts[2]); //create a new proposal

                    simulateMembersBehaviour(memberId, out);
                    //handle Prepare phase
                    //if the operation is Prepare, the acceptors check if they can promise to accept the proposal or not
                    if(operation.equalsIgnoreCase("prepare")){
                        Promise promise = acceptors.get(memberId).onPrepare(proposal); //call the onPrepare method of the acceptor object and check if the new proposal has higher number than the previous one. if true then return the promise (accepted), otherwise return false(rejected) as promise
                        //if promise is not equal null and isAcknowledge return true, then it send back a promise message to the socket
                        if(promise != null && promise.isAcknowledge()){
                            out.println("promise");
                        }
                        else out.println("rejected");
                    }
                    //handle Accept phase
                    else if(operation.equalsIgnoreCase("accept")){
                        boolean accepted = acceptors.get(memberId).acceptProposal(proposal); //call the acceptProposal method of the acceptor object and check if the proposals are equal or not. if equal, then the proposal will be accepted otherwise rejected
                        if(accepted){
                            out.println("accepted");
                        }
                        else out.println("rejected");
                    }
                }
            }
        }catch (IOException e){
            System.err.println("Couldn't start on port " + port + " for Member M" + memberId);
        }

    }

    //this method simulate real world behaviours and network issues for council members in the election system
    public static void simulateMembersBehaviour(int memberId, PrintWriter out) {
        try{
            if(memberId == 2){
                Thread.sleep(random.nextInt(5000) + 1000); // delayed response from 1 to 6 seconds randomly where network connection is slow
                System.out.println("Member M" + memberId + " is delayed between 1 to 6 seconds");
            } else if (memberId == 3) {
                //there is 50% chance not to response and message will drop for member 3
                if(random.nextBoolean()){
                    System.out.println("Member M" + memberId + " is not responding (message dropped)");
                    return;
                }
            } else if (memberId >= 4 && memberId <= 9) {
                Thread.sleep(random.nextInt(3000)); // random delays from 0 to 3 seconds for member 4 to 9
                System.out.println("Member M" + memberId + " respond after 3 seconds delay");
            }
            //simulate going offline for any members (chance for going offline is 20%)
            if(random.nextInt(10) < 2){
                System.out.println("Member M" + memberId + " is went offline");
                out.println("offline");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //display number of votes count for each candidate
    private static void printVoteCounts() {
        System.out.println("Vote Counts:");
        for (Map.Entry<String, Integer> entry : votesCount.entrySet()) {
            System.out.println(entry.getKey() + " gets: " + entry.getValue() + " votes");
        }
    }
}


