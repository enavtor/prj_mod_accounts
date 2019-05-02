package com.droidmare.accounts.Utils;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Extracts system properties using reflection
 * @author Carolina on 11/10/2017.
 */

class STBProperties {

    /** SystemProperties class package and name */
    private static final String PROPERTIES_CLASS_NAME="android.os.SystemProperties";

    /** Method to call by reflection */
    private static final String REFLECTION_METHOD="get";

    /** Ethernet connection IP address */
    static final String PROPERTY_ETH_IP="dhcp.eth0.ipaddress";

    /**
     * Gets system property value using reflection
     * @param context App context
     * @param key Key to find the property
     * @return Property value
     */
    static String getProperty(Context context,String key) throws InvocationTargetException{
        try {
            ClassLoader classLoader=context.getClassLoader();
            Class<?> SystemProperties=classLoader.loadClass(PROPERTIES_CLASS_NAME);
            Method method=SystemProperties.getMethod(REFLECTION_METHOD,String.class);
            return ((String)method.invoke(SystemProperties,key));
        }catch(Exception e){throw new InvocationTargetException(e);}
    }
}
