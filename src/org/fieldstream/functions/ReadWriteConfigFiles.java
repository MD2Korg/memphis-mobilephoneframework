package org.fieldstream.functions;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fieldstream.Constants;
import org.fieldstream.functions.AlarmScheduler.Alarm;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.Environment;

public class ReadWriteConfigFiles {
	private static ReadWriteConfigFiles INSTANCE = null;
	public class SELF{
		public String type;
		public String text;
		public int modelID;
	};
	public Vector<SELF> self;
	public void loadDeadPeriodsDB()
	{
		Calendar cal=Calendar.getInstance();
		long now=cal.getTimeInMillis();		
		cal.set(Calendar.HOUR_OF_DAY, 0);cal.set(Calendar.MINUTE, 0);cal.set(Calendar.SECOND, 0);cal.set(Calendar.MILLISECOND, 0);
		long today=cal.getTimeInMillis();

		DatabaseLogger.active=true;		DatabaseLogger db=DatabaseLogger.getInstance(this);
		Constants.QUIETSTART=db.getlastfromdeadperiod("quietstart");
		Constants.QUIETEND=db.getlastfromdeadperiod("quietend");
		if (now > Constants.QUIETEND || Constants.QUIETSTART>=Constants.QUIETEND) {
			Constants.QUIETSTART=now;
			Constants.QUIETEND=now;
		}
		Constants.STUDYSTART=db.getlastfromdeadperiod("studystart");
		if(Constants.STUDYSTART<=0)
			Constants.STUDYSTART=now;
		Constants.DAYSTART=Constants.STUDYSTART;
		while(Constants.DAYSTART>now)		Constants.DAYSTART-=Constants.DAYMILLIS;
		while(Constants.DAYSTART+Constants.DAYMILLIS<now)			Constants.DAYSTART+=Constants.DAYMILLIS;
		Constants.DAYEND=Constants.DAYSTART+Constants.DAYMILLIS;
		
		Constants.SLEEPSTART=db.getdeadperiod("sleepstart", today-Constants.DAYMILLIS, today);
		if(Constants.SLEEPSTART==-1) Constants.SLEEPSTART=getdefaulttime(-1,22);
		Constants.SLEEPEND=db.getdeadperiod("sleepend", today, today+Constants.DAYMILLIS);
		if(Constants.SLEEPEND==-1) Constants.SLEEPEND=getdefaulttime(0,8);
		
		if(now>Constants.SLEEPEND){
			Constants.SLEEPSTART=db.getdeadperiod("sleepstart", today,today+Constants.DAYMILLIS);
			if(Constants.SLEEPSTART==-1) Constants.SLEEPSTART=getdefaulttime(0,22);			
			Constants.SLEEPEND=db.getdeadperiod("sleepend", today+Constants.DAYMILLIS,today+Constants.DAYMILLIS*2);
			if(Constants.SLEEPEND==-1) Constants.SLEEPEND=getdefaulttime(1,8);
		}
		Log.ema_alarm("loadDeadPeriod", "sleepstart="+Constants.millisecondToDateTime(Constants.SLEEPSTART)+ 
				" sleepend="+Constants.millisecondToDateTime(Constants.SLEEPEND)+" DayStart="+Constants.millisecondToDateTime(Constants.DAYSTART)+
				" DayEnd="+Constants.millisecondToDateTime(Constants.DAYEND));
	}
	public long getdefaulttime(int day,int hour)
	{
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);cal.set(Calendar.MINUTE, 0);cal.set(Calendar.SECOND, 0);cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DAY_OF_YEAR, day);
		return cal.getTimeInMillis();
	}
	public Alarm[] loadAlarms(int day) {
		Alarm  alarms[];
		alarms=readAlarms();
		alarms=correctAlarmTime(alarms,day);
		printAlarms(alarms);	
		return alarms;
	}
	void printAlarms(Alarm[] alarms)
	{
		String str="";
		Log.ema_alarm("", "Alarm length: "+alarms.length);
		for(int i=0;i<alarms.length;i++){
			str=str+" Name: "+alarms[i].alarmName;
			str=str+" Time: "+Constants.millisecondToDateTime(alarms[i].alarmTime);
		}
		Log.ema_alarm("loadAlarms", str);
	}

	public static ReadWriteConfigFiles getInstance(Object holder) {
		if (INSTANCE == null) {
			INSTANCE = new ReadWriteConfigFiles();
		}
		//		context=(Context) holder;		
		return INSTANCE;
	}
	public boolean writeDeadPeriodsDB(long quietStart, long quietEnd, long sleepStart, long sleepEnd,long studyStart) {
		boolean res=true;
		DatabaseLogger.active=true;
		DatabaseLogger db;
		db=DatabaseLogger.getInstance(this);
//		if(db.isdeadperiod("studystart",studyStart)!=1)
			res&=db.logAnything4("deadperiod", System.currentTimeMillis(),"studystart",String.valueOf(studyStart));
//		if(db.isdeadperiod("sleepstart",sleepStart)!=1)		
			res&=db.logAnything4("deadperiod", System.currentTimeMillis(),"sleepstart",String.valueOf(sleepStart));
//		if(db.isdeadperiod("sleepend",sleepEnd)!=1)
			res&=db.logAnything4("deadperiod", System.currentTimeMillis(),"sleepend",String.valueOf(sleepEnd));
		if(quietStart!=quietEnd){
//			if(db.isdeadperiod("quietstart",quietStart)!=1)
				res&=db.logAnything4("deadperiod", System.currentTimeMillis(),"quietstart",String.valueOf(quietStart));
//			if(db.isdeadperiod("quietend",quietEnd)!=1)
				res&=db.logAnything4("deadperiod", System.currentTimeMillis(),"quietend",String.valueOf(quietEnd));		
		}
		return res;
	}
