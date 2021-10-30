/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package paxos;

import java.util.Date;

/**
 *
 * @author Administrator
 */
public class Proposer {

    public Proposer() {
        setPlayerCount((short) 0, (short) 0);
        mValue = new ProposeData();
    }

    public Proposer(short proposerCount, short acceptorCount) {
        setPlayerCount(proposerCount, acceptorCount);
        mValue = new ProposeData();
    }

    //Setting number of participants
    public void setPlayerCount(short proposerCount, short acceptorCount) {
        mProposerCount = proposerCount;///Number of proposers
        mAcceptorCount = acceptorCount;//Number of acceptors
    }

    //Initiation of the propose phase
    public void startPropose(ProposeData value) {
        mValue.setSerialNum(value.serialNum());
        mValue.setValue(value.value());
        mProposeFinished = false;
        mIsAgree = false;
        mMaxAcceptedSerialNum = 0;
        mOkCount = 0;
        mRefuseCount = 0;
        Date curTime = new Date();
        mStart = curTime.getTime();//MS at 0:0 min 0 s from 1 January 1970
    }

    /*
     * Phase overtime
     * millSecond：Time out of decision making
     */
    public boolean isTimeOut( int millSecond )
    {
        Date curTime = new Date();
        int waitTime = (int)(curTime.getTime() - mStart);//MS at 0:0 min 0 s from 1 January 1970
        if ( waitTime > millSecond ) return true;

        return false;
    }

    //Obtain proposal
    public ProposeData getProposal() {
        return mValue;
    }

    //The proposal is voted on and the propose phase restarts if it fails
    public boolean proposed(boolean ok, ProposeData lastAcceptValue) {
        if (mProposeFinished) {
            return true;//May be a phase 1 late response that directly ignores messages
        }
        if (!ok) {
            mRefuseCount++;
            //Half refused, did not need to wait for other acceptors to vote, to restart the propose phase
            if (mRefuseCount > mAcceptorCount / 2) {
                mValue.setSerialNum(mValue.serialNum() + mProposerCount);
                startPropose(mValue);
                return false;
            }
            return true;
        }

        mOkCount++;
        /*
		        It is not necessary to check the branches: serialnum is null
		        Because serialnum > m_ Maxacceptedserialnum, with serialnum non-0 as mutual requirement
         */
        //Record all received offers, with the largest one numbered, when awarded proposal weight themselves
        if (lastAcceptValue.serialNum() > mMaxAcceptedSerialNum) {
            mMaxAcceptedSerialNum = lastAcceptValue.serialNum();
            if (mValue.value() != lastAcceptValue.value()) {
                mValue.setValue(lastAcceptValue.value());
            }
        }
        if (mOkCount > mAcceptorCount / 2) {
            mOkCount = 0;
            mProposeFinished = true;
        }
        return true;
    }

    //Start accept phase, meets condition successfully start accept phase returns to ture, does not meet start condition returns false
    public boolean startAccept() {
        return mProposeFinished;
    }

    //Proposal was accepted, and accepted failure restarted the propose phase
    public boolean accepted(boolean ok) {
        if (!mProposeFinished) {
            return true;//May be a late response from last stage 2, ignoring messages directly
        }
        if (!ok) {
            mRefuseCount++;
            //Half refused, did not need to wait for other acceptors to vote, to restart the propose phase
            if (mRefuseCount > mAcceptorCount / 2) {
                mValue.setSerialNum(mValue.serialNum() + mProposerCount);
                startPropose(mValue);
                return false;
            }
            return true;
        }

        mOkCount++;
        if (mOkCount > mAcceptorCount / 2) {
            mIsAgree = true;
        }

        return true;
    }

    //Proposal approved
    public boolean isAgree() {
        return mIsAgree;
    }

    private short mProposerCount;///Number of proposers
    private short mAcceptorCount;//Number of acceptor
    private ProposeData mValue;//Number of acceptors
    private boolean mProposeFinished;//Complete ticket, prepare to start phase II
    private boolean mIsAgree;//m_ The value is approved
    private int mMaxAcceptedSerialNum;//The proposal that has been accepted has the largest flow number of
    private long mStart;//Phase start time, phase I, phase II shared
    private short mOkCount;//Number of votes, phase I, phase II shared
    private short mRefuseCount;//Reject volume, phase I, phase II shared
}
