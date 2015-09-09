package br.inf.ufes.lprm.example;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.iso.mpeg.mpegv._2010.iidl.InteractionInfoType;
import org.iso.mpeg.mpegv._2010.siv.AccelerationSensorType;

public class SenaMove extends JFrame {

	private static final long serialVersionUID = 8714913730963411424L;

	private JLabel lblNumber1 = new JLabel("");
	private JLabel lblNumber2 = new JLabel("");
	private JLabel lblNumber3 = new JLabel("");
	private JLabel lblNumber4 = new JLabel("");
	private JLabel lblNumber5 = new JLabel("");
	private JLabel lblNumber6 = new JLabel("");

	public SenaMove() {
		super("SenaMove");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 560, 100);
		getContentPane().setLayout(null);
		lblNumber1.setForeground(Color.RED);
		lblNumber1.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNumber1.setBounds(42, 25, 56, 16);
		getContentPane().add(lblNumber1);

		lblNumber2.setForeground(Color.BLUE);
		lblNumber2.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNumber2.setBounds(133, 25, 56, 16);
		getContentPane().add(lblNumber2);

		lblNumber3.setForeground(Color.CYAN);
		lblNumber3.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNumber3.setBounds(226, 25, 56, 16);

		getContentPane().add(lblNumber3);
		lblNumber4.setForeground(Color.MAGENTA);
		lblNumber4.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNumber4.setBounds(313, 25, 56, 16);
		getContentPane().add(lblNumber4);

		lblNumber5.setForeground(Color.GREEN);
		lblNumber5.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNumber5.setBounds(396, 25, 56, 16);
		getContentPane().add(lblNumber5);

		lblNumber6.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNumber6.setBounds(480, 25, 56, 16);
		getContentPane().add(lblNumber6);

		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	private void getRandomNumber() {
		ArrayList numbersGenerated = new ArrayList();
		for (int i = 0; i < 6; i++) {
			Random randNumber = new Random();
			int iNumber = randNumber.nextInt(60) + 1;

			if(!numbersGenerated.contains(iNumber)) {
				numbersGenerated.add(iNumber);
			} else {
				i--;
			}
		}
		lblNumber1.setText(""+numbersGenerated.get(0));
		lblNumber2.setText(""+numbersGenerated.get(1));
		lblNumber3.setText(""+numbersGenerated.get(2));
		lblNumber4.setText(""+numbersGenerated.get(3));
		lblNumber5.setText(""+numbersGenerated.get(4));
		lblNumber6.setText(""+numbersGenerated.get(5));
	}

	private long lastUpdate = 0;
	private float lastX, lastY, lastZ;
	private static final int SHAKE_THRESHOLD = 600;

	public static InetAddress ipHostAddress = null;
	public static void main(String[] args) {

		List<Inet4Address> ipv4Interfaces = new ArrayList<Inet4Address>();
		Enumeration interfacesList = null;
		try {
			interfacesList = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException s) {
			System.err.println("*Error to obtain network interfaces.");
			s.printStackTrace();
			System.exit(0);
		}
		while(interfacesList.hasMoreElements()){
			NetworkInterface ni = (NetworkInterface)interfacesList.nextElement();
			Enumeration ee = ni.getInetAddresses();
			while(ee.hasMoreElements()) {
				try {
					Inet4Address ia = (Inet4Address)ee.nextElement();
					if (!ia.isLoopbackAddress())
						ipv4Interfaces.add(ia);
				}
				catch (Exception e){}
			}
		}

		if (ipv4Interfaces.size() > 1){
			System.out.println("*More than one interface was detected.");
			int i = 1;
			for (Inet4Address inet4Address : ipv4Interfaces) {
				System.out.println(i + " - " + inet4Address.getHostAddress());
				i++;
			}

			System.out.println("*Enter number of interface:");
			BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
			int option = 99;
			while (option >= i || option < 1 ){
				try {
					option = Integer.parseInt(buffer.readLine());
				} catch (Exception e) {
					System.err.println("*Error: invalid number.");
				}
				if (option >= 1 && option < i)
					ipHostAddress = ipv4Interfaces.get(option-1);
				else
					System.out.println(">>> Invalid option.");
			}
		}

		if (ipHostAddress == null)
			ipHostAddress = ipv4Interfaces.get(0);

		SenaMove senaMove = new SenaMove();

		JAXBContext jaxbContext;
		Unmarshaller jaxbUnmarshaller = null;
		try {
			jaxbContext = JAXBContext.newInstance(InteractionInfoType.class);
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		DatagramSocket serverSocket;
		try {
			serverSocket = new DatagramSocket(12345,ipHostAddress);
			System.out.println("SERVER OPENED ON "+ipHostAddress.getHostAddress());
			byte[] receiveData = new byte[1024];
			while (true) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String message = new String(receivePacket.getData()).trim();
				System.out.println(new Date().getTime() + " RECEIVED: " + message);
				if (!message.startsWith("ping")){
					message = message.substring(0, message.lastIndexOf("</InteractionInfo>") + 18);
					StringReader reader = new StringReader(message);
					try {
						JAXBElement<InteractionInfoType> jii = jaxbUnmarshaller.unmarshal(new StreamSource(reader), InteractionInfoType.class);
						InteractionInfoType ii = jii.getValue();
						if (ii.getSensedInfoList() != null && ii.getSensedInfoList().getSensedInfo() != null){
							AccelerationSensorType si = (AccelerationSensorType)ii.getSensedInfoList().getSensedInfo().get(0);
							long curTime = System.currentTimeMillis();
							if ((curTime - senaMove.lastUpdate) > 100) {
								long diffTime = (curTime - senaMove.lastUpdate);
								senaMove.lastUpdate = curTime;
								float speed = Math.abs(si.getAcceleration().getX() + si.getAcceleration().getY() + si.getAcceleration().getZ() - senaMove.lastX - senaMove.lastY - senaMove.lastZ)/ diffTime * 10000;
								if (speed > SHAKE_THRESHOLD) {
									senaMove.getRandomNumber();
								}
								senaMove.lastX = si.getAcceleration().getX();
								senaMove.lastY = si.getAcceleration().getY();
								senaMove.lastZ = si.getAcceleration().getZ();
							}
						}
					} catch (JAXBException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}  
	}  
}
