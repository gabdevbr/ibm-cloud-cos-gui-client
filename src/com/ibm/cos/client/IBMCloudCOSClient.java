package com.ibm.cos.client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cos.client.auth.AuthenticationService;
import com.ibm.cos.client.auth.IBMAuthenticationService;
import com.ibm.cos.client.auth.AuthenticationException;
import com.ibm.cos.client.service.CloudStorageService;
import com.ibm.cos.client.service.IBMCloudStorageService;
import com.ibm.cos.client.service.StorageException;
import com.ibm.cos.client.model.Credentials;
import com.ibm.cos.client.model.ConnectionConfig;
import com.ibm.cos.client.model.FileItem;
import com.ibm.cos.client.util.FileUtils;
import com.ibm.cos.client.operation.FileOperationHandler;
import com.ibm.cos.client.operation.CloudFileOperationHandler;
import com.ibm.cos.client.operation.FileOperationListener;

public class IBMCloudCOSClient extends JFrame {
    private JTextField accessKeyField;
    private JPasswordField secretKeyField;
    private JTextField endpointField;
    private JButton connectButton;
    private JComboBox<String> bucketComboBox;
    private JTextField prefixField;
    private JButton refreshButton;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JButton uploadButton;
    private JButton downloadButton;
    private JButton deleteButton;
    private JLabel statusLabel;
    private JTextField searchField;
    private JButton searchButton;

    private static final String PARENT_FOLDER = ".. (parent)";
    private static final String FOLDER_TYPE = "Folder";
    private static final String FILE_TYPE = "File";
    
    private final AuthenticationService authService;
    private CloudStorageService storageService;
    private FileOperationHandler fileOperationHandler;
    private String currentBucket = "";
    private String currentPrefix = "";

    public IBMCloudCOSClient() {
        this(new IBMAuthenticationService());
    }
    
    public IBMCloudCOSClient(AuthenticationService authService) {
        this.authService = authService;
        
        setTitle("IBM Cloud Object Storage Client");
        setSize(1000, 750);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        createAuthPanel();
        createNavigationPanel();
        createStatusBar();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createAuthPanel() {
        JPanel authPanel = new JPanel(new BorderLayout(10, 5));
        authPanel.setBorder(BorderFactory.createTitledBorder("Authentication"));

        // Top row - Endpoint
        JPanel endpointPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        endpointPanel.add(new JLabel("Endpoint:"));
        endpointField = new JTextField("s3.br-sao.cloud-object-storage.appdomain.cloud", 40);
        endpointPanel.add(endpointField);

        // Bottom row - Credentials and Connect button
        JPanel credentialsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        credentialsPanel.add(new JLabel("Access Key:"));
        accessKeyField = new JTextField(15);
        credentialsPanel.add(accessKeyField);

        credentialsPanel.add(new JLabel("Secret Key:"));
        secretKeyField = new JPasswordField(15);
        credentialsPanel.add(secretKeyField);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> authenticate());
        credentialsPanel.add(connectButton);

        authPanel.add(endpointPanel, BorderLayout.NORTH);
        authPanel.add(credentialsPanel, BorderLayout.CENTER);

        add(authPanel, BorderLayout.NORTH);
    }

    private void createNavigationPanel() {
        JPanel navPanel = new JPanel(new BorderLayout(10, 10));
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topNav.add(new JLabel("Bucket:"));
        bucketComboBox = new JComboBox<>();
        bucketComboBox.setPreferredSize(new Dimension(200, 25));
        bucketComboBox.addActionListener(e -> onBucketSelected());
        bucketComboBox.setEnabled(false);
        topNav.add(bucketComboBox);

        topNav.add(new JLabel("Prefix:"));
        prefixField = new JTextField(20);
        prefixField.setEditable(false);
        topNav.add(prefixField);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshFiles());
        refreshButton.setEnabled(false);
        topNav.add(refreshButton);

        // Search panel - second row
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(30);
        searchField.setToolTipText("Search for files/folders recursively");
        searchField.addActionListener(e -> performSearch()); // Enter key triggers search
        searchPanel.add(searchField);

        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        searchButton.setEnabled(false);
        searchPanel.add(searchButton);

