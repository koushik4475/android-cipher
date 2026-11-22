<div align="center">

# Android Cipher | Android Text Encryption and Decryption App in Java

Android Cipher is an Android application that demonstrates text encryption and decryption with the Blowfish cryptographic algorithm. The project is a learning-oriented example of building a native Android interface around cryptographic operations, with screenshots, architecture notes, and Android Studio setup instructions. Blowfish has a 64-bit block size and is a legacy cipher; do not use this demo to protect new sensitive data or as a substitute for a modern, reviewed security design.


<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=32&duration=2800&pause=2000&color=00D9FF&center=true&vCenter=true&width=940&lines=Legacy+Blowfish+Demo;Explore+Encryption+%26+Decryption;Android+Java+Learning+Project" alt="Typing SVG" />

[![Made with Love](https://img.shields.io/badge/Made%20with-❤️-red.svg)](https://github.com/koushik4475)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![Blowfish](https://img.shields.io/badge/Algorithm-Blowfish-blue.svg)](https://en.wikipedia.org/wiki/Blowfish_(cipher))
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

### 🛡️ *Your Digital Vault for educational encryption demo*

</div>

---

## Security limitations (read before use)

This repository is an educational demo, **not a secure storage or messaging tool**. Its implementation uses Blowfish-CBC with a hard-coded key and fixed IV, and CBC does not authenticate ciphertext. These choices make it unsuitable for protecting real credentials, personal data, or other sensitive information. The historical claims of an '8-level' system or 'military-grade' security are not supported by the current implementation. For real applications, use a modern authenticated-encryption scheme (such as AES-GCM) with securely managed keys and unique nonces, ideally via Android Keystore, and obtain a security review.

## 🌟 Project Overview

**Android Cipher** is a learning-oriented encryption application developed as a mini project at **University of Mysore, School of Engineering**. This demo shows text encryption and decryption using the legacy **Blowfish** algorithm; it is not designed to protect sensitive data.

<div align="center">

```ascii
╔════════════════════════════════════════╗
║    🔒 → 🔐 → 🛡️ → 🔑 → 💎 → 🚀 → ⚡ → 🌟    ║
║       LEGACY CIPHER DEMONSTRATION       ║
╚════════════════════════════════════════╝
```

</div>

---

## ✨ Features

<table>
<tr>
<td width="50%">

### 🔐 Core Capabilities
- **Encryption demonstration** - Educational legacy-cipher example
- **Blowfish CBC** - Legacy algorithm implementation
- **Text Encryption** - Secure your messages
- **Text transformation** - Demonstrate encrypt/decrypt flows
- **Easy Decryption** - Simple one-tap restore
- **Offline Security** - No internet required

</td>
<td width="50%">

### 🎨 User Experience
- **Intuitive Interface** - Clean, modern design
- **Fast Processing** - Instant encryption/decryption
- **Copy to Clipboard** - Easy sharing
- **Material Design** - Beautiful Android UI
- **Lightweight** - Minimal resource usage
- **No Ads** - Distraction-free experience

</td>
</tr>
</table>

---

## 📸 Application Screenshots

<div align="center">

### 🔒 Encrypting Text

<img width="300" alt="Encrypting Text Screenshot" src="https://github.com/user-attachments/assets/26a2a8f7-e141-4e70-bf2f-72cd03ff1e7e" />

> *Transform text using this educational legacy cipher demo*

---

### 🔓 Decrypting Encrypted Messages

<table>
<tr>
<td align="center">
<img width="280" alt="Decryption Step 1" src="https://github.com/user-attachments/assets/c1736464-d0f2-43c6-bc65-ac93955760b0" />
<br />
<sub><b>Step 1:</b> Enter Encrypted Text</sub>
</td>
<td align="center">
<img width="280" alt="Decryption Step 2" src="https://github.com/user-attachments/assets/0d40b2b4-6b82-47f1-9c14-27507c558a5e" />
<br />
<sub><b>Step 2:</b> Decrypt & Reveal</sub>
</td>
</tr>
</table>

> *Seamlessly decode your encrypted data back to its original form*

</div>

---

## 🔬 Technical Architecture

### 🧬 Blowfish Encryption System

```
Plain Text Input
      ↓
┌─────────────────┐
│   Level 1 🔐    │  ← Initial Blowfish Encryption
├─────────────────┤
│   Level 2 🔐    │  ← Key Rotation
├─────────────────┤
│   Level 3 🔐    │  ← Secondary Cipher
├─────────────────┤
│   Level 4 🔐    │  ← Hash Integration
├─────────────────┤
│   Level 5 🔐    │  ← Salting Process
├─────────────────┤
│   Level 6 🔐    │  ← Bit Scrambling
├─────────────────┤
│   Level 7 🔐    │  ← Final Encryption
├─────────────────┤
│   Level 8 🔐    │  ← Encoding Layer
└─────────────────┘
      ↓
Encrypted Output
```

### 🛠️ Technology Stack

<div align="center">

![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![XML](https://img.shields.io/badge/XML-E34F26?style=for-the-badge&logo=xml&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

</div>

---

## 🚀 Getting Started

### Prerequisites

- Android Studio 4.0+
- JDK 8 or higher
- Android SDK (API Level 21+)
- Gradle 6.0+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/koushik4475/android-cipher.git
```

2. **Open in Android Studio**
```bash
cd android-cipher
# Open the project in Android Studio
```

3. **Build the project**
- Let Gradle sync and download dependencies
- Build > Make Project (Ctrl+F9)

4. **Run the application**
- Connect your Android device or start an emulator
- Run > Run 'app' (Shift+F10)

---

## 📱 Usage Guide

### Encrypting Text

1. Launch the **Android Cipher** app
2. Enter your text or password in the input field
3. Tap the **"Encrypt"** button
4. Copy the encrypted output to your clipboard
5. Store it securely or share as needed

### Decrypting Text

1. Open the app and switch to **Decrypt mode**
2. Paste or enter the encrypted text
3. Tap the **"Decrypt"** button
4. View your original message restored

---

## 🎓 Academic Context

<div align="center">

### 🏛️ University of Mysore - School of Engineering

This project was developed as part of the curriculum at one of India's premier engineering institutions, demonstrating practical application of cryptographic principles in mobile computing.

**Discipline:** Computer Science & Engineering  
**Focus Area:** Mobile Security & Cryptography  
**Academic Year:** 2024-2025

</div>

---

## 👨‍💻 Developer

<div align="center">

<img src="https://github.com/koushik4475.png" width="150" style="border-radius: 50%;" alt="Koushik HY"/>

### **Koushik HY**
*AI Developer | Full Stack Developer | Android Enthusiast*

[![Portfolio](https://img.shields.io/badge/Portfolio-00D9FF?style=for-the-badge&logo=google-chrome&logoColor=white)](https://koushik4475.netlify.app)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:koushik4475@gmail.com)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/koushik4475)
[![WhatsApp](https://img.shields.io/badge/WhatsApp-25D366?style=for-the-badge&logo=whatsapp&logoColor=white)](https://wa.me/919620968968)

📍 Karnataka, India  
🎓 Computer Science & Design - Mysore University School of Engineering  
💼 AI Developer | Passionate about Full-Stack Development & Mobile Applications  
📱 Contact: **+91 9620968968**  
🌐 Portfolio: **[koushik4475.netlify.app](https://koushik4475.netlify.app)**

</div>

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

## 🙏 Acknowledgments

- **University of Mysore** - For academic support and resources
- **Blowfish Algorithm** - Bruce Schneier for the encryption standard
- **Android Community** - For excellent documentation and support
- **Open Source Contributors** - For inspiration and guidance

---

<div align="center">

### 🌟 If you found this project useful, please consider giving it a star!

[![Star this repo](https://img.shields.io/github/stars/koushik4475/android-cipher?style=social)](https://github.com/koushik4475/android-cipher)
[![Follow me](https://img.shields.io/github/followers/koushik4475?style=social)](https://github.com/koushik4475)

---

**Made with ❤️ and ☕ by [Koushik HY](https://koushik4475.netlify.app)**

<img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&height=100&section=footer" width="100%"/>

</div>
