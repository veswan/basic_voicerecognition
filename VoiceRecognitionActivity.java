package com.example.vetest1;

import java.util.ArrayList;



import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class VoiceRecognitionActivity extends Activity implements RecognitionListener {
	private static int tries = 0;
	private static int currItem = 0;
	private static final String[] items= { "Washington", "Paris", "London" }; 
	
	private TextView returnedText, statusText;
	private ToggleButton toggleButton;
	private SpeechRecognizer speech = null;
	private Intent recognizerIntent;
	private String LOG_TAG = "VoiceRecognitionActivity";
	protected boolean mIsListening = false;

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		returnedText = (TextView) findViewById(R.id.textView1);
		statusText   = (TextView) findViewById(R.id.textView2);
		toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
		toggleButton.setTextOff("Click to Speak");
		toggleButton.setTextOn("...listening...");
		toggleButton.setChecked(false);

		speech = SpeechRecognizer.createSpeechRecognizer(this);
		speech.setRecognitionListener(this);
		
		recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
				"en");
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
		
		
		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					 startSpeech();
				} else {
					speech.stopListening();
				}
			}
		});

	}

	
	private void startSpeech() {

        if(!mIsListening && toggleButton.isChecked())
        {
            mIsListening = true;
            speech.startListening(recognizerIntent);
        }
	}
	MyAnimationListener myAnimationListener = new MyAnimationListener();
	class MyAnimationListener implements AnimationListener{

	    @Override
	    public void onAnimationEnd(Animation animation) {
	    
	    	

	    }

	    @Override
	    public void onAnimationRepeat(Animation animation) {
	    }

	    @Override
	    public void onAnimationStart(Animation animation) {
	    }

	}
	private final int ANUM_DURATION = 2000;
	private void listenAgain()
	{
	    if(mIsListening) {
			String txt = "Say >>> " + items[currItem % items.length] +" <<<";
			returnedText.setText(txt);
			
			
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 1500);
			animation.setDuration(ANUM_DURATION);
		//	animation.setFillAfter(false);
			animation.setAnimationListener(myAnimationListener);
			returnedText.startAnimation(animation);
			
			
            mIsListening = false;
            speech.cancel();
            startSpeech();
	    }
	}
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (speech != null) {
			speech.destroy();
			Log.i(LOG_TAG, "destroy");
		}

	}

	@Override
	public void onBeginningOfSpeech() {
		statusText.setText("onBeginningOfSpeech");
		Log.i(LOG_TAG, "onBeginningOfSpeech");
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		Log.i(LOG_TAG, "onBufferReceived: " + buffer);
		statusText.setText("onBufferReceived");
	}

	@Override
	public void onEndOfSpeech() {
		Log.i(LOG_TAG, "onEndOfSpeech");
		//toggleButton.setChecked(false);
		statusText.setText("onEndOfSpeech");
	}


	@Override
	public void onError(int errorCode) {
		
		String errorMessage = getErrorText(errorCode);
		Log.d(LOG_TAG, "FAILED " + errorMessage+":"+tries);
		new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                listenAgain();
            }
        },100);
		if (tries < 5)
			return;
		returnedText.setText(errorMessage+":"+tries);
	//	toggleButton.setChecked(false);
		
		
		
	}

	@Override
	public void onEvent(int arg0, Bundle arg1) {
		Log.i(LOG_TAG, "onEvent");
		printBundle(LOG_TAG, arg1);
	}

	@Override
	public void onPartialResults(Bundle arg0) {
		Log.i(LOG_TAG, "onPartialResults");
	}

	@Override
	public void onReadyForSpeech(Bundle arg0) {
		Log.i(LOG_TAG, "onReadyForSpeech");
	}

	@Override
	public void onResults(Bundle results) {
		Log.i(LOG_TAG, "onResults");
		ArrayList<String> matches = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		String text = "You said:\n";
		boolean ok = false;
		for (String result : matches) {
			text += result + "\n";
			Log.i(LOG_TAG, "onResults ===================================================" + result);
			ok = items[currItem % items.length].equals(result);
			if (ok)
				break;
		}
		
		tries = 0;
		returnedText.setText(text);
		statusText.setText(ok ? "SUCCESS" : "keep trying");
		currItem = (++currItem % items.length);
		
		listenAgain();
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
		//progressBar.setProgress((int) rmsdB);
	}

	public static String getErrorText(int errorCode) {
		String message;
		switch (errorCode) {
		case SpeechRecognizer.ERROR_AUDIO:
			message = "Audio recording error";
			break;
		case SpeechRecognizer.ERROR_CLIENT:
			tries++;
			message = "Client side error";
			break;
		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
			message = "Insufficient permissions";
			break;
		case SpeechRecognizer.ERROR_NETWORK:
			message = "Network error";
			break;
		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
			message = "Network timeout";
			break;
		case SpeechRecognizer.ERROR_NO_MATCH:
			message = "No match";
			break;
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
			message = "RecognitionService busy";
			break;
		case SpeechRecognizer.ERROR_SERVER:
			message = "error from server";
			break;
		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			message = "No speech input";
			break;
		default:
			message = "Didn't understand, please try again.";
			break;
		}
		return message;
	}
//////////////////////////
	public static void printBundle(String tag, Bundle bundle) {
		if (bundle == null)
			return;
		for (String key : bundle.keySet())
		{
		    Log.d(tag, "printBundle " + key + " = \"" + bundle.get(key) + "\"");
		}
	}
}
