package com.rajput.ies_andro;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ies.algos.IDEA.IdeaCipher;
import com.ies.algos.IDEA.RSA;

public class RemoteBluetooth extends Activity {
	
	// Layout view
	ArrayAdapter<String> msgAdapter;
	static Map<String,HashMap<String,BigInteger>> rsaCollection=new HashMap<String, HashMap<String, BigInteger>>();
	static BigInteger myPublicRSA, myModulusRSA;
	static {
		RSA.keys(128, 128, 20, new java.security.SecureRandom());
		myModulusRSA=RSA.modulus;
		myPublicRSA=RSA.publicExponent;
		Log.v("my keys: modulus ",""+RSA.modulus);
		Log.v("my keys: publicExponent ",""+RSA.publicExponent);
	}
	private TextView mTitle;
	EditText editText1;
	// Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    // Key names received from the BluetoothCommandService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
	
	// Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for Bluetooth Command Service
    private BluetoothCommandService mCommandService = null;
    private long startTime;
    private double currentMemory=0l;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        ListView listViewMSG = (ListView)findViewById(R.id.listViewMSG);
        msgAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listViewMSG.setAdapter(msgAdapter);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        editText1=(EditText)findViewById(R.id.editText1);
        currentMemory=(( (double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))- ((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024)));
        Log.v("memory used: ","mb-"+currentMemory);
        msgAdapter.add("My RSA Public("+RSA.publicExponent.toByteArray().length+"): "+byteArrayToHexString(RSA.publicExponent.toByteArray()));
        msgAdapter.add("\nMy RSA Modulus("+RSA.modulus.toByteArray().length+"): "+byteArrayToHexString(RSA.modulus.toByteArray()));
    }

	@Override
	protected void onStart() {
		super.onStart();
		
		// If BT is not on, request that it be enabled.
        // setupCommand() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		// otherwise set up the command service
		else {
			if (mCommandService==null)
				setupCommand();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (mCommandService != null) {
			if (mCommandService.getState() == BluetoothCommandService.STATE_NONE) {
				mCommandService.start();
			}
		}
	}

	private void setupCommand() {
		// Initialize the BluetoothChatService to perform bluetooth connections
        mCommandService = new BluetoothCommandService(this, mHandler);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mCommandService != null)
			mCommandService.stop();
	}
	
	private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
	
	// The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothCommandService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    findViewById(R.id.editText1).setVisibility(View.VISIBLE);
                    findViewById(R.id.button1).setVisibility(View.VISIBLE);
                    break;
                case BluetoothCommandService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothCommandService.STATE_LISTEN:
                case BluetoothCommandService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                
                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                Intent fileExploreIntent = new Intent(
    					FileBrowserActivity.INTENT_ACTION_SELECT_FILE,
    					null,
    					RemoteBluetooth.this,
    					FileBrowserActivity.class
    					);
    			fileExploreIntent.putExtra(
    					FileBrowserActivity.showCannotReadParameter, 
    					false);
    			startActivityForResult(
    					fileExploreIntent,
    					6
    					);
                break;
            case MESSAGE_READ:
            	switch (msg.arg1) {
	                case 1:
	                	RSA.setModulus((BigInteger) msg.obj);
	                	return;
	                case 2:
	                	RSA.setPublicExponent((BigInteger) msg.obj);
	                    return;
                }
            }
        }
    };
	
	public void onActivityResult(int requestCode, int resultCode,  final Intent data) {
		
		if(data!=null && data.hasExtra(FileBrowserActivity.returnFileParameter)){
			new Thread(new Runnable() { @Override public void run() {
				
				File newFile = new File(data.getStringExtra(FileBrowserActivity.returnFileParameter));
				
BigInteger objectToBeSent = RSA.encrypt(new BigInteger(newFile.getName().getBytes()));
mCommandService.writeObject(objectToBeSent);
				
try {
	FileInputStream fis = new FileInputStream(newFile);
	int size = 0;
	final int BUFFER_SIZE = 8;
	IdeaCipher ideaReciever = new IdeaCipher(new String(toMD5(newFile.getName())));
	byte[] buffer = new byte[1024 * 1024 * 4];
	byte[] newBuffer = null;
	byte[] cipher = new byte[BUFFER_SIZE];
	long dataProceed = 0;
	while ((size = fis.read(buffer)) > -1) {
		newBuffer = new byte[(size + BUFFER_SIZE - size	% BUFFER_SIZE)];
		for (int i = 0; i < size; i += BUFFER_SIZE) {
			// plain = new byte[BUFFER_SIZE];
			cipher = new byte[BUFFER_SIZE];
			ideaReciever.encrypt(buffer, i, cipher, 0);
			System.arraycopy(cipher, 0, newBuffer, i, BUFFER_SIZE);
			dataProceed += BUFFER_SIZE;
			Log.e("Data Procceed:" , ": "+ FileBrowserActivity.formatBytes(dataProceed));
		}
		mCommandService.writeObject(newBuffer);
		mCommandService.flush();
	}
	fis.close();
	mCommandService.writeObject(Integer.valueOf(-1));
	mCommandService.flush();
} catch (Exception e) {
	e.printStackTrace();
				}
			}}).start();
			return;
		}
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mCommandService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupCommand();
            } else {
                // User did not enable Bluetooth or an error occured
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
        	Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			mCommandService.write(BluetoothCommandService.VOL_UP);
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			mCommandService.write(BluetoothCommandService.VOL_DOWN);
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	public void onClickSend(View v){
		String msg = editText1.getText().toString();
		if(msg.trim().length()==0){
			return;
		}
        mCommandService.write(msg.getBytes());
    }
	public String toMD5(String md5) {
 	   try {
 	        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
 			md.reset();
 	        byte[] array = md.digest(md5.getBytes("UTF-8"));
 	        StringBuffer sb = new StringBuffer();
 	        for (int i = 0; i < array.length; ++i) {
 	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
 	        }
 	        return sb.toString();
 	   } catch (java.security.NoSuchAlgorithmException e) {e.printStackTrace();}
 		catch (java.io.UnsupportedEncodingException e1) {e1.printStackTrace();}
 	    return null;
 	}
	public static String byteArrayToHexString(byte[] b) {
	    StringBuffer sb = new StringBuffer(b.length * 2);
	    for (int i = 0; i < b.length; i++) {
	      int v = b[i] & 0xff;
	      if (v < 16) {
	        sb.append('0');
	      }
	      sb.append(Integer.toHexString(v));
	    }
	    return sb.toString().toUpperCase();
	}
}