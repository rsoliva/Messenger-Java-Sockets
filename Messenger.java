import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Messenger{
	static FromStd fromStd;
	static Thread fStd;

	static Echo echo;
	static Thread e;

	public static void main(String[] args){

		if ( args.length == 0 )	{
			return;
		}

		if (args[0].compareTo("-l") == 0){
			runAsServer(args);
		}
		else{
			runAsClient(args);
		}

	}

	public static void runAsServer(String[] args){

		try	{
			int port_number= Integer.valueOf( args[1] );
			ServerSocket server_socket= new ServerSocket( port_number );

			Socket client_socket= server_socket.accept();

			server_socket.close();

			fromStd = new FromStd(client_socket);
			fStd = new Thread(fromStd);

			echo = new Echo(client_socket);
			e = new Thread(echo);

			fStd.start();
			e.start();

			fStd.join();
			e.join();
			
		}
		catch ( Exception e ){
			System.out.println( e.getMessage() );
		}

	}

	public static void runAsClient(String[] args){

		try	{
			int port_number= Integer.valueOf( args[0] );
			
			Socket client_socket= new Socket( "localhost", port_number );

			fromStd = new FromStd(client_socket);
			fStd = new Thread(fromStd);

			echo = new Echo(client_socket);
			e = new Thread(echo);

			fStd.start();
			e.start();

			e.join();
			//fStd.stop();
			fStd.join();
			
		}
		catch ( Exception e ){
			System.out.println( "Error: " + e.getMessage() );
		}

	}

}

class FromStd implements Runnable{

	BufferedReader stdInput;
	PrintWriter output;
	String source = "temp";
	Socket client_socket;
	public FromStd(Socket client_socket){
		this.client_socket = client_socket;
		try{
			//handles stdInput
			stdInput= new BufferedReader(new InputStreamReader(System.in));
			//outputs to other
			output = new PrintWriter( client_socket.getOutputStream(), true );
		}
		catch (Exception e){
			System.out.println("Error: " + e.getMessage());
		}
		
	}

	@Override 
	public void run(){
		try{
			source = stdInput.readLine();
			while(!client_socket.isClosed()){
				output.println(source);
				if(source.equalsIgnoreCase("NULL")){
					break;
				}
				source = stdInput.readLine();
			}
		
			//client_socket.close();
			stdInput.close();
			output.close();
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
}

class Echo implements Runnable{

	BufferedReader input;
	String source = "temp";
	Socket client_socket;
	public Echo(Socket client_socket){
		this.client_socket = client_socket;
		try{
			//handles input from other
			input= new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
		}
		catch (Exception e){
		}
		
	}

	@Override 
	public void run(){
		try{
			while(!client_socket.isClosed()){
				source = input.readLine();
				if(source.equals("null")){
					break;
				}
				System.out.println(source);
			}
			client_socket.shutdownInput();
			client_socket.shutdownOutput();
			//client_socket.close();
		}
		catch(Exception e){}
		try{
			client_socket.close();
		}
		catch(IOException e){}
		
	}
}