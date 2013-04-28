package main.java.com.github.lazycure;

import java.util.Collections;
import java.util.List;

import main.java.com.github.lazycure.activities.Activity;
import main.java.com.github.lazycure.activities.ActivityManager;
import main.java.com.github.lazycure.db.DatabaseHandler;
import main.java.com.github.lazycure.ui.ActivitiesTableManager;

import main.java.com.github.lazycure.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


/*
 * Presentation layer of the main view
 */
public class ViewActivitiesActivity extends LazyCureActivity {

	private long lastActivity = 0;
	private ImageButton doneButton;
	private ImageButton menuButton;
	private ImageButton settingsButton;
	private TextView timeLabel;
	private EditText activityNameEditText;
	DatabaseHandler db = new DatabaseHandler(this);
	private ActivitiesTableManager activitiesTableManager = new ActivitiesTableManager(this);
	private String DEFAULT_SPLIT_SEPARATOR = ",";
	private String SPLIT_SEPARATOR = DEFAULT_SPLIT_SEPARATOR;
	private boolean SPLIT_ACTIVITIES = true;
	private String DEFAULT_ACTIVITY_NAME_ORIGINAL = "rest";
	private String DEFAULT_ACTIVITY_NAME = DEFAULT_ACTIVITY_NAME_ORIGINAL;
	private boolean DEFAULT_ACTIVITY_MODE = false;
	private String DEFAULT_ACTIVITY_COLOR = "yellow"; 
	private String ACTIVITY_COLOR = DEFAULT_ACTIVITY_COLOR;
	
	private RefreshHandler mRedrawHandler = new RefreshHandler();
		@SuppressLint("HandlerLeak")
		class RefreshHandler extends Handler {
		    @Override
		    public void handleMessage(Message msg) {
		    	ViewActivitiesActivity.this.updateTimer(lastActivity);
		    }

		    public void sleep(long delayMillis) {
		      this.removeMessages(0);
		      sendMessageDelayed(obtainMessage(0), delayMillis);
		    }
		  };

	private void updateTimer(long baseMillis){
		long start = lastActivity;
	    long duration = System.currentTimeMillis() - start;
	    String text = Time.formatWithDay(duration);
	    timeLabel.setText(text);
	    mRedrawHandler.sleep(Time.ONE_SECOND);
	  }
	
	public void updateSharedPrefs() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SPLIT_SEPARATOR = sharedPrefs.getString("split_separator", DEFAULT_SPLIT_SEPARATOR);
        SPLIT_ACTIVITIES = sharedPrefs.getBoolean("split_switcher", true);
        DEFAULT_ACTIVITY_NAME = sharedPrefs.getString("default_activity_name", DEFAULT_ACTIVITY_NAME_ORIGINAL);
        DEFAULT_ACTIVITY_MODE = sharedPrefs.getBoolean("default_activity", false);
        ACTIVITY_COLOR = sharedPrefs.getString("appearance_color", DEFAULT_ACTIVITY_COLOR);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        updateSharedPrefs();
        
