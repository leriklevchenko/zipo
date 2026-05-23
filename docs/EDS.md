# EDS key store

Production private keys must not be committed to the repository. Generate a PKCS12 key store locally and add it to GitHub Actions secrets as Base64.

```bash
keytool -genkeypair \
  -alias license-signing \
  -keyalg RSA \
  -keysize 2048 \
  -sigalg SHA256withRSA \
  -storetype PKCS12 \
  -keystore eds-keystore.p12 \
  -storepass '<store-password>' \
  -keypass '<key-password>' \
  -dname 'CN=ZIoVPO License Signing, O=ZIoVPO, C=RU' \
  -validity 3650

base64 -i eds-keystore.p12 | tr -d '\n'
```

Required GitHub Secrets:

- `EDS_KEYSTORE_BASE64` - Base64 content of `eds-keystore.p12`
- `EDS_KEYSTORE_PASSWORD` - PKCS12 store password
- `EDS_KEY_PASSWORD` - private key password
- `EDS_KEY_ALIAS` - key alias, for example `license-signing`

For local development, either use the same variables or point the app to a local file:

```env
EDS_KEYSTORE_LOCATION=file:src/test/resources/eds/test-eds.p12
EDS_KEYSTORE_PASSWORD=changeit
EDS_KEY_PASSWORD=changeit
EDS_KEY_ALIAS=license-signing
```
