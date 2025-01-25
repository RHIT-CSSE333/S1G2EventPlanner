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

-- Step 4. Add review
-- NULL Person
EXEC AddReview @PersonID = NULL,
				@VenueID = 1,
				@EventID = NULL,
				@Title = 'wonderful',
				@Rating = 5,
				@PostedOn = '1/23/23'
-- Both venue and event are null
EXEC AddReview @PersonID = 1,
				@VenueID = NULL,
				@EventID = NULL,
				@Title = 'wonderful',
				@Rating = 5,
				@PostedOn = '1/23/23'
-- both venue and event are not null
EXEC AddReview @PersonID = 1,
				@VenueID = 1,
				@EventID = 1,
				@Title = 'wonderful',
				@Rating = 5,
				@PostedOn = '1/23/23'
-- venue does not exist
EXEC AddReview @PersonID = 1,
				@VenueID = 100,
				@EventID = NULL,
				@Title = 'wonderful',
				@Rating = 5,
				@PostedOn = '1/23/23'
-- event does not exist
EXEC AddReview @PersonID = 1,
				@VenueID = NULL,
				@EventID = 100,
				@Title = 'wonderful',
				@Rating = 5,
				@PostedOn = '1/23/23'
-- person does not exist
EXEC AddReview @PersonID = 10203,
				@VenueID = NULL,
				@EventID = 2,
				@Title = 'wonderful',
				@Rating = 5,
				@PostedOn = '1/23/23'
-- posted from the future
DECLARE @TIMEE datetime
SELECT @TIMEE = DATEADD(DAY, 1, GETUTCDATE())
EXEC AddReview @PersonID = 1,
				@VenueID = NULL,
				@EventID = 7,
				@Title = 'wonderful',
				@Rating = 5,
				@PostedOn = @TIMEE
-- rating too big
EXEC AddReview @PersonID = 1,
				@VenueID = NULL,
				@EventID = 7,
				@Title = 'wonderful',
				@Rating = 50,
				@PostedOn = '1/23/23'

-- success
EXEC AddReview @PersonID = 1,
				@VenueID = NULL,
				@EventID = 7,
				@Title = 'wonderful',
				@Rating = 5,
				@PostedOn = '1/23/23'
-- fail (already reviewed this event)
EXEC AddReview @PersonID = 1,
				@VenueID = NULL,
				@EventID = 7,
				@Title = 'wonderful',
				@Rating = 5,
				@PostedOn = '1/23/23'

SELECT * FROM Reviews

-- Step 5. EventAvailableForPublic
EXEC dbo.ShowAvailableEvents
SELECT * FROM Event

SELECT dbo.EventAvailableForPublic(2)  -- False
SELECT dbo.EventAvailableForPublic(7)  -- False
SELECT dbo.EventAvailableForPublic(8)  -- False
SELECT dbo.EventAvailableForPublic(9)  -- True
SELECT dbo.EventAvailableForPublic(10)  -- True

-- Step 6. RegisterForEvent
-- ids are null
EXEC RegisterForEvent @PersonID = NULL,
						@EventID = NULL
-- person does not exist
EXEC RegisterForEvent @PersonID = 100,
						@EventID = 2
-- event does not exist
EXEC RegisterForEvent @PersonID = 1,
						@EventID = 200
-- event not available
EXEC RegisterForEvent @PersonID = 1,
						@EventID = 7
-- success
EXEC RegisterForEvent @PersonID = 1,
						@EventID = 9
-- fail (record exists already)
EXEC RegisterForEvent @PersonID = 1,
						@EventID = 9
SELECT * FROM AttendsEvent

-- Step 7. CancelRegistration
-- ids are null
EXEC CancelRegistration @PersonID = NULL,
						@EventID = NULL
-- person does not exist
EXEC CancelRegistration @PersonID = 100,
						@EventID = 2
-- event does not exist
EXEC CancelRegistration @PersonID = 1,
						@EventID = 200
-- Registration not found
EXEC CancelRegistration @PersonID = 1,
						@EventID = 10
-- success
EXEC CancelRegistration @PersonID = 1,
						@EventID = 9
SELECT * FROM AttendsEvent