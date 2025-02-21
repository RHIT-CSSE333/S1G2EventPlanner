USE [master]
GO
--ALTER DATABASE EventPlannerS1G2_TEST SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
--DROP DATABASE EventPlannerS1G2_TEST;
GO
/****** Object:  Database [EventPlannerS1G2_TEST]    Script Date: 2/21/2025 12:20:21 AM ******/
CREATE DATABASE [EventPlannerS1G2_TEST]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'EventPlannerTESTData', FILENAME = N'c:\var\opt\mssql\data\EventPlannerS1G2_TEST.mdf' , SIZE = 20480KB , MAXSIZE = 92160KB , FILEGROWTH = 12%)
 LOG ON 
( NAME = N'EventPlannerTESTLog', FILENAME = N'c:\var\opt\mssql\data\EventPlannerS1G2_TEST.ldf' , SIZE = 51200KB , MAXSIZE = 51200KB , FILEGROWTH = 17%)
 WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET COMPATIBILITY_LEVEL = 160
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [EventPlannerS1G2_TEST].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET ARITHABORT OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET  ENABLE_BROKER 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
--ALTER DATABASE [EventPlannerS1G2_TEST] SET TRUSTWORTHY OFF 
--GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET READ_COMMITTED_SNAPSHOT OFF 
GO
--ALTER DATABASE [EventPlannerS1G2_TEST] SET HONOR_BROKER_PRIORITY OFF 
--GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET RECOVERY FULL 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET  MULTI_USER 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET PAGE_VERIFY CHECKSUM  
GO
--ALTER DATABASE [EventPlannerS1G2_TEST] SET DB_CHAINING OFF 
--GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'EventPlannerS1G2_TEST', N'ON'
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET QUERY_STORE = ON
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [EventPlannerS1G2_TEST]
GO
/****** Object:  User [S1G2User]    Script Date: 2/21/2025 12:20:21 AM ******/
CREATE USER [S1G2User] FOR LOGIN [S1G2User] WITH DEFAULT_SCHEMA=[dbo]
GO
/****** Object:  User [

]    Script Date: 2/21/2025 12:20:21 AM ******/
CREATE USER [jinx] FOR LOGIN [jinx] WITH DEFAULT_SCHEMA=[dbo]
GO
CREATE USER [rogersj2] FOR LOGIN [rogersj2] WITH DEFAULT_SCHEMA=[dbo]
GO
/****** Object:  User [anisima]    Script Date: 2/21/2025 12:20:21 AM ******/
--CREATE USER [anisima] FOR LOGIN [anisima] WITH DEFAULT_SCHEMA=[dbo]
--GO
ALTER ROLE [db_datareader] ADD MEMBER [S1G2User]
GO
ALTER ROLE [db_datawriter] ADD MEMBER [S1G2User]
GO
ALTER ROLE [db_owner] ADD MEMBER [jinx]
GO
ALTER ROLE [db_ddladmin] ADD MEMBER [jinx]
GO
ALTER ROLE [db_datawriter] ADD MEMBER [jinx]
GO
ALTER ROLE [db_owner] ADD MEMBER [rogersj2]
GO
ALTER ROLE [db_ddladmin] ADD MEMBER [rogersj2]
GO
ALTER ROLE [db_datawriter] ADD MEMBER [rogersj2]
GO
--ALTER ROLE [db_owner] ADD MEMBER [anisima]
GO
/****** Object:  UserDefinedFunction [dbo].[EventAvailableForPublic]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   FUNCTION [dbo].[EventAvailableForPublic](
	@EventID int)
RETURNS bit
AS
BEGIN
	-- Check that a) the event should be public and b) the deadline for registration has not passed
	DECLARE @RegistrationDeadline datetime,
			@IsPublic bit,
			@ReturnValue bit

	SET @ReturnValue = 0

	SELECT @RegistrationDeadline = RegistrationDeadline, @IsPublic = isPublic
	FROM [Event]
	WHERE ID = @EventID

	IF @RegistrationDeadline > GETUTCDATE()
		AND @IsPublic = 1
	BEGIN
		SET @ReturnValue = 1
	END
	
	RETURN @ReturnValue
END




--EXEC ShowAvailableEvents
GO
/****** Object:  Table [dbo].[Event]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Event](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](100) NOT NULL,
	[StartTime] [datetime] NOT NULL,
	[EndTime] [datetime] NOT NULL,
	[VenueID] [int] NULL,
	[isPublic] [bit] NOT NULL,
	[Price] [int] NOT NULL,
	[RegistrationDeadline] [datetime] NOT NULL,
	[PaymentStatus] [bit] NOT NULL,
	[PaymentId] [char](50) NOT NULL,
	[CheckInId] [char](50) NULL,
 CONSTRAINT [PK__Event__3214EC27D39D4365] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_EventCheckInId] UNIQUE NONCLUSTERED 
(
	[CheckInId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_EventPaymentId] UNIQUE NONCLUSTERED 
(
	[PaymentId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_Name_StartTime_VenueID] UNIQUE NONCLUSTERED 
(
	[Name] ASC,
	[StartTime] ASC,
	[VenueID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Venue]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Venue](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](50) NOT NULL,
	[MaxCapacity] [int] NOT NULL,
	[PricingType] [tinyint] NOT NULL,
	[Price] [decimal](10, 2) NOT NULL,
	[State] [varchar](20) NOT NULL,
	[City] [varchar](30) NOT NULL,
	[StreetAddress] [nvarchar](100) NOT NULL,
	[ZipCode] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_Venue] UNIQUE NONCLUSTERED 
(
	[Name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_Venue_Name] UNIQUE NONCLUSTERED 
(
	[Name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[AvailableEventsForPublic]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO




CREATE      VIEW [dbo].[AvailableEventsForPublic]
AS
-- View provides information about events available for public right now
-- We need to have the name and timing for the event, registration deadline, name and address for the venue, and price for the event
SELECT e.Id, e.Name, e.RegistrationDeadline, e.StartTime, e.EndTime, e.Price, v.Name as VenueName, v.Id AS VenueId, v.MaxCapacity, 
		v.StreetAddress + ', ' + v. City + ', ' + v.State + ' ' + CAST(v.ZipCode AS nvarchar(10)) as VenueAddress
FROM Event e
JOIN Venue v ON e.VenueID = v.ID
WHERE dbo.EventAvailableForPublic(e.ID) = 1
GO
/****** Object:  View [dbo].[VenueDetails]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   VIEW [dbo].[VenueDetails]
AS
SELECT 
    [ID],
    [Name],
    [MaxCapacity],
    [PricingType],
    [Price],
    [State],
    [City],
    [StreetAddress],
    [ZipCode]
FROM 
    [Venue];
GO
/****** Object:  Table [dbo].[AttendsEvent]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[AttendsEvent](
	[PersonID] [int] NOT NULL,
	[EventID] [int] NOT NULL,
	[Invited] [tinyint] NULL,
	[RSVPStatus] [tinyint] NOT NULL,
	[Attendance] [tinyint] NULL,
	[PaymentStatus] [bit] NOT NULL,
	[PaymentId] [char](50) NOT NULL,
 CONSTRAINT [PK__AttendsE__BDBBB702E2C746CF] PRIMARY KEY CLUSTERED 
(
	[PersonID] ASC,
	[EventID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_GuestPaymentId] UNIQUE NONCLUSTERED 
(
	[PaymentId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[EventService]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[EventService](
	[EventID] [int] NOT NULL,
	[ServiceID] [int] NOT NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HostEvents]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HostEvents](
	[PersonID] [int] NOT NULL,
	[EventID] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[PersonID] ASC,
	[EventID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PendingEventInvitation]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PendingEventInvitation](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[PersonEmail] [nvarchar](50) NOT NULL,
	[EventId] [int] NOT NULL,
	[InvitationId] [char](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[InvitationId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[PersonEmail] ASC,
	[EventId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Person]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Person](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[Email] [nvarchar](50) NOT NULL,
	[PhoneNo] [char](10) NOT NULL,
	[FirstName] [nvarchar](20) NOT NULL,
	[MInit] [char](1) NULL,
	[LastName] [nvarchar](20) NOT NULL,
	[DOB] [date] NOT NULL,
	[PasswordHash] [nvarchar](128) NOT NULL,
	[PasswordSalt] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UX_Email] UNIQUE NONCLUSTERED 
(
	[Email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Reviews]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Reviews](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[PersonID] [int] NOT NULL,
	[VenueID] [int] NULL,
	[EventID] [int] NULL,
	[Title] [nvarchar](50) NULL,
	[Rating] [tinyint] NOT NULL,
	[Description] [nvarchar](max) NULL,
	[PostedOn] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Service]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Service](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](50) NOT NULL,
	[Description] [nvarchar](max) NOT NULL,
	[Price] [money] NOT NULL,
	[VendorID] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_Name_VendorID] UNIQUE NONCLUSTERED 
(
	[Name] ASC,
	[VendorID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Transaction]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Transaction](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[PersonID] [int] NOT NULL,
	[Type] [tinyint] NOT NULL,
	[Amount] [int] NOT NULL,
	[PaidOn] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Vendor]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Vendor](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](20) NULL,
PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_Vendor] UNIQUE NONCLUSTERED 
(
	[Name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_Vendor_Name] UNIQUE NONCLUSTERED 
(
	[Name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[AttendsEvent] ADD  CONSTRAINT [DF__AttendsEv__Invit__6E01572D]  DEFAULT ((1)) FOR [Invited]
GO
ALTER TABLE [dbo].[AttendsEvent] ADD  CONSTRAINT [DF__AttendsEv__RSVPS__6EF57B66]  DEFAULT ((0)) FOR [RSVPStatus]
GO
ALTER TABLE [dbo].[AttendsEvent] ADD  DEFAULT ((0)) FOR [PaymentStatus]
GO
ALTER TABLE [dbo].[Event] ADD  CONSTRAINT [DF_Event_PaymentStatus]  DEFAULT ((0)) FOR [PaymentStatus]
GO
ALTER TABLE [dbo].[Person] ADD  DEFAULT ('') FOR [PasswordHash]
GO
ALTER TABLE [dbo].[Person] ADD  DEFAULT ('') FOR [PasswordSalt]
GO
ALTER TABLE [dbo].[Reviews] ADD  CONSTRAINT [DF_Reviews_PostedOn]  DEFAULT (getdate()) FOR [PostedOn]
GO
ALTER TABLE [dbo].[AttendsEvent]  WITH CHECK ADD  CONSTRAINT [FK__AttendsEv__Event__5812160E] FOREIGN KEY([EventID])
REFERENCES [dbo].[Event] ([ID])
GO
ALTER TABLE [dbo].[AttendsEvent] CHECK CONSTRAINT [FK__AttendsEv__Event__5812160E]
GO
ALTER TABLE [dbo].[AttendsEvent]  WITH CHECK ADD  CONSTRAINT [FK__AttendsEv__Perso__571DF1D5] FOREIGN KEY([PersonID])
REFERENCES [dbo].[Person] ([ID])
GO
ALTER TABLE [dbo].[AttendsEvent] CHECK CONSTRAINT [FK__AttendsEv__Perso__571DF1D5]
GO
ALTER TABLE [dbo].[EventService]  WITH CHECK ADD  CONSTRAINT [FK__EventServ__Event__5AEE82B9] FOREIGN KEY([EventID])
REFERENCES [dbo].[Event] ([ID])
GO
ALTER TABLE [dbo].[EventService] CHECK CONSTRAINT [FK__EventServ__Event__5AEE82B9]
GO
ALTER TABLE [dbo].[EventService]  WITH CHECK ADD FOREIGN KEY([ServiceID])
REFERENCES [dbo].[Service] ([ID])
GO
ALTER TABLE [dbo].[HostEvents]  WITH CHECK ADD  CONSTRAINT [FK__HostEvent__Event__4D94879B] FOREIGN KEY([EventID])
REFERENCES [dbo].[Event] ([ID])
GO
ALTER TABLE [dbo].[HostEvents] CHECK CONSTRAINT [FK__HostEvent__Event__4D94879B]
GO
ALTER TABLE [dbo].[HostEvents]  WITH CHECK ADD FOREIGN KEY([PersonID])
REFERENCES [dbo].[Person] ([ID])
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD  CONSTRAINT [FK__Reviews__EventID__114A936A] FOREIGN KEY([EventID])
REFERENCES [dbo].[Event] ([ID])
GO
ALTER TABLE [dbo].[Reviews] CHECK CONSTRAINT [FK__Reviews__EventID__114A936A]
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD FOREIGN KEY([PersonID])
REFERENCES [dbo].[Person] ([ID])
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD FOREIGN KEY([VenueID])
REFERENCES [dbo].[Venue] ([ID])
GO
ALTER TABLE [dbo].[Service]  WITH CHECK ADD FOREIGN KEY([VendorID])
REFERENCES [dbo].[Vendor] ([ID])
GO
ALTER TABLE [dbo].[Transaction]  WITH CHECK ADD FOREIGN KEY([PersonID])
REFERENCES [dbo].[Person] ([ID])
GO
ALTER TABLE [dbo].[Event]  WITH CHECK ADD  CONSTRAINT [CK__Event__Price__04E4BC85] CHECK  (([Price]>=(0)))
GO
ALTER TABLE [dbo].[Event] CHECK CONSTRAINT [CK__Event__Price__04E4BC85]
GO
ALTER TABLE [dbo].[Person]  WITH CHECK ADD  CONSTRAINT [CX_PhoneFormat] CHECK  (([PhoneNo] like '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'))
GO
ALTER TABLE [dbo].[Person] CHECK CONSTRAINT [CX_PhoneFormat]
GO
ALTER TABLE [dbo].[Person]  WITH CHECK ADD  CONSTRAINT [ImpossibleBirthday] CHECK  (([DOB]<=getdate()))
GO
ALTER TABLE [dbo].[Person] CHECK CONSTRAINT [ImpossibleBirthday]
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD CHECK  (([VenueID] IS NULL AND [EventID] IS NOT NULL OR [VenueID] IS NOT NULL AND [EventID] IS NULL))
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD CHECK  (([Rating]>=(1) AND [Rating]<=(5)))
GO
/****** Object:  StoredProcedure [dbo].[AddPendingEventInvitation]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[AddPendingEventInvitation]
(
	@Email nvarchar(50),
	@EventId int,
	@InvitationId char(50)
)
AS
BEGIN

	/*
	-------------------------------------------------------------------------------------------------------
    Stored Procedure: AddPendingEventInvitation

    Purpose:
    This procedure adds a pending event invitation. It should be called when a user tried to invite
	a person not already registered in the system to an event. It stores a unique identifier of the invitation 
	to send the invitation to the invited user when they sign up.

	Parameters:
		@Email				nvarchar(50)
		@EventID			int
		@InvitationId		char(50)

	Returns:
		0 when the pending invitation is successfully added

	Throws:
		50001 if any of the arguments is null
		50002 when a non-existent EventID is given

	-------------------------------------------------------------------------------------------------------
	*/

	IF (@Email IS NULL)
	BEGIN;
		THROW 50001, 'Email cannot be null', 1;
	END

	IF (@EventID IS NULL)
	BEGIN;
		THROW 50001, 'EventID cannot be null', 2;
	END

	IF (@InvitationId IS NULL)
	BEGIN;
		THROW 50001, 'InvitationId cannot be null', 3;
	END

	IF @EventID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Event WHERE ID = @EventID)
	BEGIN;
        THROW 50002, 'Error: Event does not exist.', 1;
	END

	INSERT INTO PendingEventInvitation(PersonEmail, EventId, InvitationId)
	VALUES(@Email, @EventId, @InvitationId)

	RETURN 0

