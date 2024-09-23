package com.starryassociates.sftp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SftpService {

    private static final Logger logger = Logger.getLogger(SftpService.class.getName());

    private static final String HHS_SFTP_SECRET_NAME = "HHS_SFTP_SECRET";
    private static final String CITI_SFTP_SECRET_NAME = "CITI_SFTP_SECRET";

    public String performSftpOperation(String clientName, String operationType, String fileName) {
        try {
            if ("upload".equals(operationType)) {
                uploadFilesMatchingWildcard(clientName, fileName);
            } else if ("download".equals(operationType)) {
                downloadFilesMatchingWildcard(clientName, fileName, true);
            } else {
                return "Unsupported operation: " + operationType;
            }
            return "SFTP operation successful for client: " + clientName;
        } catch (Exception e) {
            logger.severe("SFTP operation failed: " + e.getMessage());
            return "SFTP operation failed: " + e.getMessage();
        }
    }

    private Map<String, Object> getSftpClientConfig(String secretName) throws Exception {
        SecretsManagerClient secretsManagerClient = SecretsManagerClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse secretValueResponse = secretsManagerClient.getSecretValue(getSecretValueRequest);
        String secretJsonString = secretValueResponse.secretString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode secretJson = objectMapper.readTree(secretJsonString);

        Map<String, Object> config = new HashMap<>();
        config.put("host", secretJson.get("host").asText());
        config.put("port", secretJson.get("port").asInt());
        config.put("sftpUser", secretJson.get("sftpUser").asText());
        config.put("privateKeyPath", secretJson.get("privateKeyPath").asText());
        config.put("passphrase", secretJson.has("passphrase") ? secretJson.get("passphrase").asText() : "");
        config.put("remoteGetDir", secretJson.get("remoteGetDir").asText());
        config.put("remotePutDir", secretJson.get("remotePutDir").asText());
        config.put("localGetDir", System.getenv("EXPORT_FOLDER"));
        config.put("localPutDir", System.getenv("IMPORT_FOLDER"));
        config.put("remoteGetFileWildcard", Arrays.asList(secretJson.get("remoteGetFileWildcard").asText().split(",")));
        config.put("remotePutFileWildcard", Arrays.asList(secretJson.get("remotePutFileWildcard").asText().split(",")));

        secretsManagerClient.close();

        return config;
    }

    public void uploadFilesMatchingWildcard(String clientName, String fileName) throws Exception {
        sftpOperation(clientName, (channelSftp, config) -> {
            String localGetDir = (String) config.get("localGetDir");
            List<String> wildcards = (List<String>) config.get("remotePutFileWildcard");
            List<File> matchingFiles = getMatchingFiles(localGetDir, wildcards);

            if (matchingFiles.isEmpty()) {
                throw new FileNotFoundException("No files found matching the wildcard(s) for upload.");
            }

            if (fileName != null) {
                matchingFiles.removeIf(file -> !file.getName().startsWith(fileName));
                if (matchingFiles.isEmpty()) {
                    throw new FileNotFoundException("No files found starting with: " + fileName);
                }
            }

            for (File file : matchingFiles) {
                String remoteFilePath = config.get("remotePutDir") + "/" + file.getName();
                channelSftp.put(file.getAbsolutePath(), remoteFilePath);
                logger.info("File uploaded: Local - " + file.getAbsolutePath() + ", Remote - " + remoteFilePath);
            }
        });
    }

    public void downloadFilesMatchingWildcard(String clientName, String fileName, boolean oneFileOnly) throws Exception {
        sftpOperation(clientName, (channelSftp, config) -> {
            channelSftp.cd((String) config.get("remoteGetDir"));
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(".");

            List<ChannelSftp.LsEntry> matchingFiles = new ArrayList<>();
            for (String wildcard : (List<String>) config.get("remoteGetFileWildcard")) {
                for (ChannelSftp.LsEntry entry : files) {
                    if (matchesWildcard(entry.getFilename(), wildcard)) {
                        matchingFiles.add(entry);
                    }
                }
            }

            if (fileName != null) {
                matchingFiles.removeIf(entry -> !entry.getFilename().startsWith(fileName));
                if (matchingFiles.isEmpty()) {
                    throw new FileNotFoundException("No files found starting with: " + fileName);
                }
            }

            for (ChannelSftp.LsEntry entry : matchingFiles) {
                String localFilePath = config.get("localPutDir") + "/" + entry.getFilename();
                channelSftp.get(entry.getFilename(), localFilePath);
                logger.info("Downloaded file: " + entry.getFilename() + " to " + localFilePath);
                if (oneFileOnly) {
                    break;
                }
            }
        });
    }

    private void sftpOperation(String clientName, SftpOperation operation) throws Exception {
        String secretName = getSecretNameForClient(clientName);
        Map<String, Object> config = getSftpClientConfig(secretName);

        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = createSession(config);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            logger.info("Connected to SFTP client: " + clientName);
            operation.execute(channelSftp, config);

        } catch (Exception e) {
            throw new RuntimeException("SFTP operation failed for client: " + clientName, e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private String getSecretNameForClient(String clientName) {
        switch (clientName) {
            case "HHS_SFTP":
                return HHS_SFTP_SECRET_NAME;
            case "CITI_SFTP":
                return CITI_SFTP_SECRET_NAME;
            default:
                throw new IllegalArgumentException("Invalid client name: " + clientName);
        }
    }

    private Session createSession(Map<String, Object> config) throws JSchException {
        JSch jsch = new JSch();
        String privateKeyPath = (String) config.get("privateKeyPath");
        String passphrase = (String) config.get("passphrase");

        if (passphrase != null && !passphrase.isEmpty()) {
            jsch.addIdentity(privateKeyPath, passphrase);
        } else {
            jsch.addIdentity(privateKeyPath);
        }

        String user = (String) config.get("sftpUser");
        String host = (String) config.get("host");
        int port = (int) config.get("port");

        Session session = jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

    private boolean matchesWildcard(String fileName, String wildcard) {
        String regex = wildcard.replace(".", "\\.").replace("*", ".*").replace("#", ".");
        return Pattern.matches(regex + ".*", fileName);
    }

    private List<File> getMatchingFiles(String directoryPath, List<String> wildcards) throws FileNotFoundException {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new FileNotFoundException("Invalid directory: " + directoryPath);
        }

        List<File> matchingFiles = new ArrayList<>();
        for (String wildcard : wildcards) {
            String regex = wildcard.replace(".", "\\.").replace("*", ".*").replace("#", ".");
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.getName().matches(regex)) {
                    matchingFiles.add(file);
                }
            }
        }

        matchingFiles.sort(Comparator.comparingLong(File::lastModified).reversed());
        return matchingFiles.size() > 10 ? matchingFiles.subList(0, 10) : matchingFiles;
    }

    @FunctionalInterface
    interface SftpOperation {
        void execute(ChannelSftp channelSftp, Map<String, Object> config) throws Exception;
    }
}
