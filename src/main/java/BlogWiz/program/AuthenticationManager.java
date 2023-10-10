package BlogWiz.program;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.jcraft.jsch.*;

public class AuthenticationManager {
    public Session session;

    public void connect(String hostname, int port, String username, String password) throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(username, hostname, port);

        // Set the password for authentication
        session.setPassword(password);

        // Disable strict host key checking (not recommended for production)
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        // Connect to the SSH server
        session.connect();
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
    
    public String executeShellCommand(String directory, Session session) throws JSchException {
        StringBuilder output = new StringBuilder();

        try {
            // Open an SSH channel of type "exec"
            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            // Define the recursive shell command to list all files and subdirectories
            String command = "find " + directory + " -type d -exec sh -c 'echo \"Directory: {}\"' \\; -o -type f -exec sh -c 'echo \"File: {}\"' \\;";
            
            channel.setCommand(command);

            // Get input and error streams for reading command output
            BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(channel.getErrStream()));

            // Connect the channel
            channel.connect();

            // Read command output from the input stream
            String line;
            while ((line = in.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Check for any error messages from the error stream
            while ((line = err.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Disconnect the channel
            channel.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            throw new JSchException("Error executing shell command: " + e.getMessage());
        }

        return output.toString();
    }
}
