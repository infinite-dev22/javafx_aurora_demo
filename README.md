# Aurora JavaFX Demo

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-17%2B-green)
![Gradle](https://img.shields.io/badge/Gradle-8.0%2B-red)

A stunning demonstration of Microsoft's Aurora effect implemented as a background for JavaFX applications. This project showcases how to integrate the mesmerizing Aurora animation into your JavaFX applications while maintaining performance and customizability.

## 🌟 Features

- Fluid and performant Aurora background animation
- Seamless integration with any JavaFX application
- Customizable colors and animation parameters
- Gradle-based build system
- Cross-platform support
- Platform-specific installer generation using jlink

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Gradle 8.0 or higher
- Git (optional, for cloning)

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/javafx_aurora_demo.git
cd javafx_aurora_demo
```

2. Build the project:
```bash
./gradlew build
```

3. Run the demo:
```bash
./gradlew run
```

## 🛠️ Building Platform Installers

This project supports creating platform-specific installers using jlink. To create an installer for your platform:

```bash
./gradlew jlinkZip   # Creates a ZIP distribution
./gradlew jpackage   # Creates a platform-specific installer
```

Generated installers can be found in the `build/installer` directory.

## 🏗️ Project Structure

```
javafx_aurora_demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │       └── inc
│   │           └── nomard
│   │               └── aurora_demo
│   │   └── resources/
│   └── test/
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```

## 📦 Dependencies

- JavaFX 17+

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Microsoft for the original Aurora effect inspiration
- The JavaFX community for their continuous support
- All contributors who help improve this project

## 📧 Contact

[mwigojm@gmail.com](mailto:mwigojm@gmail.com)

Project Link: [https://github.com/yourusername/javafx_aurora_demo](https://github.com/yourusername/javafx_aurora_demo)

---

⭐️ If you found this project helpful or interesting, please consider giving it a star!