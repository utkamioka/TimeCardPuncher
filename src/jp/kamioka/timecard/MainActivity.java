package jp.kamioka.timecard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener
{
	private static final String TAG = "MainActivity";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button punchButton = (Button)findViewById(R.id.button_punch);
        punchButton.setOnClickListener(this);
    }

	@Override
	public void onClick(View v)
	{
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
		String calendar = preference.getString("pref_selectcalendar", null);
		Log.d(TAG, "onClick(): preference: calendar="+calendar);
		
		if ( calendar == null ) {
			new AlertDialog.Builder(this)
			.setTitle("Notice")
			.setMessage("empty calendar name")
			.show();
			return;
		}
		
		try {
			long startTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis();

			CalendarAccessor.Event event = new CalendarAccessor.Event("打刻", startTime, endTime);
			new CalendarAccessor(this).addEvent(calendar, event);
			Toast.makeText(this, calendar+"<<"+event, Toast.LENGTH_LONG).show();
		} catch ( CalendarAccessException e ) {
			Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();			
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, 0, 0, R.string.label_preference).setIcon(android.R.drawable.ic_menu_manage);
		menu.add(Menu.NONE, 1, 1, R.string.label_about).setIcon(android.R.drawable.ic_menu_info_details);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch ( item.getItemId() ) {
		case 0:
			Intent intent = new Intent(this, PrefActivity.class);
			startActivity(intent);
			break;
		default:
			new AlertDialog.Builder(this)
			.setTitle(R.string.label_version)
			.setMessage(Const.NAME+"-"+Const.VERSION)
			.setPositiveButton("OK", null)
			.show();
			break;
		}
		return true;
	}
}