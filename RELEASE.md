# Notes for release managers

(tl;dr: follow this [tutorial](https://www.youtube.com/watch?v=nd2ULXyBaV8))

The following instructions assume you've already
1. Generated a GPG key pair,
2. Distributed your public key to the `keyserver.ubuntu.com` key server
3. Configured your `~/.gradle/gradle.properties` file with your credentials

## Step 1. Deploy to Maven Central

```bash
./gradlew publishToMavenCentral --no-configuration-cache
```

## Step 2. Monitor release on Maven Central

Once your deployment is validated on the [Deployments page](https://central.sonatype.com/publishing/deployments), it will automatically attempt to publish a new release. You will then have to wait ~15 minutes for the Component to be published.