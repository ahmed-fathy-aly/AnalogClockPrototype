package ahmed.aly.analogclockprototype;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

/**
 * @author ahmed fathy aly
 * The activity contains a text view and an analog clock The text view shows the
 * time indicated by the analog clock
 */
public class MainActivity extends Activity
{
	/* The views */
	static TextView currentTimeTextView;
	static AnalogClockView analogClock;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set references to the views
		currentTimeTextView = (TextView) findViewById(R.id.textViewCurrentTime);
		analogClock = (AnalogClockView) findViewById(R.id.analogClock);
	}

	/**
	 * Gets the time indicated by the analog clock and sets it to the text view
	 */
	public static void setCurrentTimeText()
	{
		// The current hours
		String hourString = "" + analogClock.getCurrentHours();
		if (hourString.length() == 1)
			hourString = "0" + hourString;

		// The current minutes
		String minuteString = "" + analogClock.getCurrentMinutes();
		if (minuteString.length() == 1)
			minuteString = "0" + minuteString;

		currentTimeTextView.setText(hourString + ":" + minuteString);
	}

}
