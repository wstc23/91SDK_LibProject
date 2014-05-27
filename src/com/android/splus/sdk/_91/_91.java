package com.android.splus.sdk._91;




import com.android.splus.sdk.apiinterface.IPayManager;
import com.android.splus.sdk.apiinterface.InitBean;
import com.android.splus.sdk.apiinterface.InitBean.InitBeanSuccess;
import com.android.splus.sdk.apiinterface.InitCallBack;
import com.android.splus.sdk.apiinterface.LoginCallBack;
import com.android.splus.sdk.apiinterface.LogoutCallBack;
import com.android.splus.sdk.apiinterface.NetHttpUtil.DataCallback;
import com.android.splus.sdk.apiinterface.RechargeCallBack;
import com.android.splus.sdk.apiinterface.UserAccount;
import com.nd.commplatform.NdCommplatform;
import com.nd.commplatform.NdErrorCode;
import com.nd.commplatform.NdMiscCallbackListener;
import com.nd.commplatform.NdMiscCallbackListener.OnLoginProcessListener;
import com.nd.commplatform.NdPageCallbackListener;
import com.nd.commplatform.OnInitCompleteListener;
import com.nd.commplatform.entry.NdAppInfo;
import com.nd.commplatform.entry.NdBuyInfo;
import com.nd.commplatform.gc.widget.NdToolBar;
import com.nd.commplatform.gc.widget.NdToolBarPlace;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class _91 implements IPayManager {
    private static final String TAG = "_91";

    private static _91 m_91;

    // 平台参数
    private Properties mProperties;

    private String mAppId;

    private String mAppKey;

    private InitBean mInitBean;

    private InitCallBack mInitCallBack;

    private Activity mActivity;

    private LoginCallBack mLoginCallBack;

    private RechargeCallBack mRechargeCallBack;

    // 下面参数仅在测试时用
    private UserAccount mUserModel;

    private int mUid = 0;

    private String mPassport = "";

    private String mSessionid = "";

    private NdToolBar mToolBar;

    private boolean mAppForeground = true;

    /**
     * @Title: _91
     * @Description:( 将构造函数私有化)
     */
    private _91() {

    }

    /**
     * @Title: getInstance(获取实例)
     * @author xiaoming.yuan
     * @data 2014-2-26 下午2:30:02
     * @return _91 返回类型
     */
    public static _91 getInstance() {

        if (m_91 == null) {
            synchronized (_91.class) {
                if (m_91 == null) {
                    m_91 = new _91();
                }
            }
        }
        return m_91;
    }

    @Override
    public void setInitBean(InitBean bean) {
        this.mInitBean = bean;
        this.mProperties = mInitBean.getProperties();
    }

    @Override
    public void init(Activity activity, Integer gameid, String appkey, InitCallBack initCallBack, boolean useUpdate, Integer orientation) {
        this.mInitCallBack = initCallBack;
        this.mActivity = activity;
        this.mAppForeground = true;
        mInitBean.initSplus(activity, initCallBack, new InitBeanSuccess(){
            @Override
            public void initBeaned(boolean initBeanSuccess) {
                if (mInitBean.getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                    NdCommplatform.getInstance().ndSetScreenOrientation(NdCommplatform.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (mInitBean.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
                    NdCommplatform.getInstance().ndSetScreenOrientation(NdCommplatform.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    NdCommplatform.getInstance().ndSetScreenOrientation(NdCommplatform.SCREEN_ORIENTATION_AUTO);
                }

                if (mProperties != null) {
                    mAppId = mProperties.getProperty("91_appid") == null ? "0" : mProperties.getProperty("91_appid");
                    mAppKey = mProperties.getProperty("91_appkey") == null ? "" : mProperties.getProperty("91_appkey");
                }
                NdAppInfo appInfo = new NdAppInfo();
                appInfo.setCtx(mActivity);
                appInfo.setAppId(Integer.parseInt(mAppId));// 应用ID
                appInfo.setAppKey(mAppKey);// 应用Key
                /*
                 * NdVersionCheckLevelNormal 版本检查失败可以继续进行游戏 NdVersionCheckLevelStrict
                 * 版本检查失败则不能进入游戏 默认取值为NdVersionCheckLevelStrict
                 */
                appInfo.setNdVersionCheckStatus(NdAppInfo.ND_VERSION_CHECK_LEVEL_STRICT);
                // 初始化91SDK
                NdCommplatform.getInstance().ndInit(mActivity, appInfo, mOnInitCompleteListener);

            }
        } );

    }

    OnInitCompleteListener mOnInitCompleteListener = new OnInitCompleteListener() {

        @Override
        protected void onComplete(int ndFlag) {
            switch (ndFlag) {
                case OnInitCompleteListener.FLAG_NORMAL:
                    mInitCallBack.initSuccess("初始化成功", null);
                    break;
                case OnInitCompleteListener.FLAG_FORCE_CLOSE:
                    mInitCallBack.initFaile("取消登录");
                    break;
                default:
                    mInitCallBack.initFaile("取消登录");
                    // 如果还有别的Activity或资源要关闭的在这里处理
                    break;
            }
        }

    };

    @Override
    public void login(Activity activity, LoginCallBack loginCallBack) {
        this.mActivity = activity;
        this.mLoginCallBack = loginCallBack;
        NdCommplatform.getInstance().ndLogin(activity, mNdMiscCallbackListener);
    }

    OnLoginProcessListener mNdMiscCallbackListener = new NdMiscCallbackListener.OnLoginProcessListener() {

        @Override
        public void finishLoginProcess(int code) {
            String tip = "";
            if (code == NdErrorCode.ND_COM_PLATFORM_SUCCESS) {
                // 账号登录成功，此时可用初始化玩家游戏数据
                tip = "账号登录成功";
                NdCommplatform nc = com.nd.commplatform.NdCommplatform.getInstance();
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("SessionId", nc.getSessionId());
                params.put("Uin", nc.getLoginUin());
                params.put("Token", nc.getToken().toString());
                params.put("NickName", nc.getLoginNickName());
                // TODO 后台生产相对应的账号
                // NetHttpUtil.getDataFromServerPOST(mActivity,new
                // RequestModel(APIConstants.TS_VERIFY, params, new
                // LoginParser()),mLoginDataCallBack);
                mLoginCallBack.loginSuccess(null);

            } else if (code == NdErrorCode.ND_COM_PLATFORM_ERROR_CANCEL) {
                tip = "取消账号登录";
                mLoginCallBack.backKey("取消登录");

            } else {
                mLoginCallBack.loginFaile("登录失败，错误代码：" + code);
                tip = "登录失败，错误代码：" + code;
            }
            Toast.makeText(mActivity, tip, Toast.LENGTH_SHORT).show();
        }
    };

    private DataCallback<JSONObject> mLoginDataCallBack = new DataCallback<JSONObject>() {

        @Override
        public void callbackSuccess(JSONObject paramObject) {

            try {
                if (paramObject != null && paramObject.optInt("code") == 1) {
                    JSONObject data = paramObject.optJSONObject("data");
                    mUid = data.optInt("uid");
                    mPassport = data.optString("passport");
                    mSessionid = data.optString("sessionid");
                    mUserModel=new UserAccount() {

                        @Override
                        public Integer getUserUid() {

                            return mUid;

                        }

                        @Override
                        public String getUserName() {

                            return mPassport;

                        }

                        @Override
                        public String getSession() {

                            return mSessionid;

                        }
                    };
                    mLoginCallBack.loginSuccess(mUserModel);
                } else {
                    mLoginCallBack.loginFaile(paramObject.optString("msg"));
                }
            } catch (Exception e) {
                mLoginCallBack.loginFaile(e.getLocalizedMessage());
            }
        }

        @Override
        public void callbackError(String error) {
            mLoginCallBack.loginFaile(error);
        }

    };

    @Override
    public void recharge(Activity activity, Integer serverId, String serverName, Integer roleId, String roleName, String outOrderid, String pext, RechargeCallBack rechargeCallBack) {
        rechargeByQuota(activity, serverId, serverName, roleId, roleName, outOrderid, pext, 0f, rechargeCallBack);
    }

    @Override
    public void rechargeByQuota(Activity activity, Integer serverId, String serverName, Integer roleId, String roleName, String outOrderid, String pext, Float money, RechargeCallBack rechargeCallBack) {
        this.mActivity = activity;
        this.mRechargeCallBack = rechargeCallBack;

        if (NdCommplatform.getInstance().isLogined()) {
            // 已经是登录状态
            Toast.makeText(mActivity, "已经是登录状态", Toast.LENGTH_SHORT).show();
        } else {
            // 未登录状态
            Toast.makeText(mActivity, "未登录状态,请重新登录游戏", Toast.LENGTH_SHORT).show();
        }
        String cooOrderSerial = UUID.randomUUID().toString();
        int needPayCoins = 50;
        int ndUniPayForCoin = NdCommplatform.getInstance().ndUniPayForCoin(cooOrderSerial, needPayCoins, outOrderid, activity);

        // HashMap<String, Object> params = new HashMap<String, Object>();

        // NetHttpUtil.getDataFromServerPOST(activity, new
        // RequestModel(APIConstants.TS_PAY, params,new LoginParser()),
        // mRechargeDataCallBack);

    }

    private DataCallback<JSONObject> mRechargeDataCallBack = new DataCallback<JSONObject>() {

        @Override
        public void callbackSuccess(JSONObject paramObject) {

            try {
                if (paramObject != null && paramObject.optInt("code") == 1) {
                    String data = paramObject.optString("data");
                    PayRechargeBean rechargeBean = new PayRechargeBean(data);
                    NdBuyInfo buyInfo = new NdBuyInfo(); //
                    buyInfo.setSerial(rechargeBean.getOrderid());
                    buyInfo.setProductId(rechargeBean.getProductId());// 商品ID，厂商也可以使用固定商品ID
                                                                      // 例如“1”
                    buyInfo.setProductName(rechargeBean.getProductName());// 产品名称
                    buyInfo.setProductPrice(rechargeBean.getProductPrice());// 产品现价
                                                                            // (不能小于0.01个91豆)
                    buyInfo.setProductOrginalPrice(rechargeBean.getProductOrginalPrice());// 产品原价，同上面的价格
                    buyInfo.setCount(rechargeBean.getCount());// 购买数量(商品数量最大10000，最新1)
                    buyInfo.setProductName(rechargeBean.getProductDescription());// 服务器分区，不超过20个字符，只允许英文或数字

                    int aError = NdCommplatform.getInstance().ndUniPayAsyn(buyInfo, mActivity, new NdMiscCallbackListener.OnPayProcessListener() {
                        @Override
                        public void finishPayProcess(int code) {
                            switch (code) {
                                case NdErrorCode.ND_COM_PLATFORM_SUCCESS:
                                    Toast.makeText(mActivity, "购买成功", Toast.LENGTH_SHORT).show();
                                    mRechargeCallBack.rechargeSuccess(mUserModel);
                                    break;
                                case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_FAILURE:
                                    Toast.makeText(mActivity, "购买失败", Toast.LENGTH_SHORT).show();
                                    mRechargeCallBack.rechargeFaile("购买失败");
                                    break;
                                case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_CANCEL:
                                    Toast.makeText(mActivity, "取消购买", Toast.LENGTH_SHORT).show();
                                    mRechargeCallBack.rechargeFaile("取消购买");
                                    break;
                                case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_ASYN_SMS_SENT:
                                    Toast.makeText(mActivity, "订单已提交，充值短信已发送", Toast.LENGTH_SHORT).show();
                                    mRechargeCallBack.rechargeFaile("订单已提交，充值短信已发送");
                                    break;
                                case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_REQUEST_SUBMITTED:
                                    Toast.makeText(mActivity, "订单已提交", Toast.LENGTH_SHORT).show();
                                    mRechargeCallBack.rechargeFaile("订单已提交");
                                    break;
                                default:
                                    Toast.makeText(mActivity, "购买失败", Toast.LENGTH_SHORT).show();
                                    mRechargeCallBack.rechargeFaile("您输入参数有错，无法提交购买请求");
                                    break;
                            }
                        }
                    });
                    if (aError != 0) {
                        Toast.makeText(mActivity, "您输入参数有错，无法提交购买请求", Toast.LENGTH_SHORT).show();
                        mRechargeCallBack.rechargeFaile("您输入参数有错，无法提交购买请求");
                    }

                } else {
                    mRechargeCallBack.rechargeFaile(paramObject.optString("msg"));
                }

            } catch (Exception e) {
                mRechargeCallBack.rechargeFaile(e.getLocalizedMessage());
            }
        }

        @Override
        public void callbackError(String error) {
            mRechargeCallBack.rechargeFaile(error);

        }

    };

    @Override
    public void exitSDK() {
        NdCommplatform.getInstance().setOnPlatformBackground(null);
        NdCommplatform.getInstance().destory();
    }


    @Override
    public void logout(Activity activity, LogoutCallBack logoutCallBack) {

        NdCommplatform.getInstance().ndLogout(NdCommplatform.LOGOUT_TO_RESET_AUTO_LOGIN_CONFIG, activity);
        logoutCallBack.logoutCallBack();
    }

    @Override
    public void setDBUG(boolean logDbug) {
        if (logDbug) {
            NdCommplatform.getInstance().ndSetDebugMode(0);// 设置调试模式

        }
    }

    @Override
    public void enterUserCenter(Activity activity, LogoutCallBack logoutCallBack) {

        NdCommplatform.getInstance().ndEnterPlatform(0, activity);
    }

    @Override
    public void sendGameStatics(Activity activity, Integer serverId, String serverName, Integer roleId, String roleName, String level) {
    }

    @Override
    public void enterBBS(Activity activity) {
        NdCommplatform.getInstance().ndEnterAppBBS(activity, 0);
    }

    @Override
    public void creatFloatButton(Activity activity, boolean showlasttime, int align, float position) {
        int place = 1;
        if (align == 0 && position < 0.5f) {
            place = NdToolBarPlace.NdToolBarTopLeft;
        } else if (align == 0 && position == 0.5f) {
            place = NdToolBarPlace.NdToolBarLeftMid;
        } else if (align == 0 && position > 0.5f) {
            place = NdToolBarPlace.NdToolBarBottomLeft;
        } else if (align != 0 && position < 0.5f) {
            place = NdToolBarPlace.NdToolBarTopRight;
        } else if (align != 0 && position == 0.5f) {
            place = NdToolBarPlace.NdToolBarRightMid;
        } else if (align != 0 && position > 0.5f) {
            place = NdToolBarPlace.NdToolBarTopRight;
        }
        mToolBar = NdToolBar.create(activity, place);
        mToolBar.show();
    }

    @Override
    public void onResume(Activity activity) {

        if (!mAppForeground) {// 从后台切到前台，打开91SDK暂停页

            NdCommplatform.getInstance().ndPause(new NdPageCallbackListener.OnPauseCompleteListener(activity) {

                @Override
                public void onComplete() {

                    // Toast.makeText(activity, "退出DEMO",
                    // Toast.LENGTH_LONG).show();
                }
            });
            mAppForeground = true;
        }

    }

    /**
     * 判断App是否在前台运行
     *
     * @return
     */
    public boolean isAppOnForeground(Activity activity) {
        ActivityManager activityManager = (ActivityManager) activity.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = activity.getApplicationContext().getPackageName();
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {
        if (!isAppOnForeground(activity)) {// app进入后台
            mAppForeground = false;
        }
    }

    @Override
    public void onDestroy(Activity activity) {
        if (mToolBar != null) {
            mToolBar.recycle();
            mToolBar = null;
        }
    }

}
