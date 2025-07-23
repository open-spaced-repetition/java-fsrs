# Notes for release managers

(tl;dr: follow this [tutorial](https://www.youtube.com/watch?v=nd2ULXyBaV8))

The following instructions assume you've already
1. Generated a GPG key pair,
2. Distributed your public key to the `keyserver.ubuntu.com` key server
3. Configured your `~/.gradle/gradle.properties` file with your credentials

## Step 1. Build, sign and deploy to Maven Central

This can all be done by executing the following command:

```bash
./gradlew publishToMavenCentral --no-configuration-cache
```

## Step 2. Publish on Maven Central

Once your deployment is valideated on the [Deployments page](https://central.sonatype.com/publishing/deployments), press Publish. You will then have to wait ~15 minutes for the Component to be published.