package com.android.splus.sdk._91;
import com.android.splus.sdk.apiinterface.APIConstants;
import com.android.splus.sdk.apiinterface.DateUtil;
import com.android.splus.sdk.apiinterface.IPayManager;
import com.android.splus.sdk.apiinterface.InitBean;
import com.android.splus.sdk.apiinterface.InitBean.InitBeanSuccess;
import com.android.splus.sdk.apiinterface.InitCallBack;
import com.android.splus.sdk.apiinterface.LoginCallBack;
import com.android.splus.sdk.apiinterface.LoginParser;
import com.android.splus.sdk.apiinterface.LogoutCallBack;
import com.android.splus.sdk.apiinterface.MD5Util;
import com.android.splus.sdk.apiinterface.NetHttpUtil;
import com.android.splus.sdk.apiinterface.NetHttpUtil.DataCallback;
import com.android.splus.sdk.apiinterface.RechargeCallBack;
import com.android.splus.sdk.apiinterface.RequestModel;
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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class _91 implements IPayManager {
    private static final String TAG = "_91";

    private static _91 m_91;

    // 平台参数
    private Properties mProperties;

    private String mAppId;

    private String mAppKey;

    private InitBean mInitBean;

    private InitCallBack mInitCallBack;

    private Activity mActivity=null;

    private LoginCallBack mLoginCallBack;

    private RechargeCallBack mRechargeCallBack;

    // 下面参数仅在测试时用
    private UserAccount mUserModel;

    private int mUid = 0;

    private String mPassport;

    private String mSessionid;

    private NdToolBar mToolBar=null;

    private boolean mAppForeground = true;

    private int mServerId;
    private float mMoney ;
    private String mPayway="91" ;

    private ProgressDialog mProgressDialog;



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
                    mInitCallBack.initFaile("取消初始化");
                    break;
                default:
                    mInitCallBack.initFaile("取消初始化");
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
                Integer gameid = mInitBean.getGameid();
                String partner = mInitBean.getPartner();
                String referer = mInitBean.getReferer();
                long unixTime = DateUtil.getUnixTime();
                String deviceno=mInitBean.getDeviceNo();
                String signStr =deviceno+gameid+partner+referer+unixTime+mInitBean.getAppKey();
                String sign=MD5Util.getMd5toLowerCase(signStr);

                params.put("deviceno", deviceno);
                params.put("gameid", gameid);
                params.put("partner",partner);
                params.put("referer", referer);
                params.put("time", unixTime);
                params.put("sign", sign);
                params.put("partner_sessionid", nc.getSessionId());
                params.put("partner_uid", nc.getLoginUin());
                params.put("partner_token", nc.getToken().toString().trim());
                params.put("partner_nickname", nc.getLoginNickName());
                params.put("partner_appid", mAppId);
                String hashMapTOgetParams = NetHttpUtil.hashMapTOgetParams(params, APIConstants.LOGIN_URL);
                System.out.println(hashMapTOgetParams);

                showProgressDialog(mActivity);
                NetHttpUtil.getDataFromServerPOST(mActivity,new RequestModel(APIConstants.LOGIN_URL, params, new LoginParser()),mLoginDataCallBack);

            } else if (code == NdErrorCode.ND_COM_PLATFORM_ERROR_CANCEL) {
                tip = "取消账号登录";
                mLoginCallBack.backKey("取消登录");
            } else {
                Log.e(TAG, "登录失败，错误代码：" + code);
                mLoginCallBack.loginFaile("登录失败");
                tip = "登录失败";
            }
            Log.d(TAG, tip);
        }
    };

    private DataCallback<JSONObject> mLoginDataCallBack = new DataCallback<JSONObject>() {

        @Override
        public void callbackSuccess(JSONObject paramObject) {
            closeProgressDialog();
            Log.d(TAG, "mLoginDataCallBack---------"+paramObject.toString());
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
            closeProgressDialog();
            mLoginCallBack.loginFaile(error);
        }

    };

    @Override
    public void recharge(Activity activity, Integer serverId, String serverName, Integer roleId, String roleName, String outOrderid, String pext, RechargeCallBack rechargeCallBack) {
        rechargeByQuota(activity, serverId, serverName, roleId, roleName, outOrderid, pext, 0f, rechargeCallBack);
    }

    @Override
    public void rechargeByQuota(Activity activity, final Integer serverId, final String serverName, final Integer roleId, final String roleName, final String outOrderid, final String pext, Float money, RechargeCallBack rechargeCallBack) {
        this.mActivity = activity;
        this.mRechargeCallBack = rechargeCallBack;
        this.mServerId=serverId;
        this.mMoney=money;
        if (NdCommplatform.getInstance().isLogined()) {
            // 已经是登录状态
            if (mMoney == 0) {
                final EditText editText = new EditText(activity);
                InputFilter[] filters = { new InputFilter.LengthFilter(6) };
                editText.setFilters( filters );
                editText.setInputType( InputType.TYPE_CLASS_NUMBER );
                new AlertDialog.Builder(activity).setTitle("请输入金额")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(editText)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(TextUtils.isEmpty(editText.getText().toString())||editText.getText().toString().startsWith("0")){
                            Toast.makeText(mActivity, "请输入金额", Toast.LENGTH_SHORT).show();
                            return;
                        }else{
                            mMoney=Float.parseFloat(editText.getText().toString());
                            HashMap<String, Object> params = new HashMap<String, Object>();
                            Integer gameid = mInitBean.getGameid();
                            String partner = mInitBean.getPartner();
                            String referer = mInitBean.getReferer();
                            long unixTime = DateUtil.getUnixTime();
                            String deviceno=mInitBean.getDeviceNo();
                            String signStr =gameid+serverName+deviceno+referer+partner+mUid+mMoney+mPayway+unixTime+mInitBean.getAppKey();
                            String sign=MD5Util.getMd5toLowerCase(signStr);

                            params.put("deviceno", deviceno);
                            params.put("gameid", gameid);
                            params.put("partner",partner);
                            params.put("referer", referer);
                            params.put("time", unixTime);
                            params.put("sign", sign);
                            params.put("uid",mUid);
                            params.put("passport",mPassport);
                            params.put("serverId",serverId);
                            params.put("serverName",serverName);
                            params.put("roleId",roleId);
                            params.put("roleName",roleName);
                            params.put("money",mMoney);
                            params.put("pext",pext);
                            params.put("money",mMoney);
                            params.put("payway",mPayway);
                            params.put("outOrderid",outOrderid);
                            String hashMapTOgetParams = NetHttpUtil.hashMapTOgetParams(params, APIConstants.PAY_URL);
                            System.out.println(hashMapTOgetParams);
                            NetHttpUtil.getDataFromServerPOST(mActivity, new RequestModel(APIConstants.PAY_URL, params,new LoginParser()),mRechargeDataCallBack);

                        }
                    }

                }).show();

            }else{
                HashMap<String, Object> params = new HashMap<String, Object>();
                Integer gameid = mInitBean.getGameid();
                String partner = mInitBean.getPartner();
                String referer = mInitBean.getReferer();
                long unixTime = DateUtil.getUnixTime();
                String deviceno=mInitBean.getDeviceNo();
                String signStr =gameid+serverName+deviceno+referer+partner+mUid+mMoney+mPayway+unixTime+mInitBean.getAppKey();
                String sign=MD5Util.getMd5toLowerCase(signStr);

                params.put("deviceno", deviceno);
                params.put("gameid", gameid);
                params.put("partner",partner);
                params.put("referer", referer);
                params.put("time", unixTime);
                params.put("sign", sign);
                params.put("uid",mUid);
                params.put("passport",mPassport);
                params.put("serverId",serverId);
                params.put("serverName",serverName);
                params.put("roleId",roleId);
                params.put("roleName",roleName);
                params.put("money",mMoney);
                params.put("pext",pext);
                params.put("money",money);
                params.put("payway",mPayway);
                params.put("outOrderid",outOrderid);
                String hashMapTOgetParams = NetHttpUtil.hashMapTOgetParams(params, APIConstants.PAY_URL);
                System.out.println(hashMapTOgetParams);
                NetHttpUtil.getDataFromServerPOST(activity, new RequestModel(APIConstants.PAY_URL, params,new LoginParser()),mRechargeDataCallBack);

            }
        }else {
            // 未登录状态
            Toast.makeText(mActivity, "未登录状态,请重新登录游戏", Toast.LENGTH_SHORT).show();
        }

    }

    private DataCallback<JSONObject> mRechargeDataCallBack = new DataCallback<JSONObject>() {

        @Override
        public void callbackSuccess(JSONObject paramObject) {
            Log.d(TAG, "mRechargeDataCallBack---------"+paramObject.toString());
            try {
                if (paramObject != null && (paramObject.optInt("code") == 1||paramObject.optInt("code") == 24)) {
                    JSONObject data = paramObject.optJSONObject("data");
                    String orderid=data.optString("orderid");
                    NdBuyInfo buyInfo = new NdBuyInfo(); //
                    buyInfo.setSerial(orderid);
                    buyInfo.setProductId( mInitBean.getPartner());// 商品ID，厂商也可以使用固定商品ID 例如“1”
                    buyInfo.setProductName("游戏道具");// 产品名称
                    buyInfo.setProductPrice(mMoney);// 产品现价(不能小于0.01个91豆)
                    buyInfo.setProductOrginalPrice(mMoney);// 产品原价，同上面的价格
                    buyInfo.setCount(1);// 购买数量(商品数量最大10000，最小是1)
                    buyInfo.setPayDescription(String.valueOf(mServerId));// 服务器分区，不超过20个字符，只允许英文或数字

                    int aError = NdCommplatform.getInstance().ndUniPayAsyn(buyInfo, mActivity, new NdMiscCallbackListener.OnPayProcessListener() {
                        @Override
                        public void finishPayProcess(int code) {
                            switch (code) {
                                case NdErrorCode.ND_COM_PLATFORM_SUCCESS:
                                    Log.d(TAG, "购买成功");
                                    mRechargeCallBack.rechargeSuccess(mUserModel);
                                    break;
                                case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_FAILURE:
                                    Log.d(TAG, "购买失败");
                                    mRechargeCallBack.rechargeFaile("购买失败");
                                    break;
                                case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_CANCEL:
                                    Log.d(TAG, "取消购买");
                                    mRechargeCallBack.rechargeFaile("取消购买");
                                    break;
                                case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_ASYN_SMS_SENT:
                                    Log.d(TAG, "订单已提交，充值短信已发送");
                                    mRechargeCallBack.rechargeFaile("订单已提交，充值短信已发送");
                                    break;
                                case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_REQUEST_SUBMITTED:
                                    Log.d(TAG, "订单已提交");
                                    mRechargeCallBack.rechargeFaile("订单已提交");
                                    break;
                                default:
                                    Log.d(TAG, "您输入参数有错，无法提交购买请求");
                                    mRechargeCallBack.rechargeFaile("您输入参数有错，无法提交购买请求");
                                    break;
                            }
                        }
                    });
                    if (aError != 0) {
                        Log.d(TAG, "您输入参数有错，无法提交购买请求");
                        mRechargeCallBack.rechargeFaile("您输入参数有错，无法提交购买请求");
                    }

                } else {
                    Log.d(TAG, paramObject.optString("msg"));
                    mRechargeCallBack.rechargeFaile(paramObject.optString("msg"));
                }

            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
                mRechargeCallBack.rechargeFaile(e.getLocalizedMessage());
            }
        }

        @Override
        public void callbackError(String error) {
            Log.d(TAG, error);
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
        try {
            if(mToolBar==null){
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
                    place = NdToolBarPlace.NdToolBarBottomRight;
                }
                mToolBar = NdToolBar.create(activity, place);
            }
            mToolBar.show();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


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
        //        try {
        //            if (mToolBar != null) {
        //                mToolBar.recycle();
        //                mToolBar = null;
        //            }
        //
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
    }

    /**
     * @return void 返回类型
     * @Title: showProgressDialog(设置进度条)
     * @author xiaoming.yuan
     * @data 2013-7-12 下午10:09:36
     */
    protected void showProgressDialog(Activity activity) {
        if (! activity.isFinishing()) {
            try {
                this.mProgressDialog = new ProgressDialog(activity);// 实例化
                // 设置ProgressDialog 的进度条style
                this.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条风格，风格为圆形，旋转的
                this.mProgressDialog.setTitle("登陆");
                this.mProgressDialog.setMessage("加载中...");// 设置ProgressDialog 提示信息
                // 设置ProgressDialog 的进度条是否不明确
                this.mProgressDialog.setIndeterminate(false);
                // 设置ProgressDialog 的进度条是否不明确
                this.mProgressDialog.setCancelable(false);
                this.mProgressDialog.setCanceledOnTouchOutside(false);
                this.mProgressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * @return void 返回类型
     * @Title: closeProgressDialog(关闭进度条)
     * @author xiaoming.yuan
     * @data 2013-7-12 下午10:09:30
     */
    protected void closeProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing())
            this.mProgressDialog.dismiss();
    }
}