END

GO
/****** Object:  StoredProcedure [dbo].[AddReview]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE    PROCEDURE [dbo].[AddReview] (
	@PersonID int,
	@VenueID int,
	@EventID int,
	@Title varchar(50) = null, --Title can be blank and will default to such
	@Rating tinyint,
	@Description nvarchar(MAX) = null --Same with title, description can be null
)
AS
BEGIN
		
	/*
	-------------------------------------------------------------------------------------------------------
    Stored Procedure: AddReview

    Purpose:
    This procedure inserts a new review written by the person with @PersonID about the vendor with 
	@VendorID. ReviewType, Date, and Rating is required, but not Description or Title

	Parameters:
		@PersonID			int
		@VenueID			int
		@EventID			int
		@Title				varchar(50)
		@Rating				tinyint
		@Description		nvarchar(MAX)

	Returns:
		0 when the review is successfully added

	Throws:
		50001 when a non-nullable field is null
		50002 when a non-existent VenueID or EventID or PersonID is given
		50004 when a review between the vendor and person already exists
		52002 when both VebueID and EventID are supplied
		52003 when the date the review is left is in the future
		52004 when the rating isn't between 1 and 5

	-------------------------------------------------------------------------------------------------------
	*/

	-- If the required fields (VendorId, PersonID, ReviewType, Rating, and Date) are null, throw an error
	IF @PersonID IS NULL
	BEGIN;
		THROW 50001, 'PersonID cannot be null', 1;
	END
	IF @Rating IS NULL
	BEGIN;
		THROW 50001, 'Rating cannot be null', 1;
	END

	IF (@EventID IS NULL AND @VenueID IS NULL)
	BEGIN;
		THROW 50001, 'EventID and VenueID cannot both be null', 1;
	END

	-- check if only one of VenueID and EventID is supplied
	IF (@EventID IS NOT NULL AND @VenueID IS NOT NULL)
	BEGIN;
		THROW 52002, 'EventID and VenueID cannot both be not null', 1;
	END

    -- check if VenueID exists or not
    IF @VenueID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Venue WHERE ID = @VenueID)
	BEGIN;
        THROW 50002, 'Error: Venue does not exist.', 2;
	END

	-- check if EventID exists or not
    IF @EventID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Event WHERE ID = @EventID)
	BEGIN;
        THROW 50002, 'Error: Event does not exist.', 2;
	END

	-- check if PersonID exists or not
    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @PersonID)
	BEGIN;
        THROW 50002, 'Error: Person does not exist.', 2;
	END

	-- check to see if the review meets uniqueness requirement
	IF EXISTS(SELECT 1 FROM Reviews WHERE (((VenueID IS NOT NULL AND VenueID = @VenueID) AND PersonID = @PersonID) OR 
										  ((EventID IS NOT NULL AND EventID = @EventID) AND PersonID = @PersonID)))
	BEGIN;
		THROW 50004, 'Error: You have already reviewed this', 3;
	END
    
	-- Check if the rating is between 1-5
	IF NOT (@Rating >= 1 AND @Rating <= 5)
		THROW 52004, 'Error: The rating must be between 1 and 5', 4;

    -- Insert values
    INSERT INTO Reviews (PersonID, VenueID, EventID, Title, Rating, [Description], PostedOn)
    VALUES (@PersonID, @VenueID, @EventID, @Title, @Rating, @Description, GETDATE());

	PRINT 'Successfully created Review'
	RETURN 0

END

GO
/****** Object:  StoredProcedure [dbo].[AddService]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/*
    Stored Procedure: AddService

    Purpose:
    This procedure inserts a new service into the 'Service' table.
*/

CREATE PROCEDURE [dbo].[AddService]
    @Name NVARCHAR(50),
    @Description NVARCHAR(MAX),
    @Price MONEY,
    @VendorID INT
AS
BEGIN
----------------------------------------------------------------------------------
---
---  Add service to the Service table
---
---  Parameters:
---     @Name					nvarchar(50)
---     @Description			nvarchar(MAX)
---     @Price					money
---     @VendorID				int
---
---  Returns:
---     0 on success
---
---  Throws:
---     50001 if required fields are NULL
---     50002 if VendorID does not exist
---     50004 if a service with the same name already exists for the vendor
---     53003 if the price is less than 0
---
------------------------------------------------------------------------------------


    -- Check if required fields are NULL
    IF @Name IS NULL
	BEGIN;
        THROW 50001, 'Error: Name cannot be NULL.', 1;
	END
    IF @Description IS NULL
	BEGIN;
        THROW 50001, 'Error: Description cannot be NULL.', 1;
	END
    IF @Price IS NULL
	BEGIN;
        THROW 50001, 'Error: Price cannot be NULL.', 1;
	END
    IF @VendorID IS NULL
	BEGIN;
        THROW 50001, 'Error: VendorID cannot be NULL.', 1;
	END

    -- Check if the VendorID exists
    IF NOT EXISTS (SELECT 1 FROM Vendor WHERE ID = @VendorID)
	BEGIN;
        THROW 50002, 'Error: VendorID does not exist.', 2;
	END

    -- Check if the service already exists for the same vendor
    IF EXISTS (SELECT 1 FROM Service WHERE Name = @Name AND VendorID = @VendorID)
	BEGIN;
        THROW 50004, 'Error: A service with this name already exists for this vendor.', 3;
	END

	-- Check if the price is valid
	IF @Price < 0
	BEGIN;
		THROW 53003, 'Error: The price should be >= 0', 4;
	END

    -- Insert new service
    INSERT INTO Service (Name, Description, Price, VendorID)
    VALUES (@Name, @Description, @Price, @VendorID);

	PRINT 'Sucessfully added a new service'
	RETURN 0
