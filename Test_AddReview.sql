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