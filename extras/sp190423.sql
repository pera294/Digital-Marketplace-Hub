
CREATE TABLE [Artikal]
( 
	[IdArtikal]          integer  IDENTITY  NOT NULL ,
	[Naziv]              varchar(100)  NOT NULL ,
	[IdProdavnica]       integer  NULL ,
	[Cena]               decimal(10,3)  NOT NULL ,
	[Kolicina]           decimal(10,3)  NOT NULL 
	CONSTRAINT [DefaultNula_1531429866]
		 DEFAULT  0
)
go

CREATE TABLE [Grad]
( 
	[IdGrad]             integer  IDENTITY  NOT NULL ,
	[Naziv]              varchar(100)  NOT NULL 
)
go

CREATE TABLE [Kupac]
( 
	[IdKupac]            integer  IDENTITY  NOT NULL ,
	[Ime]                varchar(100)  NULL ,
	[GradKupca]          integer  NOT NULL ,
	[Novac]              decimal(10,3)  NULL 
	CONSTRAINT [DefaultNula_686877109]
		 DEFAULT  0
)
go

CREATE TABLE [Porudzbina]
( 
	[IdPorudzbina]       integer  IDENTITY  NOT NULL ,
	[Stanje]             varchar(100)  NOT NULL 
	CONSTRAINT [stanjedefault_187827908]
		 DEFAULT  'created'
	CONSTRAINT [Stanje_198753646]
		CHECK  ( [Stanje]='created' OR [Stanje]='sent' OR [Stanje]='arrived' ),
	[IdKupac]            integer  NOT NULL ,
	[DatumPotvrdjena]    datetime  NULL ,
	[DatumPoslataDoKupca] datetime  NULL ,
	[DatumStiglaDoKupca] datetime  NULL ,
	[UkupnaCena]         decimal(10,3)  NULL ,
	[PDV]                integer  NULL ,
	[TrenutnaLokacija]   integer  NULL ,
	[Preostalo]          integer  NULL 
)
go

CREATE TABLE [PovezaniGradovi]
( 
	[IdGrad1]            integer  NOT NULL ,
	[IdGrad2]            integer  NOT NULL ,
	[RazdaljinaDani]     integer  NOT NULL 
)
go

CREATE TABLE [Prodavnica]
( 
	[IdProdavnica]       integer  IDENTITY  NOT NULL ,
	[Naziv]              varchar(100)  NULL ,
	[IdGrad]             integer  NOT NULL ,
	[Popust]             integer  NULL 
	CONSTRAINT [DefaultNula_1639251421]
		 DEFAULT  0,
	[Profit]             decimal(10,3)  NULL 
	CONSTRAINT [DefaultNula_1689513683]
		 DEFAULT  0
)
go

CREATE TABLE [Stavka]
( 
	[IdStavka]           integer  IDENTITY  NOT NULL ,
	[IdPorudzbina]       integer  NOT NULL ,
	[IdArtikal]          integer  NOT NULL ,
	[Kolicina]           integer  NULL ,
	[Cena]               decimal(10,3)  NULL 
)
go

CREATE TABLE [Transakcija]
( 
	[IdTransakcija]      integer  IDENTITY  NOT NULL ,
	[IdKupac]            integer  NULL ,
	[IdProdavnica]       integer  NULL ,
	[IdPorudzbina]       integer  NULL ,
	[Sistem]             decimal(10,3)  NULL ,
	[Novac]              decimal(10,3)  NULL ,
	[Datum]              datetime  NULL 
)
go

ALTER TABLE [Artikal]
	ADD CONSTRAINT [XPKArtikal] PRIMARY KEY  CLUSTERED ([IdArtikal] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XPKGrad] PRIMARY KEY  CLUSTERED ([IdGrad] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XAK1Grad] UNIQUE ([Naziv]  ASC)
go

ALTER TABLE [Kupac]
	ADD CONSTRAINT [XPKKupac] PRIMARY KEY  CLUSTERED ([IdKupac] ASC)
go

ALTER TABLE [Porudzbina]
	ADD CONSTRAINT [XPKPorudzbina] PRIMARY KEY  CLUSTERED ([IdPorudzbina] ASC)
go

ALTER TABLE [PovezaniGradovi]
	ADD CONSTRAINT [XPKPovezaniGradovi] PRIMARY KEY  CLUSTERED ([IdGrad1] ASC,[IdGrad2] ASC)
go

ALTER TABLE [Prodavnica]
	ADD CONSTRAINT [XPKProdavnica] PRIMARY KEY  CLUSTERED ([IdProdavnica] ASC)
go

ALTER TABLE [Prodavnica]
	ADD CONSTRAINT [XAK1Prodavnica] UNIQUE ([Naziv]  ASC)
go

ALTER TABLE [Stavka]
	ADD CONSTRAINT [XPKStavka] PRIMARY KEY  CLUSTERED ([IdStavka] ASC)
go

ALTER TABLE [Transakcija]
	ADD CONSTRAINT [XPKTransakcija] PRIMARY KEY  CLUSTERED ([IdTransakcija] ASC)
go


ALTER TABLE [Artikal]
	ADD CONSTRAINT [R_6] FOREIGN KEY ([IdProdavnica]) REFERENCES [Prodavnica]([IdProdavnica])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Kupac]
	ADD CONSTRAINT [R_12] FOREIGN KEY ([GradKupca]) REFERENCES [Grad]([IdGrad])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Porudzbina]
	ADD CONSTRAINT [R_7] FOREIGN KEY ([IdKupac]) REFERENCES [Kupac]([IdKupac])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Porudzbina]
	ADD CONSTRAINT [R_17] FOREIGN KEY ([TrenutnaLokacija]) REFERENCES [Grad]([IdGrad])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [PovezaniGradovi]
	ADD CONSTRAINT [R_3] FOREIGN KEY ([IdGrad1]) REFERENCES [Grad]([IdGrad])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [PovezaniGradovi]
	ADD CONSTRAINT [R_4] FOREIGN KEY ([IdGrad2]) REFERENCES [Grad]([IdGrad])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Prodavnica]
	ADD CONSTRAINT [R_5] FOREIGN KEY ([IdGrad]) REFERENCES [Grad]([IdGrad])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Stavka]
	ADD CONSTRAINT [R_10] FOREIGN KEY ([IdArtikal]) REFERENCES [Artikal]([IdArtikal])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Stavka]
	ADD CONSTRAINT [R_11] FOREIGN KEY ([IdPorudzbina]) REFERENCES [Porudzbina]([IdPorudzbina])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Transakcija]
	ADD CONSTRAINT [R_13] FOREIGN KEY ([IdKupac]) REFERENCES [Kupac]([IdKupac])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Transakcija]
	ADD CONSTRAINT [R_14] FOREIGN KEY ([IdProdavnica]) REFERENCES [Prodavnica]([IdProdavnica])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Transakcija]
	ADD CONSTRAINT [R_16] FOREIGN KEY ([IdPorudzbina]) REFERENCES [Porudzbina]([IdPorudzbina])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go