END;
GO
/****** Object:  StoredProcedure [dbo].[AddServiceToEvent]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[AddServiceToEvent](
	@EventID int,
	@ServiceID int
)
AS
BEGIN
	IF NOT EXISTS( SELECT 1 FROM [Event] WHERE ID = @EventID)	
		THROW 51000, 'Event does not exist', 1;
	IF NOT EXISTS(SELECT 1 FROM [Service] WHERE ID = @ServiceID)
		THROW 51001, 'Service does not exist', 1;
	IF EXISTS (SELECT 1 FROM [EventService] WHERE EventID = @EventID AND ServiceID = @ServiceID)
		THROW 51002, 'EventService already exists', 2;
	INSERT INTO EventService (EventID, ServiceID)
	VALUES (@EventID, @ServiceID)
END
GO
/****** Object:  StoredProcedure [dbo].[AddSuccessfulPaymentForGuests]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE       PROCEDURE [dbo].[AddSuccessfulPaymentForGuests]
(
	@PaymentId char(50),
	@PersonId int
)
AS
BEGIN
    /*
    -------------------------------------------------------------------------------------------------------
    Stored Procedure: AddSuccessfulPaymentForGuests

    Purpose:
    Marks event as paid and adds a new record to the Payments table

    Parameters:
		@PaymentId				char(50)
		@PersonId				int

    Returns:
        0 on success

    Throws:
        50001 - Required fields cannot be NULL
		50002 - Event does not exist
		50003 - Person does not exist

    -------------------------------------------------------------------------------------------------------
    */

	-- check if input is null
    IF @PaymentId IS NULL
        THROW 50001, 'Error: PaymentID cannot be NULL.', 1;
    -- check if Event exists
    IF NOT EXISTS (SELECT 1 FROM [AttendsEvent] WHERE PaymentId = @PaymentId)
        THROW 50002, 'Error: Event does not exist.', 2;
	-- check if person exists
    IF NOT EXISTS (SELECT 1 FROM [Person] WHERE ID = @PersonId)
        THROW 50003, 'Error: Person does not exist.', 3;

	DECLARE @Price decimal(10, 2)
	SELECT @Price = e.Price FROM AttendsEvent ae
	JOIN [Event] e ON e.Id = ae.EventID
	WHERE ae.PaymentId = @PaymentId

	DECLARE @Now datetime
	SET @Now = GETDATE()

	-- Update the paid state
	UPDATE AttendsEvent
	SET PaymentStatus = 1, RSVPStatus = 0
	WHERE PaymentID = @PaymentId

	-- Create new transaction record
	INSERT INTO [Transaction](PersonID, [Type], Amount, PaidOn)
	VALUES (@PersonId, 1, @Price, @Now)

    RETURN 0;
END;
GO
/****** Object:  StoredProcedure [dbo].[GetFinancialInfoForHost]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE     PROCEDURE [dbo].[GetFinancialInfoForHost]
	@EventID int,
    @Price decimal(10, 2) OUTPUT,
	@PaymentId char(50) OUTPUT
AS
BEGIN
    /*
    -------------------------------------------------------------------------------------------------------
    Stored Procedure: GetFinancialInfoForHost

    Purpose:
    Calculates the price the host needs to pay to create an event

    Parameters:
        @EventID                int
        @Price	                decimal(10, 2)	OUTPUT
		@PaymentId				char(50)		OUTPUT

    Returns:
        0 on success

    Throws:
        50001 - Required fields cannot be NULL
		50002 - EventID does not exist
		50004 - Pricing type is not hourly or daily

    -------------------------------------------------------------------------------------------------------
    */

	-- check if input is null
    IF @EventID IS NULL
        THROW 50001, 'Error: EventID cannot be NULL.', 1;
    -- check if Event exists
    IF NOT EXISTS (SELECT 1 FROM [Event] WHERE ID = @EventID)
        THROW 50002, 'Error: Event does not exist.', 2;

	DECLARE @ServicePrice decimal(10,2)
	DECLARE @VenuePrice decimal(10, 2)
	DECLARE @PricingType int
	DECLARE @StartTime datetime
	DECLARE @EndTime datetime
	DECLARE @EventPaymentId char(50)
    
    SELECT @VenuePrice = v.Price, @PricingType = v.PricingType, @StartTime = e.StartTime, @EndTime = e.EndTime, @EventPaymentId = e.PaymentId
    FROM [Event] e
    JOIN Venue v ON v.Id = e.VenueID
    WHERE e.Id = @EventId

	SELECT @ServicePrice = SUM(price)
	FROM [Service] s
	JOIN [EventService] es ON s.ID = es.ServiceID
	WHERE es.EventID = @EventID

	IF @PricingType = 0         -- Hourly pricing
    BEGIN
        SET @Price = CEILING(CAST(DATEDIFF(MINUTE, @StartTime, @EndTime) AS FLOAT) / 60) * @VenuePrice;
    END
    ELSE IF @PricingType = 1    -- Daily pricing
    BEGIN
        SET @Price = ((DATEDIFF(DAY, @StartTime, @EndTime) + 1) * @VenuePrice);
		IF (@ServicePrice IS NOT NULL AND @ServicePrice > 0)
			SET @Price = @Price + @ServicePrice;
    END
	ELSE
	BEGIN
		;throw 50004, 'Error: Unknown pricing type', 3;
	END

	SET @PaymentId = @EventPaymentId

    RETURN 0;
END;

GO

/****** Object:  StoredProcedure [dbo].[AddSuccessfulPaymentForHosts]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE     PROCEDURE [dbo].[AddSuccessfulPaymentForHosts]
(
	@PaymentId char(50),
	@PersonId int
)
AS
BEGIN
    /*
    -------------------------------------------------------------------------------------------------------
    Stored Procedure: AddSuccessfulPaymentForHosts

    Purpose:
    Marks event as paid and adds a new record to the Payments table

    Parameters:
		@PaymentId				char(50)
		@PersonId				int

    Returns:
        0 on success

    Throws:
        50001 - Required fields cannot be NULL
		50002 - Event does not exist
		50003 - Person does not exist

    -------------------------------------------------------------------------------------------------------
    */

	-- check if input is null
    IF @PaymentId IS NULL
        THROW 50001, 'Error: PaymentID cannot be NULL.', 1;
    -- check if Event exists
    IF NOT EXISTS (SELECT 1 FROM [Event] WHERE PaymentId = @PaymentId)
        THROW 50002, 'Error: Event does not exist.', 2;
	-- check if person exists
    IF NOT EXISTS (SELECT 1 FROM [Person] WHERE ID = @PersonId)
        THROW 50003, 'Error: Person does not exist.', 3;

	DECLARE @EventID int
	SELECT @EventID = ID FROM [Event] WHERE PaymentId = @PaymentId

	DECLARE @Price decimal(10, 2)
	DECLARE @Pid CHAR(50)   -- not used
	EXEC [GetFinancialInfoForHost] @EventID, @Price OUTPUT, @Pid OUTPUT

	DECLARE @Now datetime
	SET @Now = GETDATE()

	-- Update the paid state
	UPDATE [Event]
	SET PaymentStatus = 1 WHERE ID = @EventID

	-- Create new transaction record
	INSERT INTO [Transaction](PersonID, [Type], Amount, PaidOn)
	VALUES (@PersonId, 0, @Price, @Now)

    RETURN 0;
END;

GO
/****** Object:  StoredProcedure [dbo].[CancelPrivateRegistration]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[CancelPrivateRegistration]
    @PersonID INT,
    @EventID INT
AS
BEGIN

    IF @PersonID IS NULL OR @EventID IS NULL
        THROW 50001, 'PersonID and EventID cannot be NULL.', 1;

    IF NOT EXISTS (SELECT 1 FROM AttendsEvent WHERE PersonID = @PersonID AND EventID = @EventID)
        THROW 50002, 'User is not invited to this event.', 2;

	IF EXISTS (
        SELECT 1 
        FROM Event 
        WHERE ID = @EventID AND RegistrationDeadline < GETDATE()
    )
    THROW 50003, 'Registration deadline has passed. User cannot cancel the registration.', 3;

    UPDATE AttendsEvent
    SET RSVPStatus = 1
    WHERE PersonID = @PersonID AND EventID = @EventID;

    RETURN 0;
END;
GO
/****** Object:  StoredProcedure [dbo].[CancelRegistration]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE   PROCEDURE [dbo].[CancelRegistration]
(
	@PersonID int,
	@EventID int
)
AS
BEGIN
----------------------------------------------------------------------------------
---
---  Cancel person's registration for event
---
---  Parameters:
---		@PersonID	int
---		@EventID	int
---
---  Returns:
---		0 on success
---
---  Throws:
---		50001 if one or two of the params is null
---		50002 if Person or Event with specified IDs do not exist
---		50003 if a registration record for the person and event was not found
---
------------------------------------------------------------------------------------

	IF @PersonID IS NULL
	BEGIN
		;throw 50001, 'PersonID cannot be NULL', 1; 
	END

	IF @EventID IS NULL
	BEGIN
		;throw 50001, 'EventID cannot be NULL', 1; 
	END

	IF (SELECT COUNT(ID) FROM Person WHERE ID = @PersonID) = 0
	BEGIN
		;throw 50002, 'Person not found', 1;
	END

	IF (SELECT COUNT(ID) FROM Event WHERE ID = @EventID) = 0
	BEGIN
		;throw 50002, 'Event not found', 2;
	END

	IF (SELECT COUNT(EventID) FROM AttendsEvent WHERE PersonID = @PersonID AND EventID = @EventID) = 0
	BEGIN
		;throw 50003, 'Registration for these Person and Event not found', 1;
	END

	DELETE FROM AttendsEvent
	WHERE PersonID = @PersonID AND EventID = @EventID

	PRINT 'Registration deleted successfully'
	RETURN 0

END
GO
/****** Object:  StoredProcedure [dbo].[CheckIn]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[CheckIn]
(
	@PersonId int,
	@CheckInId char(50)
)
AS
BEGIN

	/*
	-------------------------------------------------------------------------------------------------------
    Stored Procedure: CheckIn

    Purpose:
    Check in for an event

	Parameters:
		@PersonId			int
		@CheckInId			int

	Throws:
		50001 if PersonId or EventId is null
		50002 when a non-existent PersonId or EventId is given

	-------------------------------------------------------------------------------------------------------
	*/

	IF @PersonID IS NULL
	BEGIN
		;throw 50001, 'PersonID cannot be NULL', 1; 
	END
	IF @CheckInId IS NULL
	BEGIN
		;throw 50001, 'CheckInID cannot be NULL', 1; 
	END

	IF (SELECT COUNT(ID) FROM Person WHERE ID = @PersonID) = 0
	BEGIN
		;throw 50002, 'Person not found', 1;
	END

	IF (SELECT COUNT(ID) FROM Event WHERE CheckInId = @CheckInId) = 0
	BEGIN
		;throw 50002, 'Event not found', 2;
	END

	DECLARE @EventId int
	SELECT @EventId = ID FROM Event WHERE CheckInId = @CheckInId

	IF NOT EXISTS (SELECT 1 FROM Event WHERE ID = @EventID AND isPublic = 0)
	BEGIN;
        THROW 50004, 'You can only check in to private events', 1;
	END

	IF NOT EXISTS (SELECT 1 FROM AttendsEvent WHERE EventID = @EventID AND PersonID = @PersonID AND Invited = 1)
	BEGIN;
		THROW 50005, 'You are not invited to the event', 1;
	END

	DECLARE @StartTime datetime
	SELECT @StartTime = StartTime FROM Event WHERE ID = @EventId

	IF @StartTime BETWEEN GETDATE() AND DATEADD(HOUR, 1, GETDATE())
	BEGIN
		-- StartDate is within the next hour
		UPDATE AttendsEvent SET Attendance = 1
		WHERE PersonID = @PersonId AND EventID = @EventId
	END
	ELSE
	BEGIN
		IF @StartTime < GETDATE()
		BEGIN;
			-- StartDate is in the past
			THROW 50006, 'Check in over', 1;
		END
		ELSE
		BEGIN;
			THROW 50007, 'Too early', 1;
		END
	END

