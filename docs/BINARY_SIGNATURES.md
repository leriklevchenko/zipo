# Binary Signature API

Binary signature exports are available for authenticated users and admins:

- `GET /api/user/signatures/binary/full`
- `GET /api/user/signatures/binary/incremental?since=<ISO-8601 instant>`

Both endpoints return `multipart/mixed` with three parts:

1. `manifest.bin` - binary manifest.
2. `manifest.sig` - Base64 EDS signature of `manifest.bin`.
3. `signatures.bin` - binary signature payload.

All numeric values are written in Java `DataOutputStream` format, big-endian.
Strings are written as `int32 length` followed by UTF-8 bytes. A `null` string
is written as length `-1`. UUID values are written as two `int64` numbers:
most significant bits and least significant bits. Date/time values are written
as `int64` epoch milliseconds; absent time values are `-1`.

## Manifest

`manifest.bin` layout:

| Field | Type | Description |
| --- | --- | --- |
| magic | 4 bytes | `ZSGM` |
| formatVersion | uint16 | Current value: `1` |
| exportType | uint8 | `1 = FULL`, `2 = INCREMENTAL` |
| generatedAt | int64 | Server generation time, epoch millis |
| since | int64 | Incremental lower bound, epoch millis, or `-1` |
| recordCount | int32 | Number of records in `signatures.bin` |
| dataLength | int32 | Length of `signatures.bin` in bytes |
| dataSha256 | 32 bytes | SHA-256 hash of `signatures.bin` |

## Data

`signatures.bin` layout:

| Field | Type | Description |
| --- | --- | --- |
| magic | 4 bytes | `ZSGD` |
| formatVersion | uint16 | Current value: `1` |
| exportType | uint8 | `1 = FULL`, `2 = INCREMENTAL` |
| recordCount | int32 | Number of signature records |

Each signature record:

| Field | Type |
| --- | --- |
| id | UUID |
| name | string |
| version | string |
| pattern | string |
| description | string |
| status | uint8, enum ordinal |
| createdAt | int64 epoch millis |
| updatedAt | int64 epoch millis |
| digitalSignature | string |
