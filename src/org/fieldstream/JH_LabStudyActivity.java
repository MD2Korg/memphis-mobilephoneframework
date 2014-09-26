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
package org.fieldstream;

//@author Syed Monowar Hossain

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import edu.cmu.ices.stress.phone.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;



import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class JH_LabStudyActivity extends BaseActivity {

	// exit dialog stuff
	DatabaseLogger db;
	private final static String SELFREPORT_CODE = "3841";

	private final static int SESSION_MULTI=501;
	private final static int EVENTORDER_MULTI=601;
	private final static int EVENTORDER_UNORDERED=602;
	private final static int EVENT_BEGIN=701;
	private final static int EVENT_RUNNING=702;
	private final static int EVENT_END=703;
	private final static int MARK_BEGIN=711;
	private final static int MARK_END=712;

	private final static int SESSION_END=801;
	private final static int LEFT_BUTTON=1;
	private final static int RIGHT_BUTTON=2;
	private final static int EVENT_BUTTON=3;
	private String selStudyName;

	private String []listSession;
	private int noSession;
	private int selSessionInd;

	private String [][]listEvent;
	private String [][]listEventType;
	private int [][]usedEvent;
	private int []noEvent;

	private String [][]listOrderEvent;
	private int []orderEvent;
	private int []noListOrderEvent;
	private String[][]nameListOrderEvent;
	private int selOrderEventInd;
	private int buttonPressed;
	private int selEventInd;
	private int curState;
	private long eventStartTime;
	private long eventEndTime;
	private TextView TV_studyname;
	private TextView TV_sessionname;
	private TextView TV_eventorder;
	private TextView TV_taskname;
	private Spinner S_list_unordered_event;
	private TextView TV_taskstatus;
	private TextView TV_taskinfo;

	private Button B_event;
	private Spinner S_list_session_event;
	//private TextView label;
	//private TextView incentives;
	private EditText E_comment;
	private Button B_savecomment;
	private Button B_cancelcomment;
	private Button B_left;
	private Button B_right;


	private Intent jh_selfReportIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		start=0;
		super.onCreate(savedInstanceState);
		// why imp??
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (LabStudyActivity)_onCreate()");
//		DatabaseLogger.makeActive();
		titleText="LabStudy (Johns Hopkins)";
//		setTitleBar(NOT_CONNECTED);

		db = DatabaseLogger.getInstance(this);
    	db.logAnything("labstudy_log", "Program Starts", System.currentTimeMillis());

//		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.labstudy_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		titleText="LabStudy (JH)";
		setTitleBar(NOT_CONNECTED);

		//setContentView(R.layout.network_setup_layout);
		TV_studyname=(TextView)findViewById(R.id.TV_studyname);
		TV_sessionname=(TextView)findViewById(R.id.TV_sessionname);
		TV_eventorder=(TextView)findViewById(R.id.TV_eventorder);
		TV_taskname=(TextView)findViewById(R.id.TV_taskname);
		S_list_unordered_event=(Spinner)findViewById(R.id.S_list_unorderd_event);
		TV_taskstatus=(TextView)findViewById(R.id.TV_taskstatus);
		TV_taskinfo=(TextView)findViewById(R.id.TV_taskinfo);

		B_event=(Button)findViewById(R.id.B_event);
		S_list_session_event=(Spinner)findViewById(R.id.S_list_session_event);

		E_comment=(EditText)findViewById(R.id.E_comment);
		B_savecomment=(Button)findViewById(R.id.B_savecomment);
		B_cancelcomment=(Button)findViewById(R.id.B_cancelcomment);

		B_left=(Button)findViewById(R.id.B_left);
		B_right=(Button)findViewById(R.id.B_right);

		// read config in!
		readLabStudyConfig();
		initState();
		createUI(curState);
		initListener();
		jh_selfReportIntent = new Intent(getBaseContext(),JH_SelfReportEventActivity.class);
		startActivity(jh_selfReportIntent);


	}
	void createAlertDialog(String msg)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg)
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                finish();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}


	private void readLabStudyConfig() {
	File root = Environment.getExternalStorageDirectory();
	try {
		File dir = new File(root+"/"+Constants.CONFIG_DIR);
		dir.mkdirs();
		File setupFile = new File(dir, Constants.LABSTUDY_CONFIG_FILENAME);
		if (!setupFile.exists()){
			createAlertDialog("\""+Constants.LABSTUDY_CONFIG_FILENAME+"\" is not found");
			return;
		}
		int i;
		NodeList nl;Node nd;Element el;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse(setupFile);

        dom.getDocumentElement ().normalize ();

        //<study name>
        selStudyName=dom.getElementsByTagName("studyname").item(0).getChildNodes().item(0).getNodeValue().trim();

        //<session>
        nl=dom.getElementsByTagName("session");
        noSession=nl.getLength();
        listSession=new String[noSession+1];listSession[0]="Select a Session";
        noEvent=new int[noSession+1];
        listEvent=new String[noSession+1][];
        listEventType=new String[noSession+1][];
        usedEvent=new int[noSession+1][];
        listOrderEvent=new String[noSession+1][];
        nameListOrderEvent=new String[noSession+1][];
        noListOrderEvent=new int[noSession+1];

        for(i=0;i<nl.getLength();i++){
        	nd = nl.item(i);
        	el=(Element)nd;
        	listSession[i+1]=el.getElementsByTagName("sessionname").item(0).getChildNodes().item(0).getNodeValue().trim();

        	if(el.getElementsByTagName("eventorder").getLength()==0)
        			noListOrderEvent[i+1]=0;
        	else if(el.getElementsByTagName("eventorder").item(0).getChildNodes().item(0).getNodeValue().trim().equals("unordered"))
        		noListOrderEvent[i+1]=0;
        	else if(el.getElementsByTagName("eventorder").item(0).getChildNodes().item(0).getNodeValue().trim().equals("ordered")){
        		noListOrderEvent[i+1]=el.getElementsByTagName("eventorder").getLength();

        		nameListOrderEvent[i+1]=new String[noListOrderEvent[i+1]+1];
        		listOrderEvent[i+1]=new String[noListOrderEvent[i+1]+1];
        		nameListOrderEvent[i+1][0]="Select an Event Order";
        		listOrderEvent[i+1][0]="";
        		for(int j=0;j<el.getElementsByTagName("eventorder").getLength();j++){
            		Element el1=(Element)el.getElementsByTagName("eventorder").item(j);
            		nameListOrderEvent[i+1][j+1]=el1.getAttribute("name").trim();
            		listOrderEvent[i+1][j+1]=el1.getAttribute("order").trim();
        		}
        	}
        	//System.out.println(noListOrderEvent[i]);

        	noEvent[i+1]=el.getElementsByTagName("event").getLength();
        	listEvent[i+1]=new String[noEvent[i+1]+1];
        	listEventType[i+1]=new String[noEvent[i+1]+1];
        	usedEvent[i+1]=new int[noEvent[i+1]+1];

        	listEvent[i+1][0]="Select an Event";
        	for(int j=0;j<noEvent[i+1];j++){
        		int ind;
        		Element el1=(Element)el.getElementsByTagName("event").item(j);
        		if(el1.getAttribute("id").trim()=="")
        			ind=j+1;
        		else
        			ind=Integer.parseInt(el1.getAttribute("id").trim());
        		listEvent[i+1][ind]=el1.getChildNodes().item(0).getNodeValue().trim();
        		if(el1.getAttribute("type").trim()=="")
        			listEventType[i+1][ind]="mark2";
        		else if(el1.getAttribute("type").trim().equals("mark1")==true)
        			listEventType[i+1][ind]="mark1";
        		else
        			listEventType[i+1][ind]="mark2";
        		usedEvent[i+1][ind]=0;
        	}
        }

/*//        String temp="Studyname="+selStudyname+", SsnNo="+Integer.toString(noSession)+""
	    String temp="";
	//    temp+=selStudyName;
	//    temp+=", ";temp+=Integer.toString(noSession);

        //System.out.println("-----------------------------------\n"+selStudyName);
        for(i=1;i<=noSession;i++){
        	temp+=", ";temp+="["+Integer.toString(i)+".."+Integer.toString(noListOrderEvent[i])+" "+Integer.toString(noEvent[i])+"]";
//        	System.out.println(listSession[i]);
        	for(int j=1;j<=noListOrderEvent[i];j++){
        		//temp=temp+"["+nameListOrderEvent[i][j]+"->"+listOrderEvent[i][j]+"]";
        		System.out.println(nameListOrderEvent[i][j]+"->"+listOrderEvent[i][j]);
        	}
        		System.out.println(noEvent[i]);
        	for(int j=1;j<=noEvent[i];j++)
        		System.out.print(listEvent[i][j]+" ");
        	System.out.println();
        }
    	createAlertDialog(temp);
  */
    }catch (Exception x) {
    //System.out.println (" Parsing error" + ", line " + err.getLineNumber () + ", uri "+ err.getSystemId ());
    //System.out.println("" + err.getMessage ()); }catch (SAXException e) {
    //Exception x = e.getException ();
    //((x == null) ? e : x).printStackTrace (); }catch (Throwable t) {
    x.printStackTrace ();
    }
//    catch(Exception e){
//    	System.out.println(e.getMessage());
//    }
    //System.exit (0);
/*		selStudyName="NIDA Study";
		listSession=new String[4];
		listSession[0]="Select a Session";
		listSession[1]="Day 1: PASAT-C(math) stressor";
		listSession[2]="Day 2: oCRH stressor";
		listSession[3]="Day 3: CPT stressor";
		noSession=3;
		this.listEvent=new String[noSession+1][9];
		listEvent[1][0]="Select an Event";
		listEvent[1][1]="Breakfast";
		listEvent[1][2]="Instructions";
		listEvent[1][3]="Level 1 difficulty";
		listEvent[1][4]="Level 2 difficulty";
		listEvent[1][5]="Level 3 difficulty";
		listEvent[1][6]="Multiple simultaneous saliva collections and self-reports";
		listEvent[1][7]="Guided relaxation session";
		listEvent[1][8]="Lunch";

		listEvent[2][0]="Select an Event";
		listEvent[2][1]="Rest period";
		listEvent[2][2]="Breakfast";
		listEvent[2][3]="Injection of CRH";
		listEvent[2][4]="Multiple simultaneous saliva collections and self-reports";
		listEvent[2][5]="Lunch";

		listEvent[3][0]="Select an Event";
		listEvent[3][1]="Breakfast";
		listEvent[3][2]="Quiet time/rest period/acclimation period";
		listEvent[3][3]="Hand in water";
		listEvent[3][4]="Hand out of water";
		listEvent[3][5]="Multiple simultaneous saliva collections and self-reports";
		listEvent[3][6]="Guided relaxation";
		listEvent[3][7]="Lunch";


		noEvent=new int[noSession+1];
		noEvent[1]=8;
		noEvent[2]=5;
		noEvent[3]=7;


		this.listOrderEvent=new String[noSession+1][3];
		listOrderEvent[1][0]="Select Order of Events";
		listOrderEvent[1][1]="1,2,3,4,5,6,7,8";
		listOrderEvent[1][2]="3,2,1,4,5,7,6,8";
		listOrderEvent[2][0]="Select Order of Events";
		listOrderEvent[2][1]="1,2,3,4,5";
		listOrderEvent[3][0]="Select Order of Events";
		listOrderEvent[3][1]="7,6,5,4,3,2,1";

		noListOrderEvent=new int[noSession+1];

		noListOrderEvent[1]=2;
		noListOrderEvent[2]=1;
		noListOrderEvent[3]=0;
*/

/*		loadDeadPeriods();


		startStuff();
		startService(emaScheduler);

		// callback to disable stress inference when the time is right!
		Log.i("onActivityResult", "started the scheduler service");
		CharSequence text = "Configuration successfully loaded";
		//label.setText("Running the study");
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(getApplicationContext(), text, duration);
		toast.show();
		*/
	}

	/* This is called when the app is killed. */
	@Override
	protected void onDestroy() {
		db.logAnything("labstudy_log", "Program Ends", System.currentTimeMillis());
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (LabStudyActivity)_onDestroy()");

		super.onDestroy();
	}


	void setEventOrder()
	{
		int i=0;
		String order=this.listOrderEvent[this.selSessionInd][this.selOrderEventInd];

		StringTokenizer st = new StringTokenizer(order,",");
		orderEvent=new int[this.noEvent[this.selSessionInd]+1];
		 while (st.hasMoreTokens()) {
//			 Toast.makeText(getBaseContext(), st.nextToken(),Toast.LENGTH_SHORT).show();

			 orderEvent[++i]=Integer.parseInt(st.nextToken());
		 }
	}
	void initState()
	{
    	db.logAnything("labstudy_log", "StudyName: "+this.selStudyName, System.currentTimeMillis());

		selSessionInd=0;
		selOrderEventInd=0;
		selEventInd=0;
		buttonPressed=0;
		if(noSession>1)
			curState=SESSION_MULTI;
		else{
			selSessionInd=noSession;
//	    	db.logAnything("labstudy_log", "SessionName: "+this.selStudyName, System.currentTimeMillis());

			if(noListOrderEvent[selSessionInd]>1)
				curState=EVENTORDER_MULTI;
			else if(noListOrderEvent[selSessionInd]==0){
				orderEvent=new int[this.noEvent[this.selSessionInd]+1];
				noListOrderEvent[selSessionInd]=1;
				nameListOrderEvent[selSessionInd]=new String [2];
				nameListOrderEvent[selSessionInd][1]="Unordered";
				listOrderEvent[selSessionInd]=new String [2];
				listOrderEvent[selSessionInd][1]="";
				this.selEventInd=1;
				for(int i=0;i<=noEvent[selSessionInd];i++) orderEvent[i]=0;
				curState=EVENTORDER_UNORDERED;

			}
			else{
				selOrderEventInd=1;
				setEventOrder();
				selEventInd=orderEvent[1];
				if(listEventType[selSessionInd][selEventInd]=="mark1")
					curState=MARK_BEGIN;
				else
					curState=EVENT_BEGIN;
			}
		}
	}
	void changeState()
	{
		int prevState=curState;

		if(buttonPressed==LEFT_BUTTON){
			switch(prevState){
			case EVENT_BEGIN:
			case MARK_BEGIN:
	            // prepare the alert box
	            AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

	            // set the message to display
	            alertbox.setMessage("Do you really want to SKIP this Event? (this event may not appear in future)");

	            // set a positive/yes button and create a listener
	            alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

	                // do something when the button is clicked
	                public void onClick(DialogInterface arg0, int arg1) {
	                    //Toast.makeText(getApplicationContext(), "'Yes' button clicked", Toast.LENGTH_SHORT).show();
	                	db.logAnything("labstudy_log", "EventSkip: "+listEvent[selSessionInd][orderEvent[selEventInd]], System.currentTimeMillis());
	                	if(listOrderEvent[selSessionInd][1]!="")
	                	selEventInd++;
	    				if(selEventInd<=noEvent[selSessionInd]){
	    					if(listOrderEvent[selSessionInd][1]=="")
	    						curState=EVENTORDER_UNORDERED;
	    					else{
	    						if(listEventType[selSessionInd][selEventInd]=="mark1")
	    							curState=MARK_BEGIN;
	    						else
	    							curState=EVENT_BEGIN;
	    					}
	    					createUI(curState);
	    				}
	    				else {
	    					curState=SESSION_END;
	    					createUI(curState);
	    				}
	                }
	            });
	            // set a negative/no button and create a listener
	            alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {

	                // do something when the button is clicked
	                public void onClick(DialogInterface arg0, int arg1) {
	                    //Toast.makeText(getApplicationContext(), "'No' button clicked", Toast.LENGTH_SHORT).show();
	                }
	            });

	            // display box
	            alertbox.show();

				break;
			case EVENT_RUNNING:
	            // prepare the alert box
	            AlertDialog.Builder alertbox1 = new AlertDialog.Builder(this);

	            // set the message to display
	            alertbox1.setMessage("Do you really want to restart this Event?");

	            // set a positive/yes button and create a listener
	            alertbox1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

	                // do something when the button is clicked
	                public void onClick(DialogInterface arg0, int arg1) {
	                    //Toast.makeText(getApplicationContext(), "'Yes' button clicked", Toast.LENGTH_SHORT).show();

	                	db.logAnything("labstudy_log", "EventRestart: "+listEvent[selSessionInd][orderEvent[selEventInd]], System.currentTimeMillis());
	                	curState=EVENT_BEGIN;
	    				createUI(curState);
	                }
	            });
	            // set a negative/no button and create a listener
	            alertbox1.setNegativeButton("No", new DialogInterface.OnClickListener() {

	                // do something when the button is clicked
	                public void onClick(DialogInterface arg0, int arg1) {
	                    //Toast.makeText(getApplicationContext(), "'No' button clicked", Toast.LENGTH_SHORT).show();
	                }
	            });

	            // display box
	            alertbox1.show();
				break;
			}
		}
		else if(buttonPressed==RIGHT_BUTTON){
			switch(prevState){
			case SESSION_MULTI:
				this.selSessionInd=(int)this.S_list_session_event.getSelectedItemId();
				if(selSessionInd==0)
					 Toast.makeText(getBaseContext(), "Session is not selected",
			                 Toast.LENGTH_SHORT).show();
				else{
                	db.logAnything("labstudy_log", "SessionSelect: "+listSession[selSessionInd], System.currentTimeMillis());
					if(noListOrderEvent[selSessionInd]==0){
						orderEvent=new int[this.noEvent[this.selSessionInd]+1];
						noListOrderEvent[selSessionInd]=1;
						nameListOrderEvent[selSessionInd]=new String [2];
						nameListOrderEvent[selSessionInd][1]="Unordered";
						listOrderEvent[selSessionInd]=new String [2];
						listOrderEvent[selSessionInd][1]="";
						for(int i=0;i<=noEvent[selSessionInd];i++) orderEvent[i]=0;
						curState=EVENTORDER_UNORDERED;
						this.selEventInd=1;
	                	db.logAnything("labstudy_log", "EventOrderSelect: Unordered", System.currentTimeMillis());
						createUI(curState);
					}
					else if(noListOrderEvent[selSessionInd]==1){
						this.selOrderEventInd=1;
						setEventOrder();
						this.selEventInd=1;
						if(listEventType[selSessionInd][selEventInd]=="mark1")
							curState=MARK_BEGIN;
						else
							curState=EVENT_BEGIN;
	                	db.logAnything("labstudy_log", "EventOrderSelect: "+listEvent[selSessionInd][orderEvent[selEventInd]], System.currentTimeMillis());

						createUI(curState);
					}
					else{
						curState=EVENTORDER_MULTI;
						createUI(curState);
					}
				}
				break;
			case EVENTORDER_MULTI:
				this.selOrderEventInd=(int)this.S_list_session_event.getSelectedItemId();
				if(selOrderEventInd==0)
					 Toast.makeText(getBaseContext(), "Event Order is not selected",
			                 Toast.LENGTH_SHORT).show();
				else{
						db.logAnything("labstudy_log", "EventOrderSelect: "+nameListOrderEvent[selSessionInd][selOrderEventInd], System.currentTimeMillis());

						if(listEventType[selSessionInd][selEventInd]=="mark1")
							curState=MARK_BEGIN;
						else
							curState=EVENT_BEGIN;
						setEventOrder();
						this.selEventInd=1;
						createUI(curState);
					}

				break;
			case EVENT_END:
			case MARK_END:
				if(listOrderEvent[selSessionInd][1]!="")
				selEventInd++;
				if(listOrderEvent[selSessionInd][1]==""){
					if(selEventInd>noEvent[selSessionInd])
						curState=SESSION_END;
					else
						curState=EVENTORDER_UNORDERED;
					createUI(curState);
				}
				else{
					if(selEventInd<=this.noEvent[this.selSessionInd]){
						if(listEventType[selSessionInd][selEventInd]=="mark1")
							curState=MARK_BEGIN;
						else
							curState=EVENT_BEGIN;
						createUI(curState);
					}
					else {
						curState=SESSION_END;
						createUI(curState);
					}
				}
				break;

			case SESSION_END:
            	db.logAnything("labstudy_log", "SessionEnd: "+listSession[selSessionInd], System.currentTimeMillis());

				finish();
				break;

			case EVENTORDER_UNORDERED:
				String str=this.S_list_session_event.getSelectedItem().toString();
				int ind=(int)this.S_list_session_event.getSelectedItemId();
				selOrderEventInd=1;

				//this.event.
				if(ind==0)
					 Toast.makeText(getBaseContext(), "Event is not selected",
			                 Toast.LENGTH_SHORT).show();
				else{
					// Toast.makeText(getBaseContext(), listEventType[selSessionInd][selEventInd],
			        //         Toast.LENGTH_SHORT).show();

					if(usedEvent[selSessionInd][ind]!=0){
			            AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

			            // set the message to display
			            alertbox.setMessage("Selected Event has already been used. Want to use again?");

			            // set a positive/yes button and create a listener
			            alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			                // do something when the button is clicked
			                public void onClick(DialogInterface arg0, int arg1) {
			                    func();

			                }
			            });
			            // set a negative/no button and create a listener
			            alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {

			                // do something when the button is clicked
			                public void onClick(DialogInterface arg0, int arg1) {

			                	//Toast.makeText(getApplicationContext(), "'No' button clicked", Toast.LENGTH_SHORT).show();
			                }
			            });

			            // display box
			            alertbox.show();
					}
					if(usedEvent[selSessionInd][ind]==0){
						func();
					}
				}
				break;
			}
		}
		else if(buttonPressed==EVENT_BUTTON){
			switch(prevState){
			case EVENT_BEGIN:
				eventStartTime=System.currentTimeMillis();
				db.logAnything("labstudy_log", "EventStart: "+listEvent[selSessionInd][orderEvent[selEventInd]], eventStartTime);
				curState=EVENT_RUNNING;
				createUI(curState);
				break;

			case EVENT_RUNNING:
			case MARK_BEGIN:
				eventEndTime=System.currentTimeMillis();
				if(prevState==MARK_BEGIN) eventStartTime=eventEndTime;
				db.logLabStudy_mark("labstudy_mark", listSession[selSessionInd], listEvent[this.selSessionInd][orderEvent[selEventInd]], eventStartTime, eventEndTime);
				if(prevState==EVENT_RUNNING){
				db.logAnything("labstudy_log", "EventEnd: "+listEvent[selSessionInd][orderEvent[selEventInd]], eventEndTime);
					curState=EVENT_END;
				}
				else{
					db.logAnything("labstudy_log", "EventMark: "+listEvent[selSessionInd][orderEvent[selEventInd]], eventEndTime);
					curState=MARK_END;
				}
				usedEvent[selSessionInd][orderEvent[selEventInd]]++;
				createUI(curState);
				break;
			}
		}
	}
	public void func()
	{
		String str=this.S_list_session_event.getSelectedItem().toString();
		int ind=(int)this.S_list_session_event.getSelectedItemId();
		int a=0;
//		this.selEventInd++;
		for(int i=1;i<=this.noEvent[selSessionInd];i++){
			String[] subparts = str.split(" ", 2);

			if(subparts[1].equals( listEvent[selSessionInd][i])==true){
				orderEvent[selEventInd]=i;
				a=i;

			}
		}
//		orderEvent[selEventInd]=ind;
		db.logAnything("labstudy_log", "EventSelect: "+listEvent[selSessionInd][a], System.currentTimeMillis());

		if(selEventInd<=this.noEvent[this.selSessionInd]){
			if(listEventType[selSessionInd][ind]=="mark1")
				curState=MARK_BEGIN;
			else
				curState=EVENT_BEGIN;
			createUI(curState);
		}
		else {
			curState=SESSION_END;
			createUI(curState);

		}

	}
	public void initListener()
	{

		B_left.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				buttonPressed=LEFT_BUTTON;
				changeState();
			}
		});

		B_right.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				buttonPressed=RIGHT_BUTTON;
				changeState();
			}
		});
		B_event.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				buttonPressed=EVENT_BUTTON;
				changeState();
			}
		});

		B_savecomment.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String comment=E_comment.getText().toString();

				comment=comment.trim();
				if(comment.length()==0){
				 Toast.makeText(getBaseContext(), "comment box is empty...",
		                 Toast.LENGTH_SHORT).show();
				}
				else{
			    	db.logAnything("labstudy_log", "Comment: "+comment, System.currentTimeMillis());

					 Toast.makeText(getBaseContext(), "saved...",
			                 Toast.LENGTH_SHORT).show();
				}
				E_comment.setText("");
			}
		});
		B_cancelcomment.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				E_comment.setText("");
			}
		});


	}
	private final int LIGHTBLUE=0xff6699ff;
	private final int DARKGREEN= Color.rgb(7, 68, 15);
	private final int LIGHTGREEN=0xff1d9624;

	private final int SILVER=0xff999999;
	private final int YELLOW=0xffffff99;
	private final int RED=0xffe9d707;
	private final int DARKBLUE=0xff000066;

	public void createUI(int status)
	{
		ArrayAdapter<String> adapter;
		int curEventInd;
		String currentTime;
		Date d=new Date();

		this.TV_studyname.setVisibility(View.VISIBLE);
		this.TV_studyname.setText(selStudyName);
		this.TV_studyname.setTextColor(LIGHTBLUE);
		this.TV_sessionname.setVisibility(View.VISIBLE);
		this.TV_eventorder.setVisibility(View.VISIBLE);

		this.TV_taskname.setVisibility(View.VISIBLE);
		this.S_list_unordered_event.setVisibility(View.INVISIBLE);
		this.TV_taskstatus.setVisibility(View.INVISIBLE);
		this.TV_taskinfo.setVisibility(View.INVISIBLE);
		//this.TV_instruction.setVisibility(View.INVISIBLE);

		this.B_event.setVisibility(View.INVISIBLE);
		this.S_list_session_event.setVisibility(View.INVISIBLE);

		this.B_left.setVisibility(View.INVISIBLE);
		this.B_right.setVisibility(View.INVISIBLE);

		switch (status){
		case SESSION_MULTI:
			TV_sessionname.setText("<Not Selected Yet>");
			TV_sessionname.setTextColor(SILVER);
			TV_eventorder.setText("<Not Selected Yet>");
			TV_eventorder.setTextColor(SILVER);


			this.TV_taskname.setText("Task: Session Selection");
			TV_taskname.setTextColor(LIGHTGREEN);

			//this.TV_instruction.setVisibility(View.VISIBLE);
			//this.TV_instruction.setText("(Select a session from dropdown list)");

			this.S_list_session_event.setVisibility(View.VISIBLE);
			adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,listSession);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.S_list_session_event.setAdapter(adapter);

			this.B_right.setVisibility(View.VISIBLE);
			this.B_right.setText("Next");
			break;
		case EVENTORDER_MULTI:
			TV_sessionname.setText(listSession[selSessionInd]);
			TV_sessionname.setTextColor(LIGHTGREEN);
			TV_eventorder.setText("<Not Selected Yet>");
			TV_eventorder.setTextColor(SILVER);

			this.TV_taskname.setText("Task: Event Order Selection");
			this.TV_taskname.setTextColor(YELLOW);
			//this.TV_instruction.setVisibility(View.VISIBLE);
			//this.TV_instruction.setText("(Select an order of events from dropdown list)");
			this.S_list_session_event.setVisibility(View.VISIBLE);
			adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,nameListOrderEvent[selSessionInd]);

			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.S_list_session_event.setAdapter(adapter);

			this.B_right.setVisibility(View.VISIBLE);
			this.B_right.setText("Next");

			break;
		case EVENTORDER_UNORDERED:
			TV_sessionname.setText(listSession[selSessionInd]);
			TV_sessionname.setTextColor(0xff00ff00);
			TV_eventorder.setText("Unordered");
			TV_eventorder.setTextColor(YELLOW);
			this.TV_taskname.setText("Task: Event Selection");
			this.TV_taskname.setTextColor(YELLOW);

			this.S_list_session_event.setVisibility(View.VISIBLE);
			String unorderedEvent[]=new String [noEvent[selSessionInd]+1-this.selEventInd+1];
			unorderedEvent[0]="Select an Event";
			int pt=0,i,j=0;
			for(i=1;i<=this.noEvent[selSessionInd];i++){
				String res="";
				//for(j=1;j<this.selEventInd;j++)
				//if(this.orderEvent[j]==i)
				//	break;
				//if(j==selEventInd)
				if(listEventType[selSessionInd][i]=="mark1") res="[M]";else res="[E]";
				if(usedEvent[selSessionInd][i]!=0) res+="*";else res+="";
				unorderedEvent[++pt]=res+" "+this.listEvent[selSessionInd][i];
			}
			adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,unorderedEvent);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.S_list_session_event.setAdapter(adapter);

			this.B_right.setVisibility(View.VISIBLE);
			this.B_right.setText("Next");