END
GO
/****** Object:  StoredProcedure [dbo].[InviteUserToEvent]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[InviteUserToEvent]
    @EventID INT,
    @PersonID INT,
	@PaymentId CHAR(50)
AS
BEGIN
    /*
    -------------------------------------------------------------------------------------------------------
    Stored Procedure: InviteUserToEvent

    Purpose:
    This procedure invites a user to a private event by adding an entry to `AttendsEvent` table.

    Parameters:
        @EventID    INT  -- The private event ID
        @PersonID   INT  -- The person being invited
		@PaymentID  CHAR(50)

    Returns:
        0 on success

    Throws:
        50001 - Required fields cannot be NULL
        50002 - Event does not exist
        50003 - Person does not exist
        50004 - The event is not private (Only private events allow invitations)
        50005 - The user is already invited to this event

    -------------------------------------------------------------------------------------------------------
    */

    -- check if input is null
    IF @EventID IS NULL
        THROW 50001, 'Error: EventID cannot be NULL.', 1;
    IF @PersonID IS NULL
        THROW 50001, 'Error: PersonID cannot be NULL.', 1;

    -- check if Event exists
    IF NOT EXISTS (SELECT 1 FROM Event WHERE ID = @EventID)
        THROW 50002, 'Error: Event does not exist.', 2;

    -- check if person exists
    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @PersonID)
        THROW 50003, 'Error: Person does not exist.', 3;

    -- Check if event is private
    IF NOT EXISTS (SELECT 1 FROM Event WHERE ID = @EventID AND isPublic = 0)
        THROW 50004, 'Error: Only private events can have invitations.', 4;

    -- check if the user is already invited
    IF EXISTS (SELECT 1 FROM AttendsEvent WHERE EventID = @EventID AND PersonID = @PersonID)
        THROW 50005, 'Error: This user is already invited to the event.', 5;

    INSERT INTO AttendsEvent (PersonID, EventID, Invited, RSVPStatus, Attendance, PaymentId)
    VALUES (@PersonID, @EventID, 1, 2, NULL, @PaymentId);  -- Invited=1 represents invited, RSVPStatus=2 represents no response yet

    RETURN 0;
END;
GO

create   procedure [dbo].[CompletePendingInvitation]
(
	@PersonId int,
	@InvitationId char(50),
	@PaymentId char(50)
)
AS
BEGIN

	/*
	-------------------------------------------------------------------------------------------------------
    Stored Procedure: CompletePendingInvitation

    Purpose:
    Completes pending invitation after a person signed up

	Parameters:
		@PersonId			int
		@InvitationId		char(50)
		@PaymentId			char(50)

	Throws:
		50001 if PersonId is null
		50002 when a non-existent PersonId is given

	-------------------------------------------------------------------------------------------------------
	*/

	IF (@PersonId IS NULL)
	BEGIN;
		THROW 50001, 'PersonId cannot be null', 1;
	END

	IF (@InvitationId IS NULL)
	BEGIN;
		THROW 50001, 'InvitationId cannot be null', 2;
	END

	IF NOT EXISTS (SELECT 1 FROM Person WHERE Id = @PersonId)
	BEGIN;
        THROW 50002, 'Error: Person does not exist.', 1;
	END

	IF NOT EXISTS (SELECT 1 FROM PendingEventInvitation WHERE InvitationId = @InvitationId)
	BEGIN;
        THROW 50002, 'Error: Invitation does not exist.', 2;
	END

	DECLARE @EventId int
	SELECT @EventId = EventId FROM PendingEventInvitation WHERE InvitationId = @InvitationId

	DELETE FROM PendingEventInvitation WHERE InvitationId = @InvitationId   -- Purposefully deleting before inviting so that if Invite throws, we don't keep the record
	EXEC InviteUserToEvent @EventId, @PersonId, @PaymentId 

	RETURN 0

end
GO
/****** Object:  StoredProcedure [dbo].[CompletePendingInvitation]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  StoredProcedure [dbo].[CreateEvent]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE     PROCEDURE [dbo].[CreateEvent]
    @Name NVARCHAR(100),
    @StartTime DATETIME,
    @EndTime DATETIME,
    @VenueID INT,
    @Price Decimal(10,2),
    @RegistrationDeadline DATETIME,
    @HostPersonID INT,
	@PaymentStatus BIT = 0, -- 0 represents not payed yet, 1 represents payed, will be updated in new procedure after host pay the venue
	@PaymentId CHAR(50),
	@isPublic BIT = 0,
    @CheckInId CHAR(50),
    @EventID INT OUTPUT
AS
BEGIN
    /*
    -------------------------------------------------------------------------------------------------------
    Stored Procedure: CreateEvent

    Purpose:
    This procedure creates a new private event while ensuring data integrity.

    Parameters:
        @Name                   NVARCHAR(100)
        @StartTime              DATETIME
        @EndTime                DATETIME
        @VenueID                INT
        @Price                  INT
        @RegistrationDeadline   DATETIME
        @HostPersonID           INT (Event Creator)
		@PaymentStatus		    BIT (0 - not payed yet, 1 - payed)
		@PaymentId				CHAR(50)
        @isPublic				BIT = 0 (1 - public, 0 - private)
        @CheckInId              CHAR(50)
        @EventID                INT OUTPUT (Returns the created EventID)

    Returns:
        0 on success

    Throws:
        50001 - Required fields cannot be NULL
        50002 - VenueID does not exist
		50003 - HostPersonID does not exist
		50007 if an event is double booked.
		50201 if the event starts in less than 3 hours from now
        50202 if the event lasts less than an hour
        50203 if the registration deadline is not before the event starts
        50204 if the price is less than 0

    -------------------------------------------------------------------------------------------------------
    */

	-- check if input is null
    IF @Name IS NULL
        THROW 50001, 'Error: Name cannot be NULL.', 1;
    IF @StartTime IS NULL
        THROW 50001, 'Error: StartTime cannot be NULL.', 1;
    IF @EndTime IS NULL
        THROW 50001, 'Error: EndTime cannot be NULL.', 1;
    IF @VenueID IS NULL
        THROW 50001, 'Error: VenueID cannot be NULL.', 1;
    IF @Price IS NULL
        THROW 50001, 'Error: Price cannot be NULL.', 1;
    IF @RegistrationDeadline IS NULL
        THROW 50001, 'Error: RegistrationDeadline cannot be NULL.', 1;
    IF @HostPersonID IS NULL
        THROW 50001, 'Error: HostPersonID cannot be NULL.', 1;

    -- check if Venue exists
    IF NOT EXISTS (SELECT 1 FROM Venue WHERE ID = @VenueID)
        THROW 50002, 'Error: Venue does not exist.', 2;

    -- check if HostPersonID exists
    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @HostPersonID)
        THROW 50003, 'Error: HostPersonID does not exist.', 3;

	IF EXISTS(
		SELECT 1
		FROM [Event]
		WHERE (VenueID = @VenueID) AND ((StartTime <= @StartTime AND EndTime >= @StartTime) OR (StartTime <= @EndTime AND EndTime >= @EndTime) OR (StartTime <= @EndTime AND EndTime >= @StartTime)))
	BEGIN;
		THROW 50007, 'An event already occupies this block', 1;
	END
	IF @StartTime < DATEADD(HOUR, 3, GETUTCDATE())    -- Not allowing to add events less than 3 hours in advance
	BEGIN;
		THROW 50201, 'Event should start at least three hours from now', 1;
	END
	IF @EndTime < DATEADD(HOUR, 1, @StartTime)    -- An event should last at least an hour
	BEGIN;
		THROW 50202, 'Event should last at least an hour', 1;
	END
	IF @RegistrationDeadline IS NULL   -- Default to 24 hours before the start date
	BEGIN
		SET @RegistrationDeadline = DATEADD(DAY, -1, @StartTime)
	END
	IF @RegistrationDeadline >= @StartTime
	BEGIN;
		THROW 50203, 'Registration should end before the event starts', 1;
	END
	IF @Price < 0
	BEGIN;
		THROW 50204, 'Price should be >= 0', 1;
	END

    -- create private event（isPublic = 0）
    INSERT INTO Event (Name, StartTime, EndTime, VenueID, isPublic, Price, RegistrationDeadline, PaymentStatus, PaymentId, CheckInId)
    VALUES (@Name, @StartTime, @EndTime, @VenueID, @isPublic, @Price, @RegistrationDeadline, @PaymentStatus, @PaymentId, @CheckInId);

    -- get newly created EventID
    SET @EventID = SCOPE_IDENTITY();  
	Insert INTO HostEvents (PersonID, EventID)
	VALUES (@HostPersonID, @EventID);

    RETURN 0;
END;
GO
/****** Object:  StoredProcedure [dbo].[CreatePerson]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

/*
    Stored Procedure: CreatePerson

    Purpose:
    This procedure inserts a new person in the 'Person' table while ensuring data integrity.
    It checks for duplicate emails, ensures the date of birth is not in the future, and validates
	credit card information if provided. If any condition fails, an error is thrown to prevent invalid 
	data insertion.
*/

CREATE   PROCEDURE [dbo].[CreatePerson]
    @Email NVARCHAR(50),
    @PhoneNo CHAR(10),
    @FirstName NVARCHAR(20),
    @MInit CHAR(1) = NULL,
    @LastName NVARCHAR(20),
    @DOB DATE,
	@PasswordHash NVARCHAR(128),
	@PasswordSalt NVARCHAR(50),
	@PersonID int OUTPUT

