create table if not exists app_users (
                                         id bigserial primary key,
                                         username varchar(50) unique not null,
    password varchar(255) not null,
    role varchar(20) not null default 'USER',
    email varchar(100) not null
    );

create table if not exists cars (
                                    car_id        int primary key,
                                    model         varchar(100) not null,
    license_plate varchar(20) unique not null,
    price_per_km  decimal(10,2) not null check (price_per_km > 0),
    available     boolean not null default true
    );

create table if not exists users (
                                     user_id   int primary key,
                                     name      varchar(100) not null,
    email     varchar(100) unique not null,
    phone     varchar(20) unique not null
    );

create table if not exists rides (
                                     ride_id       bigserial primary key,
                                     user_id       int not null references users(user_id) on delete cascade,
    car_id        int not null references cars(car_id) on delete restrict,
    distance_km   decimal(10,2) not null check (distance_km > 0),
    duration_hours decimal(8,2) not null check (duration_hours > 0),
    status        varchar(20) not null check (status in ('CREATED', 'IN_PROGRESS', 'COMPLETED'))
    );

create unique index if not exists uq_active_ride
    on rides (car_id)
    where status = 'IN_PROGRESS';

create table if not exists payments (
                                        payment_id    bigserial primary key,
                                        ride_id       bigint unique not null references rides(ride_id) on delete cascade,
    amount        decimal(10,2) not null check (amount >= 0)
    );

create index if not exists idx_ride_user on rides(user_id);
create index if not exists idx_ride_car on rides(car_id);

create table if not exists user_sessions (
                                             id bigserial primary key,
                                             user_id bigint references app_users(id),
    refresh_token text not null,
    expires_at timestamp not null,
    created_at timestamp not null default now(),
    status varchar(20) not null default 'ACTIVE'
    );

create table if not exists product (
                                        id uuid primary key,
                                        name varchar(255) not null,
    is_blocked boolean not null default false
    );

create table if not exists license_type (
                                             id uuid primary key,
                                             name varchar(100) not null,
    default_duration_in_days int not null,
    description text
    );

create table if not exists license (
                                        id uuid primary key,
                                        code varchar(255) not null unique,
    product_id uuid not null references product(id),
    type_id uuid not null references license_type(id),
    owner_id bigint not null references app_users(id),
    user_id bigint references app_users(id),
    first_activation_date date,
    ending_date date,
    blocked boolean not null default false,
    device_count int not null default 0,
    description text,
    constraint ck_license_device_count_nonnegative check (device_count >= 0)
    );

create table if not exists device (
                                       id uuid primary key,
                                       name varchar(255) not null,
    mac_address varchar(255) not null unique,
    user_id bigint not null references app_users(id)
    );

create table if not exists device_license (
                                               id uuid primary key,
                                               license_id uuid not null references license(id),
    device_id uuid not null references device(id),
    activation_date date not null default current_date,
    constraint uk_device_license_license_device unique (license_id, device_id)
    );

create table if not exists license_history (
                                                id uuid primary key,
                                                license_id uuid not null references license(id),
    user_id bigint references app_users(id),
    status varchar(100) not null,
    change_date date not null default current_date,
    description text
    );

create index if not exists idx_license_product_id on license(product_id);
create index if not exists idx_license_type_id on license(type_id);
create index if not exists idx_license_owner_id on license(owner_id);
create index if not exists idx_license_user_id on license(user_id);
create index if not exists idx_device_user_id on device(user_id);
create index if not exists idx_device_license_license_id on device_license(license_id);
create index if not exists idx_device_license_device_id on device_license(device_id);
create index if not exists idx_license_history_license_id on license_history(license_id);