//			this.S_list_unordered_event.setVisibility(View.VISIBLE);
//			adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,this.listEvent[1]);
//			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//			this.S_list_unordered_event.setAdapter(adapter);
			//this.TV_taskname.setText("Task: ");
			//this.TV_instruction.setVisibility(View.VISIBLE);
			//this.TV_instruction.setText("(Select an event from dropdown list)");

			break;
		case MARK_BEGIN:
			TV_sessionname.setText(listSession[selSessionInd]);
			TV_sessionname.setTextColor(LIGHTGREEN);
			TV_eventorder.setText(this.nameListOrderEvent[this.selSessionInd][this.selOrderEventInd]+"("+this.listOrderEvent[this.selSessionInd][this.selOrderEventInd]+")");
			TV_eventorder.setTextColor(YELLOW);
			curEventInd=this.orderEvent[selEventInd];
			this.TV_taskname.setTextColor(RED);
			this.TV_taskname.setText("MARK: "+listEvent[this.selSessionInd][curEventInd]);
			this.TV_taskstatus.setVisibility(View.VISIBLE);
			this.TV_taskstatus.setText("Not Marked Yet");

			//this.TV_instruction.setVisibility(View.VISIBLE);
			//this.TV_instruction.setText("(Click \"Begin Event\" button to start event)");
			this.B_event.setVisibility(View.VISIBLE);

			this.B_event.setText("Click to Mark Event");
			this.B_event.setTextColor(DARKGREEN);
			this.B_event.setTypeface(null,Typeface.BOLD);
			//this.B_event.setHeight(42);

			this.B_left.setVisibility(View.VISIBLE);
			this.B_left.setText("Skip Event");
			break;
		case MARK_END:
			TV_sessionname.setText(listSession[selSessionInd]);
			TV_sessionname.setTextColor(LIGHTGREEN);
			TV_eventorder.setText(this.nameListOrderEvent[this.selSessionInd][this.selOrderEventInd]+"("+this.listOrderEvent[this.selSessionInd][this.selOrderEventInd]+")");

