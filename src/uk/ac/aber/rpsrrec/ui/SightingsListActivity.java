package uk.ac.aber.rpsrrec.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.aber.plantcatalog.R;
import uk.ac.aber.rpsrrec.data.Sighting;
import uk.ac.aber.rpsrrec.data.Visit;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class SightingsListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sightings_list);

		Bundle data = getIntent().getExtras();

		Visit visit = data.getParcelable("visit");

		Sighting sighting = data.getParcelable("sighting");

		ListView listview = (ListView) findViewById(R.id.sightingsList_list);
		String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
			"Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
			"Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
			"OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
			"Android", "iPhone", "WindowsMobile" };

		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i]);
		}
		StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list);

		listview.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sightings_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}

	/** Called when the user clicks the Start Recording button */
	public void recordSighting(View view) {
		Intent intent = new Intent(this, SightingEntryActivity.class);
/*
		EditText editText = (EditText) findViewById(R.id.visit_ReserveName);
		String reserve = editText.getText().toString();

		visit.setReserve(reserve);
		intent.putExtra("visit", visit);
*/
		startActivity(intent);
	}

}
