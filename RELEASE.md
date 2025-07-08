# Notes for release managers

(tl;dr: follow this amazing [tutorial](https://www.youtube.com/watch?v=i1kg5OUwJi8) with subtitles on)

The following instructions assume you've already
1. Generated a GPG key pair,
2. Distributed your public key to the `keyserver.ubuntu.com` key server 
3. Configured your `~/m2/settings.xml` file with your token credentials

## Step 1. Compile code to `target/`

```bash
mvn clean install
```

## Step 2. Sign these specific files with GPG

```bash
# change MAJOR.MINOR.PATCH
gpg -ab target/fsrs-MAJOR.MINOR.PATCH.pom
gpg -ab target/fsrs-MAJOR.MINOR.PATCH.jar
gpg -ab target/fsrs-MAJOR.MINOR.PATCH-sources.jar
gpg -ab target/fsrs-MAJOR.MINOR.PATCH-javadoc.jar
```

Note that GPG will ask for your password.

## Step 3. Publish to Maven Central

```bash
mvn deploy
```