//			TV_eventorder.setText(this.listOrderEvent[this.selSessionInd][this.selOrderEventInd]);
			TV_eventorder.setTextColor(YELLOW);

			curEventInd=this.orderEvent[selEventInd];
			this.TV_taskname.setTextColor(RED);
			this.TV_taskname.setText("MARK: "+listEvent[this.selSessionInd][curEventInd]);

			this.TV_taskstatus.setVisibility(View.VISIBLE);
			this.TV_taskstatus.setText("Event is Marked");

			d.setTime(eventStartTime);
			currentTime = "Marked at: "+DateFormat.getTimeInstance().format(d);

			this.TV_taskinfo.setVisibility(View.VISIBLE);
			this.TV_taskinfo.setText(currentTime);
			this.B_right.setVisibility(View.VISIBLE);
			this.B_right.setText("Next Event");

			//this.TV_taskstatus.setText(listEvent[selSessionInd][selEventInd]);

			//this.TV_instruction.setVisibility(View.VISIBLE);
			//this.TV_instruction.setText("Event Ended (Ended at hh:mm:ss)");

			break;

		case EVENT_BEGIN:
			TV_sessionname.setText(listSession[selSessionInd]);
			TV_sessionname.setTextColor(LIGHTGREEN);
			TV_eventorder.setText(this.nameListOrderEvent[this.selSessionInd][this.selOrderEventInd]+"("+this.listOrderEvent[this.selSessionInd][this.selOrderEventInd]+")");
			TV_eventorder.setTextColor(YELLOW);
			curEventInd=this.orderEvent[selEventInd];
			this.TV_taskname.setTextColor(RED);
			this.TV_taskname.setText("Event: "+listEvent[this.selSessionInd][curEventInd]);
			this.TV_taskstatus.setVisibility(View.VISIBLE);
			this.TV_taskstatus.setText("Not Started Yet");

			//this.TV_instruction.setVisibility(View.VISIBLE);
			//this.TV_instruction.setText("(Click \"Begin Event\" button to start event)");
			this.B_event.setVisibility(View.VISIBLE);

			this.B_event.setText("Click to Begin Event");
			this.B_event.setTextColor(DARKGREEN);
			this.B_event.setTypeface(null,Typeface.BOLD);

			//this.B_event.setHeight(42);

			this.B_left.setVisibility(View.VISIBLE);
			this.B_left.setText("Skip Event");
			break;
		case EVENT_RUNNING:
			TV_sessionname.setText(listSession[selSessionInd]);
			TV_sessionname.setTextColor(LIGHTGREEN);
			TV_eventorder.setText(this.nameListOrderEvent[this.selSessionInd][this.selOrderEventInd]+"("+this.listOrderEvent[this.selSessionInd][this.selOrderEventInd]+")");

