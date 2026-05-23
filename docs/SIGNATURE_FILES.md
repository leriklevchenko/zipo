# Signature File Management

This optional module lets an administrator create antivirus signatures from
uploaded files and store the original files in MinIO.

## Local MinIO

Start PostgreSQL and MinIO:

```bash
docker compose up -d postgres minio minio-init
```

The `minio-init` service creates a private bucket and a separate application
user. The server uses the application access key, not the MinIO root account.

Default local values:

| Variable | Value |
| --- | --- |
| `MINIO_ENDPOINT` | `http://localhost:9000` |
| `MINIO_PUBLIC_ENDPOINT` | `http://localhost:9000` |
| `MINIO_BUCKET` | `ziovpo-signature-files` |
| `MINIO_ACCESS_KEY` | `ziovpo-app` |
| `MINIO_SECRET_KEY` | `ziovpo-app-secret-12345` |

The MinIO console is available at `http://localhost:9001`.

## API

All endpoints are under `/api/admin/**`, so only users with role `ADMIN` can
use them.

### Upload File And Create Signature

`POST /api/admin/signatures/files`

Content type: `multipart/form-data`.

Parts and parameters:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `file` | file | yes | Source signature file |
| `name` | text | no | Signature name; defaults to original filename |
| `version` | text | no | Defaults to `1.0` |
| `description` | text | no | Optional description |

The server calculates SHA-256 from file bytes and uses it as the signature
pattern. The original file is uploaded to the private MinIO bucket. The database
record stores MinIO object key, original filename, content type, file size, and
file SHA-256.

### Get Pre-Signed URLs

`POST /api/admin/signatures/files/presigned-urls`

Request:

```json
{
  "signatureIds": ["<signature uuid>"]
}
```

Response contains only signatures that have an uploaded source file. URL
expiration is configured by `MINIO_PRESIGNED_URL_EXPIRATION_SECONDS`.
