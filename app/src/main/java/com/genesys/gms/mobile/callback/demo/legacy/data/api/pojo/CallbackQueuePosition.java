package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by Stan on 2/8/2015.
 */
public class CallbackQueuePosition {
    private final int _position;
    private final int _eta;
    private final int _total_waiting;
    private final boolean _agent_ready_threshold_passed;

    public CallbackQueuePosition(int _position,
                                 int _eta,
                                 int _total_waiting,
                                 boolean _agent_ready_threshold_passed) {
        this._position = _position;
        this._eta = _eta;
        this._total_waiting = _total_waiting;
        this._agent_ready_threshold_passed = _agent_ready_threshold_passed;
    }

    public int getPosition() {
        return _position;
    }

    public int getEta() {
        return _eta;
    }

    public int getTotalWaiting() {
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