-- Step 1: Show existing services before adding new ones
SELECT * FROM Service;

-- Step 2: Test inserting invalid services

-- Test Case 1: Insert a service with NULL name (should fail with error 50001)
EXEC dbo.AddService 
    @Name=NULL, 
    @Description='Test Service Description', 
    @Price=50.00, 
    @VendorID=1;

-- Test Case 2: Insert a service with NULL description (should fail with error 50001)
EXEC dbo.AddService 
    @Name='Invalid Service', 
    @Description=NULL, 
    @Price=100.00, 
    @VendorID=1;

-- Test Case 3: Insert a service with NULL price (should fail with error 50001)
EXEC dbo.AddService 
    @Name='Invalid Service Price', 
    @Description='Description', 
    @Price=NULL, 
    @VendorID=1;

-- Test Case 4: Insert a service with NULL VendorID (should fail with error 50001)
EXEC dbo.AddService 
    @Name='Invalid Service Vendor', 
    @Description='Description', 
    @Price=100.00, 
    @VendorID=NULL;

-- Test Case 5: Insert a service with non-existing VendorID (should fail with error 50002)
EXEC dbo.AddService 
    @Name='Service Non-Existing Vendor', 
    @Description='Test Description', 
    @Price=75.00, 
    @VendorID=99999; -- Assume this VendorID does not exist

-- Test Case 6: Insert a service with duplicate name for the same vendor (should fail with error 50004)
-- First, insert a valid service
EXEC dbo.AddService 
    @Name='Unique Service', 
    @Description='Service Description', 
    @Price=200.00, 
    @VendorID=3;

-- Now, inserting a service with the same name and same VendorID
EXEC dbo.AddService 
    @Name='Unique Service', 
    @Description='Duplicate Description', 
    @Price=150.00, 
    @VendorID=3; -- Should fail

-- Deleting inserted test case for retesting purpose
DELETE FROM Service 
WHERE Name = 'Unique Service' 
AND VendorID = 3;

-- Test Case 7: Insert a service with negative price (should fail with error 53003)
EXEC dbo.AddService 
    @Name='Negative Price Service', 
    @Description='Service with Negative Price', 
    @Price=-10.00, 
    @VendorID=3;

-- Step 3: Test successful service insertion

-- Test Case 8: Insert a valid service for an existing vendor
EXEC dbo.AddService 
    @Name='Valid Service', 
    @Description='This is a valid service.', 
    @Price=150.00, 
    @VendorID=3;

-- Step 4: Verify inserted services
SELECT * FROM Service;

-- Deleting inserted test case for retesting purpose
DELETE FROM Service 
WHERE Name = 'Unique Service' 
AND VendorID = 3;

-- Deleting inserted test case for retesting purpose
DELETE FROM Service 
WHERE Name = 'Valid Service' 
AND VendorID = 3;
