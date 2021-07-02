import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.net.*;
import java.io.*;

public class GreenChatGui extends JFrame 
implements Runnable
{
	//서버
	Socket sock;
	final int port = 12345; 
	DataInputStream in;
	DataOutputStream out;
	String host, nick;
	
	Thread listener;
	boolean isStop = false;

	//상수
	public static final int INIT = 0;
	public static final int EXIT = -1;
	
	JPanel pC = new JPanel(new BorderLayout());
	
	//로그인, 채팅창 텝
	JTabbedPane tabP = new JTabbedPane();
	
	//폰트
	Font font = new Font("궁서체", Font.BOLD, 25);
	
	//로그인
	JPanel pLogin = new JPanel(new GridLayout(0,2,50,100));//로그인 패널
	JLabel lbLoginHost = new JLabel("호스트", JLabel.CENTER);
	JLabel lbLoginNick = new JLabel("닉네임", JLabel.CENTER);
	
	JTextField tfHost = new JTextField("127.0.0.1",20);//호스트 텍스트필드
	JTextField tfNick = new JTextField("닉네임",20);//닉네임 텍스트필드
	JButton btCon = new JButton("입장");//입장
	JButton btReset = new JButton("다시쓰기");//다시쓰기
	
	//--채팅방
	JPanel pChat = new JPanel(new BorderLayout());
	
	//단어 패널
	JPanel chatPanelpN = new JPanel(new FlowLayout(0));
	JLabel lbTime = new JLabel("GreenChat");//단어 라벨
	
	//채팅 패널
	JPanel chatPanelpC = new JPanel(new BorderLayout());
	JTextArea taChat  = new JTextArea();//채팅창
	JTextField tfInput = new JTextField("채팅을 입력하세요",20);//채팅 입력창
	
	//우측 정보창
	JPanel pE = new JPanel(new GridLayout(2,1));
	JPanel pInfo = new JPanel();
	JPanel pInfoBack = new JPanel(new BorderLayout());
	JPanel pInfoLb = new JPanel(new GridLayout(2,2));
	JPanel pInfoBt = new JPanel(new GridLayout(1,0));
	
	//정보창 내정보
	JLabel jLabel6 = new JLabel("호스트", JLabel.CENTER);
	JLabel lbHost = new JLabel();
	JLabel lbId = new JLabel("닉네임", JLabel.CENTER);
	JLabel lbNick = new JLabel();
	
	//정보창 버튼
	JButton btLogout = new JButton("로그아웃");
	JButton btExit = new JButton("종료");

	public GreenChatGui() {
		super(":::GreenChatGui:::");
		Container cp = this.getContentPane();
		
		//센터 패널
		cp.add(pC, "Center");
		
		//로그인, 채팅방, 테마색
		pC.add(tabP, "Center");
		tabP.addTab("로그인", pLogin);
		tabP.addTab("채팅방", pChat);
		pC.setBackground(new Color(247, 237, 219));
		
		//로그인 페이지
		pLogin.add(lbLoginHost);
		lbLoginHost.setFont(font);
		pLogin.add(tfHost);
		pLogin.add(lbLoginNick);
		lbLoginNick.setFont(font);
		pLogin.add(tfNick);
		pLogin.add(btCon);
		pLogin.add(btReset);
		pLogin.setBackground(Color.white);
		pLogin.setBorder(new EmptyBorder(100, 80, 100, 80));
		
		
		//우측 정보창
		cp.add(pE, "East");
		
		//우측 정보창 하단
		pE.add(pInfo, "Center");
		pInfo.setBackground(Color.white);
		pE.add(pInfoBack, "South");
		
		//우측 정보창 내정보
		pInfoBack.add(pInfoLb, "Center");
		pInfoLb.setBorder(BorderFactory.createTitledBorder("##내 정보##"));
		pInfoLb.setBackground(new Color(152, 187, 147));
		pInfoLb.add(jLabel6);
		pInfoLb.add(lbHost);
		pInfoLb.add(lbId);
		pInfoLb.add(lbNick);
		
		//우측 정보창 버튼
		pInfoBack.add(pInfoBt, "South");
		pInfoBt.add(btLogout);
		btLogout.setEnabled(false);
		pInfoBt.add(btExit);
		btLogout.setPreferredSize(new Dimension(150, 40));
		btExit.setPreferredSize(new Dimension(150, 40));
		
		
		//----------------------------채팅방
		
		pChat.add(chatPanelpN, "North");
		
		//단어창
		chatPanelpN.add(lbTime);
		lbTime.setFont(font);
		chatPanelpN.setBackground(new Color(152, 187, 147));
		
		//채팅창
		pChat.add(chatPanelpC, "Center");
		chatPanelpC.add(new JScrollPane(taChat), "Center");
		chatPanelpC.add(tfInput, "South");
		chatPanelpC.setBackground(new Color(152, 187, 147));
		chatPanelpC.setPreferredSize(new Dimension(250, ABORT));
		chatPanelpC.setBorder(new EmptyBorder(0, 5, 0, 5));
		
		
		//핸들러
		MyHandler handler = new MyHandler();
		btCon.addActionListener(handler);//입장 버튼
		btReset.addActionListener(handler);//다시쓰기 버튼
		btLogout.addActionListener(handler);//로그아웃 버튼
		btExit.addActionListener(handler);//종료 버튼
		tfInput.addActionListener(handler);//채팅입력창

	}//--생성자
	
	@Override
	public void run() {
		
		try {
			sock = new Socket(host, port);
			taChat.append("\nConnected to "+host+": 12345\n");
			out = new DataOutputStream(sock.getOutputStream());
			in = new DataInputStream(sock.getInputStream());
			
			//닉네임을 서버쪽에 보내기
			out.writeUTF(nick);
			out.flush();
			
			//서버가 보내오는 메시지를 듣는 메소드
			listen();
			
		}catch(IOException e) {
			System.out.println("클의 run() 예외"+e+"\n");
			taChat.append("\n서버 접속 또는 듣는 중 예외 발생"+e+"\n");
			
			try {
				close();
			} catch (IOException e1) {
				
				System.out.println("오류발생: "+e.getMessage());
			}
		}
		
	}//--run() 끝
	
	//로그인
	public void chatLogin() {
		host = tfHost.getText();
		nick = tfNick.getText();
		
		//유효성 검사
		if(nick==null || host==null || nick.trim().isEmpty() 
				|| host.trim().isEmpty()) 
		{
			showMsg("호스트명, 닉네임 모두 입력해야 해요");
			return;
			
		}
		
     	//내 정보 등록
		lbHost.setText(host);
		lbNick.setText(nick);
		isStop = false;
		
		//스레드 실행
		if(listener==null) {
			
			listener = new Thread(this);
			listener.start();
			this.setEnabled(EXIT);
			
		}
		
	}//--chatLogin() 끝
	
	//나가기
	public void chatExit() {
		//로그아웃 확인
		int yn = JOptionPane.showConfirmDialog(this, "로그아웃할까요??",
				"로그아웃 확인", JOptionPane.YES_NO_OPTION);
		
		if(yn==JOptionPane.NO_OPTION) return;
		
		//서버쪽에 로그아웃 메시지
		try {
			out.writeUTF("Exit#"+nick);
			out.flush();
			
			//스레드 중지
			isStop = true;
			listener = null;
			
			//버튼 활성화 여부
			setEnabled(INIT);
			close();
			
		}catch(IOException e) {
			
			System.out.println("로그아웃 중 예외: "+e.getMessage());
		}
		
	}//--chatExit() 끝

	//메시지 듣고 출력
	private void listen() 
	throws IOException
	{
		while(!isStop) {
			String serMsg = in.readUTF();
			taChat.append(serMsg+"\n");
			
			//커서위치를 채팅창 끝으로
			String txt = taChat.getText();
			taChat.setCaretPosition(txt.length()-1);
			
		}
	}//--listen() 끝
	
	//메시지 보내기
	private void sendMessage(String myMsg) {
		try {
			out.writeUTF(myMsg);
			out.flush();
			
		}catch(IOException e) {
			System.out.println("sendMessage()예외: "+e);
		}
		
	}//--sendMessage() 끝
	
	//경고 메시지
	public void showMsg(String str) {
		
		JOptionPane.showMessageDialog(this, str);
		
	}//--showMsg() 끝
	
	//SetEnable
	private void setEnabled(int mode) {
		if(mode==INIT) {
			btCon.setEnabled(true);
			btLogout.setEnabled(false);
			
		}else if(mode==EXIT) {
			btCon.setEnabled(false);
			btLogout.setEnabled(true);
			
		}
		
	}//--SetEnable() 끝
	
	//닫아주기
	private void close()
	throws IOException
	{
		isStop = true;
		if(in!=null) in.close();
		if(out!=null) out.close();
		if(sock!=null) sock.close();		
		
	}//--close() 끝
	
	//핸들러
	public class MyHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object obj = e.getSource();
			
			//연결
			if(obj==btCon) {
				chatLogin();
				
			//로그아웃	
			}else if(obj==btLogout) {
				chatExit();
				
			//채팅입력	
			}else if(obj==tfInput) {
				String myMsg = tfInput.getText();
				sendMessage(myMsg);
				tfInput.setText("");
				
			//다시쓰기
			}else if(obj==btReset) {
				tfHost.setText("");
				tfNick.setText("");
				tfHost.requestFocus();
			
			//종료하기
			}else if(obj==btExit) {
				System.exit(EXIT);
				try {
					close();
				} catch (IOException ex) {
					
					System.out.println("종료중 예외 발생 "+ex);
				}
			}
			
		}
		
	}//--핸들러 끝
	
	public static void main(String[] args) {
		GreenChatGui my = new GreenChatGui();
		my.setSize(750, 600);
		my.setVisible(true);
	}

}