package src.com.stormcolor;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Enumeration; 


public class Serial {
	private Modeler modeler;
	public InputStream in;
	public OutputStream out;
	
	public Serial(Modeler modeler) {
        super();	
		this.modeler = modeler;
    }
    
	public static void list() {  
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();  
          
        while(ports.hasMoreElements())  
            System.out.println(((CommPortIdentifier)ports.nextElement()).getName());  
    }  
	
    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() ) {
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort ) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
                
                (new Thread(new SerialReader(in, modeler))).start();

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    public void sendSerial(String msg) throws IOException{
		try{
		    out.write(msg.getBytes());
		    out.flush();
	    }catch(Exception e){
	        e.printStackTrace();
	    }
    }
    
    /** */
    public static class SerialReader implements Runnable {    	
        InputStream in;
        private Modeler modeler;
        
        public SerialReader (InputStream in, Modeler modeler) {
            this.in = in;
    		this.modeler = modeler;
        }
        
        public void run () {
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                while ( ( len = this.in.read(buffer)) > -1 ) {
                    //System.out.print(new String(buffer,0,len));
                    
                    // call to modeler
                    modeler.machine.onSerialReceived(new String(buffer,0,len));
                }
            } catch ( IOException e ) {
                e.printStackTrace();
            }            
        }
    }
    
    
}
