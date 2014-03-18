package ahmed.aly.analogclockprototype;

import java.util.Stack;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;

/**
 * This view represents an analog clock. The handles of the clock could be
 * dragged when touched. The two handles are not related, changing the minutes
 * hand doesn't affect the hours hand
 */
public class AnalogClockView extends View
{

	/* Dimensions of the view */
	int backGroundWidth, backGroundHeight;
	int centerX, centerY;
	int clockWidth, clockHeight;
	int minuteHandWidth, minuteHandHeight;
	int hourHandWidth, hourHandHeight;

	/* The bitmaps of the clock and handles */
	Boolean imageLoaded;
	Bitmap clockBitmap, minuteHandBitmap, hourHandBitmap, minuteHandHilightedBitmap,
			hourHandHilightedBitmap;

	/* Value of the clock */
	float minuteHandAngle;
	float hourHandAngle;

	/* The state of the clock */
	enum ClockState
	{
		IDLE, MINUTE_HAND_SELECTED, HOUR_HAND_SELECTED
	};

	ClockState clockState;
	boolean initialized = false;

	public AnalogClockView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	public AnalogClockView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public AnalogClockView(Context context)
	{
		super(context);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// Calculate the dimensions needed
		backGroundWidth = MeasureSpec.getSize(widthMeasureSpec);
		backGroundHeight = MeasureSpec.getSize(heightMeasureSpec);

		initialize();
	}

