package com.droidmare.statistics;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sends app statistics to statistics receiver
 * @author Carolina on 03/10/2017.
 * @modifyBy Eduardo on 23/05/2018.
 */

public class StatisticService extends StatisticAPI {

    /** App action name */
    private static final String APP_ACTION_NAME="action";

    /** Additional information name */
    private static final String ADDITIONAL_INFO_NAME="additional_info";

    /** Action types over app */
    public static final String ON_CREATE ="onCreate";
    public static final String ON_START = "onStart";
    public static final String ON_RESUME = "onResume";
    public static final String ON_RESTART = "onRestart";
    public static final String ON_PAUSE = "onPause";
    public static final String ON_STOP = "onStop";
    public static final String ON_DESTROY = "onDestroy";

    /**
     * Constructor with params
     * @param context App context
     */
    public StatisticService(Context context) {
        super(context);
    }

    /**
     * Statistic additional information format:
     * <code>
     *     o[0]: action of app
     *     o[1]: additional information of app
     * </code>
     */

    @Override
    protected JSONObject generateAdditionalInfo(Object... o) {
        try{
            JSONObject object=new JSONObject();
            object.put(APP_ACTION_NAME,o[0]);
            object.put(ADDITIONAL_INFO_NAME,o[1]);
            return object;
        }
        catch (JSONException jse){return null;}
    }
}
