package com.weihuagu.receiptnotice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.util.Log;

import com.google.gson.Gson;
import com.weihuagu.receiptnotice.util.DeviceInfoUtil;
import com.weihuagu.receiptnotice.util.ExternalInfoUtil;
import com.weihuagu.receiptnotice.util.LogUtil;
import com.weihuagu.receiptnotice.util.PreferenceUtil;
import com.weihuagu.receiptnotice.util.SSLSocketFactoryCompat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLSocketFactory;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;


/**
 * Created by xinghui on 9/20/16.
 * <p>
 * calling this in your Application's onCreate
 * startService(new Intent(this, NotificationCollectorMonitorService.class));
 * <p>
 * BY THE WAY Don't Forget to Add the Service to the AndroidManifest.xml File.
 * <service android:name=".NotificationCollectorMonitorService"/>
 */
public class NotificationCollectorMonitorService extends Service {

    /**
     * {@link Log#isLoggable(String, int)}
     * <p>
     * IllegalArgumentException is thrown if the tag.length() > 23.
     */
    private static final String TAG = "NotifiCollectorMonitor";
    private Timer timer = null;
    private String echointerval = null;
    private TimerTask echotimertask = null;
    private WakeLock wl = null;

    private void setWakelock() {
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        if (preference.isWakelock())
            obtainWakelock();
    }

    private void obtainWakelock() {
        PowerManager pm = (PowerManager) getSystemService(
                Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "receiptnotice:NotificationCollectorMonitorServicewakelock");
        wl.acquire();

    }

    private void releaseWakelock() {
        if (wl != null) wl.release();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        ensureCollectorRunning();
        startEchoTimer();
        setWakelock();
    }

