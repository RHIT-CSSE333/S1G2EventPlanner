-- Step 1: Show existing people in the database before deletion
SELECT * FROM Person;

-- Step 2: Test deleting a person that does not exist (should fail with error 50003)
EXEC dbo.DeletePerson @Email='nonexistent@example.com';

-- Step 3: Insert a test person to delete
EXEC dbo.CreateOrUpdatePerson 
    @Email='delete_test@example.com', 
    @PhoneNo='9876543210', 
    @FirstName='Test', 
    @LastName='Delete', 
    @DOB='1990-10-10';

-- Verify the test person is inserted
SELECT * FROM Person WHERE Email = 'delete_test@example.com';

-- Step 4: Successfully delete the test person
EXEC dbo.DeletePerson @Email='delete_test@example.com';

-- Step 5: Verify the person was deleted (should return empty result)
SELECT * FROM Person WHERE Email = 'delete_test@example.com';

-- Step 6: Attempt to delete the same person again (should fail with error 50003)
EXEC dbo.DeletePerson @Email='delete_test@example.com';