AS
BEGIN
----------------------------------------------------------------------------------
---
---  Insert or update a person in the Person table
---
---  Parameters:
---     @Email					nvarchar(50)
---     @PhoneNo				char(10)
---     @FirstName				nvarchar(20)
---     @MInit					char(1) = NULL
---     @LastName				nvarchar(20)
---     @DOB					date
---     @PasswordHash			nvarchar(128)
---		@PasswordSalt			nvarchar(50)
---     @PersonID				int OUTPUT (the ID of created Person)
---
---  Returns:
---     0 on success
---
---  Throws:
---		50208 if Person with this email exists already
---     50001 if required fields are NULL
---     50201 if Email already exists
---     50202 if Date of Birth is not in the past
---     50206 if phone number format is incorrect
---
------------------------------------------------------------------------------------

	-- check if input parameters are null
	IF @Email IS NULL
	BEGIN;
        THROW 50001, 'Error: Email cannot be NULL.', 1;
	END
	IF  @PhoneNo IS NULL
	BEGIN;
        THROW 50001, 'Error: PhoneNo cannot be NULL.', 1;
	END
	IF @FirstName IS NULL
	BEGIN;
        THROW 50001, 'Error: FirstName cannot be NULL.', 1;
	END
	IF @LastName IS NULL
	BEGIN;
        THROW 50001, 'Error: LastName cannot be NULL.', 1;
	END
	IF @DOB IS NULL
	BEGIN;
        THROW 50001, 'Error: DOB cannot be NULL.', 1;
	END
	IF @PasswordHash IS NULL
	BEGIN;
		THROW 50001, 'Error: PasswordHash cannot be null.', 1;
	END
	IF @PasswordSalt IS NULL
	BEGIN;
		THROW 50001, 'Error: PasswordSalt cannot be null.', 1;
	END

	IF EXISTS(SELECT Email FROM Person WHERE Email = @Email) 
	BEGIN;
		THROW 50208, 'Error: User with this email already exists', 1;
	END

    -- check if DOB is legal
    IF @DOB >= CAST(GETDATE() AS DATE)
	BEGIN;
        THROW 50202, 'Error: Date of Birth must be in the past.', 3;
	END

	-- check phone number formate
	IF @PhoneNo NOT LIKE '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'
	BEGIN;
		THROW 50206, 'Error: Wrong format for phone number. Expected: 9999999999', 7;
	END
	
	-- Insert values
	INSERT INTO Person (Email, PhoneNo, FirstName, MInit, LastName, DOB, PasswordHash, PasswordSalt)
	VALUES (@Email, @PhoneNo, @FirstName, @MInit, @LastName, @DOB, @PasswordHash, @PasswordSalt);
	
	SET @PersonID = SCOPE_IDENTITY();

	RETURN 0
END;
GO
/****** Object:  StoredProcedure [dbo].[CreatePrivateEvent]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[CreatePrivateEvent]
    @Name NVARCHAR(100),
    @StartTime DATETIME,
    @EndTime DATETIME,
    @VenueID INT,
    @Price Decimal(10,2),
    @RegistrationDeadline DATETIME,
    @HostPersonID INT,
	@PaymentStatus BIT = 0, -- 0 represents not payed yet, 1 represents payed, will be updated in new procedure after host pay the venue
	@PaymentId CHAR(50),
    @EventID INT OUTPUT
AS
BEGIN
    /*
    -------------------------------------------------------------------------------------------------------
    Stored Procedure: CreatePrivateEvent

    Purpose:
    This procedure creates a new private event while ensuring data integrity.

    Parameters:
        @Name                   NVARCHAR(100)
        @StartTime              DATETIME
        @EndTime                DATETIME
        @VenueID                INT
        @Price                  INT
        @RegistrationDeadline   DATETIME
        @HostPersonID           INT (Event Creator)
		@PaymentStatus		    BIT 
		@PaymentId				CHAR(50)
        @EventID                INT OUTPUT (Returns the created EventID)

    Returns:
        0 on success

    Throws:
        50001 - Required fields cannot be NULL
        50002 - VenueID does not exist
        50003 - StartTime must be in the future
        50004 - RegistrationDeadline must be before StartTime

    -------------------------------------------------------------------------------------------------------
    */

	-- check if input is null
    IF @Name IS NULL
        THROW 50001, 'Error: Name cannot be NULL.', 1;
    IF @StartTime IS NULL
        THROW 50001, 'Error: StartTime cannot be NULL.', 1;
    IF @EndTime IS NULL
        THROW 50001, 'Error: EndTime cannot be NULL.', 1;
    IF @VenueID IS NULL
        THROW 50001, 'Error: VenueID cannot be NULL.', 1;
    IF @Price IS NULL
        THROW 50001, 'Error: Price cannot be NULL.', 1;
    IF @RegistrationDeadline IS NULL
        THROW 50001, 'Error: RegistrationDeadline cannot be NULL.', 1;
    IF @HostPersonID IS NULL
        THROW 50001, 'Error: HostPersonID cannot be NULL.', 1;

    -- check if Venue exists
    IF NOT EXISTS (SELECT 1 FROM Venue WHERE ID = @VenueID)
        THROW 50002, 'Error: Venue does not exist.', 2;

    -- check if HostPersonID exists
    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @HostPersonID)
        THROW 50003, 'Error: HostPersonID does not exist.', 3;

    -- check if StartTime is future time
    IF @StartTime <= GETDATE()
        THROW 50004, 'Error: Event start time must be in the future.', 4;

    -- check if RegistrationDeadline is earlier than StartTime
    IF @RegistrationDeadline >= @StartTime
        THROW 50005, 'Error: Registration deadline must be before event start time.', 5;

    -- create private event（isPublic = 0）
    INSERT INTO Event (Name, StartTime, EndTime, VenueID, isPublic, Price, RegistrationDeadline, PaymentStatus, PaymentId)
    VALUES (@Name, @StartTime, @EndTime, @VenueID, 0, @Price, @RegistrationDeadline, @PaymentStatus, @PaymentId);

    -- get newly created EventID
    SET @EventID = SCOPE_IDENTITY();  
	Insert INTO HostEvents (PersonID, EventID)
	VALUES (@HostPersonID, @EventID);

    RETURN 0;
END;
GO
/****** Object:  StoredProcedure [dbo].[DeletePerson]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/*
    Stored Procedure: DeletePerson

    Purpose:
    This procedure deletes a person from the 'Person' table based on their email.
    If the person does not exist, an error is thrown to prevent invalid deletions.
*/

CREATE PROCEDURE [dbo].[DeletePerson]
    @Email NVARCHAR(50)
AS
BEGIN
----------------------------------------------------------------------------------
---
---  Delete person from the Person table
---
---  Parameters:
---     @Email					nvarchar(50)
---
---  Returns:
---     0 on success
---
---  Throws:
---     50003 if Email not found
---
------------------------------------------------------------------------------------

    DECLARE @PersonID INT;

    -- Get PersonID based on Email
    SELECT @PersonID = ID FROM Person WHERE Email = @Email;

    -- If Email does not exist, throw an error
    IF @PersonID IS NULL
	BEGIN;
        THROW 50003, 'Error: Email not found.', 1;
	END

    -- Delete the person record
    DELETE FROM Person WHERE ID = @PersonID;

	PRINT 'Person successfully deleted'
	RETURN 0
END;
GO
/****** Object:  StoredProcedure [dbo].[DeleteService]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/*
    Stored Procedure: DeleteService

    Purpose:
    This procedure deletes a service from the 'Service' table based on its ID.
*/

CREATE PROCEDURE [dbo].[DeleteService]
    @ServiceID INT
AS
BEGIN
----------------------------------------------------------------------------------
---
---  Delete service from the Service table
---
---  Parameters:
---     @ServiceID				int
---
---  Returns:
---     0 on success
---
---  Throws:
---     52200 if ServiceID does not exist
---
------------------------------------------------------------------------------------

    -- Check if the ServiceID exists
    IF NOT EXISTS (SELECT 1 FROM Service WHERE ID = @ServiceID)
	BEGIN;
        THROW 52200, 'Error: ServiceID does not exist.', 1;
	END

    -- Delete the service record
    DELETE FROM Service WHERE ID = @ServiceID;

	PRINT 'Service deleted successfully'
	RETURN 0
END;
GO
/****** Object:  StoredProcedure [dbo].[GetAllServices]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[GetAllServices]
AS
SELECT s.ID, s.[Description], s.[Name], s.Price, s.VendorID
FROM [Service] s
GO
/****** Object:  StoredProcedure [dbo].[GetAllVendors]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/*
	Returns all vendors 
*/
CREATE PROCEDURE [dbo].[GetAllVendors]
AS
SELECT v.ID, v.Name
FROM Vendor v
GO
/****** Object:  StoredProcedure [dbo].[GetAllVenues]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[GetAllVenues]
AS
BEGIN
    SELECT * FROM [VenueDetails];
END
GO
/****** Object:  StoredProcedure [dbo].[GetCheckInId]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[GetCheckInId]
(
	@PersonId int,
	@EventId int,
	@CheckInId char(50) OUTPUT
)
AS
BEGIN

	/*
	-------------------------------------------------------------------------------------------------------
    Stored Procedure: GetCheckInId

    Purpose:
    Get check in id for an event if user hosts this event to generate the check-in qr code

	Parameters:
		@PersonId			int
		@EventId			int
		@CheckInId			char(50) OUTPUT

	Throws:
		50001 if PersonId or EventId is null
		50002 when a non-existent PersonId or EventId is given
		50003 if the user is not authorized to get the id (doesn't host the event)

	-------------------------------------------------------------------------------------------------------
	*/

	IF @PersonID IS NULL
	BEGIN
		;throw 50001, 'PersonID cannot be NULL', 1; 
	END
	IF @EventID IS NULL
	BEGIN
		;throw 50001, 'EventID cannot be NULL', 1; 
	END

	IF (SELECT COUNT(ID) FROM Person WHERE ID = @PersonID) = 0
	BEGIN
		;throw 50002, 'Person not found', 1;
	END

	IF (SELECT COUNT(ID) FROM Event WHERE ID = @EventID) = 0
	BEGIN
		;throw 50002, 'Event not found', 2;
	END

	IF (SELECT COUNT(PersonId) FROM HostEvents WHERE PersonID = @PersonId AND EventID = @EventId) = 0
	BEGIN;
		;throw 50003, 'User not authorized to get the ID', 1;
	END

	SELECT @CheckInId = CheckInId FROM Event WHERE ID = @EventId	

END


