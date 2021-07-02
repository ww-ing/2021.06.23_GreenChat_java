import java.net.*;
import java.io.*;
import java.util.*;

public class GreenChatServer extends Thread {
	
	Vector<GreenChatHandler> vector = new Vector<>();
	final int port = 12345;
	ServerSocket server;

	public GreenChatServer() {
		try {
			server = new ServerSocket(port);
			System.out.println("##서버 시작됨["+port+"번 에서 대기중...##");
			this.start();
			
		}catch(IOException e) {
			System.out.println("##서버 시작 중 예외 에러 발생: "+e.getMessage()+"##");
		}
		
	}//--생성자
	
	public void run() {
		while(true) {
			try {
				Socket sock = server.accept();
				System.out.println("##["+sock.getInetAddress()+"]님이 접속##");
				GreenChatHandler chatThread = new GreenChatHandler(sock, vector);
				chatThread.start();
				
			}catch(IOException e) {
				System.out.println("##연결 실패 :"+e+"##");
			}
		}
		
	} 

	public static void main(String[] args) {
		
		new GreenChatServer();
		
	}

}
