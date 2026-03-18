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