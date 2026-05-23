create table if not exists users(
    id bigserial primary key,
    username varchar(50) not null unique,
    password varchar(200) not null,
    role varchar(20) not null
);

create table if not exists user_sessions(
    id uuid primary key,
    username varchar(50) not null,
    device_id varchar(100),
    access_token varchar(512),
    refresh_token varchar(512),
    access_token_expiry timestamp,
    refresh_token_expiry timestamp,
    status varchar(20)
);

create index if not exists idx_user_sessions_refresh on user_sessions(refresh_token);
create index if not exists idx_user_sessions_username on user_sessions(username);

create table if not exists licenses(
    id uuid primary key,
    user_id bigint not null,
    device_id varchar(100) not null,
    created_at timestamp not null,
    activated_at timestamp,
    expires_at timestamp,
    validity_days integer not null,
    status varchar(20) not null,
    blocked boolean not null default false,
    constraint fk_license_user foreign key(user_id) references users(id)
);

create index if not exists idx_licenses_user_device on licenses(user_id, device_id);
create index if not exists idx_licenses_status on licenses(status);

create table if not exists antivirus_signatures(
    id uuid primary key,
    name varchar(255) not null,
    version varchar(100) not null,
    pattern varchar(4000) not null,
    description varchar(1000),
    status varchar(20) not null,
    digital_signature varchar(1000) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create index if not exists idx_antivirus_signatures_status on antivirus_signatures(status);
create index if not exists idx_antivirus_signatures_updated_at on antivirus_signatures(updated_at);

create table if not exists antivirus_signature_history(
    id uuid primary key,
    signature_id uuid not null,
    action varchar(20) not null,
    name varchar(255) not null,
    version varchar(100) not null,
    pattern varchar(4000) not null,
    description varchar(1000),
    status varchar(20) not null,
    digital_signature varchar(1000) not null,
    changed_at timestamp not null,
    constraint fk_signature_history_signature foreign key(signature_id) references antivirus_signatures(id)
);

create index if not exists idx_signature_history_signature on antivirus_signature_history(signature_id);

create table if not exists signature_audit(
    id uuid primary key,
    signature_id uuid not null,
    action varchar(20) not null,
    username varchar(100) not null,
    created_at timestamp not null,
    constraint fk_signature_audit_signature foreign key(signature_id) references antivirus_signatures(id)
);

create index if not exists idx_signature_audit_signature on signature_audit(signature_id);

alter table if exists antivirus_signatures
    add column if not exists file_object_key varchar(512);

alter table if exists antivirus_signatures
    add column if not exists file_original_name varchar(255);

alter table if exists antivirus_signatures
    add column if not exists file_content_type varchar(255);

alter table if exists antivirus_signatures
    add column if not exists file_size bigint;

alter table if exists antivirus_signatures
    add column if not exists file_sha256 varchar(64);
