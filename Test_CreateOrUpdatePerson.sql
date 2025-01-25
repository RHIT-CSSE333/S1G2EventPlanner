-- Step 1: Show existing people in the database
SELECT * FROM Person;

-- Step 2: Test inserting invalid persons

-- Test Case 1: Insert a person with NULL email (should fail with error 50001)
EXEC dbo.CreateOrUpdatePerson 
    @Email=NULL, 
    @PhoneNo='1234567890', 
    @FirstName='John', 
    @LastName='Doe', 
    @DOB='1990-01-01';

-- Test Case 2: Insert a person with an existing email (should fail with error 50201)
EXEC dbo.CreateOrUpdatePerson 
    @Email='purpleguy@gmail.com',  -- Assume this email already exists
    @PhoneNo='1234567890', 
    @FirstName='Jane', 
    @LastName='Smith', 
    @DOB='1992-02-02';

-- Test Case 3: Insert a person with a future DOB (should fail with error 50202)
DECLARE @FutureDOB DATE;
SET @FutureDOB = DATEADD(DAY, 1, GETDATE()); -- Adds 1 day to current date
EXEC dbo.CreateOrUpdatePerson 
    @Email='future_dob@example.com', 
    @PhoneNo='1234567890', 
    @FirstName='Alice', 
    @LastName='Brown', 
    @DOB=@FutureDOB; -- Tomorrow's date

-- Test Case 4: Insert a person with an invalid credit card number (should fail with error 50203)
EXEC dbo.CreateOrUpdatePerson 
    @Email='invalid_cc@example.com', 
    @PhoneNo='1234567890', 
    @FirstName='Bob', 
    @LastName='White', 
    @DOB='1985-05-15', 
    @CCNum='12345';  -- Not 16 digits

-- Test Case 5: Insert a person with an invalid CVV (should fail with error 50204)
EXEC dbo.CreateOrUpdatePerson 
    @Email='invalid_cvv@example.com', 
    @PhoneNo='1234567890', 
    @FirstName='Eve', 
    @LastName='Adams', 
    @DOB='1988-08-08', 
    @CCNum='1234567812345678', 
    @CVV='12';  -- Not 3 or 4 digits

-- Test Case 6: Insert a person with an expired credit card (should fail with error 50205)
EXEC dbo.CreateOrUpdatePerson 
    @Email='expired_cc@example.com', 
    @PhoneNo='1234567890', 
    @FirstName='Charlie', 
    @LastName='Davis', 
    @DOB='1993-03-03', 
    @CCNum='1234567812345678', 
    @CCExpDate='2023-01-01',  -- Expired date
    @CVV='123';

-- Test Case 7: Insert a person with an invalid phone number format (should fail with error 50206)
EXEC dbo.CreateOrUpdatePerson 
    @Email='invalid_phone@example.com', 
    @PhoneNo='abcdefghij',  -- Wrong format (should be 10 digits)
    @FirstName='David', 
    @LastName='Johnson', 
    @DOB='1995-06-06';

-- Step 3: Test successful person insertions

-- Test Case 8: Insert a valid person
EXEC dbo.CreateOrUpdatePerson 
    @Email='valid_person@example.com', 
    @PhoneNo='9876543210', 
    @FirstName='Olivia', 
    @LastName='Williams', 
    @DOB='1990-12-12', 
    @CCNum='9876543210987654', 
    @CCExpDate='2026-12-01', 
    @CVV='789';

-- Step 4: Test updating persons

-- Test Case 9: Try updating without supplying PersonID (should fail with error 50207)
EXEC dbo.CreateOrUpdatePerson 
    @Email='update_fail@example.com', 
    @PhoneNo='1112223333', 
    @FirstName='Michael', 
    @LastName='Clark', 
    @DOB='1987-07-07', 
    @IsEdit=1;  -- Missing @PersonID

-- Test Case 10: Successfully update an existing person
DECLARE @PersonID INT;
SELECT @PersonID = ID FROM Person WHERE Email = 'valid_person@example.com';

EXEC dbo.CreateOrUpdatePerson 
    @Email='updated_person@example.com', 
    @PhoneNo='9998887777', 
    @FirstName='Olivia', 
    @LastName='Smith', 
    @DOB='1990-12-12', 
    @CCNum='1234567812345678', 
    @CCExpDate='2026-12-01', 
    @CVV='456', 
    @IsEdit=1, 
    @PersonID=@PersonID;

-- Step 5: Verify inserted and updated persons
SELECT * FROM Person;
