import java.net.*;
import java.io.*;
import java.util.*;

public class GreenChatHandler extends Thread{
	Socket sock;
	Vector<GreenChatHandler> userV;
	
	DataInputStream in;
	DataOutputStream out;
	
	String nickName;
	
	boolean isStop = false;

	public GreenChatHandler(Socket socket, Vector<GreenChatHandler> vector) {
		this.sock = socket;
		this.userV = vector;
		
		userV.add(this);
		
		try {
			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());
			
		}catch(IOException e) {
			System.out.println("Handler - 생성자 예외: "+e);
		}
		
	}
	
	public void run() {
		
		try {
			nickName = in.readUTF();
			System.out.println("##"+nickName+"님이 입장함##");
			broadcast("##["+nickName+"]님이 입장하였습니다.##");
			
			while(!isStop) {
				String cMsg = in.readUTF();
				
				System.out.println(cMsg);
				if(cMsg.startsWith("Exit#")) {//Exit#nick 
					
					String [] token = cMsg.split("#");//문자 쪼개기
					String exitNick = token[1];//닉네임
					broadcast("##["+exitNick+"]님이 퇴장하였습니다.##");//모두에게 출력
					userV.remove(this);//백터에서 자기 자신 제거
					close();
					
				}else {
					broadcast(nickName+">>"+cMsg);
				}
				
			}
			
		}catch(IOException e) {
			
			System.out.println("Handler - run() 예외:"+e);
		}
		
	}
	
	//프로그램 닫기
	public void close() 
	throws IOException
	{
		isStop = true;
		if(in!=null) in.close();
		if(out!=null) out.close();
		if(sock!=null) sock.close();
	}		
	
	//모든 유저에게 메시지 전송
	private synchronized void broadcast(String msg) {
		Iterator<GreenChatHandler> it = userV.iterator();
		while(it.hasNext()) {
			GreenChatHandler chat = it.next();
			
			try {
				chat.out.writeUTF(msg);
				out.flush();
				
			}catch(IOException e) {
				System.out.println("broadcast()예외"+e);
				userV.remove(this);
				
				try {
					close();
				} catch (IOException ex) {
					System.out.println("close() 얘외 "+ex);
					break;
					
				}
				
				
			}
		}//--while
		
	}//--broadcast
	
}
