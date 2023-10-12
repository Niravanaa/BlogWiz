package BlogWiz.program.service;

import BlogWiz.program.AuthenticationManager;
import com.jcraft.jsch.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class FileService {
    private final AuthenticationManager authManager;

    public FileService(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    // Read the content of a file by its name
    public String getFileContent(String fileName) throws JSchException, IOException, SftpException {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) authManager.session.openChannel("sftp");
            channel.connect();
            InputStream inputStream = channel.get(fileName);
            return readInputStream(inputStream);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    // Write content to a file by its name
    public void writeFile(String fileName, String content) throws JSchException, IOException, SftpException {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) authManager.session.openChannel("sftp");
            channel.connect();
            OutputStream outputStream = channel.put(fileName);
            writeOutputStream(outputStream, content);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    public void deleteFile(String fileName, boolean isDirectory) throws JSchException, SftpException {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) authManager.session.openChannel("sftp");
            channel.connect();

            if (isDirectory) {
                deleteDirectory(channel, fileName);
            } else {
                // If it's a file, delete it directly
                channel.rm(fileName);
            }
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    private void deleteDirectory(ChannelSftp channel, String dirPath) throws SftpException {
        Vector<ChannelSftp.LsEntry> entries = channel.ls(dirPath);
        for (ChannelSftp.LsEntry entry : entries) {
            String entryName = entry.getFilename();
            if (!entryName.equals(".") && !entryName.equals("..")) {
                String entryPath = dirPath + "/" + entryName;
                SftpATTRS attrs = channel.lstat(entryPath);
                if (attrs.isDir()) {
                    // Recursively delete subdirectory
                    deleteDirectory(channel, entryPath);
                } else {
                    // Delete file
                    channel.rm(entryPath);
                }
            }
        }
        // After deleting all contents, remove the directory itself
        channel.rmdir(dirPath);
    }



    // List files and directories in a directory
    public List<String> listFiles(String directory) throws JSchException, SftpException {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) authManager.session.openChannel("sftp");
            channel.connect();

            Vector<ChannelSftp.LsEntry> entries = new Vector<>();
            List<String> fileList = new ArrayList<>();

            for (ChannelSftp.LsEntry entry : entries) {
                String name = entry.getFilename();
                if (!".".equals(name) && !"..".equals(name)) {
                    fileList.add(name);
                }
            }

            return fileList;
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    public void createFile(String fileName, Boolean isFile) throws JSchException, SftpException {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) authManager.session.openChannel("sftp");
            channel.connect();

            if (isFile)
            {
            	channel.put(new ByteArrayInputStream(new byte[0]), fileName);
				
				// Set public "read" permission (644)
				int permission = 0644;  // Octal representation
				channel.chmod(permission, fileName);
            }
            
            else
            {
            	// Create the folder
                channel.mkdir(fileName);
            }
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
    
    // Update the content of an existing file
    public void updateFile(String fileName, String newContent) throws JSchException, IOException, SftpException {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) authManager.session.openChannel("sftp");
            channel.connect();

            // Write the updated content back to the file
            OutputStream outputStream = channel.put(fileName);
            writeOutputStream(outputStream, newContent);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    // Helper method to read input stream into a string
    private String readInputStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    // Helper method to write content to an output stream
    private void writeOutputStream(OutputStream outputStream, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(content);
        }
    }
}

