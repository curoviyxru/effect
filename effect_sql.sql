CREATE TABLE public.comments (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    text character varying(4000),
    image_id bigint,
    post_id bigint NOT NULL,
    creation_date timestamp without time zone NOT NULL
);

CREATE TABLE public.feeds (
    user_id bigint NOT NULL,
    post_id bigint NOT NULL
);

CREATE TABLE public.images (
    id bigint NOT NULL,
    url character varying(500) NOT NULL,
    width integer NOT NULL,
    height integer NOT NULL,
    file_size bigint NOT NULL
);

CREATE TABLE public.posts (
    id bigint NOT NULL,
    title character varying(200) NOT NULL,
    preview_text character varying(1000),
    full_text character varying(2000000) NOT NULL,
    image_id bigint,
    creation_date timestamp without time zone NOT NULL,
    view_count bigint NOT NULL,
    category character varying(100)
);

CREATE TABLE public.tokens (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    creation_date timestamp without time zone NOT NULL,
    access_token character varying(400) NOT NULL
);

CREATE TABLE public.users (
    id bigint NOT NULL,
    full_name character varying(100),
    username character varying(40) NOT NULL,
    creation_date timestamp without time zone NOT NULL,
    about character varying(1000),
    image_id bigint,
    password_hash character varying(400) NOT NULL
);

CREATE SEQUENCE public.comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.posts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
	
ALTER SEQUENCE public.comments_id_seq OWNED BY public.comments.id;

ALTER SEQUENCE public.images_id_seq OWNED BY public.images.id;
	
ALTER SEQUENCE public.posts_id_seq OWNED BY public.posts.id;

ALTER SEQUENCE public.tokens_id_seq OWNED BY public.tokens.id;

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;

ALTER TABLE ONLY public.comments ALTER COLUMN id SET DEFAULT nextval('public.comments_id_seq'::regclass);

ALTER TABLE ONLY public.images ALTER COLUMN id SET DEFAULT nextval('public.images_id_seq'::regclass);

ALTER TABLE ONLY public.posts ALTER COLUMN id SET DEFAULT nextval('public.posts_id_seq'::regclass);

ALTER TABLE ONLY public.tokens ALTER COLUMN id SET DEFAULT nextval('public.tokens_id_seq'::regclass);

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);

SELECT pg_catalog.setval('public.comments_id_seq', 1, false);

SELECT pg_catalog.setval('public.images_id_seq', 1, false);

SELECT pg_catalog.setval('public.posts_id_seq', 1, false);

SELECT pg_catalog.setval('public.tokens_id_seq', 1, false);

SELECT pg_catalog.setval('public.users_id_seq', 1, false);

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.feeds
    ADD CONSTRAINT feeds_pkey PRIMARY KEY (user_id, post_id);

ALTER TABLE ONLY public.images
    ADD CONSTRAINT images_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT posts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.tokens
    ADD CONSTRAINT tokens_access_token_key UNIQUE (access_token);

ALTER TABLE ONLY public.tokens
    ADD CONSTRAINT tokens_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_image_id_fkey FOREIGN KEY (image_id) REFERENCES public.images(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_post_id_fkey FOREIGN KEY (post_id) REFERENCES public.posts(id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;

ALTER TABLE ONLY public.feeds
    ADD CONSTRAINT feeds_post_id_fkey FOREIGN KEY (post_id) REFERENCES public.posts(id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;

ALTER TABLE ONLY public.feeds
    ADD CONSTRAINT feeds_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT posts_image_id_fkey FOREIGN KEY (image_id) REFERENCES public.images(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE ONLY public.tokens
    ADD CONSTRAINT tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_image_id_fkey FOREIGN KEY (image_id) REFERENCES public.images(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_check CHECK (NOT ((TRIM(username) IS NULL) OR (CHAR_LENGTH(TRIM(username)) = 0)));
