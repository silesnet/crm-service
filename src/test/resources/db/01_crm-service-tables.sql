--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.11
-- Dumped by pg_dump version 9.3.4
-- Started on 2014-05-22 07:01:44

SET statement_timeout = 0;
-- SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

SET default_with_oids = false;

--
-- TOC entry 226 (class 1259 OID 3424109)
-- Name: core_routers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE core_routers (
    id integer NOT NULL,
    name character varying(50)
);


--
-- TOC entry 225 (class 1259 OID 3424107)
-- Name: core_routers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_routers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2090 (class 0 OID 0)
-- Dependencies: 225
-- Name: core_routers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE core_routers_id_seq OWNED BY core_routers.id;


--
-- TOC entry 168 (class 1259 OID 17254)
-- Name: customers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE customers (
    id bigint NOT NULL,
    history_id bigint NOT NULL,
    public_id character varying(20) NOT NULL,
    name character varying(80) NOT NULL,
    supplementary_name character varying(40),
    street character varying(40),
    city character varying(40),
    postal_code character varying(10),
    country integer,
    email character varying(50),
    dic character varying(20),
    contract_no character varying(50),
    connection_spot character varying(100),
    inserted_on timestamp without time zone NOT NULL,
    frequency integer,
    lastly_billed timestamp without time zone,
    is_billed_after boolean,
    deliver_by_email boolean,
    deliver_copy_email character varying(100),
    deliver_by_mail boolean,
    is_auto_billing boolean,
    info character varying(150),
    contact_name character varying(50),
    phone character varying(60),
    is_active boolean,
    status integer,
    shire_id bigint,
    format integer,
    deliver_signed boolean,
    symbol character varying(20),
    updated timestamp without time zone,
    synchronized timestamp without time zone,
    account_no character varying(26),
    bank_no character varying(4),
    variable integer
);


--
-- TOC entry 222 (class 1259 OID 3424094)
-- Name: drafts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE drafts (
    id integer NOT NULL,
    type character varying(20),
    user_id character varying(50),
    data character varying(5000)
);


--
-- TOC entry 221 (class 1259 OID 3424092)
-- Name: drafts_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE drafts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2091 (class 0 OID 0)
-- Dependencies: 221
-- Name: drafts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE drafts_id_seq OWNED BY drafts.id;


--
-- TOC entry 176 (class 1259 OID 17299)
-- Name: labels; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE labels (
    id bigint NOT NULL,
    parent_id bigint,
    name character varying(255) NOT NULL,
    number smallint
);


--
-- TOC entry 182 (class 1259 OID 17317)
-- Name: network; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE network (
    id integer NOT NULL,
    name character varying(50) NOT NULL,
    info character varying(500),
    active boolean,
    type integer,
    mode integer,
    wds boolean,
    frequency integer,
    vendor character varying(50),
    azimuth character varying(50),
    ssid character varying(50),
    auth character varying(50),
    polarization integer,
    country integer,
    norm character varying(50),
    area character varying(50),
    aggregation boolean,
    width character varying(50),
    antenna character varying(50),
    power character varying(50),
    master character varying(50),
    linkto character varying(50),
    hardware character varying(50),
    tdma boolean,
    latitude character varying(50),
    longitude character varying(50),
    r_updatetime timestamp without time zone,
    r_firmware character varying(50),
    r_platform character varying(50),
    r_signal character varying(50),
    r_ccq character varying(50),
    r_txrate character varying(50),
    r_rxrate character varying(50),
    r_quality character varying(50),
    r_capacity character varying(50),
    r_lanspeed character varying(50),
    r_txpower character varying(50),
    monitoring character varying(50),
    traceroute text,
    ping character varying(50),
    r_frequency integer
);


--
-- TOC entry 183 (class 1259 OID 17323)
-- Name: network_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE network_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2092 (class 0 OID 0)
-- Dependencies: 183
-- Name: network_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE network_id_seq OWNED BY network.id;


