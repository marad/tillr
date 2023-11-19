This project uses [axion-release-plugin](https://github.com/allegro/axion-release-plugin) to create releases. It
automatically creates patch semantic versions on `./gradlew release`. To bump the major or minor version you need to
create new GIT tag which denotes newer version:

```bash
./gradlew tag v0.1.0
```