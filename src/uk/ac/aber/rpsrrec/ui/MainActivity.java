package uk.ac.aber.rpsrrec.ui;

import java.util.ArrayList;

import uk.ac.aber.plantcatalog.R;
import uk.ac.aber.rpsrrec.data.Sighting;
import uk.ac.aber.rpsrrec.data.User;
import uk.ac.aber.rpsrrec.data.Visit;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Missing: 
 * Parcelable or any other way to restore session after closing/minimising app
 * Sending preparations/summary page
 * Species/reserve search for easier user selection
 * Downloading species and reserve data from database
 */

public class MainActivity extends FragmentActivity implements
		LogOnDialogFragment.LogOnDialogListener,
		ReserveEntryFragment.ReserveEntryDialogListener,
		SightingEntryFragment.SightingEntryListener,
		LocationListener {

	private Visit visit;

	private DialogFragment dialog;

	private static final String STATE_VISIT = "visit";

	private static final int REQUEST_IMAGE_CAPTURE_SPECIMEN = 1;
	private static final int REQUEST_IMAGE_CAPTURE_LOCATION = 2;
	private boolean specimenPic = false;
	private boolean locationPic = false;

	private double locLat;
	private double locLng;

	private LocationManager locationManager;
	private String provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {
			visit = savedInstanceState.getParcelable(STATE_VISIT);
		}
		if (visit == null) {
			visit = new Visit();
		}

		if (visit.getUser() == null) {
			LogOnDialogFragment logOn = new LogOnDialogFragment();
			logOn.show(getFragmentManager(), "log_on");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_add_user) {
			addUser();
		}
		else if (id == R.id.action_select_reserve) {
			selectReserve();
		}
		else if (id == R.id.action_delete_visit) {
			visit = new Visit();
			updateSightingList();
			TextView reserveView = (TextView) findViewById(R.id.locationView);
			reserveView.setText("No Location Selected");
		}
		else if (id == R.id.action_send_visit) {
			// TODO Send visit through HTTP Post
			visit = new Visit();
			updateSightingList();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable(STATE_VISIT, visit);
		super.onSaveInstanceState(savedInstanceState);
	}

	public void addUser() {
		LogOnDialogFragment logOn = new LogOnDialogFragment();
		logOn.show(getFragmentManager(), "log_on");
	}

	public void selectReserve() {
		ReserveEntryFragment reserveEntry = new ReserveEntryFragment();
		reserveEntry.show(getFragmentManager(), "reserve_entry");
	}

	public void recordSighting(View view) {
		SightingEntryFragment sightingEntry = new SightingEntryFragment();
		sightingEntry.show(getFragmentManager(), "sighting_entry");
	}

	public void updateSightingList() {
		ArrayList<Sighting> sightings = visit.getSightings();

		ArrayAdapter<Sighting> sightingsAdapter = new ArrayAdapter<Sighting>(this, android.R.layout.simple_list_item_1, sightings);

		ListView listview = (ListView) findViewById(R.id.sightingListView);
		listview.setAdapter(sightingsAdapter);
	}

// LOGON LISTENER /////////////////////////////////////////////////////////////

	@Override
	public void onLogOnDialogPositiveClick(DialogFragment dialog) {

		Dialog dialogView = dialog.getDialog();

		EditText editText = (EditText) dialogView.findViewById(R.id.userName);
		String userName = editText.getText().toString();

		editText = (EditText) dialogView.findViewById(R.id.userPhone);
		String userPhone = editText.getText().toString();

		editText = (EditText) dialogView.findViewById(R.id.userEmail);
		String userEmail = editText.getText().toString();

		if (userName.equals("") || userPhone.equals("") || userEmail.equals("")) {
			Toast.makeText(getApplicationContext(),
					"User details not complete", Toast.LENGTH_SHORT).show();
		} else {
			visit.setUser(new User(userName, userPhone, userEmail));
			visit.setDate("date");
			Toast.makeText(getApplicationContext(), "User details added",
					Toast.LENGTH_SHORT).show();
		}

		if (visit.getReserveName() == null) {
			selectReserve();
		}
	}

	@Override
	public void onLogOnDialogNegativeClick(DialogFragment dialog) {
		Toast.makeText(getApplicationContext(), "User details required",
				Toast.LENGTH_SHORT).show();

		if (visit.getReserveName() == null) {
			selectReserve();
		}
	}

// RESERVE ENTRY LISTENER /////////////////////////////////////////////////////

	@Override
	public void onReserveEntryDialogPositiveClick(DialogFragment dialog) {

		Dialog dialogView = dialog.getDialog();

		EditText editText = (EditText) dialogView.findViewById(R.id.reserveName);
		String reserveName = editText.getText().toString();

		if(reserveName.equals("")) {
			Toast.makeText(getApplicationContext(),
					"Reserve details not complete", Toast.LENGTH_SHORT).show();
		} else {
			visit.setReserve(reserveName);
			Toast.makeText(getApplicationContext(), "Reserve details added",
					Toast.LENGTH_SHORT).show();
			TextView reserveView = (TextView) findViewById(R.id.locationView);
			reserveView.setText(reserveName);
		}

	}

	@Override
	public void onReserveEntryDialogNegativeClick(DialogFragment dialog) {
		Toast.makeText(getApplicationContext(), "Reserve details required",
				Toast.LENGTH_SHORT).show();
	}

// SIGHTING ENTRY LISTENER ////////////////////////////////////////////////////

	@Override
	public void onCreateGetLocation(DialogFragment dialog) {

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);

		if (location != null) {
			onLocationChanged(location);
			Toast.makeText(this, "LAT: " + locLat + "LNG: " + locLng, Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(this, "Cant find location using " + provider, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onSightingEntryPositiveClick(DialogFragment dialog) {

		Dialog dialogView = dialog.getDialog();

		EditText specimenName = (EditText) dialogView.findViewById(R.id.specimenName);
		String name = specimenName.getText().toString();

		Spinner daforSelected = (Spinner) dialogView.findViewById(R.id.daforSelector);
		String dafor = daforSelected.getSelectedItem().toString();

		EditText descriptionDone = (EditText) dialogView.findViewById(R.id.description);
		String description = descriptionDone.getText().toString();

		ImageView specimenImageTaken = (ImageView) dialogView.findViewById(R.id.specimenImageDisplay);

		Bitmap specimenImage = ((BitmapDrawable)specimenImageTaken.getDrawable()).getBitmap();

		ImageView locationImageTaken = (ImageView) dialogView.findViewById(R.id.locationImageDisplay);

		Bitmap locationImage = ((BitmapDrawable)locationImageTaken.getDrawable()).getBitmap();

		if (name.equals("")) {
			Toast.makeText(getApplicationContext(), "Species is required", Toast.LENGTH_SHORT).show();
			return;
		}

		if (visit != null) {
			visit.addNewSighting(new Sighting(name, dafor, description, locLat, locLng, specimenImage, locationImage, specimenPic, locationPic));
		}

		dialog.dismiss();

		Toast.makeText(getApplicationContext(), "Sighting details added", Toast.LENGTH_SHORT).show();

		updateSightingList();
	}

	@Override
	public void onSightingEntryNegativeClick(DialogFragment dialog) {

		Toast.makeText(getApplicationContext(), "Sighting entry canceled", Toast.LENGTH_SHORT).show();
		dialog.dismiss();
	}

// CAMERA MANAGER /////////////////////////////////////////////////////////////

	@Override
	public void onCameraSpeciesClick(DialogFragment dialog) {
		this.dialog = dialog;
		specimenPic = true;
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE_SPECIMEN);
	}

	@Override
	public void onCameraLocationClick(DialogFragment dialog) {
		this.dialog = dialog;
		locationPic = true;
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE_LOCATION);
	}

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		if(specimenPic) {
			if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_SPECIMEN);
			}
		}
		if(locationPic) {
			if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_LOCATION);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Dialog dialogView = dialog.getDialog();
		if(specimenPic) {
			if (requestCode == REQUEST_IMAGE_CAPTURE_SPECIMEN && resultCode == RESULT_OK) {
				ImageView picDisplay = (ImageView) dialogView.findViewById(R.id.specimenImageDisplay);
				Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

				picDisplay.setImageBitmap(imageBitmap);
			}
		} 
		if(locationPic) {
			if (requestCode == REQUEST_IMAGE_CAPTURE_LOCATION && resultCode == RESULT_OK) {
				ImageView picDisp = (ImageView) dialogView.findViewById(R.id.locationImageDisplay);
				Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

				picDisp.setImageBitmap(imageBitmap);
			}
		}
	}

// LOCATION MANAGER ///////////////////////////////////////////////////////////

	@Override
	public void onLocationChanged(Location location) {
		locLat = location.getLatitude();
		locLng = location.getLongitude();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onProviderDisabled(String provider) {}

}
