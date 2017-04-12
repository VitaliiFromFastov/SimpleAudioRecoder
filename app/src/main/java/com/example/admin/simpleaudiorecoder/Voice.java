package com.example.admin.simpleaudiorecoder;

/**
 * Created by Admin on 07.03.2017.
 */

public class Voice {

    private String mVoiceTitle;
    private String mVoiceDate;
    private String mVoiceDuration;

    public Voice(){}

    public Voice(String voiceTitle, String voiceDate, String voiceDuration){

        mVoiceTitle=voiceTitle;
        mVoiceDate=voiceDate;
        mVoiceDuration= voiceDuration;
    }
    public void setmVoiceTitle(String mVoiceTitle) {
        this.mVoiceTitle = mVoiceTitle;
    }
    public String getmVoiceTitle() {
        return mVoiceTitle;
    }

    public void setmVoiceDate(String mVoiceDate) {
        this.mVoiceDate = mVoiceDate;
    }

    public String getmVoiceDate() {
        return mVoiceDate;
    }

    public void setmVoiceDuration(String mVoiceDuration) {
        this.mVoiceDuration = mVoiceDuration;
    }

    public String getmVoiceDuration() {
        return mVoiceDuration;
    }
}
