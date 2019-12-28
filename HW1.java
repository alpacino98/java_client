import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * This class communicates with a server. It gives the server username and passcode than takes
 * 3 consecutive images from it. Than is saves these images to the computer and asks the user
 * for their labels respectively. If the labels are given correctly, this is repeated until
 * EXIT command is given by the user.
 *
 * @authors  Emre Dönmez, Abidin Alp Kumbasar
 * @version 1.0
 * @since   07-11-2019
 */
public class ImageLabeler {

	public static void main(String[] args) throws IOException {
		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		try {
			Socket socket = new Socket(ip, port);

			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			InputStreamReader in = new InputStreamReader(socket.getInputStream());
			BufferedReader bf = new BufferedReader(in);
			DataInputStream dIn = new DataInputStream(socket.getInputStream());
			Scanner keyboard = new Scanner(System.in);

			//String user = "USER bilkentstu";
			System.out.println("Enter username: ");
			String user = keyboard.nextLine();
			String sender = "USER " + user;
			sendCommand(sender, output);
			String res = bf.readLine();
			System.out.println(res);

			//String pass = "PASS cs421f2019";
			System.out.println("Enter password: ");
			String pass = keyboard.nextLine();
			sender = "PASS " + pass;
			sendCommand(sender, output);
			res = bf.readLine();
			System.out.println(res);

			// Below code downloads the images and asks for the labels to the user.
			// User should check the images and write labels.
			// Iterates until EXIT command.
			boolean exit = false;
			int k = 1;
			while(!exit) {
				Boolean boo = false;
				String iget = "IGET";
				System.out.println("Ask for images? YES or EXIT");
				String answer = keyboard.nextLine();
				if(answer.equals("YES")) {
					sendCommand(iget, output);
					downloader(dIn, output);
					while(boo == false) {
						System.out.println("The images have been downloaded. Enter their labels pelase: ");
						String label = "ILBL ";
						System.out.println("First image: ");
						label = label + keyboard.nextLine();
						label = label + ",";
						System.out.println("Second image: ");
						label = label + keyboard.nextLine();
						label = label + ",";
						System.out.println("Third image: ");
						label = label + keyboard.nextLine();

						System.out.println(label);
						sendCommand(label, output);
						res = bf.readLine();
						System.out.println(res);
						if(res.equals("OK")) {
							boo = true;
							System.out.println("SUCCESS");
							System.out.println("Iteration " + String.valueOf(k) + " has been finished.");
						}
						else {
							boo = false;
							System.out.println("FAIL");
						}
					}
				}
				else if (answer.equals("EXIT")) {
					exit = true;
				}
				k++;
			}
			socket.close();
			keyboard.close();
		}
		catch(Exception e) {
			System.out.println("Something went wrong...");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * This method takes the 3 bytes that correspond to size of the sent image
	 * and turns it into integer.
	 *
	 * @param  str is the ISND message followed by size of image.
	 * @return size is the size of the image.
	 */
	public static int size_founder(byte[] str) {
		byte b1 = str[4];
		byte b2 = str[5];
		byte b3 = str[6];

		int r = ((b1 & 0xF) << 16) | ((b2 & 0xFF) << 8) | (b3 & 0xFF);
		int size = r;
		return size;
	}

	/**
	 * This method sends commands to the server.
	 *
	 * @param  com is the message to be sent.
	 * @param  out is the output stream for the message to be sent from.
	 */
	public static void sendCommand(String com, DataOutputStream out) throws IOException {
		String str = com + "\r\n";
		char[] letters = str.toCharArray();
		for (char ch : letters) {
			out.writeByte((byte) ch);
		}
	}

	/**
	 * This method saves the desired image byte array to the computer as jpg.
	 *
	 * @param  img is the image to be created in byte array form.
	 * @param  count is the image number.
	 */
	public static void img_creator(byte[] img, int count) throws IOException {
		String file = "output" + String.valueOf(count + 1) + ".jpg";
		try(FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(img);
			fos.close();
		}
	}

	/**
	 * This method uses img_creator method to save 3 consecutive images to the computer
	 * as jpg which it takes from the server in byte array format.
	 *
	 * @param  dIn is the data input stream that collects the bytes of the image.
	 * @param  output is the communication with server.
	 */
	public static void downloader(DataInputStream dIn, DataOutputStream output) throws IOException {
		int count = 0;
		String iget;
		while(count < 3) {
			int length = 7;
			byte[] message = new byte[length];                   // read length of incoming message
			if(length>0) {
				dIn.readFully(message, 0, length); // read the message
			}

			while(message[0] != 'I' && message[1] != 'S' && message[2] != 'N' && message[3] != 'D') {
				iget = "IGET";
				sendCommand(iget, output);
				dIn.readFully(message, 0, length);
			}

			if(message[0] == 'I' && message[1] == 'S' && message[2] == 'N' && message[3] == 'D') {
				System.out.println();
			}
			else {
				System.out.println("ISND message not found.");
			}
			int size = size_founder(message);

			if(count == 0) {
				byte[] image1 = new byte[size];
				dIn.readFully(image1, 0, size);
				img_creator(image1, count);
			}
			else if(count == 1) {
				byte[] image2 = new byte[size];
				dIn.readFully(image2, 0, size);
				img_creator(image2, count);
			}
			else {
				byte[] image3 = new byte[size];
				dIn.readFully(image3, 0, size);
				img_creator(image3, count);
			}
			count++;
		}
	}
}
