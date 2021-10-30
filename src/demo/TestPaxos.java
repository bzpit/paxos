package demo;


import paxos.Acceptor;
import paxos.ProposeData;
import paxos.Proposer;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPaxos implements Runnable{
    public TestPaxos(int id, Proposer proposer, Acceptor[] acceptors) {
        mId = id;
        mAcceptors = acceptors;
        mProposer = proposer;
    }

    //测试线程，模拟与Acceptor通信
    public void run() {
        int M = 9;
        ProposeData lastValue = new ProposeData();
        int[] acceptorId = new int[M];
        int count = 0;
        Date dt = new Date();
        long startTime = dt.getTime();//Starting Paxos timing
        ProposeData value = null;
        while (true) {
            value = mProposer.getProposal();//To reach for the proposal

            //System.out.println("Proposer"+mId+"Sign start (propose stage):Proposal = [No:"+value.serialNum()+"，proposal:"+ value.value()+"]");
            count = 0;
            int i = 0;
            for (i = 0; i < M; i++) {
                /*
                 * Send a message to the I < sup > th < / sup > acceptor
                 * After a certain time to reach the acceptor, the sleep (random time) simulation
                 * Acceptor processes messages, mcacceptors [i]. Propose ()
                 * Response to proposer
                 * After a certain amount of time the proposer received a response, the sleep (random time) simulation
                 * Proposer handling in response to mproposer.proposed (OK, lastvalue)
                 * Fix is the demo auxiliary parameter (not required for formal use) indicating whether this response has led proposer to modify the proposal
                 */
                //sleep(rand(200));//After random time, the message arrives at the mcacceptors [i]
                //Processing messages
                TestPaxos.msLockAcceptors[mId].lock();
                boolean ok = mAcceptors[i].Propose(value.serialNum(), lastValue);
                TestPaxos.msLockAcceptors[mId].unlock();
                sleep(rand(200));//After a random time, the message arrives at the proposer
                //Dealing with propose responses
                if (!mProposer.proposed(ok, lastValue)) //Restart the propose phase
                {
                    sleep(100);//Many more will give proposers the opportunity to complete their own phase 2 approvals in order to lower the activity
                    break;
                }
                ProposeData newValue = mProposer.getProposal();//To reach for the proposal
                if (newValue.value() != value.value()) {//Acceptor this response may recommend a proposal
                    System.out.println("Proposer"+mId+"Number start (propose phase): proposal = [number:"+newValue.serialNum()+"，proposal:"+ newValue.value()+"]");

                }
                acceptorId[count++] = i;//Records the acceptor willing to vote
                if (mProposer.startAccept()) {
                    if (0 == rand(4) % 2)
                    {
                        break;
                    }
                }
            }
            //Check that the accept start condition has not been met and if not indicated that the propose phase is to be restarted
            if (!mProposer.startAccept()) continue;

            //Start accept phase
            //Send an accept message to all acceptors willing to vote
            value = mProposer.getProposal();//To reach for the proposal
            //System.out.println("Proposer"+mId+"Number start (accept phase): proposal = [number:"+value.serialNum()+"，proposal:"+ value.value()+"]");

            for (i = 0; i < count; i++) {
                //Send an accept message to acceptor
                sleep(rand(200));//After randomization time, the accept message arrives at acceptor
                //Processing of accepted messages
                TestPaxos.msLockAcceptors[mId].lock();
                boolean ok = mAcceptors[acceptorId[i]].Accept(value);
                TestPaxos.msLockAcceptors[mId].unlock();
                sleep(rand(200));//After randomization time, the accepted response arrived at the proposer
                //Processing accepted responses
                if (!mProposer.accepted(ok)) //Restart the propose phase
                {
                    sleep(100);//Many more will give proposers the opportunity to complete their own phase 2 approvals in order to lower the activity
                    break;
                }
                if (mProposer.isAgree()) {//Successful approval of the proposal
                    dt = new Date();
                    long end = dt.getTime();
                    startTime = end - startTime;
                    System.out.println("Proposer"+mId+"time"+(int)startTime+"MS:Final proposal = [No:"+value.serialNum()+"，proposal:"+ value.value()+"]");


                    msLockFinished.lock();
                    msFinishedCount += 1;
                    if (M == msFinishedCount) {
                        msStartPaxos = dt.getTime() - msStartPaxos;
                        System.out.println("Paxos complete, time to use"+(int) msStartPaxos);

                    }
                    msLockFinished.unlock();
                    return;
                }
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestPaxos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int rand(int max){
        int r = (int) (Math.random() * max);
        return r;
    }

    private int mId;//Self junction IDS
    private Proposer mProposer;//The proposer node of thread operation
    private Acceptor[] mAcceptors;//All acceptor junctions, where a user simulation interacts with the acceptor and a direct call to the acceptor method indicates that the communication is complete
    public static Lock[] msLockAcceptors = null;
    public static long msStartPaxos;//start time
    public static int msFinishedCount;//Number of finished proposer junctions
    public static Lock msLockFinished = new ReentrantLock();//Msfinishedcount lock

}
