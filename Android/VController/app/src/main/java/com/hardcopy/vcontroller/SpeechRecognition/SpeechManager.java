package com.hardcopy.vcontroller.SpeechRecognition;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;

/**
 * Created by P16434 on 2016-02-16.
 */
public class SpeechManager {
    private static final String TAG = "SpeechRecognizer";

    private Context mContext;
    private static SpeechManager mInstance = null;

    private Intent i;
    private boolean isSpeechRecogInit = false;
    private boolean isListening = false;
    private android.speech.SpeechRecognizer mRecognizer;
    private RecognitionListener mSpeechListener;


    private SpeechManager(Context c, RecognitionListener l, String language) {
        if(mContext == null)
            mContext = c;
        if(mSpeechListener == null)
            mSpeechListener = l;

        // preparing voice recognition
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.getPackageName());
        if(language == null || language.length() < 2)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");   // or use [en-US]
        else
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
    }

    public synchronized static SpeechManager getInstance(Context c, RecognitionListener l, String language) {
        if(mInstance == null) {
            mInstance = new SpeechManager(c, l, language);
        }
        return mInstance;
    }

    public synchronized static SpeechManager resetInstance(Context c, RecognitionListener l, String language) {
        if(mInstance != null) {
            mInstance.stop();
            mInstance.end();
        }
        mInstance = new SpeechManager(c, l, language);
        return mInstance;
    }



    public void init() {
        if(isSpeechRecogInit && mRecognizer != null)
            return;
        mRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(mContext);
        mRecognizer.setRecognitionListener(mSpeechListener);
        isSpeechRecogInit = true;
    }

    public boolean isWorking() {
        return isListening;
    }

    public void start() {
        if(isListening || mRecognizer == null)
            return;
        mRecognizer.startListening(i);
        isListening = true;
    }

    public void stop() {
        if(!isListening || mRecognizer == null)
            return;
        mRecognizer.stopListening();
        isListening = false;
    }

    public void end() {
        stop();
        if(mRecognizer != null)
            mRecognizer.destroy();
        mRecognizer = null;
        isListening = false;
        isSpeechRecogInit = false;
    }

}
