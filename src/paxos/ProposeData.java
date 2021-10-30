/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package paxos;

/**
 *
 * @author Administrator
 */
public class ProposeData {
    public void setSerialNum(int serialNum)
    {
        mSerialNum = serialNum;
    }
    public int serialNum()
    {
        return mSerialNum;
    }
    public void setValue(int value)
    {
        mValue = value;
    }
    public int value()
    {
        return mValue;
    }
    private int	mSerialNum;//Serial number, increasing from 1 to ensure global uniqueness
    private int	mValue;//Proposed content
}
