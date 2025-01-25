-- Step 1. Show the events database

-- Step 2. Call the ShowAvailableEvents
EXEC dbo.ShowAvailableEvents

-- Step 3. Add events
EXEC addEvent @Name='Failed event 1 (NULLS)', @StartTime='12/23/25', @EndTime = NULL, @VenueID = NULL
EXEC addEvent @Name='Wedding In the Future', @StartTime='1/30/25', @EndTime = '1/31/25', @VenueID = 1  -- uniqueness fails

DECLARE @CURRTIME datetime
DECLARE @CURRMINONE datetime
DECLARE @CURRPLUSONE datetime

SELECT @CURRTIME = GETUTCDATE() 
SET @CURRMINONE = DATEADD(HOUR, -1, @CURRTIME)
SET @CURRPLUSONE = DATEADD(HOUR, 1, @CURRTIME)

--EXEC addEvent @Name='Failed event 3 (Start too soon)', @StartTime=@CURRPLUSONE, @EndTime = '1/30/25 1:30 PM', @VenueID = 1
--EXEC addEvent @Name='Failed event 4 (Too short)', @StartTime='1/30/25 1 PM', @EndTime = '1/30/25 1:30 PM', @VenueID = 1
--EXEC addEvent @Name='Failed event 5 (Ends before start)', @StartTime='1/30/25 1 PM', @EndTime = '1/30/25 1:30 AM', @VenueID = 1
EXEC addEvent @Name='Failed event 6 (Registration ends after start)', @StartTime='1/30/25 1 PM', @EndTime = '1/30/25 2:30 PM',
				@RegistrationDeadline='1/30/25 1:30 PM', @VenueID = 1

EXEC addEvent @Name='Failed event 7 (Price < 0)', @StartTime='1/30/25 1 PM', @EndTime = '1/30/25 2:30 PM',
				@RegistrationDeadline='1/30/25 12:30 PM', @Price=-10, @VenueID = 1

EXEC addEvent @Name='Successful event (all supplied)', @StartTime='1/30/25 1 PM', @EndTime = '1/30/25 2:30 PM',
				@RegistrationDeadline='1/30/25 12:30 PM', @Price=10, @VenueID = 1, @isPublic = 1
EXEC addEvent @Name='Successful event (autofill)', @StartTime='1/30/25 1 PM', @EndTime = '1/30/25 2:30 PM',
				@VenueID = 1

EXEC dbo.ShowAvailableEvents
SELECT * FROM Event
