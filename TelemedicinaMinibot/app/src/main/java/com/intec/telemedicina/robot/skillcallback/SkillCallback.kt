package com.intec.telemedicina.robot.skillcallback

import android.os.RemoteException

import com.ainirobot.coreservice.client.speech.SkillCallback


var mSkillCallback: SkillCallback = object : SkillCallback() {
    @Throws(RemoteException::class)
    override fun onSpeechParResult(s: String) {
        //The result of temporary speech recognition
    }

    @Throws(RemoteException::class)
    override fun onStart() {
        //Start recognition
    }

    @Throws(RemoteException::class)
    override fun onStop() {
        // end of recognition
    }

    @Throws(RemoteException::class)
    override fun onVolumeChange(volume: Int) {
        //The size of the recognized voice changes
    }

    /**
     * @param status 0: return normally
     * 1: other returns
     * 2: Noise or single_other return
     * 3: Timeout
     * 4: Forced to cancel
     * 5: The asr result ends early, without passing through NLU
     * 6: In the case of full duplex with the same semantics, other returns
     */
    @Throws(RemoteException::class)
    override fun onQueryEnded(status: Int) {
    }

    @Throws(RemoteException::class)
    override fun onQueryAsrResult(asrResult: String) {
        //asrResult: final recognition result
    }
}