        setUpViews();
    }

	@Override
	protected void onResume() {
		super.onResume();
		updateSharedPrefs();
		showActivities();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.export:
	        	Intent export = new Intent();
	        	export.setClass(LazyCureApplication.getAppContext(), ExportActivity.class);
	        	startActivity(export);
	        	break;
	        case R.id.timeLogView:
	        	Intent timeLogView = new Intent();
	        	timeLogView.setClass(LazyCureApplication.getAppContext(), TimeLogActivity.class);
	        	startActivity(timeLogView);
	        	break;
	        case R.id.about:
	        	Intent about = new Intent();
	        	about.setClass(LazyCureApplication.getAppContext(), AboutActivity.class);
	        	startActivity(about);
	        	break;
	        case R.id.settings:
	        	Intent settings = new Intent();
	        	settings.setClass(LazyCureApplication.getAppContext(), SettingsActivity.class);
	        	startActivity(settings);
	    }
	    return true;
	}

	private void showActivities() {
        List<Activity> activities = db.getAllActivities();
        OutputManager.updateActivitiesWithStartTime(activities);
		//Reverse the activities order
        Collections.reverse(activities);
        //Remove test activity
        activitiesTableManager.removeTestActivity(activities);
		//Print out the activities
		//activitiesTableManager.showTable(activities, 100);

        LayoutInflater ltInflater = getLayoutInflater();
        LinearLayout linLayout = (LinearLayout) findViewById(R.id.main);
        linLayout.removeAllViews();

        for (int i=0; i< activities.size(); i++){
        	Activity activity = activities.get(i); 
        	String name = OutputManager.cropName(activity.getName());
        	String start = Time.formatAndRoundWithDefaultTimeZone(activity.getStartTime());
        	String duration = Time.formatAndRoundWithDay(activity.getDuration());
        	int textColor = Color.BLACK;
        	//Log.d("Out", "Color parse: " + ACTIVITY_COLOR);
        	int backgroundColor = Color.parseColor(ACTIVITY_COLOR);
        	//int backgroundColor = Color.parseColor("black");
        	
            View item = ltInflater.inflate(R.layout.activity, linLayout, false);
            TextView activityItemName = (TextView) item.findViewById(R.id.activityItemName);
            activityItemName.setText(name);
            activityItemName.setTextColor(textColor);

            TextView activityItemStartTime = (TextView) item.findViewById(R.id.activityItemStartTime);
            activityItemStartTime.setText("Start: " + start);
            activityItemStartTime.setTextColor(textColor);
            
            TextView activityItemDuration = (TextView) item.findViewById(R.id.activityItemDuration);
            activityItemDuration.setText("Duration: " + duration);
            activityItemDuration.setTextColor(textColor);
            
            item.getLayoutParams().width = LayoutParams.FILL_PARENT;
            item.setBackgroundColor(backgroundColor);
            
            linLayout.addView(item);
            
            final Activity currentActivity = activity;
            item.setOnLongClickListener(new View.OnLongClickListener() {
				public boolean onLongClick(View v) {
					//Log.d("View", "Long click on activity " + currentActivity.getName());
					deleteActivity(currentActivity);
					return false;
				}
    		});
        }
        
        if (activities.size() != 0){
			//Set the latest activity time for timeLabel
			lastActivity = activities.get(0).getFinishTime().getTime();
        }
		else{
			lastActivity = System.currentTimeMillis();
		}
		updateTimer(lastActivity);
	}
	
	/**
	 * Delete the activity from DB
	 * @param activityToDelete
	 */
	private void deleteActivity(Activity activityToDelete) {
		final Activity activity = activityToDelete;
		new AlertDialog.Builder(this)
	    .setTitle("Delete activity '" + activityToDelete.getName() + "'")
	    .setMessage("Are you sure you want to delete this activity?")
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        	//Log.d("Delete", "Deleting activity " + activity.getName());
	        	db.removeActivity(activity);
	        	//Log.d("Delete", "Deleted successfully");
	        	addActivity();
	        }
	     })
	    .setNegativeButton("No", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        	//Log.d("Delete", "Delete cancelled");
	        }
	     })
	     .show();
	}
	
	private void setUpViews() {
		doneButton = (ImageButton)findViewById(R.id.done_button);
		activityNameEditText = (EditText)findViewById(R.id.input);
		timeLabel = (TextView) this.findViewById(R.id.timeLabel);
		menuButton = (ImageButton)findViewById(R.id.menu);
		settingsButton = (ImageButton)findViewById(R.id.settings);
		
		doneButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addActivity();
			}
		});
		
		menuButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent timeLogView = new Intent();
				timeLogView.setClass(LazyCureApplication.getAppContext(), TimeLogActivity.class);
	        	startActivity(timeLogView);
			}
		});
		
		settingsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent settings = new Intent();
	        	settings.setClass(LazyCureApplication.getAppContext(), SettingsActivity.class);
	        	startActivity(settings);
			}
		});
		
		if (ActivityManager.activityListIsEmpty()){
			ActivityManager.createFirstTestActivity();
		}
		
		// Get a reference to the AutoCompleteTextView in the layout
		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.input);
		// Get the string array
		String[] activities = ActivityManager.getActivitiesNames();
		// Create the adapter and set it to the AutoCompleteTextView 
		ArrayAdapter<String> adapter = 
		        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, activities);
		textView.setAdapter(adapter);

		clearInput();
	}
	
	private void addActivity() {
		String activityName = activityNameEditText.getText().toString();
		//Check if Default Activity Mode is ON and activity name is empty
		if (DEFAULT_ACTIVITY_MODE && activityName.length() == 0) {
			activityName = DEFAULT_ACTIVITY_NAME;
		}
		// Check if split feature turned on and activity has separator
		if (SPLIT_ACTIVITIES && activityName.contains(SPLIT_SEPARATOR)) {
			Intent intent = new Intent();
			intent.setClass(LazyCureApplication.getAppContext(),
					SplitActivity.class);
			intent.putExtra("inputString", activityName);
			intent.putExtra("separator", SPLIT_SEPARATOR);
			startActivity(intent);
		} else {
			ActivityManager.addActivity(activityName);
		}
		clearInput();
		showActivities();
	}
	
	protected void clearInput() {
		activityNameEditText.setText("");
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(activityNameEditText.getWindowToken(), 0);
	}
}