package db_proj;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Front-end client / interpreter that interacts with the
 * database interactively via command line and allows previewing
 * images.
 *
 */
public class Client {
	// User input reading
	InputStreamReader inputStreamReader = null;
	BufferedReader stdin = null;

	// set to true when about to quit
	private boolean shouldQuit = false;

	// Currently loaded images
	private BufferedImage img = null;

	// Client connected to the Database
	private DatabaseClient dbClient = null;

	/**
	 * Main function.
	 * @param args - expects no arguments
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Client client = new Client();
		client.run();
	}

	/**
	 * Prints initial help message and configuration.
	 */
	public void startInterpreter()  {

        if(Constants.BATCH_INSERT){
            try {
                inputStreamReader = new InputStreamReader(new FileInputStream("input"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else if(Constants.BATCH_RECONSTRUCT){
            try {
                inputStreamReader = new InputStreamReader(new FileInputStream("batchReconstruct"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else{
            inputStreamReader = new InputStreamReader(System.in);
        }

		stdin = new BufferedReader(inputStreamReader);

		display("Starting client");
		try {
			String current = new java.io.File( "." ).getCanonicalPath();
			display("Current path: " + current);
		} catch(IOException e) {
		}

		printHelp();
		prompt();
	}

	/**
	 * Runs infinite loop reading commands and processing them.
	 */
	public void run() {
		startInterpreter();

		// Adapted from: http://www.javapractices.com/topic/TopicAction.do?Id=79
		String line = null;
		while (!shouldQuit) {
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

	/**
	 * Encapsulates a textual command from the prompt.
	 * Assumes command of the form: COMMAND ARG1 ARG2 ARG3
	 *
	 */
	private class CommandArgs {
		/**
		 * Command string.
		 */
		public String command = null;

		/**
		 * Arguments strings; access through getArg.
		 */
		private String[] args = null;

		/**
		 * Parses line into command and one or more arguments.
		 * @param line - lowercased, trimmed user input
		 */
		CommandArgs(String line) {
			int firstSpace = line.indexOf(' ');
			if (firstSpace != -1) {
				command = line.substring(0, firstSpace);
				args = line.substring(firstSpace + 1, line.length()).split("\\s+");
			} else {
				command = line;
				args = new String[0];
			}
		}

		/**
		 * Single access method to argument commands
		 * @param i 0-based index of the argument
		 * @return argument
		 * @throws IllegalArgumentException - if argument out of bounds
		 */
		String getArg(int i) throws IllegalArgumentException {
			if (args == null || i >= args.length) {
				throw new IllegalArgumentException("Command " + command + " requires at least " + (i + 1) + " arguments");
			}
			return args[i];
		}
		
		/**
		 * Same as get argument, but parses the output into an integer.
		 * @param i
		 * @return
		 * @throws IllegalArgumentException
		 */
		int getArgInt(int i) throws IllegalArgumentException {
			String num = getArg(i);
			return Integer.parseInt(num);
		}

		int numArgs() {
			return args.length;
		}
	};

	/**
	 * Parses raw user input and handles it.
	 * @param line raw user input
	 */
	private void handleInput(String line) {
		CommandArgs input = new CommandArgs(line);

		if (input.command.equals("q")) {
			shouldQuit = true;
		} else {
			handleCommand(input);
		}
	}

	/**
	 * Prompts user for connection info.
	 * @return resulting info
	 * @throws IOException 
	 */
	private DbConnectionInfo readConnectionInfo() throws IOException {
		DbConnectionInfo res = new DbConnectionInfo();
		prompt("Enter [host] [port] [db_name] OR [db_name] for localhost");
		String line = stdin.readLine();
		CommandArgs args = new CommandArgs(line);
		if (args.numArgs() == 0) {
			if (args.command.equals("db")){
				res.setUrl("vise3.csail.mit.edu", "5432", "zoya");
			}else{
				res.setLocalUrl(args.command);
			}
		} else {
			res.setUrl(args.command, args.getArg(0), args.getArg(1));
		}
		prompt("Enter [username]");
		line = stdin.readLine();
		res.setUserInfo(line.trim(), null);  // TODO: add password support
		return res;
	}

	/**
	 * Initializes (if not yet) database client.
	 * @throws IOException 
	 */
	private void initDbClient() throws IOException {
		if (dbClient == null) {
			dbClient = new DatabaseClient();
		}
		if (!dbClient.connected()) {
			DbConnectionInfo cInfo = readConnectionInfo();
			dbClient.connect(cInfo);
		}
		if (!dbClient.connected()) {
			throw new ConnectException("Could not connect");
		}
	}

	/**
	 * Loads images from various sources.
	 * 
	 * @param type web, db, or local
	 * @param arg appropriate for type, i.e. url, index in database, or relative/absolute file path
	 * @return loaded image
	 * @throws IOException
	 * @throws SQLException
	 */
	private BufferedImage loadImage(String type, String arg) throws IOException, SQLException {
		BufferedImage res = null;
		if (type.equals("local")) {
			res = ImageUtils.loadImage(arg);
		} else if (type.equals("web")) {
			res = ImageUtils.loadWebImage(arg);
		} else if (type.equals("db")){
			initDbClient();
			int imgId = dbClient.getImageId(arg);
			res = dbClient.getImageOriginal(imgId);
		}
		if (res == null) {
			throw new RuntimeException("Could not read img");
		}
		return res;
	}

    /**
	 * Prints help info.
	 */
	public void printHelp() {
		display("h - help");
		display("q - quit");
		display("img-load {local|db|web} [filename; relative to current path] - load or reload an image into local image variable");
		display("img-show - show loaded image");
		display("img-store [name] - stores loaded image with name, automatically patches everything");
		display("patch-load [id] - load or reload an image patch into local image variable");
		display("reconstruct [id] - reconstruct image from patches and load into local image variable" );
		display("clean [db] - clean the database.  Pass 'db' in for [db] to do foreign, anything else will be resolved as local");
		display("random-sample [number]  - randomly sample the given number of images from the database and compress them");
        display("upload-all [folder: images in this folder will all be uploaded] - load all images, but do not compress");
        display("get-sizes -get the sizes of the database, the tables");
		display("-----------------");
	}

	private void handleCommand(CommandArgs in) {
		try {
			if (in.command.equals("h")) {
				printHelp();
			} else if (in.command.equals("img-load")) {
				img = loadImage(in.getArg(0), in.getArg(1));
			} else if (in.command.equals("upload-all")) {
                String folder = in.getArg(0);
                initDbClient();
                long startTime = new Date().getTime();
                for (File file : new File(folder).listFiles()) {
                    try {
                        img = loadImage("local", file.getAbsolutePath());
                        System.out.println("Uploading file: " + file.getName());
                    } catch (RuntimeException e) {
                        System.out.println("Could not read file: " + file.getName());
                    }
                    dbClient.insertImage(img, file.getName());
                }
                System.out.println("Finished in " + (new Date().getTime() - startTime) + " ms");
            } else if (in.command.equals("img-store")) {
				initDbClient();
                long startTime = new Date().getTime();
				dbClient.storeImage(img, in.getArg(0));
                System.out.println("Finished in " + (new Date().getTime() - startTime) + " ms");
            } else if (in.command.equals("img-show")) {
				if (img != null) {
					ImageUtils.showImage(img);
				} else {
					throw new RuntimeException("No image loaded; use img-load first");
				}
			} else if (in.command.equals("patch-load")) {
				initDbClient();
				img = dbClient.getPatch(in.getArgInt(0));

				if (img == null) {
					throw new RuntimeException("Could not read patch");
				}
			} else if (in.command.equals("reconstruct")){
				initDbClient();
                long startTime = new Date().getTime();
				img = dbClient.getImageReconstructed(in.getArgInt(0));
                System.out.println("Finished in " + (new Date().getTime() - startTime) + " ms");
			} else if (in.command.equals("clean")) {
				initDbClient();
				dbClient.clean(in.getArg(0));
			} else if (in.command.equals("random-sample")) {
				initDbClient();
                List<String> files = dbClient.randomSample(Integer.parseInt(in.getArg(0)));
                for (String file : files) {
                    System.out.println("Fetching " + file);
                    img = loadImage("db", file);
                    System.out.println("Storing " + file);
                    long startTime = new Date().getTime();
                    dbClient.storeImage(img, in.getArg(0));
                    System.out.println("Finished in " + (new Date().getTime() - startTime) + " ms");
                }
            } else if(in.command.equals("get-sizes")){
                initDbClient();
                String[] sizes = dbClient.getAllSizes();
                List<String> names = new ArrayList<String>();
                names.add("patches");
                names.add("patch_pointers");
                names.add("patch_hashes");
                names.add("images");
                System.out.println("zoya database size: " + sizes[0]);
                System.out.println(names.get(0) + " table size: " + sizes[1]);
                System.out.println(names.get(1) + " table size: " + sizes[2]);
                System.out.println(names.get(2) + " table size: " + sizes[3]);
                System.out.println(names.get(3) + " table size: " + sizes[4]);

            }
            else {
				display("Error: unknown command");
			}
			display("Ok");
		} catch (Exception e) {
			display(e.getMessage());  // do not fail on error
		}
	}

	/**
	 * Print text with newline; a shorthand.
	 */
	private void display(Object aText) {
		System.out.print(aText.toString() + "\n");
		System.out.flush();
	}

	/**
	 * Print promt character; a shorthand.
	 */
	private void prompt() {
		System.out.print("> ");
		System.out.flush();
	}

	/**
	 * Prints a prompt message; a shorthand.
	 * @param message
	 */
	private void prompt(String message) {
		System.out.print(message + " > ");
		System.out.flush();
	}
}