    private String objs2str(Object... args) {
        String s = "";
        for (Object arg : args) {
            s += arg.toString();
        }
        return s;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private boolean echoServerBySocketio(String echourl, String echojson, String token) {
        LogUtil.debugLog("socket echo url: " + echourl);
        LogUtil.debugLog("socket echo json: " + echojson);
        final Socket mSocket = EchoSocket.getInstance(echourl);
        mSocket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Transport transport = (Transport) args[0];
                transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                    @Override
                    public void call(Object... objects) {
                        Map<String, List<String>> headers = (Map<String, List<String>>) objects[0];
                        List<String> values = new ArrayList();
                        values.add(token);
                        //自定义头
                        headers.put("Token", values);
                    }
                });
            }
        });
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                LogUtil.infoLog("Socket.DISCONNECT: " + objs2str(args));
            }
        });
        mSocket.on(Socket.EVENT_PING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                LogUtil.debugLog("Socket.PING: " + objs2str(args));
            }
        });
        mSocket.on(Socket.EVENT_PONG, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                LogUtil.debugLog("Socket.PONG: " + objs2str(args));
            }
        });
        mSocket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                LogUtil.infoLog("Socket.ERROR: " + objs2str(args));
            }
        });
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                LogUtil.infoLog("Socket.CONNECT_ERROR: " + objs2str(args));
            }
        });
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                LogUtil.infoLog("Socket.CONNECT_TIMEOUT: " + objs2str(args));
            }
        });
        mSocket.on("echo", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String result = objs2str(args);
                if (!result.equals("success"))
                    LogUtil.debugLog("socket response message: " + result);
            }
        });
        mSocket.connect();
        mSocket.emit("echo", echojson);
        return true;
    }

    private String getDefaultEchoInterval() {
        if (Build.VERSION.SDK_INT >= 22)
            return "300";
        else
            return "100";
    }

    private void startEchoTimer() {
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        String interval = preference.getEchoInterval();
        this.echointerval = (!interval.equals("") ? interval : getDefaultEchoInterval());
        this.echotimertask = returnEchoTimerTask();
        this.timer = new Timer();
        int intervalmilliseconds = Integer.parseInt(this.echointerval) * 1000;
        LogUtil.infoLog("now socketio timer milliseconds:" + intervalmilliseconds);
        echotimertask.run(); // 先马上运行
        timer.schedule(echotimertask, 5 * 1000, intervalmilliseconds);
    }

    private TimerTask returnEchoTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if (!isIntervalMatchPreference()) {
                    restartEchoTimer();
                    return;
                }
                LogUtil.debugLog("once socketio timer task run");
                boolean flag = echoServer();
                if (!flag)
                    LogUtil.debugLog("socketio timer task not have a server");
            }
        };
    }

    private void restartEchoTimer() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        if (echotimertask != null) {
            echotimertask.cancel();
            echotimertask = null;
        }
        EchoSocket.clearInstance();
        LogUtil.debugLog("restart echo timer task");
        startEchoTimer();
    }

    private boolean isIntervalMatchPreference() {
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        String interval = preference.getEchoInterval();
        if (interval.equals("")) return true;
        return interval.equals(this.echointerval);
    }

    private boolean echoServer() {
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        Gson gson = new Gson();
        if (preference.isEcho() && (preference.getEchoServer() != null)) {
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(date);
            DeviceBean device = new DeviceBean();
            String deviceid = preference.getDeviceid();
            deviceid = (!deviceid.equals("") ? deviceid : DeviceInfoUtil.getUniquePsuedoID());
            device.setDeviceid(deviceid);
            device.setTime(time);
            LogUtil.debugLog("start connect socketio");
            //////////////

            Map devicemap = DeviceBeanReflect(device);
            if (devicemap == null)
                return false;
            if (preference.getEchoCustomOption().equals("") == false) {
                Map custompostoption = ExternalInfoUtil.getCustomOption(preference.getEchoCustomOption());
                if (custompostoption != null) {

                    LogUtil.debugLogWithJava("echo custom option map" + custompostoption);
                    if (custompostoption.size() > 0) devicemap.putAll(custompostoption);
                }
            }
            String token = preference.getEchoServerToken();
            echoServerBySocketio(preference.getEchoServer(), gson.toJson(devicemap), token);
            return true;

        } else return false;

    }

    private void ensureCollectorRunning() {
        ComponentName collectorComponent = new ComponentName(this, /*NotificationListenerService Inheritance*/ NLService.class);
        Log.v(TAG, "ensureCollectorRunning collectorComponent: " + collectorComponent);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean collectorRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices == null) {
            Log.w(TAG, "ensureCollectorRunning() runningServices is NULL");
            toggleNotificationListenerService();
            return;
        }
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (service.service.equals(collectorComponent)) {
                Log.w(TAG, "ensureCollectorRunning service - pid: " + service.pid + ", currentPID: " + Process.myPid() + ", clientPackage: " + service.clientPackage + ", clientCount: " + service.clientCount
                        + ", clientLabel: " + ((service.clientLabel == 0) ? "0" : "(" + getResources().getString(service.clientLabel) + ")"));
                if (service.pid == Process.myPid() /*&& service.clientCount > 0 && !TextUtils.isEmpty(service.clientPackage)*/) {
                    collectorRunning = true;
                    break;
                }
            }
        }
        if (collectorRunning) {
            Log.d(TAG, "ensureCollectorRunning: collector is running");
            return;
        }
        Log.d(TAG, "ensureCollectorRunning: collector not running, reviving...");
        toggleNotificationListenerService();
    }

    private void toggleNotificationListenerService() {
        Log.d(TAG, "toggleNotificationListenerService() called");
        ComponentName thisComponent = new ComponentName(this, /*getClass()*/ NLService.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Map DeviceBeanReflect(DeviceBean e) {
        Class cls = e.getClass();
        Field[] fields = cls.getDeclaredFields();
        Map<String, String> devicebeanmap = new HashMap<String, String>();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            try {
                devicebeanmap.put(f.getName(), (String) f.get(e));
                //System.out.println("属性名:" + f.getName() + " 属性值:" + f.get(e));
            } catch (Exception ee) {
                //LogUtil.debugLogWithJava(ee.getStackTrace().toString());
                return devicebeanmap;
            }
        }
        return devicebeanmap;
    }

    public static class EchoSocket {
        private static final int maxCount = 3;
        private static Socket instance1 = null;
        private static Socket instance2 = null;
        private static Socket instance3 = null;

        private EchoSocket() {
        }

        public static Socket getThisInstance(int i) {
            if (i == 1) return EchoSocket.instance1;
            if (i == 2) return EchoSocket.instance2;
            if (i == 3) return EchoSocket.instance3;
            else return null;
        }

        public static void clearInstance() {
            if (EchoSocket.instance1 != null) EchoSocket.instance1.close();
            if (EchoSocket.instance2 != null) EchoSocket.instance2.close();
            if (EchoSocket.instance3 != null) EchoSocket.instance3.close();
            EchoSocket.instance1 = null;
            EchoSocket.instance2 = null;
            EchoSocket.instance3 = null;
        }

        public static Socket getInstance(String socketserverurl) {
            Random random = new Random();
            int current = random.nextInt(maxCount) + 1;
            if (getThisInstance(current) == null) {
                synchronized (EchoSocket.class) {
                    if (current == 1) instance1 = getIOSocket(socketserverurl);
                    if (current == 2) instance2 = getIOSocket(socketserverurl);
                    if (current == 3) instance3 = getIOSocket(socketserverurl);
                }
            }
            return getThisInstance(current);
        }

        public static Socket getIOSocket(String socketserverurl) {
            try {
                if (Build.VERSION.SDK_INT >= 22) {
                    // set as an option
                    IO.Options opts = new IO.Options();
                    opts.transports = new String[]{WebSocket.NAME, Polling.NAME};
                    return IO.socket(socketserverurl, opts);
                } else {
                    SSLSocketFactory factory = new SSLSocketFactoryCompat();
                    ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .build();
                    List<ConnectionSpec> specs = new ArrayList<>();
                    specs.add(cs);
                    specs.add(ConnectionSpec.COMPATIBLE_TLS);
                    specs.add(ConnectionSpec.CLEARTEXT);
                    OkHttpClient client = new OkHttpClient.Builder()
                            .sslSocketFactory(factory)
                            .connectionSpecs(specs)
                            .build();
                    IO.setDefaultOkHttpWebSocketFactory(client);
                    IO.setDefaultOkHttpCallFactory(client);
                    // set as an option
                    IO.Options opts = new IO.Options();
                    opts.transports = new String[]{WebSocket.NAME, Polling.NAME};
                    opts.callFactory = client;
                    opts.webSocketFactory = client;
                    return IO.socket(socketserverurl, opts);
                }
            } catch (URISyntaxException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                LogUtil.debugLog(sw.toString());
                return null;
            } catch (KeyManagementException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }


        }
    }

    public class DeviceBean {
        public String deviceid;
        public String connectedtime;

        public void setDeviceid(String deviceid) {
            this.deviceid = deviceid;
        }

        public void setTime(String time) {
            this.connectedtime = time;
        }

    }
}
