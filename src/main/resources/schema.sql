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

create table if not exists product(
    id bigserial primary key,
    name varchar(255) not null unique,
    blocked boolean not null default false
);

create table if not exists license_type(
    id bigserial primary key,
    name varchar(100) not null unique,
    default_duration_in_days integer not null,
    description varchar(1000)
);

create table if not exists licenses(
    id uuid primary key,
    code varchar(64) not null unique,
    product_id bigint not null,
    type_id bigint not null,
    owner_id bigint not null,
    user_id bigint,
    first_activation_date timestamp,
    ending_date timestamp,
    device_count integer not null,
    description varchar(1000),
    status varchar(20) not null,
    blocked boolean not null default false,
    constraint fk_license_product foreign key(product_id) references product(id),
    constraint fk_license_type foreign key(type_id) references license_type(id),
    constraint fk_license_owner foreign key(owner_id) references users(id),
    constraint fk_license_user foreign key(user_id) references users(id)
);

create table if not exists device(
    id bigserial primary key,
    name varchar(255) not null,
    mac_address varchar(255) not null unique,
    user_id bigint not null,
    constraint fk_device_user foreign key(user_id) references users(id)
);

create table if not exists device_license(
    id bigserial primary key,
    license_id uuid not null,
    device_id bigint not null,
    activation_date timestamp not null,
    constraint fk_device_license_license foreign key(license_id) references licenses(id),
    constraint fk_device_license_device foreign key(device_id) references device(id),
    constraint uq_device_license unique(license_id, device_id)
);

create table if not exists license_history(
    id bigserial primary key,
    license_id uuid not null,
    user_id bigint not null,
    status varchar(40) not null,
    change_date timestamp not null,
    description varchar(1000),
    constraint fk_license_history_license foreign key(license_id) references licenses(id),
    constraint fk_license_history_user foreign key(user_id) references users(id)
);

alter table if exists licenses add column if not exists code varchar(64);
alter table if exists licenses add column if not exists product_id bigint;
alter table if exists licenses add column if not exists type_id bigint;
alter table if exists licenses add column if not exists owner_id bigint;
alter table if exists licenses add column if not exists first_activation_date timestamp;
alter table if exists licenses add column if not exists ending_date timestamp;
alter table if exists licenses add column if not exists device_count integer;
alter table if exists licenses add column if not exists description varchar(1000);

alter table if exists licenses alter column user_id drop not null;
alter table if exists licenses drop column if exists device_id;
alter table if exists licenses drop column if exists created_at;
alter table if exists licenses drop column if exists activated_at;
alter table if exists licenses drop column if exists expires_at;
alter table if exists licenses drop column if exists validity_days;

create index if not exists idx_licenses_code on licenses(code);
create index if not exists idx_licenses_user_product on licenses(user_id, product_id);
create index if not exists idx_licenses_status on licenses(status);
create index if not exists idx_device_mac on device(mac_address);
create index if not exists idx_device_license_license on device_license(license_id);
create index if not exists idx_license_history_license on license_history(license_id);

insert into product(name, blocked)
select 'ZIOVPO Antivirus', false
where not exists (select 1 from product where name = 'ZIOVPO Antivirus');

insert into license_type(name, default_duration_in_days, description)
select 'TRIAL', 7, 'Trial license'
where not exists (select 1 from license_type where name = 'TRIAL');

insert into license_type(name, default_duration_in_days, description)
select 'MONTH', 30, 'Monthly license'
where not exists (select 1 from license_type where name = 'MONTH');

insert into license_type(name, default_duration_in_days, description)
select 'YEAR', 365, 'Yearly license'
where not exists (select 1 from license_type where name = 'YEAR');

insert into license_type(name, default_duration_in_days, description)
select 'CORPORATE', 365, 'Corporate license'
where not exists (select 1 from license_type where name = 'CORPORATE');

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
