package temporal.web;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Streams;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * The <code>DemoClient</code> provides a simple command-line
 * client for the web services demos.  That is, this command
 * may be used to access a demo running on the web, specifying
 * properties for the web demo through the command line.  The
 * client connects to the web service version of the web demo
 * using a socket and then writes output to standard out.
 */
public class TIMEXClient extends AbstractCommand {

    private static final Properties DEFAULT_PROPERTIES = new Properties();

    TIMEXClient(String[] args) {
        super(args,DEFAULT_PROPERTIES);
    }

    /**
     * Runs the client from the command line, with arguments specified
     * as for the command demos.  Output is written to standard output.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        new TIMEXClient(args).run();
    }

    /**
     * Does the actual processing for the client, which involves
     * URL-encoding properties from the command-line, connecting
     * to a web service demo via socket, sending it the URL,
     * and reading the results and printing to standard output.
     */
    public void run() {
        InputStream clientIn = null;
        OutputStream clientOut = null;
        InputStream serverIn = null;
        OutputStream serverOut = null;
        try {
            String urlName = getArgument("serverUrl");
            URL url = new URL(urlName);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Content-type", "text/plain");
            connection.setDoOutput(true);
            serverOut = connection.getOutputStream();
            String inputFileName = getArgument("inputFile");
            if (inputFileName == null)
                clientIn = System.in;
            else
                clientIn = new FileInputStream(inputFileName);
            Streams.copy(clientIn,serverOut);
            String outputFileName = getArgument("outputFile");
            if (outputFileName == null)
                clientOut = System.out;
            else
                clientOut = new FileOutputStream(outputFileName);
            serverIn = connection.getInputStream();
            Streams.copy(serverIn,clientOut);
        } catch (Exception e) {
            System.out.println("Exception=" + e);
            e.printStackTrace(System.out);
        } finally {
            Streams.closeQuietly(serverIn);
            Streams.closeQuietly(serverOut);
            Streams.closeQuietly(clientIn);
            Streams.closeQuietly(clientOut);
        }
    }

}