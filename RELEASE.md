# Notes for release managers

(tl;dr: follow this amazing [tutorial](https://www.youtube.com/watch?v=i1kg5OUwJi8) with subtitles on)

The following instructions assume you've already
1. Generated a GPG key pair,
2. Distributed your public key to the `keyserver.ubuntu.com` key server 
3. Configured your `~/m2/settings.xml` file with your token credentials

## Step 1. Build, sign and deploy to Maven Central

This can all be done in executing the following command:

```bash
mvn deploy
```

(Note that GPG will ask for your password.)

## Step 2. Publish on Maven Central

Once your deployment is valideated on the [Deployments page](https://central.sonatype.com/publishing/deployments), press Publish. You will then have to wait ~15 minutes for the Component to be published.