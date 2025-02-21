# IntelliJ IDEA VCS Analysis Toolkit Plugin

## Description
This IntelliJ IDEA plugin provides advanced visualization of Java code, generating UML models and interactive 3D visualizations to help developers understand and analyze their projects. It allows users to explore their code in different visual formats and track changes over multiple versions.

## Features
- **Generate UML Models**: Automatically generate UML class diagrams from Java source code.
- **City Visualization**: Represents classes and interfaces as buildings, with packages forming districts. The building height corresponds to the number of methods, and their area reflects code size.
- **Circle Visualization**: Displays classes, interfaces, and packages as circles. The circle size represents code size, while the frame width indicates the number of methods. Visualize and compare multiple versions of the codebase.
- **Info Page with Repository Statistics**: Provides statistics per developer or overall project insights.
- **Efficient Code Analysis with Caching**: Initial analysis may take time, but caching significantly speeds up subsequent analyses.
- **Local and Remote Project Analysis**: Analyze local projects or download repositories directly from Git for examination.

## Installation

### From a JAR File
1. Build the plugin.
3. Go to **File** > **Settings** > **Plugins**.
4. Click **Install plugin from disk** and select the JAR file.
5. Restart IntelliJ IDEA.

## Contributing
Contributions are welcome! Feel free to open issues or submit pull requests for bug fixes, feature requests, or improvements.

### Steps to contribute:
1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Commit your changes.
4. Open a pull request with a description of the changes.
