# Paxos-Based Council Election System

This project implements a distributed fault-tolerant council election system using the Paxos consensus algorithm. The system simulates 9 council members (M1–M9) with varying response behaviors, including immediate responses, delays, message drops, and offline states. The election process uses the Prepare and Accept phases of the Paxos protocol to ensure consensus among council members.

---

## Features

1. **Distributed Design:** Each council member (acceptor) runs as a separate thread, simulating network communication.
2. **Paxos Protocol Implementation:**
   - **Prepare Phase:** Proposers request promises from acceptors for their proposals.
   - **Accept Phase:** Proposers request acceptors to commit to their proposals if quorum is reached.
3. **Real-World Simulation:** Simulates delays, message drops, and offline behavior for council members.
4. **Dynamic Testing:** Includes multiple scenarios to test fault tolerance, network issues, and the Paxos protocol.

---

## How It Works

### Components
1. **Proposer:** Sends proposals to acceptors and coordinates the election process.
2. **Acceptor:** Responds to proposals based on Paxos protocol rules (promise or accept).
3. **Council Members (M1–M9):** Simulate distributed nodes with unique behaviors.

### Phases
1. **Prepare Phase:** 
   - Proposers send proposals with unique numbers to all acceptors.
   - Acceptors respond with promises if the proposal number is higher than their last accepted proposal.
2. **Accept Phase:**
   - If quorum (5 out of 9) is reached, proposers send accept requests.
   - Acceptors commit to the proposal if conditions are met.

---

## Prerequisites

- **Java Development Kit (JDK):** Ensure JDK 11 or later is installed.
- **IDE or Terminal:** Use any IDE (e.g., IntelliJ IDEA, Eclipse) or terminal with `javac` and `java` commands.

---

## Installation

1. Clone or download the repository to your local machine.
2. Ensure all `.java` files (e.g., `Proposal.java`, `Acceptor.java`, `Promise.java`, `CouncilElection.java`, `CouncilElection_Test.java`) are in the same directory.

---

## Running the System

1. **Compile the Java files:**
   ```bash
   javac Proposal.java Promise.java Acceptor.java CouncilElection.java CouncilElection_Test.java

2. **Run The Election System:**
   ```bash
   java CouncilElection
   
---

## Testing the System
The system includes three predefined test scenarios:

1. **Multiple Simultaneous Proposals**
   - Two proposers (e.g., M1 and M2) send proposals simultaneously.
   - Tests how the Paxos protocol handles conflicts and ensures consensus.

2. **Immediate Response for All Members**
   - Simulates all council members responding immediately without delays or message drops.
   - Verifies the smooth execution of the election.

3. **Delayed or No Response**
   - Simulates various member behaviors:
       - Random delays for some members.
       - Message drops for others.
       - Some members going offline.
   - Tests the fault tolerance of the system.

4. **Running Test Scenarios**
   - The test scenarios are pre-defined in the CouncilElection_Test class.
   - To run all scenarios, execute the main method of CouncilElection_Test:
     
     ```bash
     java CouncilElection_Test


