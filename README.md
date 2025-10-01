# IBM Cloud Object Storage Desktop Client

<div align="center">

![Java](https://img.shields.io/badge/Java-8+-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.x-green.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
![IBM Cloud](https://img.shields.io/badge/IBM%20Cloud-Object%20Storage-darkblue.svg)

A modern, user-friendly desktop application for managing files in IBM Cloud Object Storage with a clean Swing GUI interface.

[Features](#features) • [Installation](#installation) • [Usage](#usage) • [Contributing](#contributing) • [Architecture](#architecture)

</div>

## 🚀 Features

- **🔐 Secure Authentication**: HMAC credentials support for IBM Cloud Object Storage
- **📁 Bucket Management**: Browse and manage all your storage buckets
- **🗂️ Folder Navigation**: Intuitive directory-style navigation with prefix support
- **⬆️ File Upload**: Upload files from your local system
- **⬇️ File Download**: Download files to your preferred location
- **🗑️ File Management**: Delete files and objects with confirmation
- **🔍 Search Functionality**: Find files quickly with built-in search
- **📊 File Information**: View file sizes, types, and modification dates
- **🎨 Responsive UI**: Modern Swing interface with status feedback
- **🏗️ SOLID Architecture**: Well-structured, maintainable codebase following SOLID principles

## 📋 Prerequisites

- **Java JDK 8** or higher
- **Maven 3.x** for building
- **IBM Cloud Object Storage** service instance with HMAC credentials

## 🛠️ Installation

### Option 1: Download Pre-built JAR

1. Download the latest `ibm-cos-client.jar` from the [releases page](../../releases)
2. Run the application:
   ```bash
   java -jar ibm-cos-client.jar
   ```

### Option 2: Build from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/gabdevbr/ibm-cloud-cos-gui-client.git
   cd ibm-cloud-cos-gui-client
   ```

2. **Build the project**:
   ```bash
   mvn clean package
   ```

3. **Run the application**:
   ```bash
   java -jar target/ibm-cos-client.jar
   ```
   
   Or directly with Maven:
   ```bash
   mvn exec:java -Dexec.mainClass="com.ibm.cos.client.IBMCloudCOSClient"
   ```

### Platform-Specific Launchers

For convenience, use the provided launch scripts:

- **macOS/Linux**: `./run.sh`
- **Windows**: `run.bat`

## 🎯 Usage

### 1. Getting IBM Cloud Credentials

1. Access your [IBM Cloud Console](https://cloud.ibm.com/)
2. Navigate to your **Cloud Object Storage** service instance
3. Go to **Service Credentials**
4. Create or view an existing **HMAC credential**
5. Copy the `access_key_id` and `secret_access_key`

### 2. Configuring the Application

1. **Set the Endpoint**: Choose the appropriate regional endpoint:
   - `s3.us-south.cloud-object-storage.appdomain.cloud` (US South)
   - `s3.us-east.cloud-object-storage.appdomain.cloud` (US East)
   - `s3.eu-gb.cloud-object-storage.appdomain.cloud` (UK)
   - `s3.eu-de.cloud-object-storage.appdomain.cloud` (Germany)
   - `s3.jp-tok.cloud-object-storage.appdomain.cloud` (Japan)
   - `s3.br-sao.cloud-object-storage.appdomain.cloud` (Brazil)

2. **Enter Credentials**: Input your Access Key ID and Secret Access Key

3. **Connect**: Click the "Connect" button to authenticate

### 3. Managing Files

- **Browse Buckets**: Select from the dropdown menu
- **Navigate Folders**: Double-click folders or use the ".. (parent)" option
- **Upload Files**: Click "Upload" and select local files
- **Download Files**: Select files and click "Download"
- **Delete Objects**: Select and click "Delete" (with confirmation)
- **Search**: Use the search field to find specific files

## 🏗️ Architecture

This project follows **SOLID principles** for maintainable and extensible code:

```
📱 UI Layer (IBMCloudCOSClient)
    ↓ depends on
🔧 Service Interfaces (AuthenticationService, CloudStorageService)
    ↓ implemented by
⚙️ Concrete Services (IBMAuthenticationService, IBMCloudStorageService)
    ↓ uses
📊 Data Models (Credentials, ConnectionConfig, FileItem)
```

### Key Components

- **Authentication Layer**: Secure credential management and client initialization
- **Storage Service**: File operations abstraction for easy extensibility
- **Data Models**: Clean representation of credentials, configurations, and file items
- **Utility Layer**: Helper functions for file operations
- **UI Layer**: Swing-based graphical interface

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### Development Setup

1. **Fork the repository**
2. **Create a feature branch**:
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make your changes** following the existing code style
4. **Test your changes**:
   ```bash
   mvn test
   mvn clean package
   ```
5. **Commit your changes**:
   ```bash
   git commit -m "Add amazing feature"
   ```
6. **Push to your branch**:
   ```bash
   git push origin feature/amazing-feature
   ```
7. **Open a Pull Request**

### Code Guidelines

- Follow **SOLID principles**
- Add **unit tests** for new functionality
- Update documentation for API changes
- Use clear, descriptive commit messages
- Ensure code compiles without warnings

### Areas for Contribution

- 🌐 Additional cloud storage providers (AWS S3, Google Cloud Storage)
- 🎨 UI/UX improvements
- 📱 Cross-platform enhancements
- 🔒 Security improvements
- 📊 Performance optimizations
- 🧪 Additional test coverage
- 📚 Documentation improvements

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| **Authentication Error** | Verify credentials and endpoint are correct for your region |
| **Connection Timeout** | Check internet connection and firewall settings |
| **Empty Bucket List** | Ensure HMAC credential has proper permissions |
| **Upload/Download Fails** | Check file permissions and available disk space |

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Gabriel** - [@gabdevbr](https://github.com/gabdevbr)

## 🙏 Acknowledgments

- [IBM Cloud Object Storage Java SDK](https://github.com/IBM/ibm-cos-sdk-java) for the excellent API
- The Java Swing community for UI inspiration
- All contributors who help make this project better

## 📊 Project Stats

- **Language**: Java 8+
- **Build Tool**: Maven
- **Dependencies**: IBM COS Java SDK 2.13.4
- **Architecture**: SOLID Principles
- **UI Framework**: Java Swing

---

<div align="center">

**⭐ Star this repository if it helped you!**

[Report Bug](../../issues) • [Request Feature](../../issues) • [Discussions](../../discussions)

</div>