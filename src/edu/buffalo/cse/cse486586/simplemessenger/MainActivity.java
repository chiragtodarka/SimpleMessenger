package edu.buffalo.cse.cse486586.simplemessenger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	
	static final String TAG = "MainActivityTag";
	static String tcpString;
	static int port;
	static String number;
	static String chatHistory = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TelephonyManager tel = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        number = tel.getLine1Number();
        Log.v(TAG, number);
        
        EditText editText = (EditText) findViewById(R.id.editText1);
        editText.setOnEditorActionListener(new OnEditorActionListener() 
        {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) 
			{
				if(arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER)
				{
					send(arg0);
				}
				return false;
			}
        	
        });
        
        new Thread(new Runnable() 
        {
            public void run() 
            {
                try
                {
                	ServerSocket serverSocket = new ServerSocket(10000);
                	
                	while(true)
                	{
                		Socket serverClientSocket = serverSocket.accept();
                    	ObjectInputStream ois = new ObjectInputStream(serverClientSocket.getInputStream());
                		final String messageFromClient = (String)ois.readObject();
                		final TextView textView = (TextView) findViewById(R.id.textView1);
                		
                		chatHistory = chatHistory + "Friend: " + messageFromClient + "\n";
                		
                		textView.post(new Runnable() {
                            public void run() {
                            	textView.setText(chatHistory);
                            }
                        });
                	}
                }
                catch (Exception e) 
                {
					//System.out.println(e.getMessage());
                	Log.v("Error1", e.toString());
				}
                
            }
        }).start();
        
        Log.v("ServerStarted", "and waiting for input");
        
	}
	
	public void send(View view)
	{
		//Code taken from https://docs.google.com/document/d/1JqBqZChFWzbnTXgbYB8Z6l9IRlzt8c0loIwa6ymkPps/pub
		TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		//end
		
		Log.v("PortStr", portStr);
		
		tcpString="5554";
		port=11112;
		if(portStr.equals("5554"))
		{
			tcpString = "10.0.2.2";
			port = 11112;
		}
		else if (portStr.equals("5556"))
		{
			tcpString = "10.0.2.2";
			port = 11108;
		}
		
		final EditText editText = (EditText) findViewById(R.id.editText1);
		String messageForServer = editText.getText().toString();
		messageForServer = messageForServer.replace("\n", "");
		messageForServer = messageForServer.replace("\r", "");
		messageForServer = messageForServer.trim();
		
		if(!messageForServer.trim().equals(""))
		{
			if(messageForServer.length()<129)
			{
				Log.v("messageForServer", messageForServer);
				new SendMessage().execute(messageForServer, chatHistory);
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Message Length cant exceed 128 characters", Toast.LENGTH_SHORT).show();
			}
			
			editText.setText("");
			editText.setHint("Enter message");
			TextView textView = (TextView) findViewById(R.id.textView1);
	        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
		}	
	}
	
	private class SendMessage extends AsyncTask<String, Void, String> 
	{
	   	    protected String doInBackground(String... messageForServer) 
	   	    {
	   	    	if(port!=0)
				{
	   	    		Log.v("in if(port)", "in if(port)");
					try
					{
						Socket clientSocket = new Socket(tcpString, port);
						Log.v("ConnectionEstablished", "ConnectionEstablished");
						ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
						String message = new String(messageForServer[0]+"");
						objectOutputStream.writeObject(message);
						//objectOutputStream.close();
					}
					catch (Exception e) 
					{
						Log.v("Error2", e.toString());
					}	
				}
	   	    	return messageForServer[0];
	   	    }
	   	    
	   	    protected void onPostExecute(String messageForServer)
	   	    {
	   	    	TextView textView = (TextView) findViewById(R.id.textView1);
        		chatHistory = chatHistory + "Me: " + messageForServer + "\n";
        		textView.setText(chatHistory);
	   	    }
	}
	    	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
