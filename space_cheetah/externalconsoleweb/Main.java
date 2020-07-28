package space_cheetah.externalconsoleweb;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
	private static Process mc;
	public static void main(String[] args) throws InterruptedException, IOException
	{
		File streamout = new File("M:\\web\\Main\\externalconsole\\streamout.txt");
		File streamin = new File("M:\\web\\Main\\externalconsole\\streamin.txt");
		PrintWriter pwf = new PrintWriter(streamin);
		pwf.print("");
		pwf.close();
		Scanner streamScanner = new Scanner(streamout);
		mc = startMC();
		new ConsoleIn(mc).start();
		new MCIn().start();
		while(true)
		{
			while(streamScanner.hasNextLine())
			{
				String line = streamScanner.nextLine();
				System.out.println(line);
				sendToMC(line, mc.getOutputStream());
			}
			PrintWriter pw = new PrintWriter(streamout);
			pw.print("");
			pw.close();
			streamScanner.close();
			Thread.sleep(100);
			streamScanner = new Scanner(streamout);
			if(!mc.isAlive()) System.exit(0);
		}
	}
	private static Process startMC() throws IOException
	{
		File config = new File("start.sh");
		ArrayList<String> startArgs = new ArrayList<String>();
		try
		{
			Scanner configReader = new Scanner(config);
			while(configReader.hasNext())
			{
				startArgs.add(configReader.next());
			}
			configReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		startArgs.trimToSize();
		String[] startArgsArray = new String[startArgs.size()];
		startArgs.toArray(startArgsArray);
		System.out.println("[ExternalConsole] Starting server");
		ProcessBuilder pb = new ProcessBuilder(startArgsArray);
		return pb.start();
	}
	private static void sendToWeb(String message) throws IOException
	{
		FileWriter filein = new FileWriter("M:\\web\\Main\\externalconsole\\streamin.txt", true);
		PrintWriter printin = new PrintWriter(filein);
		printin.println(message);
		printin.close();
		filein.close();
	}
	private static void sendToMC(String message, OutputStream os) throws IOException
	{
		os.write((message + "\n").getBytes());
		os.flush();
	}
	private static class MCIn extends Thread
	{
		@Override
		public void run()
		{
			Scanner mcConsole = new Scanner(mc.getInputStream());
			while(mcConsole.hasNextLine())
			{
				try
				{
					String message = mcConsole.nextLine();
					sendToWeb(message);
					System.out.println(message);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			mcConsole.close();
		}
	}
	private static class ConsoleIn extends Thread
	{
		Process mc;
		public ConsoleIn(Process mc)
		{
			this.mc = mc;
		}
		@Override
		public void run()
		{
			Scanner console = new Scanner(System.in);
			while(console.hasNextLine())
			{
				try
				{
					String message = console.nextLine();
					sendToWeb(message);
					sendToMC(message, mc.getOutputStream());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			console.close();
		}
	}
}
