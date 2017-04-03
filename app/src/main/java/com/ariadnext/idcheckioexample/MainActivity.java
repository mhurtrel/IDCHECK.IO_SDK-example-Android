package com.ariadnext.idcheckioexample;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ariadnext.android.smartsdk.exception.CaptureApiException;
import com.ariadnext.android.smartsdk.interfaces.AXTCaptureInterface;
import com.ariadnext.android.smartsdk.interfaces.bean.AXTDataExtractionRequirement;
import com.ariadnext.android.smartsdk.interfaces.bean.AXTDocumentType;
import com.ariadnext.android.smartsdk.interfaces.bean.AXTSdkInit;
import com.ariadnext.android.smartsdk.interfaces.bean.AXTSdkParams;
import com.ariadnext.android.smartsdk.interfaces.bean.AXTSdkResult;
import com.ariadnext.android.smartsdk.utils.LogUtils;

import org.apache.log4j.Level;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private final static int SDK_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        LogUtils.initLog4J(Level.DEBUG);

        //You need to call the init method at the beginning of the activity
        this.init();
    }

    /**
     * You Need to use this method to check if the users accept or not the permissions of the sdk
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        final boolean permissionsAllowed = AXTCaptureInterface.INSTANCE.verifyPermissions(requestCode, permissions, grantResults);
        if (permissionsAllowed){
            try {
                this.init();
            } catch (final Exception ex) {
                Log.e("SMARTSDK-CLIENT", "An exception occured during SmartSdk initialization.", ex);
                ResultUtils.INSTANCE.showSnackBarWithMsg(getString(R.string.error_init),findViewById(R.id.main_activity));
            }
        }else {
            ResultUtils.INSTANCE.showSnackBarWithMsg(getString(R.string.error_permissions),findViewById(R.id.main_activity));
        }
    }

    /**
     * You arrive here when the SDK return a result
     * @param requestCode Its the same code that you give to the SDK in entry
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SDK_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                try {

                    final AXTSdkResult result = AXTCaptureInterface.INSTANCE.getResultImageFromCapture(data);
                    /**
                     * result contains every informations that will be return by the sdk
                     */

                    ResultUtils.INSTANCE.updateResultState(this, result);
                } catch (final CaptureApiException ex) {
                    switch (ex.getCodeType()) {
                        case LICENSE_SDK_ERROR:
                            ResultUtils.INSTANCE.showSnackBarWithMsg(getString(R.string.error_not_initialized), findViewById(R.id.main_activity));
                            break;
                        case UNSUPPORTED_FEATURE:
                            ResultUtils.INSTANCE.showSnackBarWithMsg(getString(R.string.error_arch_support), findViewById(R.id.main_activity));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    /**
     * This method initialize the sdk, you need to call it before trying to capture anything with the sdk
     */
    public void init(){
        try {
            AXTSdkInit sdkInit = new AXTSdkInit("licence");
            sdkInit.setUseImeiForActivation(false);
            AXTCaptureInterface.INSTANCE.initCaptureSdk(this, sdkInit);
        } catch (CaptureApiException e) {
            Log.e("SMARTSDK-CLIENT", "An exception occured during SmartSdk initialization.", e);
            ResultUtils.INSTANCE.showSnackBarWithMsg(getString(R.string.error_init),findViewById(R.id.main_activity));
        }
    }


    /**
     * This method starts the capture with the settings you give her in entry
     */
    @OnClick(R.id.capture)
    public void capture(){
        if (AXTCaptureInterface.INSTANCE.sdkIsActivated()) {
            AXTSdkParams params = getDefaultSDKParams();
            try {
                final Intent smartSdk = AXTCaptureInterface.INSTANCE.getIntentCapture(this.getApplicationContext(), params);
                startActivityForResult(smartSdk, SDK_REQUEST_CODE);
            } catch (CaptureApiException e) {
                ResultUtils.INSTANCE.showSnackBarWithMsg(getString(R.string.error_start), this.findViewById(R.id.main_activity));
            }
        } else {
            ResultUtils.INSTANCE.showSnackBarWithMsg(getString(R.string.error_init), findViewById(R.id.main_activity));
        }
    }

    /**
     * Define the settings you want to use here
     * @return settings of the sdk
     */
    public static AXTSdkParams getDefaultSDKParams(){
        AXTSdkParams params = new AXTSdkParams();
        params.setScanBothSide(true);
        params.setExtractData(true);
        params.setDataExtractionRequirement(AXTDataExtractionRequirement.MRZ_FOUND);
        params.setDisplayResult(true);
        params.setDocType(AXTDocumentType.ID);
        params.setReadRfid(false);
        params.setUseFrontCamera(false);
        return params;
    }
}