        // Combined navigation panel
        JPanel combinedNavPanel = new JPanel(new BorderLayout());
        combinedNavPanel.add(topNav, BorderLayout.NORTH);
        combinedNavPanel.add(searchPanel, BorderLayout.CENTER);

        navPanel.add(combinedNavPanel, BorderLayout.NORTH);

        String[] columnNames = {"Name", "Type", "Size", "Last Modified"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fileTable = new JTable(tableModel);
        fileTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onDoubleClick();
                }
            }
        });

        fileTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] selectedRows = fileTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    // For download, enable if at least 1 file is selected
                    boolean enableDownload = false;
                    for (int row : selectedRows) {
                        String type = tableModel.getValueAt(row, 1).toString();
                        String name = tableModel.getValueAt(row, 0).toString();
                        if (type.equals("File") && !PARENT_FOLDER.equals(name)) {
                            enableDownload = true;
                            break;
                        }
                    }
                    downloadButton.setEnabled(enableDownload);
                    
                    // For delete, enable if no "parent" item is selected
                    boolean enableDelete = true;
                    for (int row : selectedRows) {
                        String name = tableModel.getValueAt(row, 0).toString();
                        if (PARENT_FOLDER.equals(name)) {
                            enableDelete = false;
                            break;
                        }
                    }
                    deleteButton.setEnabled(enableDelete);
                } else {
                    downloadButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileTable);
        navPanel.add(scrollPane, BorderLayout.CENTER);

        add(navPanel, BorderLayout.CENTER);
    }



    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel = new JLabel("Not connected");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        // Create link label
        JLabel linkLabel = new JLabel("<html><a href='https://gab.dev.br'>gab.dev.br</a></html>");
        linkLabel.setForeground(Color.BLUE);
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI("https://gab.dev.br"));
                    }
                } catch (Exception ex) {
                    showError("Could not open browser: " + ex.getMessage());
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                linkLabel.setText("<html><a href='https://gab.dev.br'><u>gab.dev.br</u></a></html>");
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                linkLabel.setText("<html><a href='https://gab.dev.br'>gab.dev.br</a></html>");
            }
        });
        statusPanel.add(linkLabel, BorderLayout.EAST);

        // Create the action panel with better spacing
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create buttons with consistent size
        uploadButton = new JButton("Upload Files");
        uploadButton.addActionListener(e -> uploadFile());
        uploadButton.setEnabled(false);
        uploadButton.setPreferredSize(new Dimension(120, 30));
        uploadButton.setToolTipText("Upload multiple files (supports multiple selection)");
        actionPanel.add(uploadButton);

        downloadButton = new JButton("Download Files");
        downloadButton.addActionListener(e -> downloadFile());
        downloadButton.setEnabled(false);
        downloadButton.setPreferredSize(new Dimension(120, 30));
        downloadButton.setToolTipText("Download selected files (supports multiple selection)");
        actionPanel.add(downloadButton);

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteObject());
        deleteButton.setEnabled(false);
        deleteButton.setPreferredSize(new Dimension(120, 30));
        deleteButton.setToolTipText("Delete selected items (supports multiple selection)");
        actionPanel.add(deleteButton);

        // Combine action panel and status bar
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(actionPanel, BorderLayout.NORTH);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void authenticate() {
        String accessKey = accessKeyField.getText().trim();
        String secretKey = new String(secretKeyField.getPassword()).trim();
        String endpoint = endpointField.getText().trim();

        if (accessKey.isEmpty() || secretKey.isEmpty() || endpoint.isEmpty()) {
            showError("Please enter access key, secret key, and endpoint");
            return;
        }

        setStatus("Authenticating...");
        connectButton.setEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    Credentials credentials = new Credentials(accessKey, secretKey);
                    ConnectionConfig config = new ConnectionConfig(endpoint, "us-south");
                    
                    AmazonS3 client = authService.authenticate(credentials, config);
                    authService.validateConnection(client);
                    
                    storageService = new IBMCloudStorageService(client);
                    fileOperationHandler = new CloudFileOperationHandler(storageService);
                    return true;
                } catch (AuthenticationException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        // Authentication successful
                        setStatus("Connected successfully");
                        accessKeyField.setEnabled(false);
                        secretKeyField.setEnabled(false);
                        endpointField.setEnabled(false);
                        loadBuckets();
                    } else {
                        showError("Authentication failed - check credentials and endpoint");
                        connectButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    showError("Authentication error: " + ex.getMessage());
                    connectButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void loadBuckets() {
        setStatus("Loading buckets...");

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                try {
                    return storageService.listBuckets();
                } catch (StorageException ex) {
                    ex.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    List<String> buckets = get();
                    bucketComboBox.removeAllItems();
                    for (String bucket : buckets) {
                        bucketComboBox.addItem(bucket);
                    }
                    if (!buckets.isEmpty()) {
                        bucketComboBox.setEnabled(true);
                        refreshButton.setEnabled(true);
                        uploadButton.setEnabled(true);
                        searchButton.setEnabled(true);
                        setStatus("Buckets loaded: " + buckets.size() + " found");
                    } else {
                        setStatus("No buckets found");
                    }
                } catch (Exception ex) {
                    showError("Error loading buckets: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void onBucketSelected() {
        if (bucketComboBox.getSelectedItem() != null) {
            currentBucket = bucketComboBox.getSelectedItem().toString();
            currentPrefix = "";
            prefixField.setText("");
            refreshFiles();
        }
    }

    private void refreshFiles() {
        if (currentBucket.isEmpty()) return;

        setStatus("Loading files...");

        new SwingWorker<List<FileItem>, Void>() {
            @Override
            protected List<FileItem> doInBackground() {
                try {
                    return storageService.listObjects(currentBucket, currentPrefix);
                } catch (StorageException ex) {
                    ex.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    List<FileItem> items = get();
                    populateFileTable(items);
                    setStatus("Files loaded: " + items.size() + " items");
                } catch (Exception ex) {
                    showError("Error loading files: " + ex.getMessage());
                }
            }
        }.execute();
    }
    
    private void populateFileTable(List<FileItem> items) {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        if (!currentPrefix.isEmpty()) {
            tableModel.addRow(new Object[]{PARENT_FOLDER, FOLDER_TYPE, "", ""});
        }

        for (FileItem item : items) {
            String type = item.isFile() ? FILE_TYPE : FOLDER_TYPE;
            String size = item.isFile() ? FileUtils.formatSize(item.getSize()) : "";
            String date = item.getLastModified() != null ? dateFormat.format(item.getLastModified()) : "";
            
            tableModel.addRow(new Object[]{item.getName(), type, size, date});
        }

        downloadButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }



    private void onDoubleClick() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow >= 0) {
            String name = tableModel.getValueAt(selectedRow, 0).toString();
            String type = tableModel.getValueAt(selectedRow, 1).toString();

            if (FOLDER_TYPE.equals(type)) {
                if (PARENT_FOLDER.equals(name)) {
                    int lastSlash = currentPrefix.lastIndexOf("/", currentPrefix.length() - 2);
                    if (lastSlash > 0) {
                        currentPrefix = currentPrefix.substring(0, lastSlash + 1);
                    } else {
                        currentPrefix = "";
                    }
                } else {
                    currentPrefix = currentPrefix + name;
                }
                prefixField.setText(currentPrefix);
                refreshFiles();
            }
        }
    }

    private void uploadFile() {
        if (currentBucket.isEmpty()) {
            showError("Please select a bucket");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            java.util.List<File> fileList = java.util.Arrays.asList(selectedFiles);

            uploadButton.setEnabled(false);
            
            FileOperationListener listener = new FileOperationListener() {
                @Override
                public void onOperationStarted(String message) {
                    setStatus(message);
                }

                @Override
                public void onOperationProgress(String message) {
                    setStatus(message);
                }

                @Override
                public void onOperationCompleted(String message) {
                    setStatus(message);
                    refreshFiles();
                    uploadButton.setEnabled(true);
                }

                @Override
                public void onOperationFailed(String message) {
                    showError(message);
                    uploadButton.setEnabled(true);
                }
            };
            
            fileOperationHandler.uploadFiles(fileList, currentBucket, currentPrefix, listener);
        }
    }

    private void downloadFile() {
        int[] selectedRows = fileTable.getSelectedRows();
        if (selectedRows.length == 0) return;

        // Collect only selected files (not folders)
        java.util.List<String> filesToDownload = new ArrayList<>();
        for (int row : selectedRows) {
            String name = tableModel.getValueAt(row, 0).toString();
            String type = tableModel.getValueAt(row, 1).toString();
            
            if (FILE_TYPE.equals(type) && !PARENT_FOLDER.equals(name)) {
                filesToDownload.add(currentPrefix + name);
            }
        }
        
        if (filesToDownload.isEmpty()) {
            showError("No files selected for download");
            return;
        }

        // Escolher diretório de destino
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooser.setDialogTitle("Select Download Directory");
        
        if (directoryChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetDirectory = directoryChooser.getSelectedFile();
            downloadButton.setEnabled(false);
            
            FileOperationListener listener = new FileOperationListener() {
                @Override
                public void onOperationStarted(String message) {
                    setStatus(message);
                }

                @Override
                public void onOperationProgress(String message) {
                    setStatus(message);
                }

                @Override
                public void onOperationCompleted(String message) {
                    setStatus(message);
                    downloadButton.setEnabled(true);
                }

                @Override
                public void onOperationFailed(String message) {
                    showError(message);
                    downloadButton.setEnabled(true);
                }
            };
            
            fileOperationHandler.downloadFiles(filesToDownload, currentBucket, targetDirectory, listener);
        }
    }

    private void deleteObject() {
        int[] selectedRows = fileTable.getSelectedRows();
        if (selectedRows.length == 0) return;

        // Coletar informações dos itens selecionados
        java.util.List<String> itemsToDelete = new ArrayList<>();
        java.util.List<String> itemNames = new ArrayList<>();
        
        for (int row : selectedRows) {
            String name = tableModel.getValueAt(row, 0).toString();
            String type = tableModel.getValueAt(row, 1).toString();
            
            if (PARENT_FOLDER.equals(name)) continue;
            
            String key = currentPrefix + name;
            if (FOLDER_TYPE.equals(type)) {
                key = key.substring(0, key.length() - 1);
            }
            itemsToDelete.add(key);
            itemNames.add(name);
        }
        
        if (itemsToDelete.isEmpty()) return;

        // Mensagem de confirmação baseada na quantidade de itens
        String confirmMessage;
        if (itemsToDelete.size() == 1) {
            confirmMessage = "Are you sure you want to delete '" + itemNames.get(0) + "'?";
        } else {
            confirmMessage = "Are you sure you want to delete " + itemsToDelete.size() + " items?";
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            confirmMessage,
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            deleteButton.setEnabled(false);
            
            FileOperationListener listener = new FileOperationListener() {
                @Override
                public void onOperationStarted(String message) {
                    setStatus(message);
                }

                @Override
                public void onOperationProgress(String message) {
                    setStatus(message);
                }

                @Override
                public void onOperationCompleted(String message) {
                    setStatus(message);
                    refreshFiles();
                    deleteButton.setEnabled(true);
                }

                @Override
                public void onOperationFailed(String message) {
                    showError(message);
                    refreshFiles();
                    deleteButton.setEnabled(true);
                }
            };
            
            fileOperationHandler.deleteFiles(itemsToDelete, currentBucket, listener);
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        setStatus("Error: " + message);
    }

    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            showError("Please enter a search term");
            return;
        }
        
        if (currentBucket.isEmpty()) {
            showError("Please select a bucket first");
            return;
        }

        setStatus("Searching for '" + searchTerm + "'...");
        searchButton.setEnabled(false);

        new SwingWorker<List<FileItem>, Void>() {
            @Override
            protected List<FileItem> doInBackground() {
                try {
                    return storageService.searchObjectsRecursively(currentBucket, searchTerm);
                } catch (StorageException ex) {
                    ex.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    List<FileItem> results = get();
                    searchButton.setEnabled(true);
                    
                    if (results.isEmpty()) {
                        setStatus("No files found containing '" + searchTerm + "'");
                        JOptionPane.showMessageDialog(IBMCloudCOSClient.this, 
                            "No files found containing '" + searchTerm + "'", 
                            "Search Results", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        setStatus("Found " + results.size() + " file(s) containing '" + searchTerm + "'");
                        showSearchResults(results, searchTerm);
                    }
                } catch (Exception ex) {
                    searchButton.setEnabled(true);
                    showError("Search failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showSearchResults(List<FileItem> results, String searchTerm) {
        // Create modal dialog
        JDialog searchDialog = new JDialog(this, "Search Results - '" + searchTerm + "'", true);
        searchDialog.setSize(800, 500);
        searchDialog.setLocationRelativeTo(this);
        searchDialog.setLayout(new BorderLayout());

        // Create results table
        String[] columnNames = {"File Name", "Full Path", "Size", "Last Modified"};
        DefaultTableModel searchTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        // Populate results
        for (FileItem item : results) {
            String fileName = item.getName().substring(item.getName().lastIndexOf('/') + 1);
            String fullPath = item.getName();
            String size = FileUtils.formatSize(item.getSize());
            String date = item.getLastModified() != null ? dateFormat.format(item.getLastModified()) : "";
            
            searchTableModel.addRow(new Object[]{fileName, fullPath, size, date});
        }

        JTable searchTable = new JTable(searchTableModel);
        searchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Auto-resize columns
        searchTable.getColumnModel().getColumn(0).setPreferredWidth(200); // File Name
        searchTable.getColumnModel().getColumn(1).setPreferredWidth(350); // Full Path
        searchTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Size
        searchTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Last Modified

        JScrollPane searchScrollPane = new JScrollPane(searchTable);
        searchDialog.add(searchScrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton copyUrlButton = new JButton("Copy URL");
        copyUrlButton.addActionListener(e -> {
            int selectedRow = searchTable.getSelectedRow();
            if (selectedRow >= 0) {
                String fullPath = searchTableModel.getValueAt(selectedRow, 1).toString();
                String url = buildObjectUrl(currentBucket, fullPath);
                copyToClipboard(url);
                JOptionPane.showMessageDialog(searchDialog, "URL copied to clipboard!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(searchDialog, "Please select a file first", 
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(copyUrlButton);

        JButton navigateButton = new JButton("Navigate to File");
        navigateButton.addActionListener(e -> {
            int selectedRow = searchTable.getSelectedRow();
            if (selectedRow >= 0) {
                String fullPath = searchTableModel.getValueAt(selectedRow, 1).toString();
                navigateToFile(fullPath);
                searchDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(searchDialog, "Please select a file first", 
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(navigateButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> searchDialog.dispose());
        buttonPanel.add(closeButton);

        searchDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Found " + results.size() + " file(s) containing '" + searchTerm + "'"));
        searchDialog.add(infoPanel, BorderLayout.NORTH);

        searchDialog.setVisible(true);
    }

    private String buildObjectUrl(String bucket, String key) {
        // Build URL based on endpoint and object path
        String endpoint = endpointField.getText().trim();
        if (!endpoint.startsWith("http")) {
            endpoint = "https://" + endpoint;
        }
        return endpoint + "/" + bucket + "/" + key;
    }

    private void copyToClipboard(String text) {
        try {
            java.awt.datatransfer.StringSelection stringSelection = 
                new java.awt.datatransfer.StringSelection(text);
            java.awt.datatransfer.Clipboard clipboard = 
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        } catch (Exception e) {
            showError("Failed to copy to clipboard: " + e.getMessage());
        }
    }

    private void navigateToFile(String fullPath) {
        // Navigate to the folder containing the file
        int lastSlashIndex = fullPath.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            currentPrefix = fullPath.substring(0, lastSlashIndex + 1);
        } else {
            currentPrefix = "";
        }
        prefixField.setText(currentPrefix);
        refreshFiles();
        
        // Try to select the file in the table after refresh
        SwingUtilities.invokeLater(() -> {
            String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(fileName)) {
                    fileTable.setRowSelectionInterval(i, i);
                    fileTable.scrollRectToVisible(fileTable.getCellRect(i, 0, true));
                    break;
                }
            }
        });
    }



    public static void main(String[] args) {
        // Configure system properties for better UI experience
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "IBM COS Client");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for native appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Additional UI enhancements
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                
                // Create and show the application
                new IBMCloudCOSClient();
                
            } catch (Exception e) {
                System.err.println("Error initializing application: " + e.getMessage());
                e.printStackTrace();
                
                // Fallback to default look and feel
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    new IBMCloudCOSClient();
                } catch (Exception fallbackException) {
                    System.err.println("Critical error: Cannot initialize UI");
                    fallbackException.printStackTrace();
                    System.exit(1);
                }
            }
        });
    }
}
