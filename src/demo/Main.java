/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package demo;

import paxos.Acceptor;
import paxos.ProposeData;
import paxos.Proposer;
import tool.LogFormatter;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Administrator
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        msLogger = Logger.getLogger(Main.class.getName());
        try {
            String fs = String.format("./Main.log");
            FileHandler fileHandler = new FileHandler(fs);
            msLogger.addHandler(fileHandler);
            fileHandler.setFormatter(new LogFormatter());
        } catch (IOException ex) {
            Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
        }
        String logText = "N proposers and 11 acceptors are ready for Paxos\r\n"
                + "Each proposer has an independent thread, and the acceptor does not need a thread\r\n"
                + "Waiting random time in the proposer thread: indicates the communication time with the acceptor\r\n"
                + "Calling Acceptor.Proposed () in the Proposer thread indicates that the result of the Propose request has been received.\r\n"
                + "Calling Acceptor.Accepted () in the Proposer thread indicates that the result of the Accept request has been received.\r\n"
                + "After the proposer is approved, the thread ends, and other threads continue to vote. Finally, all approve the same value and reach an agreement.\r\n\r\n";
        msLogger.info( logText );
        msLogger.info( "Paxos start" );

        int M = 9;
        Proposer []p = new Proposer[M];
        Acceptor []a = new Acceptor[M];
        int i = 0;
        for ( i = 0; i < M; i++ )
        {
            a[i] = new Acceptor();
        }

        TestPaxos.msLockAcceptors = new ReentrantLock[M];
        System.out.println(M);
        for ( i = 0; i < M; i++ )
        {
            TestPaxos.msLockAcceptors[i] = new ReentrantLock();
        }
        ProposeData value;
        Thread []t = new Thread[M];
        Date dt= new Date();
        TestPaxos.msStartPaxos = dt.getTime();
        TestPaxos []proposerRun = new TestPaxos[M];
        for ( i = 0; i < M; i++ )
        {
            p[i] = new Proposer((short)i, (short)i);
            p[i].setPlayerCount((short)M, (short)M);
            value = new ProposeData();
            value.setSerialNum(i);
            value.setValue(i);
            p[i].startPropose(value);
            proposerRun[i] = new TestPaxos(i, p[i], a);
            t[i] = new Thread(proposerRun[i]);
            t[i].start();
        }
        while ( true ) try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static Logger msLogger;

}
