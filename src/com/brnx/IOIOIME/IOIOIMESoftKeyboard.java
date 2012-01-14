package com.brnx.IOIOIME;


import ioio.lib.util.IOIOConnectionDiscovery;
import ioio.lib.util.IOIOConnectionDiscovery.IOIOConnectionSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import android.util.Log;

import com.example.android.softkeyboard.SoftKeyboard;

public class IOIOIMESoftKeyboard extends SoftKeyboard {

	/** The threads that interact with the IOIO. */

	private ArrayList<IOIOThread> al = new ArrayList<IOIOThread>();
	public static final String TAG = "IOIOIME_SERVICE";
	

	@Override public void onCreate() {
		super.onCreate();
		Log.d(this.getClass().getName(), "onCreate()");
		Collection<IOIOConnectionSpec> specs = getConnectionSpecs();
		
		Log.d(this.getClass().getName(), "specs size :"+specs.size());
		
		for (IOIOConnectionSpec spec : specs){		
			IOIOThread ioio_thread_ = new IOIOThread(spec,this);
			ioio_thread_.start();
			al.add(ioio_thread_);
		}


	}

	@Override
	public void onDestroy() {
		Log.d(this.getClass().getSimpleName(), "onDestroy()");
		for (IOIOThread ioio_thread_ : al){
			if (ioio_thread_ != null) {
				ioio_thread_.abort();
				try {
					ioio_thread_.join();
				} catch (InterruptedException e) {
				}
			}
		}
		super.onDestroy();
	}
	
	private Collection<IOIOConnectionSpec> getConnectionSpecs() {
		Collection<IOIOConnectionSpec> result = new LinkedList<IOIOConnectionSpec>();
		addConnectionSpecs("ioio.lib.util.SocketIOIOConnectionDiscovery",
				result);
		addConnectionSpecs(
				"ioio.lib.bluetooth.BluetoothIOIOConnectionDiscovery", result);
		return result;
	}

	private void addConnectionSpecs(String discoveryClassName,
			Collection<IOIOConnectionSpec> result) {
		
		try {
			Class<?> cls = Class.forName(discoveryClassName);
			IOIOConnectionDiscovery discovery = (IOIOConnectionDiscovery) cls
					.newInstance();
			discovery.getSpecs(result);
		} catch (ClassNotFoundException e) {
			Log.d(TAG, "Discovery class not found: " + discoveryClassName
					+ ". Not adding.");
		} catch (Exception e) {
			Log.w(TAG,
					"Exception caught while discovering connections - not adding connections of class "
							+ discoveryClassName, e);
		}
	}

}