GO
/****** Object:  StoredProcedure [dbo].[GetEmailForPendingInvitation]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[GetEmailForPendingInvitation]
(
	@InvitationId char(50),
	@Email nvarchar(50) OUTPUT
)
as
begin

	IF @InvitationId IS NULL
	BEGIN;
		THROW 50001, 'InvitationId cannot be null', 1;
	END

	SELECT @Email = PersonEmail FROM PendingEventInvitation WHERE InvitationId = @InvitationId

	IF @Email IS NULL
	BEGIN;
		THROW 55001, 'Invitation Id not found', 1;
	END

end
GO
/****** Object:  StoredProcedure [dbo].[GetEventById]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetEventById]
    @EventID INT
AS
BEGIN
    
    IF @EventID IS NULL
    BEGIN;
        THROW 50001, 'Error: Event ID cannot be NULL.', 1;
    END

    IF NOT EXISTS (SELECT 1 FROM Event WHERE ID = @EventID)
    BEGIN;
        THROW 50002, 'Error: Event does not exist.', 2;
    END
    
    SELECT 
        e.ID,
        e.Name,
        e.StartTime,
        e.EndTime,
        e.Price,
        e.VenueId,
        v.Name AS VenueName,
        v.StreetAddress AS VenueAddress,
        v.MaxCapacity,
        e.RegistrationDeadline,
        e.isPublic,
        e.PaymentStatus
    FROM 
        Event e
    INNER JOIN 
        Venue v ON e.VenueId = v.ID
    WHERE 
        e.ID = @EventID;

    RETURN 0;
END;
GO
/****** Object:  StoredProcedure [dbo].[GetEventID]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetEventID]
    @EventName NVARCHAR(255),
    @EventStartTime DATETIME,
    @VenueName NVARCHAR(255),
    @EventID INT OUTPUT
AS
BEGIN
----------------------------------------------------------------------------------
---
---  Get EventID 
---
---  Parameters:
---  @EventName					nvarchar(100)
---  @EventStartTime			datetime
---  @VenueName					nvarchar(50)
---
---  Throws:
---	    50001 if required field is null
---     50004 if event is not found
---
------------------------------------------------------------------------------------

    -- check if EventName is null
    IF @EventName IS NULL
    BEGIN;
        THROW 50001, 'Event name cannot be null or empty.', 1;
        RETURN;
    END;

    -- check if EventStartDate is null
    IF @EventStartTime IS NULL
    BEGIN;
        THROW 50001, 'Event start date cannot be null.', 1;
        RETURN;
    END;

    -- check if VenueName os null
    IF @VenueName IS NULL
    BEGIN;
        THROW 50001, 'Venue name cannot be null or empty.', 1;
        RETURN;
    END


    SELECT @EventID = e.ID 
    FROM Event e
    JOIN Venue v ON e.VenueID = v.ID
    WHERE e.Name = @EventName
      AND e.StartTime = @EventStartTime  
      AND v.Name = @VenueName;

    
    IF @EventID IS NULL
    BEGIN;
        THROW 50004, 'Error: Event not found.', 1;
        RETURN;
    END;

    PRINT 'EventID found.';
END;
GO
/****** Object:  StoredProcedure [dbo].[GetEventsByPerson]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[GetEventsByPerson]
(
	@PersonId int
)
AS 
BEGIN

	IF @PersonID IS NULL
	BEGIN
		;throw 50001, 'PersonID cannot be NULL', 1; 
	END

	IF (SELECT COUNT(ID) FROM Person WHERE ID = @PersonID) = 0
	BEGIN
		;throw 50002, 'Person not found', 1;
	END

	SELECT e.Id, e.[Name], e.StartTime, e.isPublic, v.ID as VenueId, v.[Name] AS VenueName,
			v.StreetAddress + ', ' + v. City + ', ' + v.State + ' ' + CAST(v.ZipCode AS nvarchar(10)) as VenueAddress 
	FROM AttendsEvent a
	JOIN [Event] e ON e.ID = a.EventID
	JOIN Venue v ON e.VenueID = v.ID
	WHERE a.PersonID = @PersonId
		AND a.RSVPStatus = 0;

END
GO
/****** Object:  StoredProcedure [dbo].[GetEventsForVenue]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetEventsForVenue]
(
    @VenueId INT
)
AS
BEGIN
    ----------------------------------------------------------------------------------
    -- GetEventsForVenue
    -- Purpose: Retrieve both public and private events for the specified venue,
    --          along with their payment status.
    --
    -- Parameters:
    --    @VenueId    INT
    --
    -- Returns:
    --    Event details including PaymentStatus
    --
    -- Throws:
    --    50001 if VenueId is NULL
    --    50002 if Venue does not exist
    ----------------------------------------------------------------------------------

    -- Check for NULL VenueId
    IF @VenueId IS NULL
    BEGIN;
        THROW 50001, 'VenueId cannot be NULL', 1;
    END

    -- Check if the Venue exists
    IF NOT EXISTS (SELECT 1 FROM Venue WHERE ID = @VenueId)
    BEGIN;
        THROW 50002, 'Venue not found', 2;
    END

    -- Retrieve both Public and Private Events
    SELECT 
        e.ID,
        e.Name,
        e.RegistrationDeadline,
        e.StartTime,
        e.EndTime,
        e.Price,
        v.Name AS VenueName,
        e.VenueId,
        v.MaxCapacity,
        v.StreetAddress AS VenueAddress,
		e.isPublic,
        e.PaymentStatus
    FROM 
        Event e
    JOIN 
        Venue v ON e.VenueId = v.ID
    WHERE 
        e.VenueId = @VenueId

END
GO
/****** Object:  StoredProcedure [dbo].[GetFinancialInfoForGuest]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE       PROCEDURE [dbo].[GetFinancialInfoForGuest]
	@EventID int,
	@GuestId int,
    @Price decimal(10, 2) OUTPUT,
	@PaymentId char(50) OUTPUT
AS
BEGIN
    /*
    -------------------------------------------------------------------------------------------------------
    Stored Procedure: GetFinancialInfoForGuest

    Purpose:
    Returns the price guests needs to pay to register for an event

    Parameters:
        @EventID                int
		@GuestId				int
        @Price	                decimal(10, 2)	OUTPUT
		@PaymentId				char(50)		OUTPUT

    Returns:
        0 on success

    Throws:
        50001 - Required fields cannot be NULL
		50002 - EventID or GuestID does not exist

    -------------------------------------------------------------------------------------------------------
    */

	-- check if input is null
    IF @EventID IS NULL
        THROW 50001, 'Error: EventID cannot be NULL.', 1;
    -- check if Event exists
    IF NOT EXISTS (SELECT 1 FROM [Event] WHERE ID = @EventID)
        THROW 50002, 'Error: Event does not exist.', 2;

	-- check if input is null
    IF @GuestId IS NULL
        THROW 50001, 'Error: GuestID cannot be NULL.', 3;
    -- check if person exists
    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @GuestId)
        THROW 50002, 'Error: Person does not exist.', 4;
    
    SELECT @Price = Price
    FROM [Event]
    WHERE Id = @EventId

	SELECT @PaymentId = PaymentId
    FROM AttendsEvent
    WHERE EventId = @EventId AND PersonID = @GuestId

    RETURN 0;
END;

GO
/****** Object:  StoredProcedure [dbo].[GetHostedEvents]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
Create PROCEDURE [dbo].[GetHostedEvents]
    @personID INT
AS
BEGIN
    SET NOCOUNT ON;

    /*
    -------------------------------------------------------------------------------------------------------
    Stored Procedure: GetActiveHostedEvents

    Purpose:
    Retrieves all active events hosted by a specific user using the HostEvents table. 
    Active events are defined as those where the event start time is in the future.

    Parameters:
        @personID INT -- The ID of the user hosting the events

    Returns:
        A list of active events hosted by the user

    -------------------------------------------------------------------------------------------------------
    */

    -- Check if PersonID is provided
    IF @personID IS NULL
    BEGIN;
        THROW 50001, 'Error: PersonID cannot be NULL.', 1;
    END

    -- Check if the Person exists
    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @personID)
    BEGIN;
        THROW 50002, 'Error: PersonID does not exist.', 2;
    END

    -- Retrieve active events hosted by the user
    SELECT 
        e.ID,
        e.Name,
        e.StartTime,
        e.EndTime,
        e.Price,
        e.VenueId,
        v.Name AS VenueName,
        v.StreetAddress AS VenueAddress,
        v.MaxCapacity,
        e.RegistrationDeadline,
        e.isPublic,
        e.PaymentStatus
    FROM 
        HostEvents he
    INNER JOIN 
        Event e ON he.EventID = e.ID
    INNER JOIN 
        Venue v ON e.VenueId = v.ID
    WHERE 
        he.PersonID = @personID

    RETURN 0;
END;
GO
/****** Object:  StoredProcedure [dbo].[GetInvitationsForUser]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetInvitationsForUser]
    @PersonID INT
AS
BEGIN
    IF @PersonID IS NULL
        THROW 50001, 'PersonID cannot be NULL', 1;

    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @PersonID)
        THROW 50002, 'Person does not exist', 2;

    SELECT 
        e.ID AS EventID,
        e.Name AS EventName,
        e.StartTime,
		e.EndTime,
		e.RegistrationDeadline,
		ae.RSVPStatus,
		ae.PaymentStatus
    FROM AttendsEvent ae
    JOIN Event e ON ae.EventID = e.ID
    WHERE ae.PersonID = @PersonID
		AND e.isPublic = 0          
        AND ae.Invited = 1;

    RETURN 0;
END;
GO
/****** Object:  StoredProcedure [dbo].[GetInvitedEvents]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetInvitedEvents]
(
    @PersonID INT
)
AS
BEGIN
----------------------------------------------------------------------------------
---
---  Stored Procedure: GetInvitedEvents
---
---  Purpose:
---      This procedure retrieves all private events to which a specific person has been invited,
---      including event details such as venue name, price, address, maximum capacity, 
---      and registration deadline.
---
---  Parameters:
---      @PersonID   INT - The ID of the person whose invitations will be retrieved.
---
---  Returns:
---      A result set containing the event details:
---      (EventID, EventName, StartTime, EndTime, VenueName, Address, Price, 
---       MaxCapacity, RegistrationDeadline, RSVPStatus)
---
---  Throws:
---      50001 if PersonID is NULL
---      50002 if PersonID does not exist in the Person table
----------------------------------------------------------------------------------

    -- Null check for PersonID
    IF @PersonID IS NULL
    BEGIN;
        THROW 50001, 'PersonID cannot be NULL.', 1;
    END

    -- Check if the person exists
    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @PersonID)
    BEGIN;
        THROW 50002, 'Error: Person not found.', 1;
    END

    -- Retrieve invited events with additional event and venue details
    SELECT 
        e.ID AS EventID,
        e.Name AS EventName,
        e.StartTime,
        e.EndTime,
        v.Name AS VenueName,
        v.StreetAddress,
		v.City,
		v.State,
        e.Price,
        v.MaxCapacity,
        e.RegistrationDeadline,
        ae.RSVPStatus
    FROM 
        AttendsEvent ae
    INNER JOIN 
        Event e ON ae.EventID = e.ID
    INNER JOIN 
        Venue v ON e.VenueID = v.ID
    WHERE 
        ae.PersonID = @PersonID
        AND ae.Invited = 0  -- 0 means the person is invited to the private event
        AND e.isPublic = 0  -- Ensure it's a private event

    PRINT 'Invited events retrieved successfully'
    RETURN 0
END

GO
/****** Object:  StoredProcedure [dbo].[GetInviteesRSVPStatus]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetInviteesRSVPStatus]
    @EventID INT
AS
BEGIN
    IF @EventID IS NULL
        THROW 50001, 'EventID cannot be NULL.', 1;

    IF NOT EXISTS (SELECT 1 FROM Event WHERE ID = @EventID)
        THROW 50002, 'Event does not exist.', 2;

    SELECT 
        p.ID AS PersonID,
        p.FirstName,
        p.LastName,
        p.Email,
        ae.RSVPStatus
    FROM 
        AttendsEvent ae
    INNER JOIN 
        Person p ON ae.PersonID = p.ID
    WHERE 
        ae.EventID = @EventID
        AND ae.Invited = 1;
END;
GO
/****** Object:  StoredProcedure [dbo].[GetPendingInvitations]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[GetPendingInvitations]
(
	@Email nvarchar(50)
)
AS
BEGIN

	/*
	-------------------------------------------------------------------------------------------------------
    Stored Procedure: GetPendingInvitations

    Purpose:
    Returns all pending invitation after a person signed up

	Parameters:
		@Email			nvarchar(50)

	Throws:
		50001 if PersonId is null
		50002 when a non-existent PersonId is given

	-------------------------------------------------------------------------------------------------------
	*/

	IF (@Email IS NULL)
	BEGIN;
		THROW 50001, 'Email cannot be null', 1;
	END

	IF @Email IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Person WHERE Email = @Email)
	BEGIN;
        THROW 50002, 'Error: Person does not exist.', 1;
	END

	SELECT InvitationId FROM PendingEventInvitation WHERE PersonEmail = @Email

