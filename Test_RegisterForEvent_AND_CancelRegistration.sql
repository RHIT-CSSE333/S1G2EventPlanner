-- Step 1. RegisterForEvent
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

-- Step 2. CancelRegistration
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