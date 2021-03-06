package main.java.com.github.lazycure.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.java.com.github.lazycure.LazyCureApplication;
import main.java.com.github.lazycure.Strings;
import main.java.com.github.lazycure.Time;
import main.java.com.github.lazycure.db.DatabaseHandler;
import android.content.Context;
import android.util.Log;

public class ActivityManager {
	
	private static Context context = LazyCureApplication.getAppContext();
	private static DatabaseHandler db = new DatabaseHandler(context);
	
	public static void createFirstTestActivity(){
		//Log.d("Test", "First activity created");
		db.addActivity(new Activity(Strings.TEST_ACTIVITY_NAME, null, Time.getCurrentDate()));
	}
	
	public static boolean activityListIsEmpty(){
		boolean isEmpty = false;
		int activitiesCount = db.getActivitiesCount();
		//Log.d("DB", "Activities count: " + activitiesCount);
		if (activitiesCount < 1){
			isEmpty = true;
		}
		return isEmpty;
	}
	
	public static void addActivity(String activityName) {
		if (activityName.length() != 0){
			String trimmed = activityName.trim();
			if (!isFirstActivity()){
				if (trimmed.equalsIgnoreCase(getLastActivity().getName())){
					continueLatestActivity();
				}
				else{
					db.addActivity(new Activity(trimmed, null, Time.getCurrentDate()));
				}
			}
			else{
		        db.addActivity(new Activity(trimmed, null, Time.getCurrentDate()));
			}
		}
	}
	
	public static void addActivity(String activityName, Date finishTime) {
		if (activityName.length() != 0){
			String trimmed = activityName.trim();
			if (!isFirstActivity()){
				if (trimmed.equalsIgnoreCase(getLastActivity().getName())){
					continueLatestActivity();
				}
				else{
					db.addActivity(new Activity(trimmed, null, finishTime));
				}
			}
			else{
		        db.addActivity(new Activity(trimmed, null, finishTime));
			}
		}
	}

	public static void removeActivity(Activity activity){
		db.removeActivity(activity);
	}

	public static void continueLatestActivity(){
		Activity latestActivity = getLastActivity();
		String activityName = latestActivity.getName();
		if (latestActivity != null){
			removeActivity(latestActivity);
		}
		addActivity(activityName);
	}

	public static Activity getLastActivity(){
		List<Activity> activities = db.getAllActivities();
		Activity lastActivity = null;
		if (activities.size()>0){
			//Log.d("Test", "Activities.size="+activities.size());
			lastActivity = activities.get(activities.size()-1);
		}
		return lastActivity;
	}
	
	public static boolean isFirstActivity(){
		return (db.getActivitiesCount() == 0);
	}
	
	public static String [] getActivitiesNames() {
		List<Activity> activities = db.getAllActivities();
		//Log.d("Test", "Activities.size="+String.valueOf(activities.size()));
		Set<String> activityNames = new HashSet<String>();
		for (int i=0; i< activities.size(); i++){
			activityNames.add(activities.get(i).getName());
		}
		String[] names = new String[activityNames.size()];
		activityNames.toArray(names); // fill the array
		return names;
	}
}
