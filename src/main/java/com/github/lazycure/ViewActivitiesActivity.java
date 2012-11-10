package main.java.com.github.lazycure;

import java.util.Collections;
import java.util.List;

import main.java.com.github.lazycure.activities.Activity;
import main.java.com.github.lazycure.activities.ActivityManager;
import main.java.com.github.lazycure.db.DatabaseHandler;
import main.java.com.github.lazycure.ui.ActivitiesTableManager;

import main.java.com.github.lazycure.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


/*
 * Presentation layer of the main view
 */
public class ViewActivitiesActivity extends LazyCureActivity {

	private long lastActivity = 0;
	private Button doneButton;
	private TextView timeLabel;
	private EditText activityNameEditText;
	DatabaseHandler db = new DatabaseHandler(this);
	private ActivitiesTableManager activitiesTableManager = new ActivitiesTableManager(this);
	
	private RefreshHandler mRedrawHandler = new RefreshHandler();
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

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setUpViews();
    }

	@Override
	protected void onResume() {
		super.onResume();
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
	        case R.id.settings:
	        	Toast.makeText(this, "Not implemented yet", Toast.LENGTH_LONG).show();
	            break;
	        case R.id.about:
	        	Intent about = new Intent();
	        	about.setClass(LazyCureApplication.getAppContext(), AboutActivity.class);
	        	startActivity(about);
	        	break;
	        case R.id.timeLogView:
	        	Intent timeLogView = new Intent();
	        	timeLogView.setClass(LazyCureApplication.getAppContext(), TimeLogActivity.class);
	        	startActivity(timeLogView);
	        	break;
	    }
	    return true;
	}

	private void showActivities() {
		if (ActivityManager.activityListIsEmpty()){
			ActivityManager.createFirstTestActivity();
		}
        List<Activity> activities = db.getAllActivities();
        OutputManager.updateActivitiesWithStartTime(activities);
		//Reverse the activities order
        Collections.reverse(activities);
		//Print out the activities
		activitiesTableManager.showTable(activities);
		if (activities.size() != 0){
			//Set the latest activity time for timeLabel
			lastActivity = activities.get(0).getFinishTime().getTime();
        }
		else{
			lastActivity = System.currentTimeMillis();
		}
		updateTimer(lastActivity);
	}
	
	private void setUpViews() {
		doneButton = (Button)findViewById(R.id.done_button);
		activitiesTableManager.setTable((TableLayout) this.findViewById(R.id.activitiesTable));
		activityNameEditText = (EditText)findViewById(R.id.input);
		timeLabel = (TextView) this.findViewById(R.id.timeLabel);
		
		doneButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addActivity();
			}
		});

		clearInput();
	}
	
	private void addActivity() {
		String activityName = activityNameEditText.getText().toString();
		ActivityManager.addActivity(activityName);
		clearInput();
		showActivities();
	}
	
	protected void clearInput() {
		activityNameEditText.setText("");
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(activityNameEditText.getWindowToken(), 0);
	}
}