end
GO
/****** Object:  StoredProcedure [dbo].[GetPersonIDByEmail]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetPersonIDByEmail]
    @Email NVARCHAR(50),
	@PersonID INT OUTPUT
AS
BEGIN
----------------------------------------------------------------------------------
---
---  Get PersonID from Email in the Person table
---
---  Parameters:
---     @Email					nvarchar(50)
---
---  Throws:
---	    50001 if required field is null
---     50003 if Email not found
---
------------------------------------------------------------------------------------

    -- check if Email is null
    IF @Email IS NULL 
    BEGIN;
        THROW 50001, 'Email cannot be null', 1;
    END

    -- check if Email exists
    IF NOT EXISTS (SELECT 1 FROM Person WHERE Email = @Email)
    BEGIN;
        THROW 50003, 'Error: Email not found.', 1;
    END

    SELECT @PersonID = ID FROM Person WHERE Email LIKE @Email;

END;

GO
/****** Object:  StoredProcedure [dbo].[GetRemainingSeatsForPrivateEvent]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetRemainingSeatsForPrivateEvent]
    @EventID INT
AS
BEGIN
    DECLARE @MaxCapacity INT, @IsPrivate BIT;
    
    SELECT @MaxCapacity = v.MaxCapacity, 
           @IsPrivate = e.isPublic
    FROM dbo.Event e
    JOIN dbo.Venue v ON e.VenueID = v.ID
    WHERE e.ID = @EventID;

    IF @IsPrivate = 1
    BEGIN;
        THROW 50001, 'This procedure only applies to private events.', 1;
        RETURN;
    END

    DECLARE @RegisteredCount INT, @NoResponseCount INT;

    SELECT @RegisteredCount = COUNT(*) 
    FROM dbo.AttendsEvent 
    WHERE EventID = @EventID AND RSVPStatus = 0;

    SELECT @NoResponseCount = COUNT(*) 
    FROM dbo.AttendsEvent 
    WHERE EventID = @EventID AND RSVPStatus = 2;

    SELECT (@MaxCapacity - @RegisteredCount - @NoResponseCount) AS RemainingSeats;
END;
GO
/****** Object:  StoredProcedure [dbo].[GetTransactions]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[GetTransactions]
    @UserID int
AS
BEGIN
    IF @UserID IS NULL
        THROW 50001, 'UserId cannot be NULL.', 1;

    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @UserID)
        THROW 50002, 'User does not exist.', 2;

    SELECT Type, Amount, PaidON
    FROM [Transaction]
    WHERE PersonID =  @UserID

END;
GO
/****** Object:  StoredProcedure [dbo].[GetUserInfo]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[GetUserInfo] (
	@UserID int
)
AS
	BEGIN
	SELECT FirstName, Minit, LastName, DOB, PhoneNo, Email
	FROM Person
	WHERE ID = @UserID
	END
GO
/****** Object:  StoredProcedure [dbo].[GetVendorServices]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/*

	GetVendorServices will return all services that match the supplied vendor id.

	RETURNS: A list of all services with vendor id = @vendorid

	THROWS:
		51000 when supplied with a non-existant vendor

*/

CREATE PROCEDURE [dbo].[GetVendorServices](
	@VendorID int
)
AS
BEGIN
	IF NOT EXISTS (SELECT 1 FROM Vendor WHERE ID = @VendorID)
		THROW 51000, 'This vendor does not exist', 1;

	SELECT s.ID, s.[Description], s.[Name], s.Price, s.VendorID
	FROM [Service] s
	WHERE VendorID = @VendorID
END
GO
/****** Object:  StoredProcedure [dbo].[GetVenueIdByName]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetVenueIdByName]
    @VenueName NVARCHAR(100),
    @VenueID INT OUTPUT
AS
BEGIN

    SELECT @VenueID = ID
    FROM Venue
    WHERE Name = @VenueName;

    IF @VenueID IS NULL
        SET @VenueID = -1;
END;
GO
/****** Object:  StoredProcedure [dbo].[GetVenueInfo]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetVenueInfo]
(
	@VenueID int
)
AS
BEGIN
	IF @VenueID IS NULL
	BEGIN
		;throw 50001, 'VenueID cannot be NULL', 1; 
	END

	IF (SELECT COUNT(ID) FROM Venue WHERE ID = @VenueID) = 0
	BEGIN
		;throw 50002, 'Venue not found', 1;
	END

	SELECT * FROM [VenueDetails] WHERE ID = @VenueID;

END

GO
/****** Object:  StoredProcedure [dbo].[RegisterForEvent]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE   PROCEDURE [dbo].[RegisterForEvent]
(
	@PersonID int,
	@EventID int,
	@PaymentID char(50)
)
AS
BEGIN
----------------------------------------------------------------------------------
---
---  Register person for event if this event is available for public
---
---  Parameters:
---		@PersonID	int
---		@EventID	int
---		@PaymentID  char(50)
---
---  Returns:
---		0 on success
---
---  Throws:
---		50001 if a param is null
---		50002 if Person or Event with specified IDs do not exist
---		50003 if the record with these parameters exists already
---		50101 if the specified event is not available for public
---		50102 if the host is trying to register for the event
---
-----------------------------------------------------------------------------------
---  Demo:
---  EXEC RegisterForEvent 12, 10501
---
------------------------------------------------------------------------------------

	IF @PersonID IS NULL
	BEGIN
		;throw 50001, 'PersonID cannot be NULL', 1; 
	END
	IF @EventID IS NULL
	BEGIN
		;throw 50001, 'EventID cannot be NULL', 1; 
	END
	IF @PaymentID IS NULL
	BEGIN
		;throw 50001, 'PaymentID cannot be NULL', 1; 
	END

	IF (SELECT COUNT(ID) FROM Person WHERE ID = @PersonID) = 0
	BEGIN
		;throw 50002, 'Person not found', 1;
	END

	IF (SELECT COUNT(ID) FROM Event WHERE ID = @EventID) = 0
	BEGIN
		;throw 50002, 'Event not found', 2;
	END

	IF (SELECT COUNT(PersonID) FROM AttendsEvent WHERE PersonID = @PersonID AND EventID = @EventID) <> 0
	BEGIN
		;throw 50003, 'This person already registered for this event', 1;
	END

	IF (dbo.EventAvailableForPublic(@EventID) = 0)
	BEGIN
		;throw 50101, 'Event not available for public', 1;
	END

	IF EXISTS(SELECT 1 FROM HostEvents WHERE PersonID = @PersonID AND EventID = @EventID)
	BEGIN
		;throw 50102, 'Hosts cannot register for their own events', 1;
	END

	INSERT INTO AttendsEvent(PersonID, EventID, RSVPStatus, Attendance, Invited, PaymentId)
	VALUES(@PersonID, @EventID, 0, 0, NULL, @PaymentID)   -- Invited is false because the event is public

	PRINT 'Registration created successfully'
	RETURN 0

END
GO
/****** Object:  StoredProcedure [dbo].[ShowAttendedEvents]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[ShowAttendedEvents] (
    @PersonID int
)
AS
BEGIN
    -- Provides information about events available for public right now
    -- This means a) the event should be public and b) the deadline for registration has not passed
    -- We need to have the name and timing for the event, registration deadline, name and address for the venue, and price for the event
    IF @PersonID IS NULL
        THROW 51000, 'PersonEmail cannot be null', 1;
    IF NOT EXISTS(SELECT 1 FROM Person WHERE ID = @PersonID)
        THROW 51001, 'Email must exist', 2;

    SELECT e.ID as ID, e.[Name] as [Name], e.StartTime, e.EndTime, e.Price, e.VenueID, v.[Name] as VenueName, v.StreetAddress as [Address], v.MaxCapacity, e.RegistrationDeadline
    FROM AttendsEvent ae
    JOIN [Event] e ON ae.EventID = e.ID
    JOIN Venue v ON e.VenueID = v.ID
    WHERE ae.PersonID = @PersonID AND ae.Attendance = 1

END
GO
/****** Object:  StoredProcedure [dbo].[ShowAvailableEvents]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[ShowAvailableEvents]
AS
-- Provides information about events available for public right now
-- This means a) the event should be public and b) the deadline for registration has not passed
-- We need to have the name and timing for the event, registration deadline, name and address for the venue, and price for the event
SELECT [Id], [Name], StartTime, EndTime, RegistrationDeadline, Price, VenueId, VenueName, MaxCapacity, a.MaxCapacity - ISNULL((SELECT COUNT(*) FROM AttendsEvent 
                                WHERE EventID = a.ID AND RSVPStatus = 0), 0) AS RemainingSeats, VenueAddress
FROM AvailableEventsForPublic a
GO
/****** Object:  StoredProcedure [dbo].[ShowAvailableEventsByVenue]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[ShowAvailableEventsByVenue] (
    @PersonEmail nvarchar(50)
)
AS
BEGIN
    -- Provides information about events available for public right now
    -- This means a) the event should be public and b) the deadline for registration has not passed
    -- We need to have the name and timing for the event, registration deadline, name and address for the venue, and price for the event
    IF @PersonEmail IS NULL
        THROW 51000, 'PersonEmail cannot be null', 1;
    IF NOT EXISTS(SELECT 1 FROM Person WHERE Email = @PersonEmail)
        THROW 51001, 'Email must exist', 2;

    DECLARE @PersonID int
    
    EXEC GetPersonIDByEmail @PersonEmail, @PersonID

    SELECT e.[Name], e.StartTime, e.EndTime
    FROM AttendsEvent ae
    JOIN [Event] e ON ae.EventID = e.ID
    JOIN Venue v ON e.VenueID = v.ID
    WHERE ae.PersonID = 1 AND ae.Attendance = 1

END
GO
/****** Object:  StoredProcedure [dbo].[ShowEventReviews]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[ShowEventReviews] (
    @EventID int
)
AS
BEGIN
    IF @EventID IS NULL
        THROW 51000, 'EventID cannot be null', 1;
    IF NOT EXISTS(SELECT 1 FROM [Event] WHERE ID = @EventID)
        THROW 51001, 'Event must exist', 2;

    SELECT r.ID AS ReviewID, 
           r.EventID, 
           e.[Name] AS EventName, 
           r.Title, 
           r.Rating, 
           r.Description AS Comment, 
           r.PostedOn, 
           CONCAT(p.FirstName, ' ', p.LastName) AS ReviewerName
    FROM Reviews r
    JOIN [Event] e ON r.EventID = e.ID
    JOIN Person p ON r.PersonID = p.ID
    WHERE r.EventID = @EventID AND e.EndTime < GETDATE()
    ORDER BY r.PostedOn DESC;
END
GO
/****** Object:  StoredProcedure [dbo].[ShowPastPublicEvents]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[ShowPastPublicEvents]
AS
BEGIN
    SELECT e.ID, e.Name, e.StartTime, e.EndTime, e.Price, e.VenueID, 
           v.Name AS VenueName, v.StreetAddress AS VenueAddress, v.MaxCapacity
    FROM [Event] e
    JOIN Venue v ON e.VenueID = v.ID
    WHERE e.IsPublic = 1 AND e.EndTime < GETDATE()
    ORDER BY e.EndTime DESC;
END;

GO
/****** Object:  StoredProcedure [dbo].[ShowVenueReviews]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[ShowVenueReviews] (
    @VenueID int
)
AS
BEGIN
    IF @VenueID IS NULL
        THROW 51000, 'VenueID cannot be null', 1;
    IF NOT EXISTS(SELECT 1 FROM Venue WHERE ID = @VenueID)
        THROW 51001, 'Venue must exist', 2;

    SELECT r.ID AS ReviewID, 
           r.VenueID, 
           v.[Name] AS VenueName, 
           r.Title, 
           r.Rating, 
           r.Description AS Comment, 
           r.PostedOn, 
           CONCAT(p.FirstName, ' ', p.LastName) AS ReviewerName
    FROM Reviews r
    JOIN Venue v ON r.VenueID = v.ID
    JOIN Person p ON r.PersonID = p.ID
    WHERE r.VenueID = @VenueID
    ORDER BY r.PostedOn DESC;
END;
GO
/****** Object:  StoredProcedure [dbo].[UpdateEmail]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-------------------------------------------------------------------
/*
	UpdateEmail will take in a userID and a new Email value and 
	change the email of the user with the matching userID
	to the new email

	THROWS:
		51000 if user does not exist

	RETURNS:
		0 if success
		1 if failed
*/
------------------------------------------------------------------
CREATE PROCEDURE [dbo].[UpdateEmail] (
    @userID int,
    @newEmail nvarchar(50)
)
AS
BEGIN
    -- Check if the user exists
    IF EXISTS (SELECT 1 FROM Person WHERE Id = @userID)
    BEGIN
        -- Update the email
        UPDATE Person
        SET Email = @newEmail
        WHERE Id = @userID;

        -- Return success message
        PRINT 'Email updated successfully.';
		RETURN 0;
    END
    ELSE
    BEGIN;
        THROW 51000, 'User Not Found', 1;
    END
