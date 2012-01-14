package com.brnx.IOIOIME;

import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.Snes;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOConnectionDiscovery.IOIOConnectionSpec;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import static android.view.KeyEvent.*; 

public class IOIOThread extends Thread {

	private IOIO ioio_;
	private boolean abort_ = false;
	private IOIOIMESoftKeyboard softkeyboard=null;
	private IOIOConnectionSpec spec_ ;
	private static Handler mHandler = new Handler();
	
	private Boolean A=false;
	private Boolean B=false;
	private Boolean X=false;
	private Boolean Y=false;
	private Boolean L=false;
	private Boolean R=false;
	private Boolean Up=false;
	private Boolean Down=false;
	private Boolean Left=false;
	private Boolean Right=false;
	private Boolean Start=false;
	private Boolean Select=false;
	
	
	private Snes snes;
	

	public IOIOThread(IOIOConnectionSpec spec,IOIOIMESoftKeyboard keyboard){
		super();
		softkeyboard = keyboard;
		spec_=spec;	
	}
	
	private void sendKeyEvent(int action, int code) {
		KeyEvent key = new KeyEvent(action,code);
		softkeyboard.getCurrentInputConnection().sendKeyEvent(key);
	}

	/** Thread body. */
	@Override
	public void run() {
		super.run();
		while (true) {
			synchronized (this) {
				if (abort_) {
					break;
				}
				try {
					ioio_ = IOIOFactory.create(spec_.className, spec_.args);
				} catch (ClassNotFoundException e) {
					Log.w(IOIOIMESoftKeyboard.TAG+"-Thread","bode");
				}
			}
			try {


				mHandler.post(
						new Runnable() { 
							public void run() {
								Toast.makeText(softkeyboard, "waiting for IOIO connection.", Toast.LENGTH_LONG).show();
							}
						}
						);

				ioio_.waitForConnect();
				mHandler.post(
						new Runnable() { 
							public void run() {
								Toast.makeText(softkeyboard, "IOIO detected.", Toast.LENGTH_LONG).show();
							}
						}
						);
				
				try {
					snes=ioio_.openSnes(3, 4, 5);
				} catch (ConnectionLostException e) {
					e.printStackTrace();
				}
			

				while (true) {
				
					pollSnes();
					sleep(10);
				}

			} catch (ConnectionLostException e) {
			} catch (Exception e) {
				Log.e(IOIOIMESoftKeyboard.TAG+"-Thread", "Unexpected exception caught", e);
				ioio_.disconnect();
				break;
			} finally {
				try {
					ioio_.waitForDisconnect();
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public void pollSnes() throws InterruptedException, ConnectionLostException{

		final byte [] result = snes.readStatus();

		
		byte tmp = result[0];
		byte tmp2 = result[1];
		boolean tmpvalue =false;

		//Right
		tmpvalue = (tmp & 1)==1 ;
		if (tmpvalue != Right ){
			sendKeyEvent(Right?ACTION_UP:ACTION_DOWN, KEYCODE_DPAD_RIGHT);
		}
		Right = tmpvalue;
		tmp >>= 1;
		
		//Left
		tmpvalue = (tmp & 1)==1 ;
		if (tmpvalue != Left ){
			sendKeyEvent(Left?ACTION_UP:ACTION_DOWN, KEYCODE_DPAD_LEFT);
		}
		Left = tmpvalue;
		tmp >>= 1;
		
		//Down
		tmpvalue = (tmp & 1)==1 ;
		if (tmpvalue != Down ){
			sendKeyEvent(Down?ACTION_UP:ACTION_DOWN, KEYCODE_DPAD_DOWN);
		}
		Down = tmpvalue;
		tmp >>= 1;
		
		//Up
		tmpvalue = (tmp & 1)==1 ;
		if (tmpvalue != Up ){
			sendKeyEvent(Up?ACTION_UP:ACTION_DOWN, KEYCODE_DPAD_UP);
		}
		Up = tmpvalue;
		tmp >>= 1;

		//Start
		tmpvalue = (tmp & 1)==1 ;
		if (tmpvalue != Start ){
			sendKeyEvent(Start?ACTION_UP:ACTION_DOWN, KEYCODE_P);
		}
		Start = tmpvalue;
		tmp >>= 1;
		
		//Select
		tmpvalue = (tmp & 1)==1 ;
		if (tmpvalue != Select ){
			sendKeyEvent(Select?ACTION_UP:ACTION_DOWN, KEYCODE_S);
		}
		Select = tmpvalue;
		tmp >>= 1;
		
		//Y
		tmpvalue = (tmp & 1)==1 ;
		if (tmpvalue != Y ){
			sendKeyEvent(Y?ACTION_UP:ACTION_DOWN, KEYCODE_Y);
		}
		Y = tmpvalue;
		tmp >>= 1;
		
		//B
		tmpvalue = (tmp & 1)==1 ;
		if (tmpvalue != B ){
			sendKeyEvent(B?ACTION_UP:ACTION_DOWN, KEYCODE_B);
		}
		B = tmpvalue;


		tmp2 >>= 4;
		
		//R
		tmpvalue = (tmp2 & 1)==1 ;
		if (tmpvalue != R){
			sendKeyEvent(R?ACTION_UP:ACTION_DOWN, KEYCODE_R);
		}
		R = tmpvalue;
		tmp2 >>= 1;
		
		//L
		tmpvalue = (tmp2 & 1)==1 ;
		if (tmpvalue != L){
			sendKeyEvent(L?ACTION_UP:ACTION_DOWN, KEYCODE_L);
		}
		L = tmpvalue;
		tmp2 >>= 1;
		
		//X
		tmpvalue = (tmp2 & 1)==1 ;
		if (tmpvalue != X){
			sendKeyEvent(X?ACTION_UP:ACTION_DOWN, KEYCODE_X);
		}
		X = tmpvalue;
		tmp2 >>= 1;
		
		//A
		tmpvalue = (tmp2 & 1)==1 ;
		if (tmpvalue != A){
			sendKeyEvent(A?ACTION_UP:ACTION_DOWN, KEYCODE_A);
		}
		A = tmpvalue;
		
	}

	/**
	 * Abort the connection.
	 * 
	 * This is a little tricky synchronization-wise: we need to be handle
	 * the case of abortion happening before the IOIO instance is created or
	 * during its creation.
	 */
	synchronized public void abort() {
		abort_ = true;
		if (ioio_ != null) {
			ioio_.disconnect();
		}
	}

}
