package com.ies;

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

import com.ies.algos.IDEA.Cryptosystem;
import com.ies.algos.IDEA.IdeaCipher;

public class RemoteBluetooth extends Activity {
	
	// Layout view
	ArrayAdapter<String> msgAdapter;
	static Map<String,HashMap<String,BigInteger>> rsaCollection=new HashMap<String, HashMap<String, BigInteger>>();
	static BigInteger myPublicRSA, myModulusRSA;
	static {
		Cryptosystem.keys(128, 128, 20, new java.security.SecureRandom());
		myModulusRSA=Cryptosystem.modulus;
		myPublicRSA=Cryptosystem.publicExponent;
		Log.v("my keys: modulus ",""+Cryptosystem.modulus);
		Log.v("my keys: publicExponent ",""+Cryptosystem.publicExponent);
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
        msgAdapter.add("My RSA Public("+Cryptosystem.publicExponent.toByteArray().length+"): "+byteArrayToHexString(Cryptosystem.publicExponent.toByteArray()));
        msgAdapter.add("\nMy RSA Modulus("+Cryptosystem.modulus.toByteArray().length+"): "+byteArrayToHexString(Cryptosystem.modulus.toByteArray()));
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
                rsaCollection.put(mConnectedDeviceName, new HashMap<String,BigInteger>());
                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                byte[] toBeSent=new byte["PUBLICRSA:".getBytes().length+myPublicRSA.toByteArray().length];
                System.arraycopy("PUBLICRSA:".getBytes(), 0, toBeSent, 0, "PUBLICRSA:".getBytes().length);
                System.arraycopy(Cryptosystem.publicExponent.toByteArray(), 0, toBeSent, "PUBLICRSA:".getBytes().length,myPublicRSA.toByteArray().length);
                
                mCommandService.write(toBeSent);
                msgAdapter.add("\nSENT RSA PUBLIC: "+byteArrayToHexString(toBeSent));
                toBeSent=null;
                toBeSent=new byte["MODULUSRSA:".getBytes().length+myModulusRSA.toByteArray().length];
                System.arraycopy("MODULUSRSA:".getBytes(), 0, toBeSent, 0, "MODULUSRSA:".getBytes().length);
                System.arraycopy(Cryptosystem.modulus.toByteArray(), 0, toBeSent, "MODULUSRSA:".getBytes().length,myModulusRSA.toByteArray().length);
                mCommandService.write(toBeSent);
                msgAdapter.add("\nSENT RSA MODULUS: "+byteArrayToHexString(toBeSent));
                break;
            case MESSAGE_READ:
            	/*String msgStr=new String((byte[]) msg.obj);
                /*Toast.makeText(getApplicationContext(), byteArrayToHexString((byte[]) msg.obj), 5000).show();
                if(msgStr.indexOf("PUBLICRSA") >= 0){//PUBLICRSA:
                	rsaCollection.get(mConnectedDeviceName).put("PUBLIC", new BigInteger(msgStr.substring(10).getBytes()));
                	Toast.makeText(getApplicationContext(), "PUBLIC: "+byteArrayToHexString(msgStr.substring(10).getBytes()), 5000).show();
                }else if(msgStr.startsWith("MODULUSRSA")){//MODULUSRSA:
                	rsaCollection.get(mConnectedDeviceName).put("MODULUS", new BigInteger(msgStr.substring(11).getBytes()));
                	Toast.makeText(getApplicationContext(), "MODULUS: "+byteArrayToHexString(msgStr.substring(11).getBytes()), 5000).show();
                }*/
            	/*String recievedMsg=new String((byte[]) msg.obj, 0, msg.arg1);
            	byte[] recievedByte=new byte[msg.arg1];
            	System.arraycopy((byte[])msg.obj, 0, recievedByte, 0, msg.arg1);
            	int size=(recievedByte).length;
            	if(recievedMsg.startsWith("PUBLICRSA")){//PUBLICRSA:
    				int indexOfModul=recievedMsg.indexOf("MODULUSRSA");
    				int byteUpto=0;
    				if(indexOfModul>-1){
    					byteUpto=size-11-indexOfModul;
    					byte receivedBytesModulus[]=new byte[size-12-indexOfModul];
        				System.arraycopy((recievedByte), 12+indexOfModul, receivedBytesModulus, 0, size-12-indexOfModul);
                    	rsaCollection.get(mConnectedDeviceName).put("MODULUS", new BigInteger(receivedBytesModulus));
    				}
    				byte receivedBytesPublic[]=new byte[size-10-11-byteUpto];
    				System.arraycopy((recievedByte), 12, receivedBytesPublic, 0, size-10-11-byteUpto);
    				rsaCollection.get(mConnectedDeviceName).put("PUBLIC", new BigInteger(receivedBytesPublic));
    				msgAdapter.add("\nRecr RSA PUBLIC EXPONENT("+receivedBytesPublic.length+"): "+byteArrayToHexString(rsaCollection.get(mConnectedDeviceName).get("PUBLIC").toByteArray()));
    				if(rsaCollection.get(mConnectedDeviceName).containsKey("MODULUS"))
    					 msgAdapter.add("\nRecr RSA MODULUS("+rsaCollection.get(mConnectedDeviceName).get("MODULUS").toByteArray().length+"): "+byteArrayToHexString(rsaCollection.get(mConnectedDeviceName).get("MODULUS").toByteArray()));
                }else if(recievedMsg.startsWith("MODULUSRSA")){//MODULUSRSA:
                	byte receivedBytesModulus[]=new byte[size-11];
    				System.arraycopy((recievedByte), 11, receivedBytesModulus, 0, size-11);
                	rsaCollection.get(mConnectedDeviceName).put("MODULUS", new BigInteger(receivedBytesModulus));
                	msgAdapter.add("\nRSA MODULUS("+receivedBytesModulus.length+"): "+byteArrayToHexString(rsaCollection.get(mConnectedDeviceName).get("MODULUS").toByteArray()));
                }*/
            	
            	byte[] receivedBytes=new byte[msg.arg1];
            	System.arraycopy((byte[])msg.obj, 0, receivedBytes, 0, msg.arg1);
            	msgAdapter.add("\nhex received bytes: "+byteArrayToHexString(receivedBytes));
            	String recievedMsg=new String(receivedBytes);
    			if(recievedMsg.startsWith("PUBLICRSA")){//PUBLICRSA:
    				int indexOfModul=recievedMsg.indexOf("MODULUSRSA");
    				int byteUpto=0;
    				if(indexOfModul>-1){
    					byteUpto=indexOfModul;
    					byte receivedBytesModulus[]=new byte[msg.arg1-11-indexOfModul];
        				System.arraycopy(receivedBytes, 11+indexOfModul, receivedBytesModulus, 0, msg.arg1-11-indexOfModul);
        				Cryptosystem.modulus=new BigInteger(receivedBytesModulus);
                    	rsaCollection.get(mConnectedDeviceName).put("MODULUS", new BigInteger(receivedBytesModulus));
                    	byteUpto+=receivedBytesModulus.length;
    				}
    				byte receivedBytesPublic[]=new byte[msg.arg1-10-byteUpto];
    				System.arraycopy(receivedBytes, 10, receivedBytesPublic, 0, msg.arg1-10-byteUpto);
    				rsaCollection.get(mConnectedDeviceName).put("PUBLIC", new BigInteger(receivedBytesPublic));
    				Cryptosystem.publicExponent=new BigInteger(receivedBytesPublic);
    				msgAdapter.add("\nRSA PUBLIC EXPONENT("+receivedBytesPublic.length+"): "+byteArrayToHexString(rsaCollection.get(mConnectedDeviceName).get("PUBLIC").toByteArray()));
    				if(rsaCollection.get(mConnectedDeviceName).containsKey("MODULUS"))
    					msgAdapter.add("\nRSA MODULUS("+rsaCollection.get(mConnectedDeviceName).get("MODULUS").toByteArray().length+"): "+byteArrayToHexString(rsaCollection.get(mConnectedDeviceName).get("MODULUS").toByteArray()));
                }else if(recievedMsg.startsWith("MODULUSRSA")){//MODULUSRSA:
                	byte receivedBytesModulus[]=new byte[msg.arg1-10];
    				System.arraycopy(receivedBytes, 10, receivedBytesModulus, 0, msg.arg1-10);
                	rsaCollection.get(mConnectedDeviceName).put("MODULUS", new BigInteger(receivedBytesModulus));
                	Cryptosystem.modulus=new BigInteger(receivedBytesModulus);
                	msgAdapter.add("\nRSA MODULUS: "+byteArrayToHexString(rsaCollection.get(mConnectedDeviceName).get("MODULUS").toByteArray()));
                }
                break;
            }
        }
    };
	
	public void onActivityResult(int requestCode, int resultCode,  Intent data) {
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
		currentMemory=(( (double)((double)(Runtime.getRuntime().totalMemory())/1024))- ((double)((double)(Runtime.getRuntime().freeMemory()/1024))));
		startTime=System.currentTimeMillis();
		//for(byte b: editText1.getText().toString().getBytes())
		//mCommandService.write(b);
		//mCommandService.write(editText1.getText().toString().getBytes());
		String msg = editText1.getText().toString();
		for(int i= 0; i<msg.length()%8; i++){
			msg += " ";
		}
		msgAdapter.add("Message      : "+msg);
		String md5=MD5(msg);
		msgAdapter.add("MD5             : \n"+md5);
		msgAdapter.add("MD5 Length: \n"+md5.getBytes().length);
		byte[]  cipherBytes=new byte[8];
        IdeaCipher ideaSender=new IdeaCipher(md5);
        ideaSender.encrypt(msg.getBytes(), cipherBytes);
        //String cipher = new String(cipherBytes);
        msgAdapter.add("Applying IDEA");
        msgAdapter.add("Hex(Cipher Text): \n"+byteArrayToHexString(cipherBytes));
        
        msgAdapter.add("For Transmission");
        
        msgAdapter.add("Applying RSA on md5");
        /*RSA key = new RSA(1024);

        byte md5EncryptByte []=null;
		try {
			md5EncryptByte = key.encrypt(new BigInteger(md5.getBytes("UTF-8"))).toByteArray();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}*/
        /*Cryptosystem.publicExponent=rsaCollection.get(mConnectedDeviceName).get("PUBLIC");
        Cryptosystem.modulus=rsaCollection.get(mConnectedDeviceName).get("modulus".toUpperCase());*/
        byte md5EncryptByte []=Cryptosystem.encrypt(new BigInteger(md5.getBytes())).toByteArray();
        msgAdapter.add("HEX RSA(MD5): "+byteArrayToHexString(md5EncryptByte));
		//System.arraycopy(encrypt.toByteArray(), 0, md5EncryptByte, 0, encrypt.toByteArray().length);
		msgAdapter.add("RSA(MD5)len : \n"+md5EncryptByte.length);
		/*for(int i = encrypt.toByteArray().length; i < 32; i++){
			md5EncryptByte [i]= (byte)0xff;
		}*/
		
		//msgAdapter.add("(RSA(MD5)+Parity)length : "+md5EncryptByte.length);
        try{
        byte byteToBeSend[] = new byte[cipherBytes.length+md5EncryptByte.length];
        System.arraycopy(cipherBytes, 0, byteToBeSend, 0, cipherBytes.length);
        System.arraycopy(md5EncryptByte, 0, byteToBeSend, cipherBytes.length, md5EncryptByte.length);
	    String dataToBeSend = new String(byteToBeSend);
	    msgAdapter.add("Sending Data:\ndataToBeSend lenght: "+dataToBeSend.length()+"\nlength of md5 cipher: "+md5EncryptByte.length);
	    msgAdapter.add(byteArrayToHexString(byteToBeSend));
	    msgAdapter.add("Elapsed Time: "+(System.currentTimeMillis()-startTime)+"ms");
	    /*msgAdapter.add("@Recever");
	    byte plain[]=new byte[cipherBytes.length];
	    IdeaCipher ideaReciever=null;
		ideaReciever = new IdeaCipher(md5EncryptByte);
		ideaReciever.decrypt(cipherBytes, plain);
	    msgAdapter.add("Descipher: "+new String(plain));*/
	    mCommandService.write(byteToBeSend);
		Toast.makeText(getApplicationContext(), "Message '"+editText1.getText().toString()+"' has been sent", 0).show();
        }catch(Exception e){e.printStackTrace();}
        double currentMemoryLocal=(((double)(Runtime.getRuntime().totalMemory()/1024))- ((double)(Runtime.getRuntime().freeMemory()/1024)));
        msgAdapter.add("Used Memory: "+(currentMemoryLocal-currentMemory));
    }
	public String MD5(String md5) {
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