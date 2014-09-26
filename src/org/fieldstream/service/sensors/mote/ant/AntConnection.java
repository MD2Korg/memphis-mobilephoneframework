//Copyright (c) 2010, University of Memphis, Carnegie Mellon University
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without modification, are permitted provided
//that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and
//      the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
//      and the following disclaimer in the documentation and/or other materials provided with the
//      distribution.
//    * Neither the names of the University of Memphis and Carnegie Mellon University nor the names of its
//      contributors may be used to endorse or promote products derived from this software without specific
//      prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
//WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
//PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
//ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
//TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//POSSIBILITY OF SUCH DAMAGE.
//
package org.fieldstream.service.sensors.mote.ant;


import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.sensors.mote.ant.Packetizer;
import org.fieldstream.service.sensors.mote.ChannelToSensorMapping;
import org.fieldstream.service.logger.Log;
import org.fieldstream.utility.CommonUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.TextView;

import com.dsi.ant.exception.*;
import com.dsi.ant.AntInterface;
import com.dsi.ant.AntInterfaceIntent;
import com.dsi.ant.AntMesg;
import com.dsi.ant.AntDefine;
//import com.dsi.ant.R;


public class AntConnection {

    /**
     * Defines the interface needed to work with all call backs this class makes
     */
    static final short WILDCARD = 0;
	   public static int SAMPLE_NO=5;
	   private static long[] mAntLastDataReceivedTime=new long[Constants.MOTE_NO];
    /** The Log Tag. */
    public static final String TAG = "ANTApp";

    /** The key for referencing the interrupted variable in saved instance data. */
    static final String ANT_INTERRUPTED_KEY = "ant_interrupted";

    /** The key for referencing the state variable in saved instance data. */
    static final String ANT_STATE_KEY = "ant_state";

    /** The interface to the ANT radio. */
    private AntInterface mAntReceiver;

    /** Is the ANT background service connected. */
    private boolean mServiceConnected = false;

    /** Stores which ANT status Intents to receive. */
    private IntentFilter statusIntentFilter;

    /** Flag to know if the ANT App was interrupted. */
    private boolean mAntInterrupted = false;

    /** Flag to know if an ANT Reset was triggered by this application. */
    private boolean mAntResetSent = false;

    /** Flag if waiting for ANT_ENABLED. Default to true as assume ANT is always enabled */
    private boolean mEnabling = true;

    // ANT Channels
    /** The ANT channel for the RIPECG. */
    static final byte MOTE_RIPECG_CHANNEL = (byte) 0;

    /** ANT+ device type for an RIPECG */
    static final byte RIPECG_DEVICE_TYPE = (byte)1; //Temporary disable to test 9 axis. //(byte)1;

    /** ANT+ channel period for an RIPECG */
    static final short RIPECG_PERIOD = 0x04EC;//8070;

    /** The ANT channel for the ALCOHOL. */
    static final byte MOTE_ALCOHOL_CHANNEL = (byte) 1;
    /** The ANT channel for the ALCOHOL Accelerometer at Right Hand. */
    static final byte MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL = (byte) 2;
    /** The ANT channel for the ALCOHOL Accelerometer at Left Hand. */
    static final byte MOTE_ALCOHOL_ACCL_LEFT_CHANNEL = (byte) 3;

    static final byte MOTE_NINE_AXIS_RIGHT_CHANNEL = (byte) 4;
    static final byte MOTE_NINE_AXIS_LEFT_CHANNEL = (byte) 5;


    /** ANT+ device type for an ALCOHOL */
    static final byte ALCOHOL_DEVICE_TYPE = (byte)0x02;//0x7C;
    static final byte ALCOHOL_ACCL_RIGHT_DEVICE_TYPE = (byte)0x02;
    static final byte ALCOHOL_ACCL_LEFT_DEVICE_TYPE = (byte)0x02;

    static final byte NINE_AXIS_DEVICE_TYPE = (byte)0x03;

    /** ANT+ channel period for an ALCOHOL */
    static final short ALCOHOL_PERIOD = (short)0x1000;//8134;

    /** ANT+ channel period for an ALCOHOL */
    static final short ALCOHOL_ACCL_PERIOD = (short)(32768/16);//16 HZ;

    static final short NINE_AXIS_PERIOD = (short)1638; //(32768/20);//20 HZ;

	   /** The ECG channel for AutoSense. */
	   static final byte ECG_CHANNEL = (byte) 0;

	   /** The ACCELX channel for AutoSense. */
	   static final byte ACCELX_CHANNEL = (byte) 1;

	   /** The ACCELY channel for AutoSense. */
	   static final byte ACCELY_CHANNEL = (byte) 2;

	   /** The ACCELZ channel for AutoSense. */
	   static final byte ACCELZ_CHANNEL = (byte) 3;

	   /** The GSR channel for AutoSense. */
	   static final byte GSR_CHANNEL = (byte) 4;

	   /** The RIP channel for AutoSense. */
	   static final byte RIP_CHANNEL = (byte) 7;

	   /** The SKIN, AMBIENCE, BATTERY channels for AutoSense. */
	   static final byte MISC_CHANNEL = (byte) 8;

	   /** The ALCOHOL channel for AutoSense. */
	   static final byte ALCOHOL_CHANNEL = (byte) 10;

	   /** The GSR channel for AutoSense. */
	   static final byte GSRWRIST_CHANNEL = (byte) 11;

	   /** The BODYTEMP channel for AutoSense. */
	   static final byte BODYTEMPWRIST_CHANNEL = (byte) 12;

	   /** The ALCOHOL Accelerometer X channel for AutoSense. */
	   static final byte ALCOHOL_ACCL_X_CHANNEL = (byte) 5;
	   /** The ALCOHOL Accelerometer Y channel for AutoSense. */
	   static final byte ALCOHOL_ACCL_Y_CHANNEL = (byte) 6;
	   /** The ALCOHOL Accelerometer Z channel for AutoSense. */
	   static final byte ALCOHOL_ACCL_Z_CHANNEL = (byte) 7;

	   static final byte NINE_AXIS_ACCL_X_CHANNEL = (byte) 0;
	   static final byte NINE_AXIS_ACCL_Y_CHANNEL = (byte) 7;
	   static final byte NINE_AXIS_ACCL_Z_CHANNEL = (byte) 1;

	   static final byte NINE_AXIS_GYRO_X_CHANNEL = (byte) 2;
	   static final byte NINE_AXIS_GYRO_Y_CHANNEL = (byte) 3;
	   static final byte NINE_AXIS_GYRO_Z_CHANNEL = (byte) 4;

	   static final byte NINE_AXIS_NULL_PACKET_CHANNEL = (byte) 15;



    /** Description of ANT's current state */
    private String mAntStateText = "";

    /** Possible states of a device channel */
    public enum ChannelStates
    {
       /** Channel was explicitly closed or has not been opened */
       CLOSED,

       /** User has requested we open the channel, but we are waiting for a reset */
       PENDING_OPEN,

       /** Channel is opened, but we have not received any data yet */
       SEARCHING,

       /** Channel is opened and has received status data from the device most recently */
       TRACKING_STATUS,

       /** Channel is opened and has received measurement data most recently */
       TRACKING_DATA,

       /** Channel is closed as the result of a search timeout */
       OFFLINE
    }

    /** Current state of the RIPECG channel */
    private ChannelStates mRIPECGState = ChannelStates.CLOSED;

    /** Current state of the ALCOHOL channel */
    private ChannelStates mALCOHOLState = ChannelStates.CLOSED;

    /** Current state of the ALCOHOL Accl Right channel */
    private ChannelStates mALCOHOLAcclRightState = ChannelStates.CLOSED;
    /** Current state of the ALCOHOL Accl Left channel */
    private ChannelStates mALCOHOLAcclLeftState = ChannelStates.CLOSED;

    private ChannelStates mNineAxisRightState = ChannelStates.CLOSED;
    private ChannelStates mNineAxisLeftState = ChannelStates.CLOSED;

    //Flags used for deferred opening of channels
    /** Flag indicating that opening of the RIPECG channel was deferred */
    private boolean mDeferredRIPECGStart = false;

    /** Flag indicating that opening of the ALCOHOL channel was deferred */
    private boolean mDeferredALCOHOLStart = false;

    /** Flag indicating that opening of the ALCOHOL Accelerometer Right channel was deferred */
    private boolean mDeferredALCOHOLAcclRightStart = false;
    /** Flag indicating that opening of the ALCOHOL Accelerometer Left channel was deferred */
    private boolean mDeferredALCOHOLAcclLeftStart = false;

    private boolean mDeferredNineAxisRightStart = false;
    private boolean mDeferredNineAxisLeftStart = false;

    /** Flag indicating that opening of the weight scale channel was deferred */
    private boolean mDeferredWeightStart = false;

    /** RIPECG device number. */
    private short mDeviceNumberRIPECG;

