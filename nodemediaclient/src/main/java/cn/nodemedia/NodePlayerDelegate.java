package cn.nodemedia;

/**
 * Created by ALiang on 17/2/16.
 */

public interface NodePlayerDelegate {
    void onEventCallback(NodePlayer player, int event, String msg);
}