//			TV_eventorder.setText(this.listOrderEvent[this.selSessionInd][this.selOrderEventInd]);
			TV_eventorder.setTextColor(YELLOW);
			curEventInd=this.orderEvent[selEventInd];
			this.TV_taskname.setTextColor(RED);
			this.TV_taskname.setText("Event: "+listEvent[this.selSessionInd][curEventInd]);
			this.TV_taskstatus.setVisibility(View.VISIBLE);
			d.setTime(eventStartTime);
			currentTime = DateFormat.getTimeInstance().format(d);
			this.TV_taskstatus.setText("Event is Running");
			this.TV_taskinfo.setVisibility(View.VISIBLE);
			this.TV_taskinfo.setText("Started at: "+ currentTime);
			//this.TV_instruction.setVisibility(View.VISIBLE);
			//this.TV_instruction.setText("(Click \"End Event\" button at the end of event)");
			this.B_left.setVisibility(View.VISIBLE);
			this.B_left.setText("Restart Event");
			this.B_event.setVisibility(View.VISIBLE);
			this.B_event.setText("Click at the End of Event");
			this.B_event.setTextColor(0xfff90606);

			break;
		case EVENT_END:
			TV_sessionname.setText(listSession[selSessionInd]);
			TV_sessionname.setTextColor(LIGHTGREEN);
			TV_eventorder.setText(this.nameListOrderEvent[this.selSessionInd][this.selOrderEventInd]+"("+this.listOrderEvent[this.selSessionInd][this.selOrderEventInd]+")");