    /** ALCOHOL device number. */
    private short mDeviceNumberALCOHOL;

    /** ALCOHOL Accelerometer Right device number. */
    private short mDeviceNumberALCOHOLAcclRight;

    /** ALCOHOL Accelerometer Left device number. */
    private short mDeviceNumberALCOHOLAcclLeft;

    private short mDeviceNumberNineAxisRight;
    private short mDeviceNumberNineAxisLeft;

    /** Weight scale device number. */
    private short mDeviceNumberWGT;

    /** Devices must be within this bin to be found during (proximity) search. */
    private byte mProximityThreshold;

    //TODO You will want to set a separate threshold for screen off and (if desired) screen on.
    /** Data buffered for event buffering before flush. */
    private short mBufferThreshold;

    /** If this application has control of the ANT Interface. */
    private boolean mClaimedAntInterface;

    /**
     * The possible RIPECG page toggle bit states.
     */
    private Context mContext;

//    private Activity mActivity;

    /**
     * Default Constructor
     */
    public AntConnection()
    {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntConnection)_Constructor()");
        //Set initial state values
        mDeferredRIPECGStart = false;
        mRIPECGState = ChannelStates.CLOSED;
        mDeferredALCOHOLStart = false;
        mALCOHOLState = ChannelStates.CLOSED;
        mDeferredALCOHOLAcclRightStart = false;
        mALCOHOLAcclRightState = ChannelStates.CLOSED;
        mDeferredALCOHOLAcclLeftStart = false;
        mALCOHOLAcclLeftState = ChannelStates.CLOSED;

        mDeferredNineAxisRightStart = false;
        mNineAxisRightState = ChannelStates.CLOSED;
        mDeferredNineAxisLeftStart = false;
        mNineAxisLeftState = ChannelStates.CLOSED;

        mDeferredWeightStart = false;
        mContext=InferrenceService.mContext;
        /*Retrieve the ANT state and find out whether the ANT App was interrupted*/
		long lastReceivedTimeDefault=0;

        AntConnection.setLast_Data_Received_Time(lastReceivedTimeDefault, Constants.MOTE_RIPECG_IND);
        AntConnection.setLast_Data_Received_Time(lastReceivedTimeDefault, Constants.MOTE_ALCOHOL_IND);
        AntConnection.setLast_Data_Received_Time(lastReceivedTimeDefault, Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND);
        AntConnection.setLast_Data_Received_Time(lastReceivedTimeDefault, Constants.MOTE_ALCOHOL_ACCL_LEFT_IND);

        AntConnection.setLast_Data_Received_Time(lastReceivedTimeDefault, Constants.MOTE_NINE_AXIS_RIGHT_IND);
        AntConnection.setLast_Data_Received_Time(lastReceivedTimeDefault, Constants.MOTE_NINE_AXIS_LEFT_IND);

        mClaimedAntInterface = false;

        // ANT intent broadcasts.
        statusIntentFilter = new IntentFilter();
        statusIntentFilter.addAction(AntInterfaceIntent.ANT_ENABLED_ACTION);
        statusIntentFilter.addAction(AntInterfaceIntent.ANT_DISABLED_ACTION);
        statusIntentFilter.addAction(AntInterfaceIntent.ANT_RESET_ACTION);
        statusIntentFilter.addAction(AntInterfaceIntent.ANT_INTERFACE_CLAIMED_ACTION);

