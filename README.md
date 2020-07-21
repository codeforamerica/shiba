# SHIBA - Securely Hosted Integrated Benefits Application

## Development setup

### Install the following system dependencies:

- Java 14 Development Kit: `brew cask install java`
- Gradle build tool version 6.3: `brew install gradle`

### Setup live reload:

- Install the [live reload chrome extension](https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei?hl=en)

### Setup IntelliJ for the project:

- Configure an additional dictionary in `preferences > editor > proofreading > spelling` using `.idea/dictionaries/shiba.xml`
- Install the Lombok plugin
- Enable annotation processing
- Set the Project SDK to Java 14 in `File > Project Structure`
- Run the application using ShibaApplication run configuration

### Test:

From the project root invoke
```./gradlew clean test```

### Setup Fake Filler(optional, Chrome only):

- Using an automatic form filler makes manual test easier.
- Install Fake Filler for Chrome(https://chrome.google.com/webstore/detail/fake-filler/bnjjngeaknajbdcgpfkgnonkmififhfo)
- Go to Fake Filler options/backup and restore(chrome-extension://bnjjngeaknajbdcgpfkgnonkmififhfo/options.html#/backup)
- Import the configuration from project root/fakeFillerConfig.txt
- Setup keyboard shortcuts following (chrome-extension://bnjjngeaknajbdcgpfkgnonkmififhfo/options.html#/keyboard-shortcuts)
