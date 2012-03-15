package botwars.main;

import java.io.IOException;
import java.util.UUID;

import org.anddev.andengine.util.Debug;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class MP_MapMenu_BT extends MapMenu{
private BluetoothAdapter mBluetoothAdapter;

// Intent request codes
private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
private static final int REQUEST_ENABLE_BT = 3;

// Unique UUID for this application
private static final UUID MY_UUID_SECURE =
    UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
private static final UUID MY_UUID_INSECURE =
    UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
private static final UUID MY_UUID =
UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

//Name for the SDP record when creating server socket
private static final String NAME_SECURE = "BluetoothChatSecure";
private static final String NAME_INSECURE = "BluetoothChatInsecure";

private AcceptThread mAcceptThread;
private ConnectThread mConnectThread;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    Toast.makeText(this, "Device does not support bluetooth", Toast.LENGTH_SHORT);
		}
		 GridView gridView = (GridView) findViewById(R.id.gridview);
	        gridView.setAdapter(new ImageAdapter(this));
	        gridView.setOnItemClickListener(new OnItemClickListener()
	        {
	        public void onItemClick(AdapterView<?> parent,
	        View v, int position, long id)
	        {if(position<3)
	       {
	        BotWars.setMap(position);
	        makeModeDialog();
	        /*Intent StartIntent = new Intent(MP_MapMenu_TCP.this, BotWars.class);
			startActivity(StartIntent);
			finish();
	        */
	       }
	        else Toast.makeText(getBaseContext(),
	               "Map Not Available",
	                Toast.LENGTH_SHORT).show();
				
	        }
	        });
	        
	        if (!mBluetoothAdapter.isEnabled()) {
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			}
	        
	}

	private void makeModeDialog() {
		 Intent serverIntent = new Intent(MP_MapMenu_BT.this, DeviceListActivity.class);
         startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
         
		AlertDialog.Builder modeDialog = new AlertDialog.Builder(this);

		modeDialog.setMessage("Select Mode");
		
		modeDialog.setPositiveButton("Server", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				mAcceptThread=new AcceptThread();
				mAcceptThread.start();

			}
		});

		modeDialog.setNegativeButton("Client", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				
				
				Debug.d("-----------------bt add= ");
				//mConnectThread=new ConnectThread(mBluetoothAdapter.getRemoteDevice(DeviceListActivity.EXTRA_DEVICE_ADDRESS));
				//mConnectThread.start();
				/*				 
				if (!mBluetoothAdapter.isEnabled()) {
				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				    startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
	    
				}
				
				if(mBluetoothAdapter.isEnabled())
				{
Debug.d("BT ISENABLED");
					Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
					// If there are paired devices
					if (pairedDevices.size() > 0) {
					    // Loop through paired devices
					    for (BluetoothDevice device : pairedDevices) {
					        // Add the name and address to an array adapter to show in a ListView
					      //  mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
					        Debug.d("AAAAAAAA  "+device.getName()+"   "+device.getAddress());
					    }
					}	
				}
				
				//Intent openMP_Client_BotWars = new Intent(MP_MapMenu_BT.this,MP_Client_BotWars_BT.class);
				//startActivity(openMP_Client_BotWars);
				//finish();*/
			}
		});

		modeDialog.show();
	}
	
	
	
	
	

private class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
 
    public AcceptThread() {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }
 
    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                //manageConnectedSocket(socket);
                try {
					mmServerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                break;
            }
        }
    }
 
    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}
	
	
private class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
 
    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
 
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mmSocket = tmp;
    }
 
    public void run() {
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();
 
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
 
        // Do work to manage the connection (in a separate thread)
       // manageConnectedSocket(mmSocket);
    }
 
    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
	
	
/*	ArrayAdapter<CharSequence> mArrayAdapter;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==REQUEST_ENABLE_BT){
			if(resultCode==RESULT_OK)
			{Debug.d("OOOOOKKKKK");
				 

			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			// If there are paired devices
			if (pairedDevices.size() > 0) {
			    // Loop through paired devices
			    for (BluetoothDevice device : pairedDevices) {
			        // Add the name and address to an array adapter to show in a ListView
			        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			        Debug.d("AAAAAAAA  "+device.getName()+"   "+device.getAddress());
			    }
			}	
			}
			
			if(resultCode==RESULT_CANCELED)
			{
				
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	*/
}