        mAntReceiver = new AntInterface();
    }

	public static long getLast_Data_Received_Time(int ind) {
		return mAntLastDataReceivedTime[ind];
	}
	public static void setLast_Data_Received_Time(long currentTime,int ind) {
		mAntLastDataReceivedTime[ind]=currentTime;
	}

    /**
     * Creates the connection to the ANT service back-end.
     */
    public boolean start()
    {
        boolean initialised = false;

        if(AntInterface.hasAntSupport(mContext))
        {
            if(!mAntReceiver.initService(mContext, mAntServiceListener))
            {
                // Need the ANT Radio Service installed.
                Log.e(TAG, "AntChannelManager Constructor: No ANT Service.");
                requestServiceinstall();
            }
            else
            {
                mServiceConnected = mAntReceiver.isServiceConnected();
                Log.h(TAG, "start():Registering ANT receiver");
                mContext.registerReceiver(mAntStatusReceiver, statusIntentFilter);

                if(mServiceConnected)
                {
                    try
                    {
                        mClaimedAntInterface = mAntReceiver.hasClaimedInterface();
                        if(mClaimedAntInterface)
                        {
                            receiveAntRxMessages(true);
                        }
                    }
                    catch (AntInterfaceException e)
                    {
                        antError();
                    }
                }

                initialised = true;
            }
        }

        return initialised;
    }

    /**
     * Requests that the user install the needed service for ant
     */
    void requestServiceinstall()
    {
//        Toast installNotification = Toast.makeText(mActivity, mActivity.getResources().getString(R.string.Notify_Service_Required), Toast.LENGTH_LONG);
 //       installNotification.show();

        AntInterface.goToMarket(mContext);

//        mContext.finish();
    }

    //Getters and setters
    public AntInterface getAntReceiver()
    {
        return mAntReceiver;
    }

    public boolean isServiceConnected()
    {
        return mServiceConnected;
    }

    public short getDeviceNumberRIPECG()
    {
        return mDeviceNumberRIPECG;
    }

    public void setDeviceNumberRIPECG(short deviceNumberRIPECG)
    {
        this.mDeviceNumberRIPECG = deviceNumberRIPECG;
    }

    public short getDeviceNumberALCOHOL()
    {
        return mDeviceNumberALCOHOL;
    }

    public void setDeviceNumberALCOHOL(short deviceNumberALCOHOL)
    {
        this.mDeviceNumberALCOHOL = deviceNumberALCOHOL;
    }

    public short getDeviceNumberWGT()
    {
        return mDeviceNumberWGT;
    }

    public void setDeviceNumberWGT(short deviceNumberWGT)
    {
        this.mDeviceNumberWGT = deviceNumberWGT;
    }

    public byte getProximityThreshold()
    {
        return mProximityThreshold;
    }

    public void setProximityThreshold(byte proximityThreshold)
    {
        this.mProximityThreshold = proximityThreshold;
    }

    public short getBufferThreshold()
    {
        return mBufferThreshold;
    }

    public void setBufferThreshold(short bufferThreshold)
    {
        this.mBufferThreshold = bufferThreshold;
    }


    public ChannelStates getRIPECGState()
    {
        return mRIPECGState;
    }

    public ChannelStates getALCOHOLState()
    {
        return mALCOHOLState;
    }

    public ChannelStates getALCOHOLAcclRightState() {
        return mALCOHOLAcclRightState;
    }
    public ChannelStates getALCOHOLAcclLeftState() {
        return mALCOHOLAcclLeftState;
    }

    public ChannelStates getNineAxisRightState(){
    	return mNineAxisRightState;
    }
    public ChannelStates getNineAxisLeftState(){
    	return mNineAxisLeftState;
    }


    public void setRIPECGState(ChannelStates mRIPECGState) {
		this.mRIPECGState = mRIPECGState;
	}

	public void setALCOHOLState(ChannelStates mALCOHOLState) {
		this.mALCOHOLState = mALCOHOLState;
	}

	public void setALCOHOLAcclRightState(ChannelStates mALCOHOLAcclRightState) {
		this.mALCOHOLAcclRightState = mALCOHOLAcclRightState;
	}

	public void setALCOHOLAcclLeftState(ChannelStates mALCOHOLAcclLeftState) {
		this.mALCOHOLAcclLeftState = mALCOHOLAcclLeftState;
	}

	public void setNineAxisRightState(ChannelStates nineAxisRightState) {
		this.mNineAxisRightState = nineAxisRightState;
	}
	public void setNineAxisLeftState(ChannelStates nineAxisLeftState) {
		this.mNineAxisLeftState = nineAxisLeftState;
	}

	public String getAntStateText()
    {
        return mAntStateText;
    }
    /**
     * Checks if ANT can be used by this application
     * Sets the AntState string to reflect current status.
     * @return true if this application can use the ANT chip, false otherwise.
     */
    public boolean checkAntState()
    {
        try
        {
            if(!AntInterface.hasAntSupport(mContext))
            {
                Log.w(TAG, "updateDisplay: ANT not supported");

                mAntStateText = "ANT Not Supported";
                return false;
            }
            else if(mServiceConnected && mAntReceiver.isEnabled())
            {
                if(mAntReceiver.hasClaimedInterface() || mAntReceiver.claimInterface())
                {
                    return true;
                }
                else
                {
                    mAntStateText = "ANT In Use";
                    return false;
                }
            }
            else if(mEnabling)
            {
                mAntStateText = "Enabling";
                return false;
            }
            else
            {
                Log.w(TAG, "updateDisplay: Service not connected/enabled");

                mAntStateText = "Disabled";
                return false;
            }
        }
        catch(AntInterfaceException e)
        {
            antError();
            return false;
        }
    }

    /**
     * Attempts to claim the Ant interface
     */
    public void tryClaimAnt()
    {
        try
        {
            mAntReceiver.requestForceClaimInterface("FieldStream");
        }
        catch(AntInterfaceException e)
        {
            antError();
        }
    }

    /**
     * Unregisters all our receivers in preparation for application shutdown
     */
    public void shutDown()
    {
        try
        {
            mContext.unregisterReceiver(mAntStatusReceiver);
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateConnection)_ShutDown()_1");

        }
        catch(IllegalArgumentException e)
        {
            // Receiver wasn't registered, ignore as that's what we wanted anyway
        }
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateConnection)_ShutDown()_2");

        receiveAntRxMessages(false);
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateConnection)_ShutDown()_3");

        if(mServiceConnected)
        {
            try
            {
    			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateConnection)_ShutDown()_4");

                if(mClaimedAntInterface)
                {
                    Log.d(TAG, "AntChannelManager.shutDown: Releasing interface");
        			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateConnection)_ShutDown()_5");

                    mAntReceiver.releaseInterface();
        			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateConnection)_ShutDown()_6");

                }
    			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateConnection)_ShutDown()_7");

                mAntReceiver.stopRequestForceClaimInterface();
    			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateConnection)_ShutDown()_8");

            }
            catch(AntServiceNotConnectedException e)
            {
                // Ignore as we are disconnecting the service/closing the app anyway
            }
            catch(AntInterfaceException e)
            {
               Log.w(TAG, "Exception in AntChannelManager.shutDown");
            }
        }
    }


    /**
     * Class for receiving notifications about ANT service state.
     */
    private AntInterface.ServiceListener mAntServiceListener = new AntInterface.ServiceListener()
    {
        public void onServiceConnected()
        {
            Log.d(TAG, "mAntServiceListener onServiceConnected()");

            mServiceConnected = true;

            try
            {
                if (mEnabling)
                {
                	Log.h(TAG, "Enabling ant receiver");
                    mAntReceiver.enable();
                }

                mClaimedAntInterface = mAntReceiver.hasClaimedInterface();

                if (mClaimedAntInterface)
                {
                    // mAntMessageReceiver should be registered any time we have
                    // control of the ANT Interface
                    receiveAntRxMessages(true);
                } else
                {
                    // Need to claim the ANT Interface if it is available, now
                    // the service is connected
                    if (mAntInterrupted == false) {
                        mClaimedAntInterface = mAntReceiver.claimInterface();
                    } else {
                        Log.i(TAG, "Not attempting to claim the ANT Interface as application was interrupted (leaving in previous state).");
                    }
                }
            } catch (AntInterfaceException e) {
                antError();
                Log.e(TAG, "enableRadio: Exception caught : "+e.getMessage());
            }

            Log.d(TAG, "mAntServiceListener Displaying icons only if radio enabled");
 //           mCallbackSink.notifyAntStateChanged();
        }

        public void onServiceDisconnected()
        {
            Log.d(TAG, "mAntServiceListener onServiceDisconnected()");

            mServiceConnected = false;
            mEnabling = false;

            if (mClaimedAntInterface)
            {
                receiveAntRxMessages(false);
            }

//            mCallbackSink.notifyAntStateChanged();
        }
    };

    /**
     * Configure the ANT radio to the user settings.
     */
    void setAntConfiguration()
    {
        if(mServiceConnected)
        {
            try
            {
                // Event Buffering Configuration
                if(mBufferThreshold > 0)
                {
                    //TODO For easy demonstration will set screen on and screen off thresholds to the same value.
                    // No buffering by interval here.
                    mAntReceiver.ANTConfigEventBuffering((short)0xFFFF, mBufferThreshold, (short)0xFFFF, mBufferThreshold);
                }
                else
                {
                    mAntReceiver.ANTDisableEventBuffering();
                }
            }
            catch(AntInterfaceException e)
            {
                Log.e(TAG, "Could not configure event buffering");
            }
        }
    }

    /**
     * Display to user that an error has occured communicating with ANT Radio.
     */
    private void antError()
    {
        mAntStateText = "ANT Error";
//        mCallbackSink.errorCallback();
    }

    /**
     * Opens a given channel using the proper configuration for the channel's sensor type.
     * @param channel The channel to Open.
     * @param deferToNextReset If true, channel will not open until the next reset.
     */

    public void openChannel(byte channel, boolean deferToNextReset)
    {
    	Log.h(TAG, "openChannel( "+channel+", "+deferToNextReset+")");
    	if(channel==MOTE_RIPECG_CHANNEL && Constants.moteActive[Constants.MOTE_RIPECG_IND]==false) return;
    	if(channel==MOTE_ALCOHOL_CHANNEL && Constants.moteActive[Constants.MOTE_ALCOHOL_IND]==false) return;
    	if(channel==MOTE_RIPECG_CHANNEL && Constants.antAddress[Constants.MOTE_RIPECG_IND]=="") return;
    	if(channel==MOTE_ALCOHOL_CHANNEL && Constants.antAddress[Constants.MOTE_ALCOHOL_IND]=="") return;

    	if(channel==MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL && Constants.moteActive[Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND]==false) return;
    	if(channel==MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL && Constants.antAddress[Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND]=="") return;
    	if(channel==MOTE_ALCOHOL_ACCL_LEFT_CHANNEL && Constants.moteActive[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND]==false) return;
    	if(channel==MOTE_ALCOHOL_ACCL_LEFT_CHANNEL && Constants.antAddress[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND]=="") return;

    	if(channel==MOTE_NINE_AXIS_RIGHT_CHANNEL && Constants.moteActive[Constants.MOTE_NINE_AXIS_RIGHT_IND]==false) return;
    	if(channel==MOTE_NINE_AXIS_RIGHT_CHANNEL && Constants.antAddress[Constants.MOTE_NINE_AXIS_RIGHT_IND]=="") return;

    	if(channel==MOTE_NINE_AXIS_LEFT_CHANNEL && Constants.moteActive[Constants.MOTE_NINE_AXIS_LEFT_IND]==false) return;
    	if(channel==MOTE_NINE_AXIS_LEFT_CHANNEL && Constants.antAddress[Constants.MOTE_NINE_AXIS_LEFT_IND]=="") return;

    	closeChannel(channel);
    	if (!deferToNextReset) {
            short deviceNumber = 0;
            byte deviceType = 0;
            byte TransmissionType = 0; // Set to 0 for wild card search
            short period = 0;
            byte freq = (byte)0x50; // 2457Mhz (ANT+ frequency)

            switch (channel) {
                case MOTE_RIPECG_CHANNEL:
                	deviceNumber=Short.valueOf(Constants.antAddress[Constants.MOTE_RIPECG_IND],16);
                	//deviceNumber = 80;//mDeviceNumberRIPECG;
                    deviceType = RIPECG_DEVICE_TYPE;
                    period = RIPECG_PERIOD;
                    mRIPECGState = ChannelStates.SEARCHING;
                    break;
                case MOTE_ALCOHOL_CHANNEL:
                	//deviceNumber = (short)0x40;//mDeviceNumberALCOHOL;
                   	deviceNumber=Short.valueOf(Constants.antAddress[Constants.MOTE_ALCOHOL_IND], 16);
                	deviceType = ALCOHOL_DEVICE_TYPE;
                    period = ALCOHOL_PERIOD;
                    mALCOHOLState = ChannelStates.SEARCHING;
                    break;
                case MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL:
                	deviceNumber=Short.valueOf(Constants.antAddress[Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND], 16);
                	deviceType = ALCOHOL_ACCL_RIGHT_DEVICE_TYPE;
                	period = ALCOHOL_ACCL_PERIOD;
                	mALCOHOLAcclRightState = ChannelStates.SEARCHING;
                	break;
                case MOTE_ALCOHOL_ACCL_LEFT_CHANNEL:
                	deviceNumber=Short.valueOf(Constants.antAddress[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND], 16);
                	deviceType = ALCOHOL_ACCL_LEFT_DEVICE_TYPE;
                	period = ALCOHOL_ACCL_PERIOD;
                	mALCOHOLAcclLeftState = ChannelStates.SEARCHING;
                	break;

                case MOTE_NINE_AXIS_RIGHT_CHANNEL:
                	Log.h(TAG, "Hex ID Right : " + Constants.antAddress[Constants.MOTE_NINE_AXIS_RIGHT_IND]);
                	deviceNumber=Short.valueOf(Constants.antAddress[Constants.MOTE_NINE_AXIS_RIGHT_IND], 16);
                	deviceType = NINE_AXIS_DEVICE_TYPE;
                	period = NINE_AXIS_PERIOD;
                	mNineAxisRightState = ChannelStates.SEARCHING;
                	break;
                case MOTE_NINE_AXIS_LEFT_CHANNEL:
                	Log.h(TAG, "Hex ID Left : " + Constants.antAddress[Constants.MOTE_NINE_AXIS_LEFT_IND]);
                	deviceNumber=Short.valueOf(Constants.antAddress[Constants.MOTE_NINE_AXIS_LEFT_IND], 16);
                	deviceType = NINE_AXIS_DEVICE_TYPE;
                	period = NINE_AXIS_PERIOD;
                	mNineAxisLeftState = ChannelStates.SEARCHING;
                	break;
            }

//            mCallbackSink.notifyChannelStateChanged(channel);
            // Configure and open channel
            if (!antChannelSetup(
                    (byte) 0x00, // Network: 1 (ANT+)
                    channel, deviceNumber, deviceType, TransmissionType, period, freq,
                    mProximityThreshold))
            {
                Log.w(TAG, "openChannel: failed to configure and open channel " + channel + ".");
                switch(channel)
                {
                    case MOTE_RIPECG_CHANNEL:
                        mRIPECGState = ChannelStates.CLOSED;
                        break;
                    case MOTE_ALCOHOL_CHANNEL:
                        mALCOHOLState = ChannelStates.CLOSED;
                        break;
                    case MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL:
                        mALCOHOLAcclRightState = ChannelStates.CLOSED;
                        break;
                    case MOTE_ALCOHOL_ACCL_LEFT_CHANNEL:
                        mALCOHOLAcclLeftState = ChannelStates.CLOSED;
                        break;
                    case MOTE_NINE_AXIS_RIGHT_CHANNEL:
                    	mNineAxisRightState = ChannelStates.CLOSED;
                    	break;
                    case MOTE_NINE_AXIS_LEFT_CHANNEL:
                    	mNineAxisLeftState = ChannelStates.CLOSED;
                    	break;
                }
 //               mCallbackSink.notifyChannelStateChanged(channel);
            } else {
            }
            Log.h(TAG, "Try Opening: channel: "+channel+", deviceNumber: "+deviceNumber+", deviceType: "+deviceType+", TransmissionType: "+TransmissionType+", period: "+period+", freq: "+ freq);
        }
        else
        {
            switch(channel)
            {
                case MOTE_RIPECG_CHANNEL:
                    mDeferredRIPECGStart = true;
                    mRIPECGState = ChannelStates.PENDING_OPEN;
                    break;
                case MOTE_ALCOHOL_CHANNEL:
                    mDeferredALCOHOLStart = true;
                    mALCOHOLState = ChannelStates.PENDING_OPEN;
                    break;
                case MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL:
                	mDeferredALCOHOLAcclRightStart = true;
                    mALCOHOLAcclRightState = ChannelStates.PENDING_OPEN;
                    break;
                case MOTE_ALCOHOL_ACCL_LEFT_CHANNEL:
                	mDeferredALCOHOLAcclLeftStart = true;
                    mALCOHOLAcclLeftState = ChannelStates.PENDING_OPEN;
                    break;

                case MOTE_NINE_AXIS_RIGHT_CHANNEL:
                	mDeferredNineAxisRightStart = true;
                    mNineAxisRightState = ChannelStates.PENDING_OPEN;
                    break;
                case MOTE_NINE_AXIS_LEFT_CHANNEL:
                	mDeferredNineAxisLeftStart = true;
                    mNineAxisLeftState = ChannelStates.PENDING_OPEN;
                    break;
            }
        }
    }

    /**
     * Attempts to cleanly close a specified channel
     * @param channel The channel to close.
     */
    public void closeChannel(byte channel)
    {
    	Log.h(TAG, "closeChannel: " + channel);
        switch(channel)
        {
            case MOTE_RIPECG_CHANNEL:
                mRIPECGState = ChannelStates.CLOSED;
                break;
            case MOTE_ALCOHOL_CHANNEL:
                mALCOHOLState = ChannelStates.CLOSED;
                break;
            case MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL:
            	mALCOHOLAcclRightState = ChannelStates.CLOSED;
            	break;
            case MOTE_ALCOHOL_ACCL_LEFT_CHANNEL:
            	mALCOHOLAcclLeftState = ChannelStates.CLOSED;
            	break;
            case MOTE_NINE_AXIS_RIGHT_CHANNEL:
            	mNineAxisRightState = ChannelStates.CLOSED;
            	break;
            case MOTE_NINE_AXIS_LEFT_CHANNEL:
            	mNineAxisLeftState = ChannelStates.CLOSED;
            	break;
        }
//        mCallbackSink.notifyChannelStateChanged(channel);
        try
        {
           mAntReceiver.ANTCloseChannel(channel);
           mAntReceiver.ANTUnassignChannel(channel);
        } catch (AntInterfaceException e)
        {
           Log.d(TAG, "closeChannel: could not cleanly close channel " + channel + ".");
           antError();
        }
    }

    /**
     * Resets the channel state machines, used in error recovery.
     */
    public void clearChannelStates()
    {
        mRIPECGState = ChannelStates.CLOSED;
//        mCallbackSink.notifyChannelStateChanged(MOTE_RIPECG_CHANNEL);
        mALCOHOLState = ChannelStates.CLOSED;
//        mCallbackSink.notifyChannelStateChanged(MOTE_ALCOHOL_CHANNEL);
        mALCOHOLAcclRightState = ChannelStates.CLOSED;
        mALCOHOLAcclLeftState = ChannelStates.CLOSED;
        mNineAxisRightState = ChannelStates.CLOSED;
        mNineAxisLeftState = ChannelStates.CLOSED;
    }

    /** check to see if a channel is open */
    public boolean isChannelOpen(byte channel)
    {
        switch(channel)
        {
            case MOTE_RIPECG_CHANNEL:
                if(mRIPECGState == ChannelStates.CLOSED || mRIPECGState == ChannelStates.OFFLINE)
                    return false;
                break;
            case MOTE_ALCOHOL_CHANNEL:
                if(mALCOHOLState == ChannelStates.CLOSED || mALCOHOLState == ChannelStates.OFFLINE)
                    return false;
                break;
            case MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL:
                if(mALCOHOLAcclRightState == ChannelStates.CLOSED || mALCOHOLAcclRightState == ChannelStates.OFFLINE)
                    return false;
                break;
            case MOTE_ALCOHOL_ACCL_LEFT_CHANNEL:
                if(mALCOHOLAcclLeftState == ChannelStates.CLOSED || mALCOHOLAcclLeftState == ChannelStates.OFFLINE)
                    return false;
                break;

            case MOTE_NINE_AXIS_RIGHT_CHANNEL:
                if(mNineAxisRightState == ChannelStates.CLOSED || mNineAxisRightState == ChannelStates.OFFLINE) {
                    return false;
                }
                break;
            case MOTE_NINE_AXIS_LEFT_CHANNEL:
                if(mNineAxisLeftState == ChannelStates.CLOSED || mNineAxisLeftState == ChannelStates.OFFLINE) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    /** request an ANT reset */
    public void requestReset()
    {
        try
        {
            mAntResetSent = true;
            mAntReceiver.ANTResetSystem();
            setAntConfiguration();
        } catch (AntInterfaceException e) {
            Log.e(TAG, "requestReset: Could not reset ANT");
            mAntResetSent = false;
            //Cancel pending channel open requests
            if(mDeferredRIPECGStart)
            {
                mDeferredRIPECGStart = false;
                mRIPECGState = ChannelStates.CLOSED;
//                mCallbackSink.notifyChannelStateChanged(MOTE_RIPECG_CHANNEL);
            }
            if(mDeferredALCOHOLStart)
            {
                mDeferredALCOHOLStart = false;
                mALCOHOLState = ChannelStates.CLOSED;
//                mCallbackSink.notifyChannelStateChanged(MOTE_ALCOHOL_CHANNEL);
            }
            if(mDeferredALCOHOLAcclRightStart) {
                mDeferredALCOHOLAcclRightStart = false;
                mALCOHOLAcclRightState = ChannelStates.CLOSED;
                //mCallbackSink.notifyChannelStateChanged(MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL);
            }
            if(mDeferredALCOHOLAcclLeftStart) {
                mDeferredALCOHOLAcclLeftStart = false;
                mALCOHOLAcclLeftState = ChannelStates.CLOSED;
                //mCallbackSink.notifyChannelStateChanged(MOTE_ALCOHOL_ACCL_LEFT_CHANNEL);
            }

            if(mDeferredNineAxisRightStart) {
                mDeferredNineAxisRightStart = false;
                mNineAxisRightState = ChannelStates.CLOSED;
            }
            if(mDeferredNineAxisLeftStart) {
                mDeferredNineAxisLeftStart = false;
                mNineAxisLeftState = ChannelStates.CLOSED;
            }

            if(mDeferredWeightStart)
            {
                mDeferredWeightStart = false;
            }
        }
    }

    /** Receives all of the ANT status intents. */
    private final BroadcastReceiver mAntStatusReceiver = new BroadcastReceiver()
    {
       public void onReceive(Context context, Intent intent)
       {
          String ANTAction = intent.getAction();

          Log.d(TAG, "enter onReceive: " + ANTAction);
          if (ANTAction.equals(AntInterfaceIntent.ANT_ENABLED_ACTION))
          {
             Log.i(TAG, "onReceive: ANT ENABLED");

             mEnabling = false;
             receiveAntRxMessages(true); // Hillol: This is a fix. Restart and first time opening application do not receive any packet
//             mCallbackSink.notifyAntStateChanged();
          }
          else if (ANTAction.equals(AntInterfaceIntent.ANT_DISABLED_ACTION))
          {
             Log.i(TAG, "onReceive: ANT DISABLED");

             mRIPECGState = ChannelStates.CLOSED;
//             mCallbackSink.notifyChannelStateChanged(MOTE_RIPECG_CHANNEL);
             mALCOHOLState = ChannelStates.CLOSED;
//             mCallbackSink.notifyChannelStateChanged(MOTE_ALCOHOL_CHANNEL);
             mALCOHOLAcclRightState = ChannelStates.CLOSED;
             mALCOHOLAcclLeftState = ChannelStates.CLOSED;
             mNineAxisRightState = ChannelStates.CLOSED;
             mNineAxisLeftState = ChannelStates.CLOSED;
             mAntStateText = "Disabled";

             mEnabling = false;

//             mCallbackSink.notifyAntStateChanged();
          }
          else if (ANTAction.equals(AntInterfaceIntent.ANT_RESET_ACTION))
          {
             Log.d(TAG, "onReceive: ANT RESET");

             if(false == mAntResetSent)
             {
                //Someone else triggered an ANT reset
                Log.d(TAG, "onReceive: ANT RESET: Resetting state");

                if(mRIPECGState != ChannelStates.CLOSED)
                {
                   mRIPECGState = ChannelStates.CLOSED;
//                   mCallbackSink.notifyChannelStateChanged(MOTE_RIPECG_CHANNEL);
                }

                if(mALCOHOLState != ChannelStates.CLOSED)
                {
                   mALCOHOLState = ChannelStates.CLOSED;
//                   mCallbackSink.notifyChannelStateChanged(MOTE_ALCOHOL_CHANNEL);
                }

                if(mALCOHOLAcclRightState != ChannelStates.CLOSED) {
                   mALCOHOLAcclRightState = ChannelStates.CLOSED;
                }
                if(mALCOHOLAcclLeftState != ChannelStates.CLOSED) {
                    mALCOHOLAcclLeftState = ChannelStates.CLOSED;
                }

                if(mNineAxisRightState != ChannelStates.CLOSED) {
                    mNineAxisRightState = ChannelStates.CLOSED;
                }
                if(mNineAxisLeftState != ChannelStates.CLOSED) {
                    mNineAxisLeftState = ChannelStates.CLOSED;
                }

                mBufferThreshold = 0;
             }
             else
             {
                mAntResetSent = false;
                //Reconfigure event buffering
                setAntConfiguration();
                //Check if opening a channel was deferred, if so open it now.
                if(mDeferredRIPECGStart)
                {
                    openChannel(MOTE_RIPECG_CHANNEL, false);
                    mDeferredRIPECGStart = false;
                }
                if(mDeferredALCOHOLStart)
                {
                    openChannel(MOTE_ALCOHOL_CHANNEL, false);
                    mDeferredALCOHOLStart = false;
                }
                if(mDeferredALCOHOLAcclRightStart) {
                    openChannel(MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL, false);
                    mDeferredALCOHOLAcclRightStart = false;
                }
                if(mDeferredALCOHOLAcclLeftStart) {
                    openChannel(MOTE_ALCOHOL_ACCL_LEFT_CHANNEL, false);
                    mDeferredALCOHOLAcclLeftStart = false;
                }

                if(mDeferredNineAxisRightStart) {
                    openChannel(MOTE_NINE_AXIS_RIGHT_CHANNEL, false);
                    mDeferredNineAxisRightStart = false;
                }
                if(mDeferredNineAxisLeftStart) {
                    openChannel(MOTE_NINE_AXIS_LEFT_CHANNEL, false);
                    mDeferredNineAxisLeftStart = false;
                }

                if(mDeferredWeightStart)
                {
                    mDeferredWeightStart = false;
                }
             }
          }
          else if (ANTAction.equals(AntInterfaceIntent.ANT_INTERFACE_CLAIMED_ACTION))
          {
             Log.i(TAG, "onReceive: ANT INTERFACE CLAIMED");

             boolean wasClaimed = mClaimedAntInterface;

             // Could also read ANT_INTERFACE_CLAIMED_PID from intent and see if it matches the current process PID.
             try
             {
                 mClaimedAntInterface = mAntReceiver.hasClaimedInterface();

                 if(mClaimedAntInterface)
                 {
                     Log.i(TAG, "onReceive: ANT Interface claimed");

                     receiveAntRxMessages(true);
                 }
                 else
                 {
                     // Another application claimed the ANT Interface...
                     if(wasClaimed)
                     {
                         // ...and we had control before that.
                         Log.i(TAG, "onReceive: ANT Interface released");

                         receiveAntRxMessages(false);

                         mAntStateText = "ANT In Use";
//                         mCallbackSink.notifyAntStateChanged();
                     }
                 }
             }
             catch(AntInterfaceException e)
             {
                 antError();
             }
          }
       }
    };

/*    public static String getHexString(byte[] data)
    {
        if(null == data)
        {
            return "";
        }

        StringBuffer hexString = new StringBuffer();
        for(int i = 0;i < data.length; i++)
        {
           hexString.append("[").append(String.format("%02X", data[i] & 0xFF)).append("]");
        }

        return hexString.toString();
    }
 */
    /** Receives all of the ANT message intents and dispatches to the proper handler. */
    private final BroadcastReceiver mAntMessageReceiver = new BroadcastReceiver()
    {
       Context mContext;

       public void onReceive(Context context, Intent intent) {
          mContext = context;
          long timestamp = System.currentTimeMillis();

          String ANTAction = intent.getAction();

          Log.d(TAG, "enter onReceive: " + ANTAction);
          if (ANTAction.equals(AntInterfaceIntent.ANT_RX_MESSAGE_ACTION)) {
             Log.d(TAG, "onReceive: ANT RX MESSAGE");

             byte[] ANTRxMessage = intent.getByteArrayExtra(AntInterfaceIntent.ANT_MESSAGE);

//             Log.d(TAG, "Rx:"+ getHexString(ANTRxMessage));
             //if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntConnection)_msgrcv("+ANTRxMessage[AntMesg.MESG_ID_OFFSET]+" "+getHexString(ANTRxMessage));

             switch(ANTRxMessage[AntMesg.MESG_ID_OFFSET]) {
                 case AntMesg.MESG_STARTUP_MESG_ID:
                     break;
                 case AntMesg.MESG_BROADCAST_DATA_ID:
                 case AntMesg.MESG_ACKNOWLEDGED_DATA_ID:

//                     byte channelNum = ANTRxMessage[AntMesg.MESG_DATA_OFFSET];
                     byte channelNum = (byte)(ANTRxMessage[AntMesg.MESG_DATA_OFFSET] & AntDefine.CHANNEL_NUMBER_MASK);
//               		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: here ("+channelNum+")");

              		long curTime=System.currentTimeMillis();

                     switch(channelNum) {
                         case MOTE_RIPECG_CHANNEL:
                            AntConnection.setLast_Data_Received_Time(curTime, Constants.MOTE_RIPECG_IND);

                             antDecodeRIPECG(ANTRxMessage, timestamp);
                             break;
                         case MOTE_ALCOHOL_CHANNEL:
                            AntConnection.setLast_Data_Received_Time(curTime, Constants.MOTE_ALCOHOL_IND);

                             antDecodeALCOHOL(ANTRxMessage, timestamp);
                             break;
                         case MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL:
                        	 AntConnection.setLast_Data_Received_Time(curTime, Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND);
                        	 antDecodeALCOHOL(ANTRxMessage, timestamp);
                        	 break;
                         case MOTE_ALCOHOL_ACCL_LEFT_CHANNEL:
                        	 AntConnection.setLast_Data_Received_Time(curTime, Constants.MOTE_ALCOHOL_ACCL_LEFT_IND);
                        	 antDecodeALCOHOL(ANTRxMessage, timestamp);
                        	 break;

                         case MOTE_NINE_AXIS_RIGHT_CHANNEL:
                        	 AntConnection.setLast_Data_Received_Time(curTime, Constants.MOTE_NINE_AXIS_RIGHT_IND);
                        	 antDecodeALCOHOL(ANTRxMessage, timestamp);
                        	 break;
                         case MOTE_NINE_AXIS_LEFT_CHANNEL:
                        	 AntConnection.setLast_Data_Received_Time(curTime, Constants.MOTE_NINE_AXIS_LEFT_IND);
                        	 antDecodeALCOHOL(ANTRxMessage, timestamp);
                        	 break;
                     }
                     break;
                 case AntMesg.MESG_BURST_DATA_ID:
                     break;
                 case AntMesg.MESG_RESPONSE_EVENT_ID:
                     responseEventHandler(ANTRxMessage);
                     break;
                 case AntMesg.MESG_CHANNEL_STATUS_ID:
                     break;
                 case AntMesg.MESG_CHANNEL_ID_ID:
                     short deviceNum = (short) ((ANTRxMessage[AntMesg.MESG_DATA_OFFSET + 1]&0xFF | ((ANTRxMessage[AntMesg.MESG_DATA_OFFSET + 2]&0xFF) << 8)) & 0xFFFF);
                     switch(ANTRxMessage[AntMesg.MESG_DATA_OFFSET]) //Switch on channel number
                     {
                         case MOTE_RIPECG_CHANNEL:
                             Log.i(TAG, "onRecieve: Received RIPECG device number: " + deviceNum);
                             mDeviceNumberRIPECG = deviceNum;
                             break;
                         case MOTE_ALCOHOL_CHANNEL:
                             Log.i(TAG, "onRecieve: Received ALCOHOL device number: " + deviceNum);
                             mDeviceNumberALCOHOL = deviceNum;
                             break;
                         case MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL:
                             Log.i(TAG, "onRecieve: Received ALCOHOL Accelerometer Right device number: " + deviceNum);
                             mDeviceNumberALCOHOLAcclRight = deviceNum;
                             break;
                         case MOTE_ALCOHOL_ACCL_LEFT_CHANNEL:
                             Log.i(TAG, "onRecieve: Received ALCOHOL Accelerometer Left device number: " + deviceNum);
                             mDeviceNumberALCOHOLAcclLeft = deviceNum;
                             break;

                         case MOTE_NINE_AXIS_RIGHT_CHANNEL:
                             Log.i(TAG, "onRecieve: Received Nine Axis Right device number: " + deviceNum);
                             mDeviceNumberNineAxisRight = deviceNum;
                             break;
                         case MOTE_NINE_AXIS_LEFT_CHANNEL:
                             Log.i(TAG, "onRecieve: Received Nine Axis Left device number: " + deviceNum);
                             mDeviceNumberNineAxisLeft = deviceNum;
                             break;
                     }
                     break;
                 case AntMesg.MESG_VERSION_ID:
                     break;
                 case AntMesg.MESG_CAPABILITIES_ID:
                     break;
                 case AntMesg.MESG_GET_SERIAL_NUM_ID:
                     break;
                 case AntMesg.MESG_EXT_ACKNOWLEDGED_DATA_ID:
                     break;
                 case AntMesg.MESG_EXT_BROADCAST_DATA_ID:
                     break;
                 case AntMesg.MESG_EXT_BURST_DATA_ID:
                     break;
             }
          }
       }

       /**
        * Handles response and channel event messages
        * @param ANTRxMessage
        */
       private void responseEventHandler(byte[] ANTRxMessage)
       {
           // For a list of possible message codes
           // see ANT Message Protocol and Usage section 9.5.6.1
           // available from thisisant.com
           switch(ANTRxMessage[AntMesg.MESG_DATA_OFFSET + 2]) //Switch on message code
           {
               case AntDefine.EVENT_RX_SEARCH_TIMEOUT:
                   switch(ANTRxMessage[AntMesg.MESG_DATA_OFFSET]) //Switch on channel number
                   {
                       case MOTE_RIPECG_CHANNEL:
                           try
                           {
                               Log.i(TAG, "responseEventHandler: Received search timeout on RIPECG channel");

                               mRIPECGState = ChannelStates.OFFLINE;
//                               mCallbackSink.notifyChannelStateChanged(MOTE_RIPECG_CHANNEL);
                               mAntReceiver.ANTUnassignChannel(MOTE_RIPECG_CHANNEL);
                           }
                           catch(AntInterfaceException e)
                           {
                               antError();
                           }
                           break;
                       case MOTE_ALCOHOL_CHANNEL:
                           try
                           {
                               Log.i(TAG, "responseEventHandler: Received search timeout on ALCOHOL channel");

                               mALCOHOLState = ChannelStates.OFFLINE;
//                               mCallbackSink.notifyChannelStateChanged(MOTE_ALCOHOL_CHANNEL);
                               mAntReceiver.ANTUnassignChannel(MOTE_ALCOHOL_CHANNEL);
                           }
                           catch(AntInterfaceException e)
                           {
                               antError();
                           }
                           break;
                       case MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL:
                           try {
                               Log.i(TAG, "responseEventHandler: Received search timeout on ALCOHOL Accelerometer Right channel");
                               mALCOHOLAcclRightState = ChannelStates.OFFLINE;
                               mAntReceiver.ANTUnassignChannel(MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL);
                           }
                           catch(AntInterfaceException e) {
                               antError();
                           }
                           break;
                       case MOTE_ALCOHOL_ACCL_LEFT_CHANNEL:
                           try {
                               Log.i(TAG, "responseEventHandler: Received search timeout on ALCOHOL Accelerometer Left channel");
                               mALCOHOLAcclLeftState = ChannelStates.OFFLINE;
                               mAntReceiver.ANTUnassignChannel(MOTE_ALCOHOL_ACCL_LEFT_CHANNEL);
                           }
                           catch(AntInterfaceException e) {
                               antError();
                           }
                           break;

                       case MOTE_NINE_AXIS_RIGHT_CHANNEL:
                           try {
                               Log.i(TAG, "responseEventHandler: Received search timeout on Nine Axis Right channel");
                               mNineAxisRightState = ChannelStates.OFFLINE;
                               mAntReceiver.ANTUnassignChannel(MOTE_NINE_AXIS_RIGHT_CHANNEL);
                           }
                           catch(AntInterfaceException e) {
                               antError();
                           }
                           break;
                       case MOTE_NINE_AXIS_LEFT_CHANNEL:
                           try {
                               Log.i(TAG, "responseEventHandler: Received search timeout on Nine Axis Left channel");
                               mNineAxisLeftState = ChannelStates.OFFLINE;
                               mAntReceiver.ANTUnassignChannel(MOTE_NINE_AXIS_LEFT_CHANNEL);
                           }
                           catch(AntInterfaceException e) {
                               antError();
                           }
                           break;
                   }
                   break;
           }
       }


       /**
        * Decode ANT+ Weight scale messages.
        *
        * @param ANTRxMessage the received ANT message.
        */

       /**
        * Decode ANT+ ALCOHOL messages.
        *
        * @param ANTRxMessage the received ANT message.
        */
	private void antDecodeALCOHOL(byte[] ANTRxMessage, long timestamp) {
		try {
			Log.d(TAG, "antDecodeALCOHOL start");
			Log.d(TAG, "antDecodeALCOHOL: Received broadcast");

			if(mALCOHOLState != ChannelStates.CLOSED) {
				Log.d(TAG, "antDecodeALCOHOL: Tracking data");
				mALCOHOLState = ChannelStates.TRACKING_DATA;
				//mCallbackSink.notifyChannelStateChanged(MOTE_ALCOHOL_CHANNEL);
			}
			if(mALCOHOLAcclRightState != ChannelStates.CLOSED) {
				Log.d(TAG, "antDecodeALCOHOL: Tracking data for right hand mote");
				mALCOHOLAcclRightState = ChannelStates.TRACKING_DATA;
			}
			if(mALCOHOLAcclLeftState != ChannelStates.CLOSED) {
				Log.d(TAG, "antDecodeALCOHOL: Tracking data for left hand mote");
				mALCOHOLAcclLeftState = ChannelStates.TRACKING_DATA;
			}

			if(mNineAxisRightState != ChannelStates.CLOSED) {
				Log.d(TAG, "antDecodeALCOHOL: Tracking data for Right hand mote [Nine Axis]");
				mNineAxisRightState = ChannelStates.TRACKING_DATA;
			}
			if(mNineAxisLeftState != ChannelStates.CLOSED) {
				Log.d(TAG, "antDecodeALCOHOL: Tracking data for Left hand mote [Nine Axis]");
				mNineAxisLeftState = ChannelStates.TRACKING_DATA;
			}

			// If using a wild card search, request the channel ID
			if(mDeviceNumberALCOHOL == WILDCARD) {
				try {
					Log.i(TAG, "antDecodeALCOHOL: Requesting device number");
					mAntReceiver.ANTRequestMessage(MOTE_ALCOHOL_CHANNEL, AntMesg.MESG_CHANNEL_ID_ID);
				} catch(AntInterfaceException e) {
					antError();
				}
			}
			// If using a wild card search, request the channel ID
			if(mDeviceNumberALCOHOLAcclRight == WILDCARD) {
				try {
					Log.i(TAG, "antDecodeALCOHOL: Requesting device number for right mote");
					mAntReceiver.ANTRequestMessage(MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL, AntMesg.MESG_CHANNEL_ID_ID);
				} catch(AntInterfaceException e) {
					antError();
				}
			}
			// If using a wild card search, request the channel ID
			if(mDeviceNumberALCOHOLAcclLeft == WILDCARD) {
				try {
					Log.i(TAG, "antDecodeALCOHOL: Requesting device number for left mote");
					mAntReceiver.ANTRequestMessage(MOTE_ALCOHOL_ACCL_LEFT_CHANNEL, AntMesg.MESG_CHANNEL_ID_ID);
				} catch(AntInterfaceException e) {
					antError();
				}
			}

			// If using a wild card search, request the channel ID
			if(mDeviceNumberNineAxisRight == WILDCARD) {
				try {
					Log.i(TAG, "antDecodeALCOHOL: Requesting device number for Right mote [Nine Axis]");
					mAntReceiver.ANTRequestMessage(MOTE_NINE_AXIS_RIGHT_CHANNEL, AntMesg.MESG_CHANNEL_ID_ID);
				} catch(AntInterfaceException e) {
					antError();
				}
			}
			// If using a wild card search, request the channel ID
			if(mDeviceNumberNineAxisLeft == WILDCARD) {
				try {
					Log.i(TAG, "antDecodeALCOHOL: Requesting device number for Left mote [Nine Axis]");
					mAntReceiver.ANTRequestMessage(MOTE_NINE_AXIS_LEFT_CHANNEL, AntMesg.MESG_CHANNEL_ID_ID);
				} catch(AntInterfaceException e) {
					antError();
				}
			}
			byte moteChannelNumber = (byte)(ANTRxMessage[AntMesg.MESG_DATA_OFFSET] & AntDefine.CHANNEL_NUMBER_MASK);
			byte mChannelNumber = (byte)(ANTRxMessage[AntMesg.MESG_DATA_OFFSET+8] & 0x0F);
			int[] samples = decodeAutoSenseSamples(ANTRxMessage);
			//if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: AntConnection_AL_channel="+mChannelNumber+" data=["+samples[0]+" "+samples[1]+" "+ samples[2]+" "+samples[3]+" "+samples[4]+"]");

			//if(Log.DEBUG_HILLOL) Log.m(TAG,"Channel="+mChannelNumber+", samples=["+samples[0]+" "+samples[1]+" "+ samples[2]+" "+samples[3]+" "+samples[4]+"]");

			if(moteChannelNumber == MOTE_ALCOHOL_CHANNEL) {
				switch(mChannelNumber) {
				case ALCOHOL_CHANNEL:
					Packetizer.getInstance().addPacket(samples,SAMPLE_NO,ChannelToSensorMapping.ALCOHOL, timestamp);
					break;
				case GSRWRIST_CHANNEL:
					Packetizer.getInstance().addPacket(samples,SAMPLE_NO,ChannelToSensorMapping.GSRWRIST, timestamp);
					break;
				case BODYTEMPWRIST_CHANNEL:
					Packetizer.getInstance().addPacket(samples,SAMPLE_NO,ChannelToSensorMapping.TEMPWRIST, timestamp);
					break;
				default:
					//Log.w(TAG, "Unknown channel " + mChannelNumber);
				}
			} else if(moteChannelNumber==MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL || moteChannelNumber==MOTE_ALCOHOL_ACCL_LEFT_CHANNEL) {
				switch(mChannelNumber) {
				case ALCOHOL_ACCL_X_CHANNEL:
					if(moteChannelNumber == MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL) {
						Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.ALCOHOL_ACCL_RIGHT_X, timestamp);
					} else if(moteChannelNumber == MOTE_ALCOHOL_ACCL_LEFT_CHANNEL) {
						Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.ALCOHOL_ACCL_LEFT_X, timestamp);
					} else {
						Log.e(TAG, "ALCOHOL_ACCL_X_CHANNEL: neither left nor right");
					}
					break;
				case ALCOHOL_ACCL_Y_CHANNEL:
					if(moteChannelNumber == MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL) {
						Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.ALCOHOL_ACCL_RIGHT_Y, timestamp);
					} else if(moteChannelNumber == MOTE_ALCOHOL_ACCL_LEFT_CHANNEL) {
						Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.ALCOHOL_ACCL_LEFT_Y, timestamp);
					} else {
						Log.e(TAG, "ALCOHOL_ACCL_Y_CHANNEL: neither left nor right");
					}
					break;
				case ALCOHOL_ACCL_Z_CHANNEL:
					if(moteChannelNumber == MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL) {
						Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.ALCOHOL_ACCL_RIGHT_Z, timestamp);
					} else if(moteChannelNumber == MOTE_ALCOHOL_ACCL_LEFT_CHANNEL) {
						Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.ALCOHOL_ACCL_LEFT_Z, timestamp);
					} else {
						Log.e(TAG, "ALCOHOL_ACCL_Z_CHANNEL: neither left nor right");
					}
					break;
				default:
					//Log.w(TAG, "Unknown channel " + mChannelNumber);
				}
			} else if(moteChannelNumber==MOTE_NINE_AXIS_RIGHT_CHANNEL) {
				switch(mChannelNumber) {
				case NINE_AXIS_ACCL_X_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_X, timestamp);
					break;
				case NINE_AXIS_ACCL_Y_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_Y, timestamp);
					break;
				case NINE_AXIS_ACCL_Z_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_Z, timestamp);
					break;
				case NINE_AXIS_GYRO_X_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_RIGHT_GYRO_X, timestamp);
					break;
				case NINE_AXIS_GYRO_Y_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_RIGHT_GYRO_Y, timestamp);
					break;
				case NINE_AXIS_GYRO_Z_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_RIGHT_GYRO_Z, timestamp);
					break;
				case NINE_AXIS_NULL_PACKET_CHANNEL:
					//convertSamplesToTwosComplement(samples); // Null packets containing battery level is not in 2's complement format.
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_RIGHT_NULL_PACKET, timestamp);
					break;
				default:
					//Log.w(TAG, "Right Unknown channel " + mChannelNumber);
					//if(Log.DEBUG_HILLOL) Log.m(TAG,"Right Channel="+mChannelNumber+", samples=["+samples[0]+" "+samples[1]+" "+ samples[2]+" "+samples[3]+" "+samples[4]+"]");
				}
			} else if(moteChannelNumber==MOTE_NINE_AXIS_LEFT_CHANNEL) {
				switch(mChannelNumber) {
				case NINE_AXIS_ACCL_X_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_X, timestamp);
					break;
				case NINE_AXIS_ACCL_Y_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_Y, timestamp);
					break;
				case NINE_AXIS_ACCL_Z_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_Z, timestamp);
					break;
				case NINE_AXIS_GYRO_X_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_LEFT_GYRO_X, timestamp);
					break;
				case NINE_AXIS_GYRO_Y_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_LEFT_GYRO_Y, timestamp);
					break;
				case NINE_AXIS_GYRO_Z_CHANNEL:
					convertSamplesToTwosComplement(samples);
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_LEFT_GYRO_Z, timestamp);
					break;
				case NINE_AXIS_NULL_PACKET_CHANNEL:
					//convertSamplesToTwosComplement(samples); // Null packets containing battery level is not in 2's complement format.
					Packetizer.getInstance().addPacket(samples, SAMPLE_NO, ChannelToSensorMapping.NINE_AXIS_LEFT_NULL_PACKET, timestamp);
					break;
				default:
					//Log.w(TAG, "Left Unknown channel " + mChannelNumber);
					//if(Log.DEBUG_HILLOL) Log.m(TAG,"Left Channel="+mChannelNumber+", samples=["+samples[0]+" "+samples[1]+" "+ samples[2]+" "+samples[3]+" "+samples[4]+"]");
				}
			}
			//mCallbackSink.notifyChannelDataChanged(MOTE_ALCOHOL_CHANNEL);
			Log.d(TAG, "antDecodeALCOHOL end");
		} catch(Exception e) {
		}
	}
       /**
        * Decode AutoSense samples.
        *
        * @param ANTRxMessage the received ANT message.
        */

       private int[] decodeAutoSenseSamples(byte[] ANTRxMessage)
       {
           int[] samples = new int[5];
           /* Decode 5 samples of 12 bits each */
           samples[0] = (short)(( (((short)ANTRxMessage[AntMesg.MESG_DATA_OFFSET+1] & 0x00FF) << 4) | (((short)ANTRxMessage[AntMesg.MESG_DATA_OFFSET+2] & 0x00FF) >>> 4) ) & 0x0FFF);
           samples[1] = (short)(( (((short)ANTRxMessage[AntMesg.MESG_DATA_OFFSET+2] & 0x00FF) << 8) | ((short)ANTRxMessage[AntMesg.MESG_DATA_OFFSET+3] & 0x00FF) ) & 0x0FFF);
           samples[2] = (short)(( (((short)ANTRxMessage[AntMesg.MESG_DATA_OFFSET+4] & 0x00FF) << 4) | (((short)ANTRxMessage[AntMesg.MESG_DATA_OFFSET+5] & 0x00FF) >>> 4) ) & 0x0FFF);
           samples[3] = (short)(( (((short)ANTRxMessage[AntMesg.MESG_DATA_OFFSET+5] & 0x00FF) << 8) | ((short)ANTRxMessage[AntMesg.MESG_DATA_OFFSET+6] & 0x00FF) ) & 0x0FFF);
           samples[4] = (short)(( (((short)ANTRxMessage[AntMesg.MESG_DATA_OFFSET+7] & 0x00FF) << 4) | (((short)ANTRxMessage[AntMesg.MESG_DATA_OFFSET+8] & 0x00FF) >>> 4) ) & 0x0FFF);

           return samples;
       }

       private void convertSamplesToTwosComplement(int[] samples) {
    	   for(int i=0; i<samples.length; i++) {
    		   samples[i] = CommonUtil.TwosComplement(samples[i], 12);
    	   }
       }

       /**
        * Decode ANT+ RIPECG messages.
        *
        * @param ANTRxMessage the received ANT message.
        */
       private void antDecodeRIPECG(byte[] ANTRxMessage, long timestamp)
       { try{
          Log.d(TAG, "antDecodeALCOHOL start");
    	  final byte[] fixed_send_order = {0,1,0,2,0,7,0,3,0,4,0,7,(byte)0xFF,(byte)0xFF,(byte)0xFF,8};
          {
             Log.d(TAG, "antDecodeALCOHOL: Received broadcast");

             if(mRIPECGState != ChannelStates.CLOSED)
             {
                Log.d(TAG, "antDecodeALCOHOL: Tracking data");

                mRIPECGState = ChannelStates.TRACKING_DATA;
//                mCallbackSink.notifyChannelStateChanged(MOTE_RIPECG_CHANNEL);
             }

             // If using a wild card search, request the channel ID
             if(mDeviceNumberRIPECG == WILDCARD)
             {
                 try
                 {
                     Log.i(TAG, "antDecodeRIPECG: Requesting device number");

                     mAntReceiver.ANTRequestMessage(MOTE_RIPECG_CHANNEL, AntMesg.MESG_CHANNEL_ID_ID);
                 }
                 catch(AntInterfaceException e)
                 {
                     antError();
                 }
             }

             byte mSequenceNumber = (byte)(ANTRxMessage[AntMesg.MESG_DATA_OFFSET+8] & 0x0F);
             byte mChannelNumber = fixed_send_order[mSequenceNumber];
             Log.d(TAG, "antDecodeAutoSense: Sequence Number " + mSequenceNumber + " Channel: " + mChannelNumber);
//    		 if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntConnection_RE)_channel="+mSequenceNumber);

             int[] samples = decodeAutoSenseSamples(ANTRxMessage);
 //   	     setIsReceived(true,Constants.MOTE_RIPECG_IND);
 //   	     setLast_Data_Received_Time(timestamp,Constants.MOTE_RIPECG_IND);
//             if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: AntConnection_RE_channel="+fixed_send_order[mSequenceNumber]+" data=["+samples[0]+" "+samples[1]+" "+ samples[2]+" "+samples[3]+" "+samples[4]+"]");
             switch(fixed_send_order[mSequenceNumber])
             {
             case ECG_CHANNEL:
					Packetizer.getInstance().addPacket(samples,SAMPLE_NO,ChannelToSensorMapping.ECG, timestamp);
            	 break;
             case ACCELX_CHANNEL:
					Packetizer.getInstance().addPacket(samples,SAMPLE_NO,ChannelToSensorMapping.ACCELX, timestamp);
            	 break;
             case ACCELY_CHANNEL:
					Packetizer.getInstance().addPacket(samples,SAMPLE_NO,ChannelToSensorMapping.ACCELY, timestamp);
              	 break;
             case ACCELZ_CHANNEL:
					Packetizer.getInstance().addPacket(samples,SAMPLE_NO,ChannelToSensorMapping.ACCELZ, timestamp);
            	 break;
             case GSR_CHANNEL:
					Packetizer.getInstance().addPacket(samples,SAMPLE_NO,ChannelToSensorMapping.GSR, timestamp);

            	 break;
             case RIP_CHANNEL:
					Packetizer.getInstance().addPacket(samples,SAMPLE_NO,ChannelToSensorMapping.RIP, timestamp);
            	 break;
             case MISC_CHANNEL:
            	 	Packetizer.getInstance().addPacket(samples, SAMPLE_NO, 8, timestamp);
	         	    //try {
	        	    	//textBATTERY.setText("Battery: " + (float)samples[0]/4096*3*2 + "V");
	        	    	//textSKIN.setText("Skin Temperature: " + samples[1]);
	        	    	//textAMBIENT.setText("Ambient Temperature: " + samples[2]);
	        	    	//Log.d(TAG, "Battery: " + (float)samples[0]/4096*3*2 + "V");
	        	    	//Log.d(TAG, "Skin Temperature: " + samples[1]);
	        	    	//Log.d(TAG, "Ambient Temperature: " + samples[2]);
	        	    //}
	        	    //catch(Exception e)
	        	    //{
	        	    //	Log.e(TAG, "antDecodeAutoSense: Caught exception " + e);
	        	    //}
         	 		//Log.d(TAG, "SKIN TEMP: " + samples[1]);
         	 		//Log.d(TAG, "AMBIENT TEMP: " + samples[2]);
         	 		//Log.d(TAG, "BATTERY: " + samples[0]);
            	 break;
            	 default:
            		 Log.w(TAG, "Unknown channel " + mChannelNumber);
//            		 if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntConnection_RE)_channel="+fixed_send_order[mSequenceNumber]+"data=["+samples[0]+" "+samples[1]+" "+ samples[2]+" "+samples[3]+" "+samples[4]+"]");
             }

//           mCallbackSink.notifyChannelDataChanged(MOTE_RIPECG_CHANNEL);
          }
          Log.d(TAG, "antDecodeALCOHOL end");
       }
       catch(Exception e){}
       }

    };

    /**
     * ANT Channel Configuration.
     *
     * @param networkNumber the network number
     * @param channelNumber the channel number
     * @param deviceNumber the device number
     * @param deviceType the device type
     * @param txType the tx type
     * @param channelPeriod the channel period
     * @param radioFreq the radio freq
     * @param proxSearch the prox search
     * @return true, if successfully configured and opened channel
     */
    private boolean antChannelSetup(byte networkNumber, byte channelNumber, short deviceNumber, byte deviceType, byte txType, short channelPeriod, byte radioFreq, byte proxSearch)
    {
       boolean channelOpen = false;

       try
       {
           mAntReceiver.ANTAssignChannel(channelNumber, AntDefine.PARAMETER_RX_NOT_TX, networkNumber);  // Assign as slave channel on selected network (0 = public, 1 = ANT+, 2 = ANTFS)
           mAntReceiver.ANTSetChannelId(channelNumber, deviceNumber, deviceType, txType);
           mAntReceiver.ANTSetChannelPeriod(channelNumber, channelPeriod);
           mAntReceiver.ANTSetChannelRFFreq(channelNumber, radioFreq);
           mAntReceiver.ANTSetChannelSearchTimeout(channelNumber, (byte)0); // Disable high priority search
           mAntReceiver.ANTSetLowPriorityChannelSearchTimeout(channelNumber,(byte) 12); // Set search timeout to 30 seconds (low priority search)

           if(deviceNumber == WILDCARD)
           {
               mAntReceiver.ANTSetProximitySearch(channelNumber, proxSearch);   // Configure proximity search, if using wild card search
           }

           mAntReceiver.ANTOpenChannel(channelNumber);

           channelOpen = true;
       }
       catch(AntInterfaceException aie)
       {
           antError();
       }

       return channelOpen;
    }

    /**
     * Enable/disable receiving ANT Rx messages.
     *
     * @param register If want to register to receive the ANT Rx Messages
     */
    private void receiveAntRxMessages(boolean register)
    {
        if(register)
        {
            Log.i(TAG, "receiveAntRxMessages: START");
            mContext.registerReceiver(mAntMessageReceiver, new IntentFilter(AntInterfaceIntent.ANT_RX_MESSAGE_ACTION));
        }
        else
        {
            try
            {
                mContext.unregisterReceiver(mAntMessageReceiver);
            }
            catch(IllegalArgumentException e)
            {
                // Receiver wasn't registered, ignore as that's what we wanted anyway
            }

            Log.i(TAG, "receiveAntRxMessages: STOP");
        }
    }
}