	/**
	 * Sets the required dimensions of the different bitmaps and loads them
	 */
	private void initialize()
	{
		// Checks if it's already initialized
		if (initialized == false)
			initialized = true;
		else
			return;

		// Calculate the dimensions needed
		centerX = backGroundWidth / 2;
		centerY = (int) (backGroundHeight / 2);
		clockWidth = (int) (backGroundWidth * 1);
		clockHeight = (int) (backGroundHeight * 1);
		minuteHandWidth = (int) (clockWidth * 0.1);
		minuteHandHeight = (int) (clockHeight * 0.4);
		hourHandWidth = (int) (clockWidth * 0.15);
		hourHandHeight = (int) (clockHeight * 0.2);

		// set some instance variables
		minuteHandAngle = 0;
		hourHandAngle = 180;
		clockState = ClockState.IDLE;

		// Load the images
		new BitmapLoaderTask().execute(false, false, false);

	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (imageLoaded)
		{
			// Draw the clock
			canvas.drawBitmap(clockBitmap, (backGroundWidth - clockWidth) / 2,
					(backGroundHeight - clockHeight) / 2, null);

			// Draw the minute hand
			canvas.save();
			canvas.rotate(minuteHandAngle - 180, centerX, centerY);
			if (clockState == ClockState.MINUTE_HAND_SELECTED)
				canvas.drawBitmap(minuteHandHilightedBitmap, centerX - minuteHandWidth / 2,
						centerY, null);
			else
				canvas.drawBitmap(minuteHandBitmap, centerX - minuteHandWidth / 2, centerY, null);
			canvas.restore();

			// Draw the hour hand
			canvas.save();
			canvas.rotate(hourHandAngle - 180, centerX, centerY);
			if (clockState == ClockState.HOUR_HAND_SELECTED)
				canvas.drawBitmap(hourHandHilightedBitmap, centerX - hourHandWidth / 2, centerY,
						null);
			else
				canvas.drawBitmap(hourHandBitmap, centerX - hourHandWidth / 2, centerY, null);
			canvas.restore();

			// Change the text in the text view in the main activity
			MainActivity.setCurrentTimeText();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			// Check the angle between the line joining the point clicked to the
			// origin and check which hand is more near
			float selectedAngle = pointToAngle(event.getX(), event.getY());
			if (Math.abs(selectedAngle - minuteHandAngle) < Math.abs(selectedAngle - hourHandAngle))
			{
				clockState = ClockState.MINUTE_HAND_SELECTED;
				minuteHandAngle = (int) selectedAngle;
			} else
			{
				clockState = ClockState.HOUR_HAND_SELECTED;
				hourHandAngle = (int) selectedAngle;
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE)
		{
			// Update the selected hand
			float selectedAngle = pointToAngle(event.getX(), event.getY());
			if (clockState == ClockState.MINUTE_HAND_SELECTED)
				minuteHandAngle = (int) selectedAngle;
			else if (clockState == ClockState.HOUR_HAND_SELECTED)
				hourHandAngle = (int) selectedAngle;
		} else
		{
			// Change the state to indicated that neither hand is selected
			clockState = ClockState.IDLE;
		}
		postInvalidate();
		return true;
	}

	/**
	 * Calculates the angle between the line joining (x,y), (centerX, centerY)
	 * and the normal.
	 * 
	 * @param x
	 * @param y
	 * @return an angle between 0 and 360
	 */
	private float pointToAngle(float x, float y)
	{
		// Get the x,y differences
		float deltaY = y - centerY;
		float deltaX = x - centerX;

		// adjust the angle to be from 0 to 360
		float angle = (float) (Math.atan2(deltaY, deltaX) * 180 / Math.PI);
		angle += 90;
		if (angle < 0)
			angle += 360;
		if (angle > 360)
			angle -= 360;

		return angle;
	}

	/**
	 * @return an integer between 0 and 59 representing the current minutes
	 */
	public int getCurrentMinutes()
	{
		return (int) (minuteHandAngle * 60 / 360);
	}

	/**
	 * @return an integer between 1 and 12 representing the current hours
	 */
	public int getCurrentHours()
	{
		int currentHours = (int) (hourHandAngle * 12 / 360);
		if (currentHours == 0)
			currentHours = 12;
		return currentHours;
	}

	/**
	 * A thread that loads all the bitmaps. When the thread is working a
	 * progress dialog appears. When it's finished the view is allowed to show
	 * the bitmaps
	 */
	class BitmapLoaderTask extends AsyncTask<Boolean, Boolean, Boolean>
	{
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute()
		{
			// flag that we're still loading and show a progress dialog
			imageLoaded = false;

			// Make a progress dialog and show it
			progressDialog = new ProgressDialog(getContext());
			progressDialog.setTitle("Loading");
			progressDialog.setMessage("loading the clock");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Boolean doInBackground(Boolean... params)
		{
			// The clock
			clockBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.clock,
					clockWidth, clockHeight);
			clockWidth = clockBitmap.getWidth();
			clockHeight = clockBitmap.getHeight();

			// The minute hand
			minuteHandBitmap = decodeSampledBitmapFromResource(getResources(),
					R.drawable.minute_hand, minuteHandWidth, minuteHandHeight);
			minuteHandWidth = minuteHandBitmap.getWidth();
			minuteHandHeight = minuteHandBitmap.getHeight();

			// The highlighted minute hand
			minuteHandHilightedBitmap = decodeSampledBitmapFromResource(getResources(),
					R.drawable.minute_hand_hilighted, minuteHandWidth, minuteHandHeight);

			// The hour hand
			hourHandBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.hour_hand,
					hourHandWidth, hourHandHeight);
			hourHandWidth = hourHandBitmap.getWidth();
			hourHandHeight = hourHandBitmap.getHeight();

			// The highlighted hour hand
			hourHandHilightedBitmap = decodeSampledBitmapFromResource(getResources(),
					R.drawable.hour_hand_hilighted, hourHandWidth, hourHandHeight);

			return false;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			// Flag that we've loaded the image and remove the dialog
			imageLoaded = true;
			progressDialog.dismiss();
			postInvalidate();

		}

		public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth,
				int reqHeight)
		{

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(res, resId, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeResource(res, resId, options);
		}

		public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
		{
			// Raw height and width of image
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth)
			{

				final int halfHeight = height / 2;
				final int halfWidth = width / 2;

				// Calculate the largest inSampleSize value that is a power of 2
				// and
				// keeps both
				// height and width larger than the requested height and width.
				while ((halfHeight / inSampleSize) > reqHeight
						&& (halfWidth / inSampleSize) > reqWidth)
				{
					inSampleSize *= 2;
				}
			}

			return inSampleSize;
		}

	}

}