/*	public void correctDeadPeriodDB()
	{
		long now;
		Calendar cal=Calendar.getInstance();
		now=cal.getTimeInMillis();
		if (now > Constants.QUIETEND || Constants.QUIETSTART>=Constants.QUIETEND) {
			Constants.QUIETSTART=now;
			Constants.QUIETEND=now;
		}
		if(Constants.SLEEPSTART>Constants.SLEEPEND){
			cal=Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 8);cal.set(Calendar.MINUTE, 00);cal.set(Calendar.SECOND, 0);cal.set(Calendar.MILLISECOND, 0);
			Constants.SLEEPEND=cal.getTimeInMillis();
			while(Constants.SLEEPSTART>Constants.SLEEPEND) Constants.SLEEPEND+=Constants.DAYMILLIS;
		}
		if(Constants.SLEEPEND<now){
			cal=Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 8);cal.set(Calendar.MINUTE, 00);cal.set(Calendar.SECOND, 00);cal.set(Calendar.MILLISECOND, 0);
			cal.add(Calendar.DAY_OF_YEAR, 1);
			Constants.SLEEPEND=cal.getTimeInMillis();
			cal=Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 22);cal.set(Calendar.MINUTE, 00);cal.set(Calendar.SECOND, 00);cal.set(Calendar.MILLISECOND, 0);
			Constants.SLEEPSTART=cal.getTimeInMillis();
			while(Constants.SLEEPEND<now){Constants.SLEEPEND+=Constants.DAYMILLIS;Constants.SLEEPSTART+=Constants.DAYMILLIS;}			
		}
		if(Constants.DAYSTART<=0)
			Constants.DAYSTART=now;
		while(Constants.DAYSTART>now){
			Constants.DAYSTART-=Constants.DAYMILLIS;
		}

		while(Constants.DAYSTART+Constants.DAYMILLIS<now){
			Constants.DAYSTART+=Constants.DAYMILLIS;
		}
		Constants.DAYEND=Constants.DAYSTART+Constants.DAYMILLIS;
	}
	*/
	public long getAlarmSleepStart(int day)
	{
		DatabaseLogger db=DatabaseLogger.getInstance(this);
		Calendar cal=Calendar.getInstance();
		if(day!=0) cal.add(Calendar.DAY_OF_YEAR, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		long stime=cal.getTimeInMillis(), etime=stime+Constants.DAYMILLIS, time;
		time=db.getdeadperiod("sleepstart", stime, etime);
		if(time<=0){
			cal.set(Calendar.HOUR_OF_DAY,22);time=cal.getTimeInMillis();				
		}
		return time;
	}
	public long getAlarmSleepEnd(int day)
	{
		long stime,etime,time;
		DatabaseLogger db=DatabaseLogger.getInstance(this);
		Calendar cal=Calendar.getInstance();
		if(day!=0) cal.add(Calendar.DAY_OF_YEAR, day);
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);

		stime=cal.getTimeInMillis();
		etime=stime+Constants.DAYMILLIS;
		time=db.getdeadperiod("sleepend", stime, etime);
		Log.ema_alarm("","searchtime="+Constants.millisecondToDateTime(stime)+"time="+time+" datetime="+Constants.millisecondToDateTime(time));
		
		if(time>0) return time;
		cal.set(Calendar.HOUR_OF_DAY, 8);
		return cal.getTimeInMillis();
		//		}
	}
	public Alarm[] correctAlarmTime(Alarm alarms[],int day) //day=0 -> today, day=1 -> next day
	{
		long sleepStart=getAlarmSleepStart(day)+2*1000;		//add 10 seconds from sleep time
		long sleepEnd=getAlarmSleepEnd(day)+2*1000;			//add 10 seconds from wakeup time
		Log.ema_alarm("", "sleepstart(today)="+Constants.millisecondToDateTime(sleepStart)+" sleepend(today)="+Constants.millisecondToDateTime(sleepEnd));
		for(int i=0;i<alarms.length;i++)
		{
			if(alarms[i].alarmName.equals("wakeup")){
				alarms[i].alarmTime=sleepEnd;
			}
			else if(alarms[i].alarmName.equals("sleep")){
				Calendar cal=Calendar.getInstance();				
				alarms[i].alarmTime=sleepStart;
				cal.setTimeInMillis(sleepStart);
				if(cal.get(Calendar.HOUR_OF_DAY)>=22 || cal.get(Calendar.HOUR_OF_DAY)<4){
					cal.set(Calendar.HOUR_OF_DAY, 22);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND,0);
					cal.set(Calendar.MILLISECOND,0);
					alarms[i].alarmTime=cal.getTimeInMillis();					
				}
			}
			else if(alarms[i].alarmName.startsWith("wakeup")==true){
				long minute=Long.parseLong(alarms[i].alarmName.substring(7));
				alarms[i].alarmTime=sleepEnd+minute*60*1000;
			}
			else {
				Calendar cal=Calendar.getInstance();				
				cal=Calendar.getInstance();				
				int hr=Integer.parseInt(alarms[i].alarmName.substring(0, 2));
				cal.add(Calendar.DAY_OF_YEAR, day);
				cal.set(Calendar.HOUR_OF_DAY, hr);
				int mn=Integer.parseInt(alarms[i].alarmName.substring(3));
				cal.set(Calendar.MINUTE, mn);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND,0);				
				alarms[i].alarmTime=cal.getTimeInMillis();
			}
			//			cal=Calendar.getInstance();
			//			while(cal.getTimeInMillis()>alarms[i].alarmTime)
			//				alarms[i].alarmTime+=Constants.DAYMILLIS;
		}
		return alarms;
	}
	public Alarm[] readAlarms() {
		Alarm alarms[]=null;
		File root = Environment.getExternalStorageDirectory();

		File dir = new File(root+"/"+Constants.CONFIG_DIR);
		dir.mkdirs();

		File setupFile = new File(dir, Constants.ALARM_CONFIG_FILENAME);
		if (!setupFile.exists())
			return null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(setupFile);

			NodeList minTimeEMAList = doc.getElementsByTagName("alarmduration");
			Element minTimeEMAElement = (Element)minTimeEMAList.item(0); 
			NodeList nodeminTimeEMAList = minTimeEMAElement.getChildNodes();
			int alarmtime = Integer.parseInt(((Node)nodeminTimeEMAList.item(0)).getNodeValue().trim());
			Constants.ALARMDURATION=alarmtime;
			minTimeEMAList = doc.getElementsByTagName("repeatalarm");
			minTimeEMAElement = (Element)minTimeEMAList.item(0); 
			nodeminTimeEMAList = minTimeEMAElement.getChildNodes();
			int repeatalarm = Integer.parseInt(((Node)nodeminTimeEMAList.item(0)).getNodeValue().trim());
			Constants.REPEATALARM=repeatalarm;

			minTimeEMAList = doc.getElementsByTagName("diffbetweenalarms");
			minTimeEMAElement = (Element)minTimeEMAList.item(0); 
			nodeminTimeEMAList = minTimeEMAElement.getChildNodes();
			int diff_alarm = Integer.parseInt(((Node)nodeminTimeEMAList.item(0)).getNodeValue().trim());
			Constants.DIFF_BETWEEN_ALARM=diff_alarm;
			NodeList list_alarm = doc.getElementsByTagName("alarm");
			int totalalarm=0;
			if (list_alarm!=null)
				totalalarm =  list_alarm.getLength();

			alarms=new Alarm[totalalarm];
			for(int iterator=0; iterator<totalalarm ; iterator++){
				alarms[iterator]=new Alarm();
				Node eventNode = list_alarm.item(iterator);
				if(eventNode!=null && eventNode.getNodeType() == Node.ELEMENT_NODE){
					Element eventElement = (Element)eventNode;
					NodeList List = eventElement.getElementsByTagName("time");
					if(List!=null){				
						Element element = (Element)List.item(0);
						NodeList Lists = element.getChildNodes();
						String alarmname=((Node)Lists.item(0)).getNodeValue().trim();
						alarms[iterator].alarmName=alarmname;
					}
					List = eventElement.getElementsByTagName("ema");
					if(List!=null){
						Element element = (Element)List.item(0);
						NodeList Lists = element.getChildNodes();
						alarms[iterator].isEMA=Integer.parseInt(((Node)Lists.item(0)).getNodeValue().trim());
					}
					if(alarms[iterator].isEMA==1){
						List = eventElement.getElementsByTagName("emaquestions");
						if(List!=null){
							Element element = (Element)List.item(0);
							NodeList Lists = element.getChildNodes();
							alarms[iterator].emaFilename=(((Node)Lists.item(0)).getNodeValue().trim());
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return alarms;
	}
	public boolean isexist_quiettime()
	{
		DatabaseLogger db=DatabaseLogger.getInstance(this);
		long starttime=db.getdeadperiodtoday("quietstart");
		long endtime=db.getdeadperiodtoday("quietend");
		Log.ema_alarm("", "starttime="+starttime+" endtime="+endtime);

		if(starttime==-1 || endtime==-1) return false;
		if(starttime>endtime) return false;
		return true;
	}
	public Vector<SELF> readSelfreportConfig() {
		self=new Vector<SELF>();

		File root = Environment.getExternalStorageDirectory();

		File dir = new File(root+"/"+Constants.CONFIG_DIR);
		dir.mkdirs();

		File setupFile = new File(dir, Constants.SELFREPORT_CONFIG_FILENAME);
		if (!setupFile.exists())
			return self;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document dom = null;

		try {
			builder = factory.newDocumentBuilder();
			dom = builder.parse(setupFile);

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Element xmlroot = dom.getDocumentElement();		
		self.clear();
		NodeList nodeList1 = xmlroot.getElementsByTagName("selfreport");
		for (int i=0; i<nodeList1.getLength();i++) {
			int ind;
			SELF temp=new SELF();
			Element element1 = (Element)nodeList1.item(i);
			NodeList nodeList = element1.getElementsByTagName("model");
			Element element = (Element)nodeList.item(0);
			String modelname = element.getFirstChild().getNodeValue();
			ind = Constants.getModelToIndex(modelname);

			nodeList = element1.getElementsByTagName("text");
			element = (Element)nodeList.item(0);
			String text = element.getFirstChild().getNodeValue();

			nodeList = element1.getElementsByTagName("type");
			element = (Element)nodeList.item(0);
			String type = element.getFirstChild().getNodeValue();

			temp.modelID=ind;
			temp.text=text;
			temp.type=type;
			self.add(temp);
		}
		return self;
	}
}
