package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by Stan on 2/8/2015.
 */
public class CallbackQueuePosition {
    private final String _position;
    private final String _eta;
    private final String _total_waiting;
    private final boolean _agent_ready_threshold_passed;

    public CallbackQueuePosition(String _position,
                                 String _eta,
                                 String _total_waiting,
                                 boolean _agent_ready_threshold_passed) {
        this._position = _position;
        this._eta = _eta;
        this._total_waiting = _total_waiting;
        this._agent_ready_threshold_passed = _agent_ready_threshold_passed;
    }

    public String getPosition() {
        return _position;
    }

    public String getEta() {
        return _eta;
    }

    public String getTotalWaiting() {
        return _total_waiting;
    }

    public boolean isAgentReadyThresholdPassed() {
        return _agent_ready_threshold_passed;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
                "[" +
                "_position=" + _position +
                ",_eta=" + _eta +
                ",_total_waiting=" + _total_waiting +
                ",_agent_ready_threshold_passed=" + _agent_ready_threshold_passed +
                "]";
    }
}