//			TV_eventorder.setText(this.listOrderEvent[this.selSessionInd][this.selOrderEventInd]);
			TV_eventorder.setTextColor(YELLOW);

			curEventInd=this.orderEvent[selEventInd];
			this.TV_taskname.setTextColor(RED);
			this.TV_taskname.setText("Event: "+listEvent[this.selSessionInd][curEventInd]);

			this.TV_taskstatus.setVisibility(View.VISIBLE);
			this.TV_taskstatus.setText("Event is Ended");

			d.setTime(eventStartTime);
			currentTime = "Started at: "+DateFormat.getTimeInstance().format(d)+", Ended at: ";



			d.setTime(eventEndTime);

			currentTime+= DateFormat.getTimeInstance().format(d);

			this.TV_taskinfo.setVisibility(View.VISIBLE);
			this.TV_taskinfo.setText(currentTime);
			this.B_right.setVisibility(View.VISIBLE);
			this.B_right.setText("Next Event");

			//this.TV_taskstatus.setText(listEvent[selSessionInd][selEventInd]);

			//this.TV_instruction.setVisibility(View.VISIBLE);
			//this.TV_instruction.setText("Event Ended (Ended at hh:mm:ss)");

			break;
		case SESSION_END:
			this.TV_taskname.setTextColor(RED);
			this.TV_taskname.setText("Session has Ended.");
			this.B_right.setVisibility(View.VISIBLE);
			this.B_right.setText("Close Program");
			break;
		}
	}
	@Override
	protected void checkKeypadCodes() {
		if (keypadCode.length() == SELFREPORT_CODE.length()) {
			if (keypadCode.equalsIgnoreCase(SELFREPORT_CODE)) {
				keypad.hide();
				keypadVisible = false;

				startActivity(jh_selfReportIntent);
			}else {
				super.checkKeypadCodes();
			}
		}
	}

}
