package db_proj;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Client {
	private boolean shouldQuit = false;
	private Image img = null;

	public static void main(String[] args) {
		Client client = new Client();
		client.run();
	}
	
	public void startClient() {
		display("Starting client");
		try {
			String current = new java.io.File( "." ).getCanonicalPath();
			display("Current path: " + current);
		} catch(IOException e) {
		}
		
		printHelp();
		prompt();
	}

	// Adapted from: http://www.javapractices.com/topic/TopicAction.do?Id=79
	public void run() {
		startClient();

		InputStreamReader inputStreamReader = new InputStreamReader(System.in);
		BufferedReader stdin = new BufferedReader(inputStreamReader);
		String line = null;
		while(!shouldQuit) {
			try {
				line = stdin.readLine();
				handleInput(line.trim().toLowerCase());
				prompt();
			} catch (IOException ex) {
				System.err.println(ex);
			}
		}
		
		display("Quitting client");
		try {
			stdin.close();
		} catch (IOException ex){
			System.err.println(ex);
		}
	}
	
	
	private void handleInput(String line) {
		String command = null;
		String args = null;
		int firstSpace = line.indexOf(' ');
		if (firstSpace != -1) {
			command = line.substring(0, firstSpace);
			args = line.substring(firstSpace + 1, line.length()).trim();
		} else {
			command = line;
			args = new String();
		}
		
		display("Command: " + command + "; args: " + args);
		if (line.equals("q")) {
			shouldQuit = true;
		} else {
			handleCommand(command, args);
		}
	}
	
	public void printHelp() {
		display("h - help");
		display("q - quit");
		display("img-load [filename; relative to current path] - load or reload an image into local image variable");
		display("img-show - show loaded image");
		display("-----------------");
	}
	
	// http://jdbc.postgresql.org/documentation/80/binary-data.html
	
	private void handleCommand(String command, String args) {
		if (command.equals("h")) {
			printHelp();
		} else if (command.equals("img-load")) {
			img = ImageUtils.loadImage(args);
			display(img == null ? "Could not read image" : "Ok");
		} else if (command.equals("img-show")) {
			if (img != null) {
				ImageUtils.showImage(img);
			} else {
				display("No image loaded; use img-load first");
			}
		} else {
			display("Error: unknown command");
		}
		
	}

	/**
	 * Display some text to stdout.
	 * The result of toString() is used.
	 */
	private void display(Object aText) {
		System.out.print(aText.toString() + "\n");
		System.out.flush();
	}
	
	private void prompt() {
		System.out.print("> ");
		System.out.flush();
	}
}
