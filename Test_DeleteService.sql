/*
    Stored Procedure: DeleteService

    Purpose:
    This procedure deletes a service from the 'Service' table based on its ID.
*/

CREATE PROCEDURE DeleteService
    @ServiceID INT
AS
BEGIN
    -- Check if the ServiceID exists
    IF NOT EXISTS (SELECT 1 FROM Service WHERE ID = @ServiceID)
        THROW 52200, 'Error: ServiceID does not exist.', 1;

    -- Delete the service record
    DELETE FROM Service WHERE ID = @ServiceID;
END;
