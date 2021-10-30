/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package paxos;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Administrator
 * Acceptor：Response to phase I proposal and phase II proposal
 */
public class Acceptor {

    public Acceptor() {
        mMaxSerialNum = 0;
        mLastAcceptValue = new ProposeData();
        mLastAcceptValue.setSerialNum(0);
        mLastAcceptValue.setValue(0);
    }

    //Agree / reject the proposal will be accepted at the next stage
    //When agreeing, promise not to agree to any proposal with a number less than mmaxserialnum, nor accept any proposal with a number less than mmaxserialnum
    public boolean Propose(int serialNum, ProposeData lastAcceptValue) {
        mLock.lock();
        if (0 >= serialNum) {
            mLock.unlock();
            return false;
        }
        if (mMaxSerialNum > serialNum) {
            mLock.unlock();
            return false;
        }
        mMaxSerialNum = serialNum;
        lastAcceptValue.setSerialNum(mLastAcceptValue.serialNum());
        lastAcceptValue.setValue(mLastAcceptValue.value());
        mLock.unlock();

        return true;
    }

    //Accept / reject proposal
    //Accept only proposals numbered > = mmaxserialnum and record them
    public boolean Accept(ProposeData value) {
        mLock.lock();
        if (0 >= value.serialNum()) {
            mLock.unlock();
            return false;
        }
        if (mMaxSerialNum > value.serialNum()) {
            mLock.unlock();
            return false;
        }
        mLastAcceptValue.setSerialNum(value.serialNum());
        mLastAcceptValue.setValue(value.value());
        mLock.unlock();
        return true;
    }
    private ProposeData mLastAcceptValue;//Finally accepted proposal
    private int mMaxSerialNum;//Maximum serial number submitted by proposal
    private Lock mLock = new ReentrantLock();
}