END;
GO
/****** Object:  StoredProcedure [dbo].[UpdateGuestPaymentStatus]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[UpdateGuestPaymentStatus]
    @PersonID INT,
    @EventID INT,
    @PaymentStatus BIT
AS
BEGIN
    SET NOCOUNT ON;

    /*
    -------------------------------------------------------------------------------------------------------
    Stored Procedure: UpdateGuestPaymentStatus

    Purpose:
    This procedure updates the payment status for a private event that a person has been invited to.

    Parameters:
        @PersonID   INT       - The ID of the person updating their RSVP status.
        @EventID    INT       - The ID of the event for which the RSVP status is being updated.
        @PaymentStatus BIT   - The payment status (0 = unpaid, 1 = paid).

    Returns:
        0 on success

    Throws:
        50001 - If any required parameter is NULL.
        50002 - If the Person or Event with the specified ID does not exist.
        50003 - If the person has not been invited to the specified event.
        50005 - If the registration deadline has passed.
    -------------------------------------------------------------------------------------------------------
    */

    -- Null checks
    IF @PersonID IS NULL
        THROW 50001, 'Error: PersonID cannot be NULL.', 1;
    IF @EventID IS NULL
        THROW 50001, 'Error: EventID cannot be NULL.', 1;
    IF @PaymentStatus IS NULL
        THROW 50001, 'Error: PaymentStatus cannot be NULL.', 1;

    -- Check if Person exists
    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @PersonID)
        THROW 50002, 'Error: Person does not exist.', 2;

    -- Check if Event exists
    IF NOT EXISTS (SELECT 1 FROM Event WHERE ID = @EventID)
        THROW 50002, 'Error: Event does not exist.', 3;

    -- Check if the person has been invited to the event
    IF NOT EXISTS (SELECT 1 FROM AttendsEvent WHERE PersonID = @PersonID AND EventID = @EventID AND Invited = 1)
        THROW 50003, 'Error: Person has not been invited to this event.', 4;

    -- Check if the registration deadline has passed
    IF EXISTS (
        SELECT 1
        FROM Event
        WHERE ID = @EventID AND StartTime < GETDATE()
    )
        THROW 50005, 'Error: Registration deadline has passed.', 6;

    -- Update RSVPStatus
    UPDATE AttendsEvent
    SET PaymentStatus = @PaymentStatus
    WHERE PersonID = @PersonID AND EventID = @EventID;

    PRINT 'Payment status updated successfully';
    RETURN 0;
END;
GO
/****** Object:  StoredProcedure [dbo].[UpdateName]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-------------------------------------------------------------------
/*
	UpdateName will take in a userID and a new first, last, and
	middle initial value and change the name of the user 
	with the matching userID to the new name

	THROWS:
		51000 if user does not exist

	RETURNS:
		0 if success
		1 if failed
*/
------------------------------------------------------------------
CREATE PROCEDURE [dbo].[UpdateName] (
    @userID int,
    @newFirst nvarchar(20),
	@newMinit char(1),
	@newLast nvarchar(20)
)
AS
BEGIN
    -- Check if the user exists
    IF EXISTS (SELECT 1 FROM Person WHERE Id = @userID)
    BEGIN
        -- Update the name
        UPDATE Person
        SET FirstName = @newFirst, MInit = @newMinit, LastName = @newLast
        WHERE Id = @userID;

        -- Return success message
        PRINT 'Name updated successfully.';
		RETURN 0;
    END
    ELSE
    BEGIN;
        THROW 51000, 'User Not Found', 1;
    END
END;
GO
/****** Object:  StoredProcedure [dbo].[UpdatePhoneNo]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-------------------------------------------------------------------
/*
	UpdatePhoneNo will take in a userID and a new phone number value and 
	change the phone number of the user with the matching userID
	to the new phone number

	THROWS:
		51000 if user does not exist

	RETURNS:
		0 if success
		1 if failed
*/
------------------------------------------------------------------
CREATE PROCEDURE [dbo].[UpdatePhoneNo] (
    @userID int,
    @newPhoneNo char(10)
)
AS
BEGIN
    -- Check if the user exists
    IF EXISTS (SELECT 1 FROM Person WHERE Id = @userID)
    BEGIN
        -- Update the email
        UPDATE Person
        SET PhoneNo = @newPhoneNo
        WHERE Id = @userID;

        -- Return success message
        PRINT 'PhoneNo updated successfully.';
		RETURN 0;
    END
    ELSE
    BEGIN;
        THROW 51000, 'User Not Found', 1;
    END
END;
GO
/****** Object:  StoredProcedure [dbo].[UpdateRSVPStatus]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[UpdateRSVPStatus]
    @PersonID INT,
    @EventID INT,
    @RSVPStatus TINYINT
AS
BEGIN
    SET NOCOUNT ON;

    /*
    -------------------------------------------------------------------------------------------------------
    Stored Procedure: UpdateRSVPStatus

    Purpose:
    This procedure updates the RSVP status for a private event that a person has been invited to.

    Parameters:
        @PersonID   INT       - The ID of the person updating their RSVP status.
        @EventID    INT       - The ID of the event for which the RSVP status is being updated.
        @RSVPStatus TINYINT   - The RSVP status (0 = Registered, 1 = Declined, 2 = No Response).

    Returns:
        0 on success

    Throws:
        50001 - If any required parameter is NULL.
        50002 - If the Person or Event with the specified ID does not exist.
        50003 - If the person has not been invited to the specified event.
        50004 - If the RSVP status is invalid.
        50005 - If the registration deadline has passed.
    -------------------------------------------------------------------------------------------------------
    */

    -- Null checks
    IF @PersonID IS NULL
        THROW 50001, 'Error: PersonID cannot be NULL.', 1;
    IF @EventID IS NULL
        THROW 50001, 'Error: EventID cannot be NULL.', 1;
    IF @RSVPStatus IS NULL
        THROW 50001, 'Error: RSVPStatus cannot be NULL.', 1;

    -- Check if Person exists
    IF NOT EXISTS (SELECT 1 FROM Person WHERE ID = @PersonID)
        THROW 50002, 'Error: Person does not exist.', 2;

    -- Check if Event exists
    IF NOT EXISTS (SELECT 1 FROM Event WHERE ID = @EventID)
        THROW 50002, 'Error: Event does not exist.', 3;

    -- Check if the person has been invited to the event
    IF NOT EXISTS (SELECT 1 FROM AttendsEvent WHERE PersonID = @PersonID AND EventID = @EventID AND Invited = 1)
        THROW 50003, 'Error: Person has not been invited to this event.', 4;

    -- Validate RSVPStatus (0 = Registered, 1 = Declined, 2 = No Response)
    IF @RSVPStatus NOT IN (0, 1, 2)
        THROW 50004, 'Error: Invalid RSVPStatus. Must be 0 (Registered), 1 (Declined), or 2 (No Response).', 5;

    -- Check if the registration deadline has passed
    IF EXISTS (
        SELECT 1
        FROM Event
        WHERE ID = @EventID AND StartTime < GETDATE()
    )
        THROW 50005, 'Error: Registration deadline has passed.', 6;

    -- Update RSVPStatus
    UPDATE AttendsEvent
    SET RSVPStatus = @RSVPStatus
    WHERE PersonID = @PersonID AND EventID = @EventID;

    PRINT 'RSVP status updated successfully';
    RETURN 0;
END;
GO
/****** Object:  StoredProcedure [dbo].[ValidateUserLogin]    Script Date: 2/21/2025 12:20:21 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[ValidateUserLogin]
    @Email NVARCHAR(50)
AS
BEGIN
	IF @Email is NULL
	BEGIN;
		THROW 50001, 'Error: Email cannot be null', 1;
	END
    -- check if Email exists
    IF NOT EXISTS (SELECT 1 FROM Person WHERE Email = @Email)
    BEGIN;
        THROW 51001, 'Error: Email not found.', 2;
    END

    SELECT PasswordHash, PasswordSalt FROM Person WHERE Email = @Email;
END;
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'0 represents not payed yet, 1 represents payed' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'Event', @level2type=N'COLUMN',@level2name=N'PaymentStatus'
GO
USE [master]
GO
ALTER DATABASE [EventPlannerS1G2_TEST] SET  READ_WRITE 
GO