--
-- TOC entry 224 (class 1259 OID 3424103)
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE products (
    id integer NOT NULL,
    name character varying(100),
    downlink integer,
    uplink integer,
    price integer,
    channel character varying(100),
    is_dedicated boolean,
    priority integer
);


--
-- TOC entry 223 (class 1259 OID 3424101)
-- Name: products_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2093 (class 0 OID 0)
-- Dependencies: 223
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE products_id_seq OWNED BY products.id;


--
-- TOC entry 197 (class 1259 OID 17389)
-- Name: services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE services (
    id bigint NOT NULL,
    customer_id bigint,
    period_from timestamp without time zone NOT NULL,
    period_to timestamp without time zone,
    name character varying(70) NOT NULL,
    price integer NOT NULL,
    frequency integer,
    download integer,
    upload integer,
    is_aggregated boolean,
    info character varying(150),
    replace_id bigint,
    additionalname character varying(50),
    bps character(1),
    old_id bigint
);


--
-- TOC entry 207 (class 1259 OID 17429)
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE users (
    id bigint NOT NULL,
    login character varying(255) NOT NULL,
    passwd character varying(255),
    name character varying(255) NOT NULL,
    roles character varying(255),
    key character varying(255)
);


--
-- TOC entry 1964 (class 2604 OID 3424112)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_routers ALTER COLUMN id SET DEFAULT nextval('core_routers_id_seq'::regclass);


--
-- TOC entry 1962 (class 2604 OID 3424097)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY drafts ALTER COLUMN id SET DEFAULT nextval('drafts_id_seq'::regclass);


--
-- TOC entry 1961 (class 2604 OID 17449)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY network ALTER COLUMN id SET DEFAULT nextval('network_id_seq'::regclass);


--
-- TOC entry 1963 (class 2604 OID 3424106)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY products ALTER COLUMN id SET DEFAULT nextval('products_id_seq'::regclass);


--
-- TOC entry 1966 (class 2606 OID 17457)
-- Name: customers_history_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY customers
    ADD CONSTRAINT customers_history_id_key UNIQUE (history_id);


--
-- TOC entry 1968 (class 2606 OID 17459)
-- Name: customers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY customers
    ADD CONSTRAINT customers_pkey PRIMARY KEY (id);


--
-- TOC entry 1973 (class 2606 OID 17467)
-- Name: network_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY network
    ADD CONSTRAINT network_name_key UNIQUE (name);


--
-- TOC entry 1976 (class 2606 OID 17481)
-- Name: services_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY services
    ADD CONSTRAINT services_pkey PRIMARY KEY (id);


--
-- TOC entry 1971 (class 2606 OID 17489)
-- Name: sis_label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY labels
    ADD CONSTRAINT sis_label_pkey PRIMARY KEY (id);


--
-- TOC entry 1978 (class 2606 OID 17493)
-- Name: sis_user_login_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT sis_user_login_key UNIQUE (login);


--
-- TOC entry 1980 (class 2606 OID 17495)
-- Name: sis_user_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT sis_user_name_key UNIQUE (name);


--
-- TOC entry 1982 (class 2606 OID 17497)
-- Name: sis_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT sis_user_pkey PRIMARY KEY (id);


--
-- TOC entry 1974 (class 1259 OID 17509)
-- Name: customer_index2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX customer_index2 ON services USING btree (customer_id);


--
-- TOC entry 1969 (class 1259 OID 17511)
-- Name: label_parent_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX label_parent_index ON labels USING btree (parent_id);


--
-- TOC entry 1984 (class 2606 OID 17544)
-- Name: fk5235105e31f2d3d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY services
    ADD CONSTRAINT fk5235105e31f2d3d FOREIGN KEY (customer_id) REFERENCES customers(id);


--
-- TOC entry 1983 (class 2606 OID 17549)
-- Name: fk600e7c55793cd404; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY customers
    ADD CONSTRAINT fk600e7c55793cd404 FOREIGN KEY (shire_id) REFERENCES labels(id);


-- Completed on 2014-05-22 07:01:46

--
-- PostgreSQL database dump complete
--

